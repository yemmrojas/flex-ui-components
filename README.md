# FlexUI - JSON Compose Renderer

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.09.00-green.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24-orange.svg)](https://developer.android.com/about/versions/nougat)

Una librería Android moderna que renderiza interfaces de usuario de Jetpack Compose dinámicamente a partir de configuraciones JSON. Ideal para prototipado rápido, Server-Driven UI (SDUI), y aplicaciones que requieren interfaces configurables sin recompilación.

## ✨ Características

- **🎨 Renderizado Dinámico**: Crea UIs completas desde JSON sin recompilar código
- **📦 Componentes Modulares**: 6 contenedores de layout + 9 componentes atómicos listos para usar
- **🔄 Arquitectura Limpia**: Separación clara entre interpretación y renderización
- **⚡ Alto Rendimiento**: Optimizado con remember, keys, lazy loading y caching
- **🛡️ Manejo Robusto de Errores**: Error boundaries y placeholders visuales
- **🎯 Type-Safe**: Aprovecha el sistema de tipos de Kotlin con sealed classes
- **🧪 Testeable**: Arquitectura modular diseñada para testing

## 📋 Requisitos

- Android SDK 24+
- Kotlin 2.0.21+
- Jetpack Compose 2024.09.00+

## 🚀 Instalación

### Gradle (Kotlin DSL)

```kotlin
// En tu archivo settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        // Agregar repositorio cuando esté publicado
    }
}

// En tu módulo app/build.gradle.kts
dependencies {
    implementation("com.libs.flex.ui:flexui:1.0.0")
}
```

## 📖 Uso Básico

### 1. Configuración Simple

```kotlin
import com.libs.flex.ui.flexui.JsonRenderer
import androidx.compose.runtime.Composable

@Composable
fun MyScreen() {
    val jsonConfig = """
        {
          "id": "main",
          "type": "contentVertical",
          "children": [
            {
              "id": "title",
              "type": "componentTextView",
              "text": "¡Hola FlexUI!",
              "textStyle": "bold",
              "fontSize": 24
            },
            {
              "id": "submit_btn",
              "type": "componentButton",
              "text": "Enviar",
              "buttonStyle": "primary"
            }
          ]
        }
    """.trimIndent()
    
    JsonRenderer(
        jsonString = jsonConfig,
        onEvent = { event ->
            // Manejar eventos de interacción
            println("Event: $event")
        }
    )
}
```

### 2. Manejo de Eventos

```kotlin
JsonRenderer(
    jsonString = jsonConfig,
    onEvent = { event ->
        when (event) {
            is ComponentEvent.Click -> {
                println("Clicked: ${event.componentId}")
                event.actionId?.let { action ->
                    handleAction(action)
                }
            }
            is ComponentEvent.ValueChange -> {
                println("Value changed: ${event.componentId} = ${event.value}")
                updateFormData(event.componentId, event.value)
            }
            is ComponentEvent.Selection -> {
                println("Selected: ${event.componentId} = ${event.selectedValue}")
                updateSelection(event.componentId, event.selectedValue)
            }
        }
    }
)
```

### 3. Manejo de Errores Personalizado

```kotlin
JsonRenderer(
    jsonString = jsonConfig,
    onEvent = { event -> handleEvent(event) },
    errorContent = { error ->
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Error, contentDescription = null)
            Text("Error: ${error.message}")
            Button(onClick = { retry() }) {
                Text("Reintentar")
            }
        }
    }
)
```

## 🧩 Componentes Disponibles

### Contenedores de Layout

| Tipo | Descripción | Propiedades Clave |
|------|-------------|-------------------|
| `contentVertical` | Organiza componentes en columna | `arrangement`, `alignment`, `children` |
| `contentHorizontal` | Organiza componentes en fila | `arrangement`, `alignment`, `children` |
| `contentScroll` | Contenedor scrollable | `scrollDirection`, `children` |
| `contentWithFloatingButton` | Scaffold con FAB | `fabIcon`, `fabPosition`, `children` |
| `contentList` | Lista lazy optimizada | `items`, `itemTemplate` |
| `contentSlider` | Slider de imágenes | `items`, `autoPlay`, `autoPlayInterval` |

### Componentes Atómicos

| Tipo | Descripción | Propiedades Clave |
|------|-------------|-------------------|
| `componentTextView` | Texto estilizado | `text`, `textStyle`, `fontSize`, `color`, `maxLines` |
| `componentInput` | Campo de entrada | `label`, `placeholder`, `inputStyle`, `validation` |
| `componentButton` | Botón interactivo | `text`, `buttonStyle`, `actionId` |
| `componentCheck` | Checkbox con label | `label`, `checked` |
| `componentSelect` | Dropdown menu | `label`, `options`, `placeholder` |
| `componentSliderCheck` | Slider numérico | `min`, `max`, `value`, `label` |
| `componentImage` | Imagen desde URL | `imageUrl`, `contentScale` |
| `componentLoader` | Indicador de carga | `loaderStyle`, `color`, `size` |
| `componentToast` | Notificación Snackbar | `message`, `duration`, `toastType` |

## 📝 Ejemplos de JSON

### Formulario Completo

```json
{
  "id": "registration_form",
  "type": "contentScroll",
  "style": {
    "padding": {
      "start": 16,
      "top": 16,
      "end": 16,
      "bottom": 16
    }
  },
  "children": [
    {
      "id": "header",
      "type": "componentTextView",
      "text": "Registro de Usuario",
      "textStyle": "bold",
      "fontSize": 28,
      "color": "#1976D2",
      "style": {
        "padding": { "bottom": 24 }
      }
    },
    {
      "id": "name_input",
      "type": "componentInput",
      "label": "Nombre completo",
      "placeholder": "Ingresa tu nombre",
      "inputStyle": "outlined",
      "validation": {
        "required": true,
        "minLength": 2,
        "errorMessage": "El nombre debe tener al menos 2 caracteres"
      }
    },
    {
      "id": "email_input",
      "type": "componentInput",
      "label": "Correo electrónico",
      "placeholder": "ejemplo@correo.com",
      "inputStyle": "outlined",
      "validation": {
        "required": true,
        "pattern": "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$",
        "errorMessage": "Formato de correo inválido"
      }
    },
    {
      "id": "country_select",
      "type": "componentSelect",
      "label": "País",
      "placeholder": "Selecciona tu país",
      "options": [
        { "label": "México", "value": "mx" },
        { "label": "Colombia", "value": "co" },
        { "label": "Argentina", "value": "ar" },
        { "label": "España", "value": "es" }
      ]
    },
    {
      "id": "age_slider",
      "type": "componentSliderCheck",
      "label": "Edad",
      "min": 18,
      "max": 100,
      "value": 25
    },
    {
      "id": "terms_check",
      "type": "componentCheck",
      "label": "Acepto los términos y condiciones",
      "checked": false
    },
    {
      "id": "newsletter_check",
      "type": "componentCheck",
      "label": "Suscribirme al boletín",
      "checked": true
    },
    {
      "id": "button_container",
      "type": "contentHorizontal",
      "arrangement": "spaceEvenly",
      "style": {
        "padding": { "top": 24 }
      },
      "children": [
        {
          "id": "cancel_btn",
          "type": "componentButton",
          "text": "Cancelar",
          "buttonStyle": "secondary",
          "actionId": "cancel_registration"
        },
        {
          "id": "submit_btn",
          "type": "componentButton",
          "text": "Registrarse",
          "buttonStyle": "primary",
          "actionId": "submit_registration"
        }
      ]
    }
  ]
}
```

### Slider de Imágenes con Auto-Play

```json
{
  "id": "promo_slider",
  "type": "contentSlider",
  "autoPlay": true,
  "autoPlayInterval": 3000,
  "items": [
    "https://example.com/promo1.jpg",
    "https://example.com/promo2.jpg",
    "https://example.com/promo3.jpg"
  ],
  "style": {
    "height": "200dp",
    "borderRadius": 12
  }
}
```

### Lista Dinámica

```json
{
  "id": "product_list",
  "type": "contentList",
  "items": [
    { "id": "1", "name": "Producto A", "price": "$10.00" },
    { "id": "2", "name": "Producto B", "price": "$20.00" },
    { "id": "3", "name": "Producto C", "price": "$30.00" }
  ],
  "itemTemplate": {
    "id": "item_{{id}}",
    "type": "contentHorizontal",
    "arrangement": "spaceBetween",
    "children": [
      {
        "id": "name_{{id}}",
        "type": "componentTextView",
        "text": "{{name}}",
        "textStyle": "semiBold"
      },
      {
        "id": "price_{{id}}",
        "type": "componentTextView",
        "text": "{{price}}",
        "color": "#4CAF50"
      }
    ]
  }
}
```

## 🎨 Sistema de Estilos

### Propiedades de Estilo Disponibles

```json
{
  "style": {
    "padding": {
      "start": 16,
      "top": 8,
      "end": 16,
      "bottom": 8
    },
    "margin": {
      "start": 0,
      "top": 0,
      "end": 0,
      "bottom": 0
    },
    "backgroundColor": "#FFFFFF",
    "borderRadius": 8,
    "elevation": 4,
    "width": "match_parent",
    "height": "wrap_content"
  }
}
```

### Valores de Dimensión

- Valores numéricos: `16` (interpretado como dp)
- Con unidad: `"16dp"`
- Especiales: `"match_parent"`, `"wrap_content"`

### Colores

Formato hexadecimal: `"#RRGGBB"` o `"#AARRGGBB"`

Ejemplos:
- `"#FF0000"` - Rojo
- `"#80000000"` - Negro semi-transparente
- `"#1976D2"` - Azul Material

## 🏗️ Arquitectura

```
┌─────────────────────────────────────────┐
│         Client Application              │
│  (MainActivity, ViewModels, etc.)       │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│         JsonRenderer API                │
│  (Public Composable Function)           │
└────────────┬───────────────┬────────────┘
             │               │
             ▼               ▼
┌────────────────────┐  ┌──────────────────┐
│ Interpretation     │  │ Visual           │
│ Module             │  │ Components       │
│                    │  │ Module           │
│ • JsonParser       │  │                  │
│ • ComponentMapper  │  │ • ComponentFactory│
│ • ValidationEngine │  │ • Containers     │
│                    │  │ • Atomic Comps   │
└────────────────────┘  └──────────────────┘
```

### Módulos Principales

**Interpretation Module** (`parser/`)
- Parseo de JSON con kotlinx.serialization
- Validación de estructura y propiedades
- Mapeo de tipos string a enums
- Construcción del árbol de componentes

**Visual Components Module** (`components/`)
- Renderizado de componentes Compose
- Aplicación de estilos y propiedades
- Manejo de estado interno
- Emisión de eventos de interacción

**Event System** (`events/`)
- Propagación de eventos de usuario
- Callbacks type-safe
- Ejecución en hilo principal

## 🔧 Configuración Avanzada

### Caching de Componentes

La librería incluye caching automático para evitar parseo redundante:

```kotlin
// El cache se maneja internamente
// Los componentes se cachean por hash MD5 del JSON
JsonRenderer(jsonString = config) // Primera vez: parsea
JsonRenderer(jsonString = config) // Segunda vez: usa cache
```

### Validación Personalizada

```json
{
  "id": "password_input",
  "type": "componentInput",
  "label": "Contraseña",
  "inputStyle": "outlined",
  "validation": {
    "required": true,
    "minLength": 8,
    "pattern": "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
    "errorMessage": "Mínimo 8 caracteres, debe incluir letras y números"
  }
}
```

## 🧪 Testing

### Unit Tests

```kotlin
class JsonParserTest {
    private val parser = JsonParser()
    
    @Test
    fun `parse should return success when JSON is valid`() = runBlocking {
        val validJson = """{"id": "test", "type": "componentButton", "text": "Click"}"""
        val result = parser.parse(validJson)
        
        assertTrue(result.isSuccess)
        assertEquals("test", result.getOrNull()?.id)
    }
    
    @Test
    fun `parse should throw ComponentTypeNotFoundException for unknown type`() = runBlocking {
        val invalidJson = """{"id": "test", "type": "unknownType"}"""
        val result = parser.parse(invalidJson)
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ComponentTypeNotFoundException)
    }
}
```

### Compose UI Tests

```kotlin
class ButtonComponentTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `button click should trigger event callback`() {
        var eventReceived: ComponentEvent? = null
        val descriptor = AtomicDescriptor(
            id = "test_btn",
            type = ComponentType.COMPONENT_BUTTON,
            text = "Click Me",
            buttonStyle = "primary"
        )
        
        composeTestRule.setContent {
            ButtonComponent(
                descriptor = descriptor,
                onEvent = { eventReceived = it }
            )
        }
        
        composeTestRule.onNodeWithText("Click Me").performClick()
        
        assertNotNull(eventReceived)
        assertTrue(eventReceived is ComponentEvent.Click)
        assertEquals("test_btn", (eventReceived as ComponentEvent.Click).componentId)
    }
}
```

## 📊 Rendimiento

### Optimizaciones Implementadas

- ✅ Parseo asíncrono en background thread (Dispatchers.IO)
- ✅ Caching de componentes parseados (MD5 hash)
- ✅ Uso de `remember` y `derivedStateOf` para cálculos costosos
- ✅ Keys estables en LazyColumn para optimizar recomposición
- ✅ Lazy loading de imágenes con Coil
- ✅ Manejo eficiente de memoria con WeakReference en cache

### Benchmarks

| Operación | Tiempo Promedio |
|-----------|-----------------|
| Parseo JSON simple (5 componentes) | ~15ms |
| Parseo JSON complejo (50 componentes) | ~80ms |
| Renderizado inicial | ~30ms |
| Recomposición con cache | ~5ms |

## 🛡️ Manejo de Errores

### Tipos de Excepciones

```kotlin
sealed class FlexUIException : Exception()

class JsonParseException(message: String, cause: Throwable?)
class ComponentTypeNotFoundException(val type: String)
class MissingPropertyException(val property: String, val componentType: String)
class ValidationException(val errors: List<String>)
class RenderException(message: String, cause: Throwable?)
```

### Error Boundaries

Los errores de renderizado se manejan a nivel de componente individual:

```kotlin
// Si un componente falla, se renderiza un placeholder
// El resto de la UI continúa funcionando
ErrorPlaceholder(message = "Error al renderizar componente")
```

## 🤝 Contribución

Las contribuciones son bienvenidas. Por favor:

1. Fork el repositorio
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

### Estándares de Código

- Seguir [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Usar [Jetpack Compose Guidelines](https://developer.android.com/jetpack/compose/guidelines)
- Escribir tests para nuevas funcionalidades
- Documentar APIs públicas con KDoc

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo [LICENSE](LICENSE) para más detalles.

## 🙏 Agradecimientos

- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Framework UI
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) - Serialización JSON
- [Coil](https://coil-kt.github.io/coil/) - Carga de imágenes

## 📞 Contacto

- **Issues**: [GitHub Issues](https://github.com/yourusername/flexui/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/flexui/discussions)

## 🗺️ Roadmap

### v1.0.0 (MVP - Actual)
- ✅ 6 contenedores de layout
- ✅ 9 componentes atómicos
- ✅ Sistema de eventos
- ✅ Manejo de errores robusto
- ✅ Caching básico

### v1.1.0 (Próximo)
- [ ] Soporte para animaciones desde JSON
- [ ] Componentes personalizados (plugin system)
- [ ] Temas dinámicos
- [ ] Validación en tiempo real
- [ ] Modo debug con inspector visual

### v2.0.0 (Futuro)
- [ ] Server-Driven UI completo con sincronización
- [ ] A/B testing integrado
- [ ] Analytics de interacciones
- [ ] Soporte para gestos personalizados
- [ ] Editor visual de JSON

---

**Hecho con ❤️ usando Kotlin y Jetpack Compose**
