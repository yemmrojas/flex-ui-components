package com.libs.flex.ui.flexui.parser.domain.service

import com.libs.flex.ui.flexui.model.AtomicDescriptor
import com.libs.flex.ui.flexui.model.ComponentType
import com.libs.flex.ui.flexui.model.LayoutDescriptor
import com.libs.flex.ui.flexui.model.SelectOption
import com.libs.flex.ui.flexui.parser.domain.ports.ComponentValidatorStrategyPort
import com.libs.flex.ui.flexui.parser.infrastructure.validator.ButtonValidator
import com.libs.flex.ui.flexui.parser.infrastructure.validator.FloatingButtonLayoutValidator
import com.libs.flex.ui.flexui.parser.infrastructure.validator.ImageValidator
import com.libs.flex.ui.flexui.parser.infrastructure.validator.LayoutPropertyValidator
import com.libs.flex.ui.flexui.parser.infrastructure.validator.ListLayoutValidator
import com.libs.flex.ui.flexui.parser.infrastructure.validator.RecursiveChildrenValidator
import com.libs.flex.ui.flexui.parser.infrastructure.validator.SelectValidator
import com.libs.flex.ui.flexui.parser.infrastructure.validator.SliderCheckValidator
import com.libs.flex.ui.flexui.parser.infrastructure.validator.SliderLayoutValidator
import com.libs.flex.ui.flexui.parser.infrastructure.validator.StylePropertyValidator
import com.libs.flex.ui.flexui.parser.infrastructure.validator.TextViewValidator
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import kotlinx.serialization.json.JsonPrimitive
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for ValidationEngine.
 *
 * Tests follow the Given-When-Then (BDD) pattern and use provider functions
 * instead of setup methods for better test isolation and readability.
 *
 * Note: These are integration tests that test the ValidationEngine with real validators.
 * Individual validator unit tests are in the validator package.
 */
class ValidationEngineTest {

    @After
    fun tearDown() {
        unmockkAll()
        clearAllMocks()
    }

    // ========== Layout Validation Tests ==========

    @Test
    fun `validate should return success when layout descriptor has all required properties`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideValidLayoutDescriptor()

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isSuccess())
    }

    @Test
    fun `validate should return failure when contentList has no items`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideLayoutDescriptor(
            type = ComponentType.CONTENT_LIST,
            items = null
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isFailure())
        val errors = result.errorList()
        assertTrue(errors.any { it.contains("contentList") && it.contains("items") })
    }

    @Test
    fun `validate should return failure when contentList has empty items array`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideLayoutDescriptor(
            type = ComponentType.CONTENT_LIST,
            items = emptyList()
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isFailure())
        val errors = result.errorList()
        assertTrue(errors.any { it.contains("contentList") && it.contains("empty") })
    }

    @Test
    fun `validate should return failure when contentSlider has no items`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideLayoutDescriptor(
            type = ComponentType.CONTENT_SLIDER,
            items = null
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isFailure())
        val errors = result.errorList()
        assertTrue(errors.any { it.contains("contentSlider") && it.contains("items") })
    }

    @Test
    fun `validate should return failure when contentSlider has autoPlay true but no interval`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideLayoutDescriptor(
            type = ComponentType.CONTENT_SLIDER,
            items = listOf(JsonPrimitive("image1.jpg")),
            autoPlay = true,
            autoPlayInterval = null
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isFailure())
        val errors = result.errorList()
        assertTrue(errors.any { it.contains("autoPlay") && it.contains("autoPlayInterval") })
    }

    @Test
    fun `validate should return failure when contentWithFloatingButton has no fabIcon`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideLayoutDescriptor(
            type = ComponentType.CONTENT_WITH_FLOATING_BUTTON,
            fabIcon = null
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isFailure())
        val errors = result.errorList()
        assertTrue(errors.any { it.contains("contentWithFloatingButton") && it.contains("fabIcon") })
    }

    @Test
    fun `validate should return failure when arrangement has invalid value`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideLayoutDescriptor(
            type = ComponentType.CONTENT_VERTICAL,
            arrangement = "invalid_arrangement"
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isFailure())
        val errors = result.errorList()
        assertTrue(errors.any { it.contains("arrangement") && it.contains("invalid") })
    }

    @Test
    fun `validate should return failure when alignment has invalid value`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideLayoutDescriptor(
            type = ComponentType.CONTENT_HORIZONTAL,
            alignment = "invalid_alignment"
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isFailure())
        val errors = result.errorList()
        assertTrue(errors.any { it.contains("alignment") && it.contains("invalid") })
    }

    @Test
    fun `validate should return failure when scrollDirection has invalid value`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideLayoutDescriptor(
            type = ComponentType.CONTENT_SCROLL,
            scrollDirection = "diagonal"
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isFailure())
        val errors = result.errorList()
        assertTrue(errors.any { it.contains("scrollDirection") && it.contains("invalid") })
    }

    @Test
    fun `validate should return failure when fabPosition has invalid value`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideLayoutDescriptor(
            type = ComponentType.CONTENT_WITH_FLOATING_BUTTON,
            fabIcon = "icon_add",
            fabPosition = "top_left"
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isFailure())
        val errors = result.errorList()
        assertTrue(errors.any { it.contains("fabPosition") && it.contains("invalid") })
    }

    @Test
    fun `validate should recursively validate children and accumulate errors`() {
        // Given
        val engine = provideValidationEngine(includeRecursive = true)
        val invalidChild = provideAtomicDescriptor(
            id = "child1",
            type = ComponentType.COMPONENT_TEXT_VIEW,
            text = null // Missing required text
        )
        val descriptor = provideLayoutDescriptor(
            type = ComponentType.CONTENT_VERTICAL,
            children = listOf(invalidChild)
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isFailure())
        val errors = result.errorList()
        assertTrue(errors.any { it.contains("child1") && it.contains("text") })
    }

    @Test
    fun `validate should validate itemTemplate when present`() {
        // Given
        val engine = provideValidationEngine(includeRecursive = true)
        val invalidTemplate = provideAtomicDescriptor(
            id = "template",
            type = ComponentType.COMPONENT_IMAGE,
            imageUrl = null // Missing required imageUrl
        )
        val descriptor = provideLayoutDescriptor(
            type = ComponentType.CONTENT_LIST,
            items = listOf(JsonPrimitive("item1")),
            itemTemplate = invalidTemplate
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isFailure())
        val errors = result.errorList()
        assertTrue(errors.any { it.contains("template") && it.contains("imageUrl") })
    }

    // ========== Atomic Component Validation Tests ==========

    @Test
    fun `validate should return success when atomic descriptor has all required properties`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideValidAtomicDescriptor()

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isSuccess())
    }

    @Test
    fun `validate should return failure when componentTextView has no text`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideAtomicDescriptor(
            type = ComponentType.COMPONENT_TEXT_VIEW,
            text = null
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isFailure())
        val errors = result.errorList()
        assertTrue(errors.any { it.contains("componentTextView") && it.contains("text") })
    }

    @Test
    fun `validate should return failure when componentImage has no imageUrl`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideAtomicDescriptor(
            type = ComponentType.COMPONENT_IMAGE,
            imageUrl = null
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isFailure())
        val errors = result.errorList()
        assertTrue(errors.any { it.contains("componentImage") && it.contains("imageUrl") })
    }

    @Test
    fun `validate should return failure when componentSelect has no options`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideAtomicDescriptor(
            type = ComponentType.COMPONENT_SELECT,
            options = null
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isFailure())
        val errors = result.errorList()
        assertTrue(errors.any { it.contains("componentSelect") && it.contains("options") })
    }

    @Test
    fun `validate should return failure when componentSelect has empty options`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideAtomicDescriptor(
            type = ComponentType.COMPONENT_SELECT,
            options = emptyList()
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isFailure())
        val errors = result.errorList()
        assertTrue(errors.any { it.contains("componentSelect") && it.contains("options") })
    }

    @Test
    fun `validate should return failure when componentSliderCheck has no min`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideAtomicDescriptor(
            type = ComponentType.COMPONENT_SLIDER_CHECK,
            min = null,
            max = 100f
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isFailure())
        val errors = result.errorList()
        assertTrue(errors.any { it.contains("componentSliderCheck") && it.contains("min") })
    }

    @Test
    fun `validate should return failure when componentSliderCheck has no max`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideAtomicDescriptor(
            type = ComponentType.COMPONENT_SLIDER_CHECK,
            min = 0f,
            max = null
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isFailure())
        val errors = result.errorList()
        assertTrue(errors.any { it.contains("componentSliderCheck") && it.contains("max") })
    }

    @Test
    fun `validate should return failure when componentSliderCheck has min greater than or equal to max`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideAtomicDescriptor(
            type = ComponentType.COMPONENT_SLIDER_CHECK,
            min = 100f,
            max = 50f
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isFailure())
        val errors = result.errorList()
        assertTrue(errors.any { it.contains("invalid range") })
    }

    @Test
    fun `validate should return failure when componentButton has no text`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideAtomicDescriptor(
            type = ComponentType.COMPONENT_BUTTON,
            text = null
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isFailure())
        val errors = result.errorList()
        assertTrue(errors.any { it.contains("componentButton") && it.contains("text") })
    }

    @Test
    fun `validate should return failure when textStyle has invalid value`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideAtomicDescriptor(
            type = ComponentType.COMPONENT_TEXT_VIEW,
            text = "Sample",
            textStyle = "invalid_style"
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isFailure())
        val errors = result.errorList()
        assertTrue(errors.any { it.contains("textStyle") && it.contains("invalid") })
    }

    @Test
    fun `validate should return failure when inputStyle has invalid value`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideAtomicDescriptor(
            type = ComponentType.COMPONENT_INPUT,
            inputStyle = "invalid_style"
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isFailure())
        val errors = result.errorList()
        assertTrue(errors.any { it.contains("inputStyle") && it.contains("invalid") })
    }

    @Test
    fun `validate should return failure when buttonStyle has invalid value`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideAtomicDescriptor(
            type = ComponentType.COMPONENT_BUTTON,
            text = "Click",
            buttonStyle = "invalid_style"
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isFailure())
        val errors = result.errorList()
        assertTrue(errors.any { it.contains("buttonStyle") && it.contains("invalid") })
    }

    @Test
    fun `validate should return failure when contentScale has invalid value`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideAtomicDescriptor(
            type = ComponentType.COMPONENT_IMAGE,
            imageUrl = "https://example.com/image.jpg",
            contentScale = "invalid_scale"
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isFailure())
        val errors = result.errorList()
        assertTrue(errors.any { it.contains("contentScale") && it.contains("invalid") })
    }

    @Test
    fun `validate should return failure when loaderStyle has invalid value`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideAtomicDescriptor(
            type = ComponentType.COMPONENT_LOADER,
            loaderStyle = "invalid_style"
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isFailure())
        val errors = result.errorList()
        assertTrue(errors.any { it.contains("loaderStyle") && it.contains("invalid") })
    }

    @Test
    fun `validate should return failure when fontSize is zero or negative`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideAtomicDescriptor(
            type = ComponentType.COMPONENT_TEXT_VIEW,
            text = "Sample",
            fontSize = -10
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isFailure())
        val errors = result.errorList()
        assertTrue(errors.any { it.contains("fontSize") && it.contains("greater than 0") })
    }

    @Test
    fun `validate should return failure when maxLines is zero or negative`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideAtomicDescriptor(
            type = ComponentType.COMPONENT_TEXT_VIEW,
            text = "Sample",
            maxLines = 0
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isFailure())
        val errors = result.errorList()
        assertTrue(errors.any { it.contains("maxLines") && it.contains("greater than 0") })
    }

    @Test
    fun `validate should return failure when size is zero or negative`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideAtomicDescriptor(
            type = ComponentType.COMPONENT_LOADER,
            size = -5
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isFailure())
        val errors = result.errorList()
        assertTrue(errors.any { it.contains("size") && it.contains("greater than 0") })
    }

    @Test
    fun `validate should return failure when color has invalid hex format`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideAtomicDescriptor(
            type = ComponentType.COMPONENT_TEXT_VIEW,
            text = "Sample",
            color = "invalid_color"
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isFailure())
        val errors = result.errorList()
        assertTrue(errors.any { it.contains("color") && it.contains("hex format") })
    }

    @Test
    fun `validate should return success when color has valid 6-digit hex format`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideAtomicDescriptor(
            type = ComponentType.COMPONENT_TEXT_VIEW,
            text = "Sample",
            color = "#FF5733"
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isSuccess())
    }

    @Test
    fun `validate should return success when color has valid 8-digit hex format with alpha`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideAtomicDescriptor(
            type = ComponentType.COMPONENT_TEXT_VIEW,
            text = "Sample",
            color = "#80FF5733"
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isSuccess())
    }

    @Test
    fun `validate should accumulate multiple errors from single component`() {
        // Given
        val engine = provideValidationEngine()
        val descriptor = provideAtomicDescriptor(
            type = ComponentType.COMPONENT_SLIDER_CHECK,
            min = null,
            max = null
        )

        // When
        val result = engine.validate(descriptor)

        // Then
        assertTrue(result.isFailure())
        val errors = result.errorList()
        assertEquals(2, errors.size)
        assertTrue(errors.any { it.contains("min") })
        assertTrue(errors.any { it.contains("max") })
    }

    // ========== Provider Functions ==========

    /**
     * Provides a ValidationEngine with all real validators.
     * This creates the complete validation pipeline as it would be in production.
     *
     * @param includeRecursive If true, includes RecursiveChildrenValidator for testing recursive validation
     */
    private fun provideValidationEngine(includeRecursive: Boolean = false): ValidationEngine {
        val validators = provideAllValidators(includeRecursive)
        return ValidationEngine(validators)
    }

    /**
     * Provides all validator strategies.
     * This mirrors the production configuration from ParserModule.
     *
     * @param includeRecursive If true, includes RecursiveChildrenValidator
     */
    private fun provideAllValidators(includeRecursive: Boolean = false): List<ComponentValidatorStrategyPort> {
        val baseValidators = listOf(
            // Layout-specific validators
            ListLayoutValidator(),
            SliderLayoutValidator(),
            FloatingButtonLayoutValidator(),

            // Atomic component validators
            TextViewValidator(),
            ButtonValidator(),
            ImageValidator(),
            SelectValidator(),
            SliderCheckValidator(),

            // Property validators
            LayoutPropertyValidator(),
            StylePropertyValidator()
        )

        return if (includeRecursive) {
            // For recursive tests, we need to create a ValidationEngine and pass it to RecursiveChildrenValidator
            // This creates a circular dependency, so we create the engine first without recursive validator,
            // then add the recursive validator
            val engineWithoutRecursive = ValidationEngine(baseValidators)
            baseValidators + RecursiveChildrenValidator(engineWithoutRecursive)
        } else {
            baseValidators
        }
    }

    private fun provideValidLayoutDescriptor() = LayoutDescriptor(
        id = "test_layout",
        type = ComponentType.CONTENT_VERTICAL,
        children = emptyList()
    )

    private fun provideLayoutDescriptor(
        id: String = "test_layout",
        type: ComponentType = ComponentType.CONTENT_VERTICAL,
        children: List<com.libs.flex.ui.flexui.model.ComponentDescriptor> = emptyList(),
        arrangement: String? = null,
        alignment: String? = null,
        scrollDirection: String? = null,
        fabIcon: String? = null,
        fabPosition: String? = null,
        items: List<kotlinx.serialization.json.JsonElement>? = null,
        itemTemplate: com.libs.flex.ui.flexui.model.ComponentDescriptor? = null,
        autoPlay: Boolean? = null,
        autoPlayInterval: Long? = null
    ) = LayoutDescriptor(
        id = id,
        type = type,
        children = children,
        arrangement = arrangement,
        alignment = alignment,
        scrollDirection = scrollDirection,
        fabIcon = fabIcon,
        fabPosition = fabPosition,
        items = items,
        itemTemplate = itemTemplate,
        autoPlay = autoPlay,
        autoPlayInterval = autoPlayInterval
    )

    private fun provideValidAtomicDescriptor() = AtomicDescriptor(
        id = "test_atomic",
        type = ComponentType.COMPONENT_INPUT
    )

    private fun provideAtomicDescriptor(
        id: String = "test_atomic",
        type: ComponentType = ComponentType.COMPONENT_INPUT,
        text: String? = null,
        imageUrl: String? = null,
        options: List<SelectOption>? = null,
        min: Float? = null,
        max: Float? = null,
        textStyle: String? = null,
        inputStyle: String? = null,
        buttonStyle: String? = null,
        contentScale: String? = null,
        loaderStyle: String? = null,
        fontSize: Int? = null,
        maxLines: Int? = null,
        size: Int? = null,
        color: String? = null
    ) = AtomicDescriptor(
        id = id,
        type = type,
        text = text,
        imageUrl = imageUrl,
        options = options,
        min = min,
        max = max,
        textStyle = textStyle,
        inputStyle = inputStyle,
        buttonStyle = buttonStyle,
        contentScale = contentScale,
        loaderStyle = loaderStyle,
        fontSize = fontSize,
        maxLines = maxLines,
        size = size,
        color = color
    )
}
