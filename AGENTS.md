# AI Agent Instructions & Architectural Guide – FlexUI (`flex-ui-components`)

Este archivo es la **fuente absoluta de verdad (Single Source of Truth)** para toda generación de código, refactorización, creación de componentes y corrección de bugs realizada por agentes de IA (Google Antigravity, GitHub Copilot, JetBrains Junie, Claude, etc.) en este repositorio.

Cualquier agente que trabaje en esta base de código **DEBE** respetar estrictamente los principios de **Clean Architecture**, **Domain-Driven Design (DDD)**, **Arquitectura Hexagonal**, patrones de **Server-Driven UI (SDUI)** y las directivas de Jetpack Compose aquí establecidas.

---

## 1. Visión del Sistema y Flujo End-to-End (SDUI Pipeline)

### 1.1 Propósito
**FlexUI** es un framework de **Server-Driven UI (SDUI)** declarativo, reactivo y transaccional para Android y Kotlin Multiplatform. Permite que el backend controle la estructura, estilo, comportamiento interactivo y validaciones de formularios y pantallas completas mediante contratos JSON tipados, garantizando:
1. **Consistencia de negocio estricta:** Ningún valor no validado puede existir en el estado de una sesión.
2. **Cero acoplamiento tecnológico en el Core:** Lógica de negocio y contratos puros 100% aislados de Android y frameworks de terceros.
3. **Renderizado reactivo de alto rendimiento:** Mapeo eficiente a Jetpack Compose con recomposiciones quirúrgicas, caché y estandarización de diseño.

### 1.2 Pipeline Global de Procesamiento

```text
[ JSON Backend / Asset ]
           │
           ▼
[ :flexui-parser ] ──────────────► Valida esquema, deserializa e indexa O(1)
           │                        retornando un `LayoutDocument` inmutable.
           ▼
[ :flexui-core ] ────────────────► Orquesta la sesión (`FormSession`), ejecuta reglas de
           │                        validación puras y procesa comandos (`CommandBus`).
           ▼
[ :flexui-runtime / :FlexUI ] ───► Resuelve estilos (`StyleResolver`), renderiza el árbol
           │                        con Jetpack Compose (`JsonRenderer`) y vincula eventos.
           ▼
[ :app / Host Application ] ────► Recibe acciones del host (`HostActionHandlerPort`),
                                    navegación y telemetría de eventos.
```

---

## 2. Mapa de Módulos y Límites Arquitectónicos

El proyecto está estructurado de acuerdo con la Arquitectura Hexagonal y la Separación Limpia de Capas:

```text
flex-ui-components/
├── flexui-core/                  # EL NÚCLEO HEXAGONAL (Pure Kotlin JVM / KMP-Ready)
├── FlexUI/ (o :flexui-runtime)   # INFRAESTRUCTURA DE PRESENTACIÓN (Android & Jetpack Compose)
├── flexui-parser/                # INFRAESTRUCTURA DE DESERIALIZACIÓN (JSON & Schemas)
└── app/                          # APLICACIÓN DEMO / CONSUMIDOR (Showcase & Integración Hilt)
```

### 2.1 Matriz de Módulos y Reglas de Dependencia

| Módulo | Tipo de Módulo | Capa / Rol | Lo que SÍ contiene | Lo que NUNCA debe contener |
|---|---|---|---|---|
| `:flexui-core` | `kotlin("jvm")` | **Dominio & Aplicación** (Centro del hexágono) | Value Objects, Aggregate Root `FormSession`, Nodos de UI, Reglas de validación, Errores/Eventos de dominio, Puertos hexagonales (`*Port`), Casos de uso y `CommandBus`. | **PROHIBIDO:** Android SDK, Jetpack Compose, Reflection, Frameworks de DI (`hilt.*`, `dagger.*`, `koin.*`), `java.io.*` o librerías de terceros (excepto `kotlinx-coroutines-core`). |
| `:flexui-parser` | `kotlin("jvm")` | **Infraestructura** (Adaptador de entrada) | Estrategias de parsing, validadores de esquema JSON, deserializadores `kotlinx-serialization` que producen `LayoutDocument`. | Reglas de negocio de formularios, estados mutables o dependencias de UI Android. |
| `:flexui-runtime` (`:FlexUI`) | `android-library` | **Infraestructura** (Adaptador de entrega / UI) | Composables de Jetpack Compose (`Text`, `Button`, `Input`, etc.), motor de estilos (`StyleProperties`), `JsonRenderer`, adaptadores de puertos (`SystemClock`, `LogcatPublisher`), ViewModels y módulos Hilt. | Lógica de validación de negocio duplicada o estados de formulario paralelos independientes de `FormSession`. |
| `:app` | `android-app` | **Presentación / Consumidor** | Activity, pantallas demo, navegación, inyección global con Hilt y configuración de tema. | Lógica de parsing o manipulación interna directa de árboles de nodos sin pasar por FlexUI. |

> **REGLA DE ORO DE DEPENDENCIAS:**
> Las dependencias siempre apuntan hacia adentro: `:app` → `:flexui-runtime` → `:flexui-core`.
> `:flexui-core` es la cúspide de pureza y nunca depende de ningún módulo exterior.

---

## 3. Arquitectura del Core (`:flexui-core`): Dominio y Aplicación

### 3.1 Manejo Funcional de Errores: Cero Excepciones
* Las excepciones están estrictamente restringidas a fallos irrecuperables de la JVM (ej. `OutOfMemoryError`).
* **Regla estricta:** Para toda validación, regla o lógica de negocio, se prohíbe `throw`, `require()`, `check()` o `assert`.
* Se utiliza programación funcional pura mediante `Either<DomainError, T>` y `Nel<E>` (Non-Empty List):
  * `DomainError.Structural`: Incoherencias en el árbol de componentes (ej. IDs duplicados, profundidad > 20, esquema no soportado).
  * `DomainError.Value`: Violación de invariantes en Value Objects o reglas (ej. dimensión negativa, color inválido).
  * `DomainError.Interaction`: Fallos de sesión, componentes no encontrados o rechazo de acciones.

### 3.2 Value Objects e Invariantes Numéricos
* Constructores privados o `internal` obligatorios con factorías estáticas `of(...)` en el `companion object`.
* Validación de finitud numérica (`value.isFinite()`) y rangos válidos sin lanzar excepciones.
* Inmutabilidad absoluta.

```kotlin
public class Dimension private constructor(public val value: Float) {
    public class Dp private constructor(value: Float) : Dimension(value) {
        public companion object {
            public fun of(value: Float): Either<DomainError.Value.InvalidDimension, Dp> {
                return if (value.isFinite() && value >= 0f) Either.Right(Dp(value))
                else Either.Left(DomainError.Value.InvalidDimension("Dp must be finite and >= 0"))
            }
        }
    }
}
```

### 3.3 Aggregate Root: `FormSession`
* `FormSession` es la **única raíz transaccional** para la interacción del formulario.
* Mantiene constructor privado e igualdad referencial (por `SessionId`).
* `FormInteractionState` es interno y totalmente encapsulado. Ningún agente puede exponerlo ni mutarlo externamente.
* Ciclo de vida transaccional del Aggregate:
  * `FormSession.start(...)`: Inicializa la sesión y siembra valores por defecto a partir del `LayoutDocument`.
  * `session.updateInput(...)`: Valida con el nodo, aplica enmascaramiento si es sensible, recalcula transiciones de error y retorna `FormOutcome(newSession, events)`.
  * `session.requestAction(...)`: Evalúa si la acción está permitida, bloqueada por validaciones pendientes o requiere despacho, retornando `ActionDecision`.
  * `session.recordActionResult(...)`: Asienta la respuesta del host (`ActionResult.Success` o `ActionResult.Failure`) emitiendo eventos de resolución.

### 3.4 CQRS y Bus de Comandos en Memoria
* **Comandos:** Implementan `Command<R>`, donde `R` es el tipo de resultado tipado de retorno.
* **Comandos con Sesión:** Comandos que interactúan con un formulario implementan `SessionCommand` (`val sessionId: String`).
* **Command Bus:** `InMemoryCommandBus` registra handlers por `KClass<out Command<*>>`, valida que no existan handlers duplicados ni colisiones de `commandName`, y envuelve la ejecución en una cadena de `CommandMiddleware`.
* **Serialización de Concurrencia:** `SessionLockMiddleware` utiliza un `Mutex` indexado por `sessionId` para garantizar que comandos concurrentes sobre el mismo formulario se ejecuten estrictamente en serie, sin pisar el estado.

### 3.5 Preservación de Valores Nativos en Comandos (`FieldValue`)
* `UpdateInputValueCommand` es un DTO inmutable y puro que transporta directamente un `value: FieldValue` fuertemente tipado (`FieldValue.Text`, `FieldValue.Bool`, `FieldValue.Number`, `FieldValue.Selection`) para evitar `ValueTypeMismatch` en controles no textuales (`CheckNode`, `SliderNode`, `SelectNode`).
* Cada componente de UI o capa de presentación es responsable de empaquetar su valor nativo en el `FieldValue` correspondiente al despachar el comando.
* Como salvaguarda defensiva en el dominio, `FormSession.updateInput()` decodifica de forma segura valores textuales crudos al tipo nativo esperado por el nodo si fuera necesario (ej. `"true"` a `FieldValue.Bool`), rechazando con `DomainError.Value.ValueTypeMismatch` únicamente cuando el valor no es convertible o es de un tipo incompatible.

---

## 4. Arquitectura de Renderizado y UI (`:flexui-runtime` / `:FlexUI`)

### 4.1 Principios de Jetpack Compose
1. **Composables Puros y Sin Estado (Stateless):** Los componentes de UI reciben su estado (`value`, `error`, `isEnabled`, `properties`) y emiten eventos (`onValueChange`, `onClick`).
2. **Cero Lógica de Negocio en `@Composable`:** Un Composable nunca valida si un campo cumple un Regex o si un formulario es válido; simplemente despacha el comando correspondiente al `CommandBus`.
3. **Estabilidad de Parámetros:** Todas las clases de modelo consumidas por la UI deben ser inmutables para garantizar que el compilador de Compose optimice las recomposiciones (marcado inteligente `@Stable` / `@Immutable`).
4. **Evitar Recomposiciones Innecesarias:** Usar `remember` y claves estables (`key(node.id.value)`) al iterar en colecciones o listas dinámicas.

### 4.2 Motor de Estilos y Tokens de Diseño
* Toda propiedad visual (`backgroundColor`, `textColor`, `padding`, `cornerRadius`, `elevation`, `fontSize`) debe resolverse a través de los adaptadores de `StyleProperties`.
* **Soporte de Colores:** Los colores hexadecimales recibidos del backend (`HexColor`) se procesan a través de las estrategias ARGB8, RGB6 y RGB3, transformándose en `androidx.compose.ui.graphics.Color` seguro.
* **Manejo de Imágenes:** Siempre utilizar Coil (`AsyncImage`) con placeholders y fallbacks para cargas asíncronas desde URL.

### 4.3 Inyección de Dependencias (Hilt)
* En `:flexui-runtime` y `:app`, Hilt se usa para cablear las dependencias técnicas:
  * Provisión de adaptadores concretos para `ClockPort`, `FormSessionPort`, `LayoutSourcePort`, etc.
  * Inyección del `CommandBus` como `@Singleton`.
  * `@EntryPoint` (`FlexUiEntryPoint`) para permitir que la capa de UI acceda al bus sin acoplar los Composables al grafo de Hilt.

---

## 5. Guía Paso a Paso para la Extensión del Framework

### 5.1 Cómo agregar un nuevo Tipo de Componente UI (End-to-End)

Cuando se requiera dar soporte a un nuevo componente en FlexUI (ej. `DatePicker`, `ToggleSwitch`, `CardContainer`):

#### Paso 1: En `:flexui-core` (Dominio)
1. Crear el nodo en `core/domain/model/node/<Name>Node.kt` implementando `UIComponentNode`.
2. Implementar las interfaces de capacidad requeridas:
   * Si agrupa otros nodos: implementar `LayoutNode` con `children: List<UIComponentNode>`.
   * Si emite acciones/clicks: implementar `InteractiveNode` con `actionId: ActionId`, `isEnabled: Boolean`, `requiresValidation: Boolean`.
   * Si almacena entradas del usuario: implementar `InputCapableNode` con `isSensitive: Boolean` y `accept(value: FieldValue): Boolean`.
3. Crear tests unitarios en `NodeAcceptanceTest` verificando sus invariantes.

#### Paso 2: En `:flexui-parser` (Infraestructura de Parsing)
1. Definir el esquema JSON y DTO de deserialización para el nuevo componente.
2. Crear o actualizar el validador estructural en `parser/infrastructure/validator/`.
3. Mapear el DTO al nodo de dominio correspondiente dentro de `ComponentMapper`.
4. Crear pruebas de integración en el parser verificando que el JSON se convierta correctamente en el `UIComponentNode`.

#### Paso 3: En `:flexui-runtime` (Renderizado Compose)
1. Crear el Composable en `components/atomic/` o `components/layout/`.
2. Registrar la estrategia en `AtomicRendererStrategy` o `LayoutRendererStrategy`.
3. Conectar los eventos de interacción (`onValueChange`, `onClick`) para que despachen `UpdateInputValueCommand` o `ProcessUserActionCommand` al `CommandBus`.

#### Paso 4: En `:app` (Demostración)
1. Agregar un JSON de prueba en `assets/` o endpoint mock con el nuevo componente.
2. Verificar visualmente su renderizado, comportamiento responsivo y tema claro/oscuro.

---

### 5.2 Cómo agregar una nueva Regla de Validación
1. Crear la regla en `core/domain/value/rule/<RuleName>Rule.kt` implementando `ValidationRule`.
2. Si la regla requiere parámetros configurables (ej. longitud mínima, patrón regex, rango numérico), declarar constructor privado y factoría `of(...)` retornando `Either`.
3. Implementar `validate(value: FieldValue): ValidationError?`.
4. Crear tests exhaustivos en `ValidationRulesTest`.
5. En `:flexui-parser`, agregar la deserialización de la regla dentro de las especificaciones del componente.

---

### 5.3 Cómo agregar un nuevo Puerto y Adaptador
1. Definir la interfaz del puerto en `core/domain/ports/<Name>Port.kt` usando exclusivamente tipos de dominio y funciones `suspend`.
2. En `:flexui-core/src/test/.../fakes/`, crear un `Fake<Name>Port` para pruebas unitarias sin dependencias externas.
3. En `:flexui-runtime`, implementar la clase adaptadora real (ej. `Android<Name>Adapter.kt`) utilizando las APIs de Android o librerías correspondientes.
4. En el módulo Hilt de `:flexui-runtime` (o `:app`), bindear la interfaz del puerto con la implementación concreta.

---

## 6. Estándares de Calidad, Tipado y Buenas Prácticas

### 6.1 Modo `explicitApi()` en Core
* En `:flexui-core`, toda declaración en `src/main` **DEBE** especificar su visibilidad (`public`, `internal`, `private`) y el tipo de retorno de funciones y propiedades.

### 6.2 Manejo de Corrutinas y Cancelación
* Nunca capturar `Throwable` o `Exception` de forma genérica en bloques `suspend` sin relanzar explícitamente `CancellationException`:
  ```kotlin
  try {
      // Operación suspendible
  } catch (e: CancellationException) {
      throw e // OBLIGATORIO: no romper la cancelación de corrutinas
  } catch (e: Exception) {
      // Manejo controlado del error
  }
  ```

### 6.3 Convenciones de Nombres

| Elemento | Convención | Ejemplo |
|---|---|---|
| Clases y Objetos | PascalCase | `FormSession`, `InMemoryCommandBus` |
| Métodos y Funciones | camelCase | `updateInput()`, `dispatch()` |
| Value Objects | PascalCase | `SessionId`, `HexColor`, `NodeKey` |
| Reglas de Validación | `*Rule` | `RequiredRule`, `MinLengthRule` |
| Eventos de Dominio | `*Event` | `FormSessionStartedEvent`, `InputValueChangedEvent` |
| Puertos Hexagonales | `*Port` | `FormSessionPort`, `LayoutSourcePort` |
| Casos de Uso | `*UseCase` | `StartFormSessionUseCase` |
| Comandos | `*Command` | `UpdateInputValueCommand` |
| Handlers de Comando | `*Handler` | `UpdateInputValueHandler` |
| Adaptadores Reales | `*Adapter` o `*RepositoryImpl` | `RoomFormSessionAdapter` |
| Fakes de Test | `Fake*` | `FakeClock`, `FakeFormSessionRepository` |
| Tests Unitarios | `*Test` | `FormSessionTest`, `CommandBusTest` |
| Métodos Proveedores de Test | `given*` o `sample*` | `givenSampleSession()`, `givenTextInputNode()` |

---

## 7. Estrategia de Testing: Métodos Proveedores (Object Mother) y TDD

### 7.1 Regla de Oro: Métodos Proveedores de Objetos (Test Data Providers)
Para garantizar tests **altamente legibles, reutilizables y sin boilerplate repetitivo**, se prohíbe instanciar grafos complejos de objetos directamente dentro de cada test. 

**En su lugar, los tests DEBEN emplear métodos proveedores (`given*` / `sample*`) con parámetros por defecto de Kotlin:**

#### Ventajas obligatorias:
1. **Legibilidad inmediata:** Cada test declara únicamente el valor que le interesa probar (ej. `givenSampleSession(document = customDoc)`).
2. **Resiliencia al cambio:** Si el constructor de `FormSession`, `LayoutDocument` o un `Node` cambia, solo se actualiza el método proveedor centralizado, sin romper decenas de tests individuales.
3. **Estructura clara Arrange-Act-Assert:** La fase de *Arrange* se reduce a 1 o 2 líneas concisas y autodescriptivas.

#### Ejemplo Canónico de Métodos Proveedores:

```kotlin
class FormSessionTest {

    // === MÉTODOS PROVEEDORES (Test Data Providers) ===

    private fun givenComponentId(id: String = "input-1"): ComponentId =
        ComponentId.of(id).fold({ throw IllegalStateException("ID inválido en test") }, { it })

    private fun givenTextInputNode(
        id: String = "input-1",
        label: String = "Correo electrónico",
        isSensitive: Boolean = false,
        rules: List<ValidationRule> = emptyList()
    ): InputNode = InputNode(
        id = givenComponentId(id),
        label = label,
        isSensitive = isSensitive,
        validationRules = rules
    )

    private fun givenLayoutDocument(
        root: UIComponentNode = givenTextInputNode(),
        version: String = "1.0.0"
    ): LayoutDocument = LayoutDocument.create(
        schemaVersion = SchemaVersion(version),
        root = root
    ).fold({ throw IllegalStateException("Documento inválido en fixture de test") }, { it })

    private fun givenSampleSession(
        sessionId: String = "session-test-1",
        document: LayoutDocument = givenLayoutDocument(),
        clock: ClockPort = FakeClock(1700000000L)
    ): FormSession = FormSession.start(
        id = SessionId.of(sessionId).fold({ throw IllegalStateException() }, { it }),
        document = document,
        clock = clock
    ).session

    // === TESTS UNITARIOS LIMPIOS Y CONCISOS ===

    @Test
    fun `updateInput records valid value and emits InputValueChangedEvent`() {
        // Arrange (1 línea autodescriptiva gracias al proveedor)
        val session = givenSampleSession()
        val key = NodeKey(givenComponentId("input-1"))
        val newValue = FieldValue.Text("usuario@ejemplo.com")

        // Act
        val outcome = session.updateInput(key, newValue, occurredAt = 1700000010L)

        // Assert
        val updatedSession = assertIs<Either.Right<FormOutcome>>(outcome).value.session
        assertEquals(newValue, updatedSession.getValue(key))
    }
}
```

### 7.2 Fakes Puros en Memoria vs Mocking Pesado
* En `:flexui-core` **no se utilizan frameworks de mocking pesado** (como MockK o Mockito) para puertos de dominio.
* Se usan **Fakes puros en memoria** ubicados en `src/test/kotlin/.../fakes/`:
  * `FakeClock`, `FakeEventPublisher`, `FakeFormSessionRepository`, `FakeLayoutSource`, `FakeHostActionHandler`.
* Esto garantiza pruebas instantáneas, predecibles, libres de reflexión y listas para Kotlin Multiplatform.

### 7.3 Pruebas Arquitectónicas con Konsist
En `src/test/kotlin/.../HexagonalArchitectureTest.kt` se validan de forma continua las siguientes reglas:
1. `domain layer should never depend on application layer`
2. `domain layer should never depend on infrastructure layer`
3. `core module should never depend on android or compose frameworks`
4. `core module should never depend on dependency injection frameworks`
5. `core production classes and interfaces should have explicit visibility modifier`

### 7.4 Comandos de Ejecución y Validación

```bash
# Compilar y verificar firmas públicas en Core
./gradlew :flexui-core:compileKotlin

# Ejecutar todos los tests unitarios y pruebas Konsist del Core
./gradlew :flexui-core:test

# Ejecutar análisis y tests completos de todo el repositorio
./gradlew check

# Compilar la aplicación Android completa
./gradlew assembleDebug
```

---

## 8. Directivas Estrictas y Prohibiciones Absolutas para Agentes de IA

Cualquier agente de IA que genere o modifique código en este repositorio debe respetar estas reglas sin excepción:

1. **PROHIBIDO introducir dependencias de Android, Compose o DI en `:flexui-core`:** Ni un solo import que comience por `android.*`, `androidx.*`, `androidx.compose.*`, `dagger.*` o `hilt.*`.
2. **PROHIBIDO lanzar excepciones en la lógica de dominio:** Prohibido `throw Exception()`, `require()`, `check()` o `assert` en reglas de validación o invariantes de negocio. Usar siempre `Either<DomainError, T>`.
3. **PROHIBIDO mutar el estado fuera del Aggregate Root:** Ningún caso de uso, handler o composable puede alterar el estado de un formulario sin invocar a `FormSession`.
4. **PROHIBIDO colocar lógica de negocio en Composables:** Los componentes Compose son puramente declarativos; delegan la interacción al `CommandBus`.
5. **PROHIBIDO silenciar `CancellationException`:** Cualquier bloque `try/catch` que capture excepciones debe relanzar inmediatamente `CancellationException`.
6. **PROHIBIDO tests con boilerplate repetitivo:** Usar siempre métodos proveedores (`given*` / `sample*`) con valores por defecto para construir objetos de prueba legibles y mantenibles.
7. **OBLIGATORIO respetar `explicitApi()` en Core:** Toda declaración en `src/main` de `:flexui-core` debe especificar explícitamente su visibilidad y tipo de retorno.
8. **OBLIGATORIO mantener las pruebas Konsist en verde:** Ninguna contribución puede romper `HexagonalArchitectureTest`.
9. **OBLIGATORIO crear pruebas unitarias para todo nuevo feature:** Todo nuevo nodo, regla, comando, caso de uso o adaptador debe incluir sus respectivos tests con fakes antes de considerarse terminado.
