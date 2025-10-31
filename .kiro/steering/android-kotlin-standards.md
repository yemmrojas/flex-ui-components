---
inclusion: always
---

# Android Kotlin Coding Standards for FlexUI Project

## General Principles

- Write clean, readable, and maintainable Kotlin code.
- Follow SOLID principles and hexagonal architecture patterns (ports and adapters).
- Prioritize immutability and functional programming when appropriate.
- Use generically typed functions and classes for code reuse when necessary.
- Use meaningful names that clearly express intent.
- Keep functions small and focused on a single responsibility, avoiding code smells.
- Never inject bulk classes into our constructors; always use abstraction through interfaces when necessary.
- Before writing any functionality, let's review which design pattern is appropriate for the solution to maintain the scalability of our code.
- We will always write this project using the TDD convention (test, driver, development).
- Our project's unit tests must cover 100% of the cases and must comply with the provider function convention to avoid the setup function beforehand. This is done so that each test can manage its mocks and instantiate them.

## Kotlin Style Guide

### Naming Conventions

- **Classes and Objects**: PascalCase (e.g., `ComponentFactory`, `JsonParser`)
- **Functions and Variables**: camelCase (e.g., `parseComponent`, `jsonString`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_CACHE_SIZE`, `DEFAULT_TIMEOUT`)
- **Private Properties**: camelCase with underscore prefix optional (e.g., `_internalState` or `internalState`)
- **Composable Functions**: PascalCase (e.g., `JsonRenderer`, `ButtonComponent`)

### File Organization

- One top-level class per file
- File name should match the class name
- Order: package, imports, class/object definition
- Group imports: Android, third-party, project
- Remove unused imports

### Code Structure

```kotlin
// 1. Package declaration
package com.libs.flex.ui.flexui.components

// 2. Imports (grouped and sorted)
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.serialization.Serializable

// 3. Class/Object definition
@Composable
fun ComponentName(
    parameter1: String,
    parameter2: Int = 0,
    modifier: Modifier = Modifier
) {
    // Implementation
}
```

## Jetpack Compose Best Practices

### Composable Functions

- Always use PascalCase for composable function names
- Place `modifier` parameter last with default value `Modifier`
- Use `remember` for state that should survive recomposition
- Use `derivedStateOf` for computed values based on state
- Avoid side effects in composable body - use `LaunchedEffect`, `DisposableEffect`, etc.

```kotlin
@Composable
fun MyComponent(
    data: String,
    onEvent: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    var state by remember { mutableStateOf("") }
    
    LaunchedEffect(data) {
        // Side effect here
    }
    
    Column(modifier = modifier) {
        // UI implementation
    }
}
```

### State Management

- Use `remember` for simple local state
- Use `rememberSaveable` for state that should survive configuration changes
- Hoist state when multiple composables need access
- Use `mutableStateOf` for observable state
- Prefer immutable data classes for state

```kotlin
// Good: State hoisting
@Composable
fun ParentComponent() {
    var text by remember { mutableStateOf("") }
    
    ChildComponent(
        text = text,
        onTextChange = { text = it }
    )
}

@Composable
fun ChildComponent(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = text,
        onValueChange = onTextChange,
        modifier = modifier
    )
}
```

### Performance Optimization

- Use `key` parameter in lists to optimize recomposition
- Use `LazyColumn`/`LazyRow` for long lists
- Avoid creating new lambdas in composable body when possible
- Use `remember` to cache expensive computations
- Use `derivedStateOf` for derived state to minimize recompositions

```kotlin
// Good: Using keys in LazyColumn
LazyColumn {
    items(
        items = itemList,
        key = { item -> item.id }
    ) { item ->
        ItemComponent(item)
    }
}

// Good: Caching expensive computation
val expensiveValue = remember(input) {
    computeExpensiveValue(input)
}
```

## Kotlin Language Features

### Null Safety

- Prefer non-nullable types
- Use safe call operator `?.` instead of explicit null checks
- Use Elvis operator `?:` for default values
- Use `let` for null-safe operations

```kotlin
// Good
val length = text?.length ?: 0
descriptor.label?.let { label ->
    Text(label)
}

// Avoid
if (text != null) {
    val length = text.length
}
```

### Data Classes

- Use data classes for immutable data structures
- Provide default values for optional parameters
- Use copy() for creating modified instances

```kotlin
@Serializable
data class StyleProperties(
    val padding: PaddingValues? = null,
    val backgroundColor: String? = null,
    val borderRadius: Int? = null
)

// Usage
val newStyle = oldStyle.copy(borderRadius = 8)
```

### Sealed Classes

- Use sealed classes for restricted type hierarchies
- Prefer sealed classes over enums when types have different properties
- Use exhaustive when expressions with sealed classes

```kotlin
sealed class ComponentEvent {
    abstract val componentId: String
    
    data class Click(
        override val componentId: String,
        val actionId: String?
    ) : ComponentEvent()
    
    data class ValueChange(
        override val componentId: String,
        val value: Any?
    ) : ComponentEvent()
}

// Usage with exhaustive when
when (event) {
    is ComponentEvent.Click -> handleClick(event)
    is ComponentEvent.ValueChange -> handleValueChange(event)
    // Compiler ensures all cases are covered
}
```

### Extension Functions

- Use extension functions to add functionality to existing classes
- Keep extension functions focused and cohesive
- Place extension functions in appropriate files

```kotlin
// Good: Focused extension function
fun Modifier.applyStyleProperties(style: StyleProperties?): Modifier {
    if (style == null) return this
    return this
        .padding(style.padding?.toDp() ?: 0.dp)
        .background(style.backgroundColor?.toColor() ?: Color.Transparent)
}
```

## Error Handling

### Exception Hierarchy

- Create custom exception classes for domain-specific errors
- Use sealed classes for exception hierarchies
- Provide meaningful error messages

```kotlin
sealed class FlexUIException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

class JsonParseException(
    message: String,
    cause: Throwable? = null
) : FlexUIException(message, cause)
```

### Result Type

- Use `Result<T>` for operations that can fail
- Handle both success and failure cases explicitly
- Avoid throwing exceptions in library code when possible

```kotlin
suspend fun parse(jsonString: String): Result<ComponentDescriptor> {
    return try {
        val descriptor = parseInternal(jsonString)
        Result.success(descriptor)
    } catch (e: Exception) {
        Result.failure(JsonParseException("Failed to parse JSON", e))
    }
}

// Usage
parser.parse(json).fold(
    onSuccess = { descriptor -> render(descriptor) },
    onFailure = { error -> showError(error) }
)
```

### Graceful Degradation

- Render error placeholders instead of crashing
- Log errors for debugging
- Provide fallback values for missing data

```kotlin
@Composable
fun SafeComponent(descriptor: ComponentDescriptor) {
    try {
        ActualComponent(descriptor)
    } catch (e: Exception) {
        ErrorPlaceholder(e.message ?: "Unknown error")
        Logger.error("Component rendering failed", e)
    }
}
```

## Coroutines and Async

### Coroutine Scope

- Use appropriate coroutine scope for the context
- Use `Dispatchers.IO` for I/O operations
- Use `Dispatchers.Default` for CPU-intensive work
- Use `Dispatchers.Main` for UI updates (default in Compose)

```kotlin
suspend fun parseJson(jsonString: String): Result<ComponentDescriptor> = 
    withContext(Dispatchers.IO) {
        // I/O operation
        json.decodeFromString(jsonString)
    }
```

### LaunchedEffect

- Use `LaunchedEffect` for side effects in composables
- Specify keys to control when effect should restart
- Cancel ongoing work when composable leaves composition

```kotlin
@Composable
fun AutoPlaySlider(interval: Long) {
    val pagerState = rememberPagerState()
    
    LaunchedEffect(interval, pagerState.currentPage) {
        delay(interval)
        val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
        pagerState.animateScrollToPage(nextPage)
    }
}
```

## Serialization

### kotlinx.serialization

- Use `@Serializable` annotation on data classes
- Provide default values for optional fields
- Use `@SerialName` for custom JSON field names when needed
- Configure Json instance with appropriate settings

```kotlin
@Serializable
data class ComponentDescriptor(
    val id: String,
    val type: ComponentType,
    val style: StyleProperties? = null,
    @SerialName("custom_field")
    val customField: String? = null
)

val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}
```

## Documentation

### KDoc Comments

- Document public APIs with KDoc
- Include parameter descriptions
- Document return values and exceptions
- Provide usage examples for complex APIs

```kotlin
/**
 * Renders a UI component tree from JSON configuration.
 *
 * @param jsonString JSON string describing the component hierarchy
 * @param onEvent Callback invoked when user interacts with components
 * @param modifier Modifier to be applied to the root component
 * @param errorContent Composable to display when parsing or rendering fails
 *
 * @throws JsonParseException if JSON is malformed
 *
 * Example:
 * ```
 * JsonRenderer(
 *     jsonString = loadJson(),
 *     onEvent = { event -> handleEvent(event) }
 * )
 * ```
 */
@Composable
fun JsonRenderer(
    jsonString: String,
    onEvent: (ComponentEvent) -> Unit = {},
    modifier: Modifier = Modifier,
    errorContent: @Composable (Throwable) -> Unit = { DefaultErrorContent(it) }
) {
    // Implementation
}
```

### Inline Comments

- Use comments to explain "why", not "what"
- Keep comments concise and up-to-date
- Remove commented-out code before committing

```kotlin
// Good: Explains reasoning
// Use IO dispatcher to avoid blocking main thread during JSON parsing
withContext(Dispatchers.IO) {
    json.parseToJsonElement(jsonString)
}

// Avoid: States the obvious
// Parse JSON string
json.parseToJsonElement(jsonString)
```

## Testing Guidelines

### Unit Tests

- Write tests for business logic and data transformations
- Use descriptive test names that explain the scenario
- Follow **Given-When-Then** pattern (BDD style)
- Use MockK for mocking when needed
- Use **provider functions** for test data (avoid setup/beforeEach)
- Each test should manage its own mocks and instantiate them

```kotlin
@Test
fun `parse should return success when JSON is valid`() = runTest {
    // Given
    val parser = provideJsonParser()
    val validJson = provideValidJson()
    
    // When
    val result = parser.parse(validJson)
    
    // Then
    assertTrue(result.isSuccess)
    assertEquals("test", result.getOrNull()?.id)
}

// Provider functions
private fun provideJsonParser() = JsonParser()

private fun provideValidJson() = """
    {
        "id": "test",
        "type": "componentButton"
    }
""".trimIndent()
```

### MockK for Mocking

- Use MockK for all mocking needs
- Clear mocks after each test with `@After` hook
- Use `every` for stubbing behavior
- Use `verify` for verifying interactions

```kotlin
class MyServiceTest {
    
    @After
    fun tearDown() {
        unmockkAll()
        clearAllMocks()
    }
    
    @Test
    fun `service should delegate to port`() = runTest {
        // Given
        val mockPort = mockk<OutputPort>()
        every { mockPort.execute(any()) } returns mockResult
        val service = provideService(mockPort)
        
        // When
        val result = service.process(input)
        
        // Then
        verify { mockPort.execute(input) }
        assertEquals(expected, result)
    }
    
    // Provider functions
    private fun provideService(port: OutputPort) = MyService(port)
    private fun provideMockResult() = Result.success(data)
}
```

### Provider Functions Convention

- Use provider functions instead of `@Before` setup
- Each test manages its own dependencies
- Improves test isolation and readability
- Makes test data explicit

```kotlin
class ParserTest {
    
    @Test
    fun `should parse valid JSON`() = runTest {
        // Given
        val parser = provideParser()
        val json = provideValidJson()
        
        // When
        val result = parser.parse(json)
        
        // Then
        assertTrue(result.isSuccess)
    }
    
    // Provider functions - clear and reusable
    private fun provideParser() = JsonParser()
    
    private fun provideValidJson() = """
        {
            "id": "test",
            "type": "componentButton"
        }
    """.trimIndent()
    
    private fun provideInvalidJson() = """
        {
            "id": "test"
        }
    """.trimIndent()
}
```

### Compose UI Tests

- Use Compose testing APIs for UI tests
- Test user interactions and state changes
- Use semantic properties for finding components

```kotlin
@Test
fun `button click should trigger event callback`() {
    // Given
    var eventReceived: ComponentEvent? = null
    val descriptor = provideButtonDescriptor()
    
    composeTestRule.setContent {
        ButtonComponent(
            descriptor = descriptor,
            onEvent = { eventReceived = it }
        )
    }
    
    // When
    composeTestRule.onNodeWithText("Click Me").performClick()
    
    // Then
    assertNotNull(eventReceived)
    assertTrue(eventReceived is ComponentEvent.Click)
}

// Provider function
private fun provideButtonDescriptor() = AtomicDescriptor(
    id = "test_button",
    type = ComponentType.COMPONENT_BUTTON,
    text = "Click Me"
)
```

## Code Review Checklist

Before submitting code for review, ensure:

- [ ] Code follows Kotlin style guide
- [ ] All public APIs are documented with KDoc
- [ ] Error handling is implemented properly
- [ ] No hardcoded strings or magic numbers
- [ ] Composables follow best practices (modifier last, state hoisting)
- [ ] Performance considerations addressed (keys, remember, lazy loading)
- [ ] Null safety properly handled
- [ ] No unused imports or variables
- [ ] Code is formatted consistently
- [ ] Tests are written for new functionality

## Architecture Guidelines

### Modular Hexagonal Architecture

- Each module (parser, renderer, validator) has its own hexagonal architecture
- Modules are self-contained with clear boundaries
- Architecture is encapsulated within each module package

#### Module Structure

```
module/
├── ModuleName.kt                    (Public API - Entry point)
├── application/                     (Application Layer - Wiring & DI)
│   └── ModuleFacade.kt
├── domain/                          (Domain Layer - Business logic)
│   ├── ports/
│   │   ├── InputPort.kt            (Use cases)
│   │   └── OutputPort.kt           (Dependencies)
│   └── service/
│       └── ModuleService.kt
└── infrastructure/                  (Infrastructure Layer - Technical details)
    ├── adapter/
    │   └── ConcreteAdapter.kt
    ├── mapper/
    │   └── TypeMapper.kt
    ├── property/
    │   └── PropertyParser.kt
    └── util/
        └── Extensions.kt
```

#### Example: Parser Module

```kotlin
// Public API (Entry point)
class JsonParser(
    strategies: List<ComponentParserStrategyPort> = defaultStrategies()
) {
    private val facade = JsonParserFacade(strategies)
    
    suspend fun parse(jsonString: String): Result<ComponentDescriptor> {
        return facade.parse(jsonString)
    }
}

// Domain Port (Interface)
interface ComponentParserStrategyPort {
    fun canParse(type: ComponentType): Boolean
    fun parse(jsonObject: JsonObject, type: ComponentType): ComponentDescriptor
}

// Infrastructure Adapter (Implementation)
class LayoutParserStrategy : ComponentParserStrategyPort {
    override fun canParse(type: ComponentType) = ComponentMapper.isLayoutType(type)
    override fun parse(jsonObject: JsonObject, type: ComponentType): ComponentDescriptor {
        // Implementation
    }
}
```

#### Benefits

- **Module Independence**: Each module evolves independently
- **Clear Boundaries**: Well-defined module interfaces
- **Easy to Understand**: Focus on one module at a time
- **Testable**: Test modules in isolation
- **Scalable**: Easy to add new modules

### Test Structure

- Mirror production code structure in tests
- Organize tests by architectural layer
- Use provider functions instead of setup methods

```
module/test/
├── ModuleNameTest.kt                (Integration tests)
├── application/
│   └── ModuleFacadeTest.kt
├── domain/
│   └── service/
│       └── ModuleServiceTest.kt
└── infrastructure/
    ├── adapter/
    │   └── AdapterTest.kt
    └── util/
        └── ExtensionsTest.kt
```

## Resources

- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Jetpack Compose Guidelines](https://developer.android.com/jetpack/compose/guidelines)
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [kotlinx.serialization Guide](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/serialization-guide.md)
- [Hexagonal Architecture Guide](https://medium.com/@edusalguero/arquitectura-hexagonal-59834bb44b7f)
- [BDD Testing with Given-When-Then](https://martinfowler.com/bliki/GivenWhenThen.html)
