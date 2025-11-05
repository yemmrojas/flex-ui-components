package com.libs.flex.ui.flexui.parser.infrastructure.validator

import com.libs.flex.ui.flexui.model.AtomicDescriptor
import com.libs.flex.ui.flexui.model.ComponentType
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for StylePropertyValidator.
 *
 * Tests follow the Given-When-Then (BDD) pattern and use provider functions
 * instead of setup methods for better test isolation and readability.
 */
class StylePropertyValidatorTest {

    @After
    fun tearDown() {
        unmockkAll()
        clearAllMocks()
    }

    // ========== canValidate Tests ==========

    @Test
    fun `canValidate should return true when descriptor is AtomicDescriptor`() {
        // Given
        val validator = provideStylePropertyValidator()
        val descriptor = provideAtomicDescriptor()

        // When
        val result = validator.canValidate(descriptor)

        // Then
        assertTrue(result)
    }

    // ========== textStyle Tests ==========

    @Test
    fun `validate should return empty list when textStyle is valid`() {
        // Given
        val validator = provideStylePropertyValidator()
        val descriptor = provideAtomicDescriptor(textStyle = "bold")

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate should return error when textStyle is invalid`() {
        // Given
        val validator = provideStylePropertyValidator()
        val descriptor = provideAtomicDescriptor(textStyle = "invalid_style")

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("invalid textStyle value"))
        assertTrue(errors[0].contains("invalid_style"))
    }

    // ========== buttonStyle Tests ==========

    @Test
    fun `validate should return empty list when buttonStyle is valid`() {
        // Given
        val validator = provideStylePropertyValidator()
        val descriptor = provideAtomicDescriptor(buttonStyle = "primary")

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate should return error when buttonStyle is invalid`() {
        // Given
        val validator = provideStylePropertyValidator()
        val descriptor = provideAtomicDescriptor(buttonStyle = "super_fancy")

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("invalid buttonStyle value"))
    }

    // ========== contentScale Tests ==========

    @Test
    fun `validate should return empty list when contentScale is valid`() {
        // Given
        val validator = provideStylePropertyValidator()
        val descriptor = provideAtomicDescriptor(contentScale = "crop")

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate should return error when contentScale is invalid`() {
        // Given
        val validator = provideStylePropertyValidator()
        val descriptor = provideAtomicDescriptor(contentScale = "stretch")

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("invalid contentScale value"))
    }

    // ========== fontSize Tests ==========

    @Test
    fun `validate should return empty list when fontSize is positive`() {
        // Given
        val validator = provideStylePropertyValidator()
        val descriptor = provideAtomicDescriptor(fontSize = 16)

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate should return error when fontSize is zero`() {
        // Given
        val validator = provideStylePropertyValidator()
        val descriptor = provideAtomicDescriptor(fontSize = 0)

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("invalid fontSize value"))
        assertTrue(errors[0].contains("greater than 0"))
    }

    @Test
    fun `validate should return error when fontSize is negative`() {
        // Given
        val validator = provideStylePropertyValidator()
        val descriptor = provideAtomicDescriptor(fontSize = -10)

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("fontSize"))
    }

    // ========== color Tests ==========

    @Test
    fun `validate should return empty list when color is valid 6-digit hex`() {
        // Given
        val validator = provideStylePropertyValidator()
        val descriptor = provideAtomicDescriptor(color = "#FF5733")

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate should return empty list when color is valid 8-digit hex with alpha`() {
        // Given
        val validator = provideStylePropertyValidator()
        val descriptor = provideAtomicDescriptor(color = "#80FF5733")

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate should return error when color is invalid format`() {
        // Given
        val validator = provideStylePropertyValidator()
        val descriptor = provideAtomicDescriptor(color = "red")

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("invalid color format"))
        assertTrue(errors[0].contains("hex format"))
    }

    @Test
    fun `validate should return error when color is missing hash`() {
        // Given
        val validator = provideStylePropertyValidator()
        val descriptor = provideAtomicDescriptor(color = "FF5733")

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("color"))
    }

    // ========== Multiple Errors Tests ==========

    @Test
    fun `validate should accumulate multiple errors`() {
        // Given
        val validator = provideStylePropertyValidator()
        val descriptor = provideAtomicDescriptor(
            textStyle = "invalid",
            fontSize = -5,
            color = "not_a_color"
        )

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(3, errors.size)
        assertTrue(errors.any { it.contains("textStyle") })
        assertTrue(errors.any { it.contains("fontSize") })
        assertTrue(errors.any { it.contains("color") })
    }

    // ========== Provider Functions ==========

    private fun provideStylePropertyValidator() = StylePropertyValidator()

    private fun provideAtomicDescriptor(
        id: String = "test_atomic",
        type: ComponentType = ComponentType.COMPONENT_TEXT_VIEW,
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
