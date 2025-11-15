# FlexUI Demo Application

This demo application showcases the FlexUI JSON Renderer library capabilities by providing multiple interactive examples of JSON-driven UI configurations.

## Features

The demo app includes 5 different JSON configurations demonstrating various FlexUI capabilities:

### 1. Form Demo (`sample_form.json`)
- **Components**: Input fields, slider, dropdown select, checkbox, buttons
- **Layout**: Vertical scroll container with form elements
- **Demonstrates**: 
  - Text input with outlined style
  - Slider component for age selection (18-100)
  - Dropdown select for country selection
  - Checkbox for newsletter subscription
  - Primary and secondary button styles
  - Event handling for all interactive components

### 2. List Demo (`sample_list.json`)
- **Components**: LazyColumn list with item templates
- **Layout**: Vertical container with dynamic list
- **Demonstrates**:
  - List rendering with item templates
  - Data binding with template placeholders ({{name}}, {{price}})
  - Horizontal arrangement within list items
  - Custom styling for list items (background, border radius)

### 3. Slider Demo (`sample_slider.json`)
- **Components**: HorizontalPager with image slider
- **Layout**: Vertical container with auto-playing slider
- **Demonstrates**:
  - Image loading from URLs (using Picsum placeholder images)
  - Auto-play functionality with 3-second intervals
  - Page indicators
  - Swipe gestures for navigation

### 4. Floating Button Demo (`sample_floating_button.json`)
- **Components**: Scaffold with FloatingActionButton
- **Layout**: Scroll container with FAB overlay
- **Demonstrates**:
  - Floating action button positioning
  - FAB click event handling with action ID
  - Content scrolling with FAB overlay
  - Icon rendering in FAB

### 5. Complex UI Demo (`sample_complex.json`)
- **Components**: Multiple nested containers with various components
- **Layout**: Complex hierarchy with hero section, feature cards, and form
- **Demonstrates**:
  - Deep component nesting
  - Multiple layout types (vertical, horizontal)
  - Custom styling (colors, padding, elevation, border radius)
  - Mixed component types in single UI
  - Visual hierarchy with styled sections

## Event Handling

The demo app implements comprehensive event logging for all component interactions:

### Click Events
```kotlin
ComponentEvent.Click(
    componentId = "submit_button",
    actionId = "submit_action",
    timestamp = 1234567890
)
```
Logged when buttons or FAB are clicked.

### ValueChange Events
```kotlin
ComponentEvent.ValueChange(
    componentId = "name_input",
    value = "John Doe",
    timestamp = 1234567890
)
```
Logged when input fields, sliders, or checkboxes change value.

### Selection Events
```kotlin
ComponentEvent.Selection(
    componentId = "country_select",
    selectedValue = "us",
    timestamp = 1234567890
)
```
Logged when dropdown selections are made.

## Architecture

The demo app follows **Clean Architecture** principles with **MVVM** pattern:

### Layers

#### Domain Layer (`domain/`)
- **Models**: `DemoType` - Enum representing available demos
- **Repository Interfaces**: `JsonAssetRepository` - Contract for loading JSON assets
- **Use Cases**: `LoadDemoJsonUseCase` - Business logic for loading demo JSON

#### Data Layer (`data/`)
- **Repository Implementations**: `JsonAssetRepositoryImpl` - Loads JSON from Android assets

#### Presentation Layer (`presentation/`)
- **Navigation**: `DemoNavigation` - Navigation Compose setup with routes
- **Screens**:
  - `HomeScreen` + `HomeViewModel` - Demo selector screen
  - `DemoScreen` + `DemoViewModel` - JSON renderer screen with event handling
- **UI State**: Sealed interfaces for representing screen states

### Navigation

Uses **Navigation Compose** with the following routes:
- `home` - Home screen with demo selector
- `demo/{demoType}` - Demo screen displaying JSON-driven UI

### Dependency Injection

Uses **Hilt** for dependency injection:
- `AppModule` - Provides repository bindings
- `@HiltViewModel` - ViewModels with constructor injection
- `@AndroidEntryPoint` - Activity and composables

### JSON Assets
All sample JSON files are located in `app/src/main/assets/`:
- `sample_form.json` - User registration form
- `sample_list.json` - Product list
- `sample_slider.json` - Image carousel
- `sample_floating_button.json` - FAB with content
- `sample_complex.json` - Multi-section complex UI

### Dependencies
- FlexUI library (`:FlexUI` module)
- Jetpack Compose for UI
- Navigation Compose for navigation
- Hilt for dependency injection
- Lifecycle ViewModel Compose for state management
- Material3 for theming

## Running the Demo

1. Ensure you have Android Studio installed
2. Open the project in Android Studio
3. Sync Gradle dependencies
4. Run the app on an emulator or physical device
5. Use the demo selector buttons to switch between different JSON configurations
6. Interact with components and observe logs in Logcat (filter by "FlexUIDemo")

## Testing Different Configurations

To test with your own JSON configurations:

1. Create a new JSON file in `app/src/main/assets/`
2. Follow the FlexUI JSON schema (see FlexUI documentation)
3. Add a new button in `DemoSelector` composable
4. Add a case in `DemoContent` to load your JSON file

## Event Logging

All component events are logged with the tag `FlexUIDemo`. To view logs:

```bash
adb logcat -s FlexUIDemo
```

Or filter by "FlexUIDemo" in Android Studio's Logcat window.

## Requirements Covered

This demo implementation satisfies the following requirements from the spec:

- **Requirement 1.1**: Library integration without complex configuration
- **Requirement 1.5**: Integration with existing Compose applications
- **Requirement 18.1**: Callback interface for component interaction events
- **Requirement 18.2**: Event invocation with component identifier and event data
- **Requirement 18.3**: Event type passing (click, change, select)
- **Requirement 18.4**: Component value passing in callbacks

## Notes

- The slider demo requires internet connection to load images from Picsum
- All events are logged to demonstrate the event handling system
- The demo uses Material3 theming for consistent appearance
- Each JSON configuration is self-contained and can be modified independently
