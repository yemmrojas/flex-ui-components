# Tasks: Módulo Core de FlexUI (`:flexui-core`)

Este documento desglosa el plan de implementación incremental para `:flexui-core` siguiendo TDD riguroso. Cada tarea es autónoma, compila de forma aislada y no introduce breaking changes.

---

## Bloque 0: Infraestructura de Build y Creación del Módulo

- [x] **0. Creación y Configuración del Módulo Gradle `:flexui-core`**
  - Registrar `:flexui-core` en `settings.gradle.kts`.
  - Crear `flexui-core/build.gradle.kts` con plugin `kotlin("jvm")`, `explicitApi()`, coroutines core y librerías de test (JUnit 5, AssertJ / Kotlin test, Konsist).
  - Crear estructura de carpetas `src/main/kotlin` y `src/test/kotlin`.
  - Verificar sincronización y compilación base con `./gradlew :flexui-core:assemble`.

---

## Bloque 1: Dominio Puro y Primitivas Funcionales (TDD)

- [x] **1. Primitivas Funcionales y Tipos Base** (ref: spec §1, design §1)
  - [x] Implementar `Either<L, R>` (`fold`, `map`, `flatMap`, `mapLeft`, combinador `zip` para acumulación).
  - [x] Implementar `Nel<E>` (Non-Empty List) inmutable.
  - [x] Tests unitarios en `EitherTest` y `NelTest` con 100% de cobertura.

- [x] **2. Errores de Dominio Tipados** (ref: spec §7, design §2)
  - [x] Crear archivo base `DomainError.kt` con interfaces selladas (`Structural`, `Value`, `Interaction`).
  - [x] Añadir nuevos errores requeridos (`SessionNotFound`, `InvalidValidationRule`, `UnsupportedSchemaVersion`, `FormValidationFailed`).
  - [x] Crear tests unitarios en `DomainErrorTest`.

- [x] **3. Value Objects con Factorías Privadas e Invariantes Numéricos** (ref: spec §2, design §3)
  - [x] Implementar `HexColor` con factoría regex y `PropertyValue`.
  - [x] Implementar `NumberRange` con factoría validando `isFinite()`.
  - [x] Implementar `StyleProperties`.
  - [x] Implementar `SessionId`, `LayoutId`, `ComponentId`, `ActionId`, `NodeKey`.
  - [x] Implementar `Dimension` (`Dp` con factoría `Dp.of(v)` validando `isFinite() && v >= 0f` sin excepciones ni `require`, `MatchParent`, `WrapContent`).
  - [x] Implementar `FontSize` y `SchemaVersion`.
  - [x] Tests unitarios en `ValueObjectsTest`.

- [x] **4. Valores de Entrada y Reglas de Validación Modulares** (ref: spec §3, design §4)
  - [x] Implementar `ValidationRule.kt` como `fun interface` limpia (SAM).
  - [x] Implementar `ValidationError.kt` en archivo independiente.
  - [x] Separar reglas en archivos individuales: `RequiredRule`, `MinLengthRule`, `MaxLengthRule`, `RegexRule`, `NumberRangeRule`.
  - [x] Tests unitarios en `ValidationRulesTest` pasando en verde.
  - [x] Incorporar factorías privadas retornando `Either` en `MinLengthRule` y `RegexRule`.

---

## Bloque 2: Nodos del Árbol de UI y LayoutDocument

- [x] **5. Nodos Estructurales y Contratos de Entrada/Interacción** (ref: spec §4, design §4)
  - Interfaces `UIComponentNode`, `InteractiveNode` (con `actionId`, `isEnabled`, `requiresValidation`), `InputCapableNode` (con `isSensitive`, `accept(value)`), `LayoutNode`.
  - Nodos atómicos: `TextNode`, `ButtonNode`, `InputNode`, `CheckNode`, `SelectNode`, `SliderNode`, `ImageNode`, `LoaderNode`, `ToastNode`, `CustomNode`, `UnsupportedFallbackNode`.
  - Nodos contenedores: `VerticalLayoutNode`, `HorizontalLayoutNode`, `ScrollLayoutNode`, `FloatingButtonLayoutNode`, `ListLayoutNode`, `SliderLayoutNode`.
  - Tests unitarios validando que cada nodo acepte exclusivamente su tipo de `FieldValue` correspondiente.

- [x] **6. `LayoutDocument` con Índices Precalculados O(1)** (ref: spec §5, design §5)
  - Factoría `LayoutDocument.create(schemaVersion, root)` acumulando errores en `Either<Nel<DomainError.Structural>, LayoutDocument>`.
  - Validaciones: profundidad máxima `<= 20`, límite de nodos `<= 200`, unicidad de `ComponentId`, referencias cruzadas de listas.
  - Precomputación de `nodeIndex`, `actionIndex` e `interactiveIndex`.
  - Tests unitarios en `LayoutDocumentTest`.

---

## Bloque 3: Aggregate Root `FormSession` y Eventos de Dominio

- [x] **7. Eventos de Dominio con Nomenclatura Uniforme (`*Event`)** (ref: spec §8, design §7)
  - Jerarquía `DomainEvent` con timestamp.
  - Implementar `FormSessionStartedEvent`, `InputValueChangedEvent` (enmascaramiento sensible), `InputValidationFailedEvent`, `InputValidationRecoveredEvent`, `UserActionDispatchedEvent`, `UserActionRejectedEvent`, `UserActionUnhandledEvent`.

- [x] **8. Aggregate Root `FormSession` e Invariantes de Formulario** (ref: spec §6, design §6)
  - [x] Clase inmutable `FormSession` con constructor privado e igualdad referencial.
  - [x] `FormInteractionState` como estado interno completamente encapsulado.
  - [x] `FormSession.start(sessionId, document, clock)` sembrando valores por defecto (ej. `defaultChecked`).
  - [x] `updateInput(key, value, occurredAt)` con enmascaramiento y transiciones de error.
  - [x] `requestAction(key, occurredAt)` retornando `ActionDecision.Dispatch` o `ActionDecision.Blocked` (persistiendo errores marcados).
  - [x] `recordActionResult(...)` generando eventos de resolución dentro del aggregate.
  - [x] Tests unitarios exhaustivos en `FormSessionTest`.

---

## Bloque 4: Puertos Hexagonales y Casos de Uso (Secuencia Pura)

- [x] **9. Puertos Hexagonales de Salida** (ref: spec §Puertos, design §7)
  - [x] Implementar `ClockPort`, `DomainEventPublisherPort` (con helper `publishSafely` relanzando `CancellationException`).
  - [x] Implementar `FormSessionPort` (`load`, `save`).
  - [x] Implementar `HostActionHandlerPort` (`handleAction`) y tipos `UserAction`, `ActionResult`.
  - [x] Implementar `LayoutSourcePort` recibiendo `LayoutRequest(layoutId: LayoutId)`.
  - [x] Crear fakes en memoria (`FakeClock`, `FakeEventPublisher`, `FakeFormSessionRepository`, `FakeLayoutSource`, `FakeHostActionHandler`) en `testFixtures` o `test`.

- [x] **10. Casos de Uso de Aplicación (Secuencia sin ifs de Reglas)** (ref: spec §RF-3/4/5, design §8.C)
  - [x] Implementar `StartFormSessionUseCase`: `load layout -> FormSession.start -> save session -> publish events`.
  - [x] Implementar `UpdateInputValueUseCase`: `load session -> updateInput -> save session -> publish events`.
  - [x] Implementar `ProcessUserActionUseCase`: `load session -> requestAction -> save & publish -> dispatch host -> record result -> save & publish`.
  - [x] Tests unitarios en `StartFormSessionUseCaseTest`, `UpdateInputValueUseCaseTest`, `ProcessUserActionUseCaseTest`.

---

## Bloque 5: Command Bus Tipado, Middlewares y Handlers

- [x] **11. Contratos del Command Bus Tipado y Comandos** (ref: spec §Comandos, design §8.A/B)
  - [x] `Command<R>`, `CommandHandler<C : Command<R>, R>` invariantes.
  - [x] `SessionCommand` con `sessionId`.
  - [x] Comandos: `StartFormSessionCommand`, `UpdateInputValueCommand`, `ProcessUserActionCommand`.

- [x] **12. Handlers de Comandos** (ref: design §8.D)
  - [x] Implementar `StartFormSessionHandler`, `UpdateInputValueHandler`, `ProcessUserActionHandler` validando Value Objects y delegando en los casos de uso.
  - [x] Tests unitarios de cada handler.

- [x] **13. Implementación del CommandBus en Memoria y Middlewares** (ref: design §8.A, ADR-002)
  - [x] Implementar `InMemoryCommandBus` con registro por `KClass<out Command<*>>`.
  - [x] Implementar middleware de serialización por sesión con `Mutex` sobre `SessionCommand`.
  - [x] Test unitario de unicidad de handlers (1 handler por clase de comando, `commandName` único).
  - [x] Test de concurrencia verificando que comandos sobre la misma sesión no se pisan.

---

## Bloque 6: Verificación de Arquitectura y Configuración del Módulo

- [x] **14. Pruebas de Arquitectura con Konsist** (ref: design §11)
  - [x] Regla: `core.domain` no importa `core.application`.
  - [x] Regla: `:flexui-core` no importa `android.*`, `androidx.compose.*`, ni librerías de DI (`dagger.*`, `hilt.*`).
  - [x] Regla: Visibilidad explícita validada (`explicitApi()`).

- [x] **15. Configuración de Build y Verificación Final**
  - [x] Ajustar `build.gradle.kts` de `:flexui-core` con `kotlin("jvm")`, `explicitApi()` y dependencias de test.
  - [x] Ejecutar suite completa de tests (`./gradlew :flexui-core:test`).
