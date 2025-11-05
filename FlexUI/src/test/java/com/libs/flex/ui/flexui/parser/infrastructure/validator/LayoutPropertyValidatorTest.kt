package com.libs.flex.ui.flexui.parser.infrastructure.validator

import com.libs.flex.ui.flexui.model.ComponentType
import com.libs.flex.ui.flexui.model.LayoutDescriptor
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for LayoutPropertyValidator.
 *
 * Tests follow the Given-When-Then (BDD) pattern and use provider functions
 * instead of setup methods for better test isolation and readability.
 */
class LayoutPropertyValidatorTest {

    @After
    fun tearDown() {
        unmockkAll()
        clearAllMocks()
    }

    // ========== canValidate Tests ==========

    @Test
    fun `canValidate should return true when descriptor is LayoutDescriptor`() {
        // Given
        val validator = provideLayoutPropertyValidator()
        val descriptor = provideLayoutDescriptor()

        // When
        val result = validator.canValidate(descriptor)

        // Then
        assertTrue(result)
    }

    // ========== arrangement Tests ==========

    @Test
    fun `validate should return empty list when arrangement is valid`() {
        // Given
        val validator = provideLayoutPropertyValidator()
        val descriptor = provideLayoutDescriptor(arrangement = "center")

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate should return error when arrangement is invalid`() {
        // Given
        val validator = provideLayoutPropertyValidator()
        val descriptor = provideLayoutDescriptor(arrangement = "invalid_arrangement")

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("invalid arrangement value"))
        assertTrue(errors[0].contains("invalid_arrangement"))
    }

    // ========== alignment Tests ==========

    @Test
    fun `validate should return empty list when alignment is valid`() {
        // Given
        val validator = provideLayoutPropertyValidator()
        val descriptor = provideLayoutDescriptor(alignment = "start")

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate should return error when alignment is invalid`() {
        // Given
        val validator = provideLayoutPropertyValidator()
        val descriptor = provideLayoutDescriptor(alignment = "invalid_alignment")

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("invalid alignment value"))
    }

    // ========== scrollDirection Tests ==========

    @Test
    fun `validate should return empty list when scrollDirection is valid`() {
        // Given
        val validator = provideLayoutPropertyValidator()
        val descriptor = provideLayoutDescriptor(scrollDirection = "vertical")

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate should return error when scrollDirection is invalid`() {
        // Given
        val validator = provideLayoutPropertyValidator()
        val descriptor = provideLayoutDescriptor(scrollDirection = "diagonal")

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("invalid scrollDirection value"))
        assertTrue(errors[0].contains("diagonal"))
    }

    // ========== fabPosition Tests ==========

    @Test
    fun `validate should return empty list when fabPosition is valid`() {
        // Given
        val validator = provideLayoutPropertyValidator()
        val descriptor = provideLayoutDescriptor(fabPosition = "end")

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate should return error when fabPosition is invalid`() {
        // Given
        val validator = provideLayoutPropertyValidator()
        val descriptor = provideLayoutDescriptor(fabPosition = "top_left")

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("invalid fabPosition value"))
    }

    // ========== Multiple Errors Tests ==========

    @Test
    fun `validate should accumulate multiple errors`() {
        // Given
        val validator = provideLayoutPropertyValidator()
        val descriptor = provideLayoutDescriptor(
            arrangement = "invalid_arr",
            alignment = "invalid_align",
            scrollDirection = "invalid_dir"
        )

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(3, errors.size)
        assertTrue(errors.any { it.contains("arrangement") })
        assertTrue(errors.any { it.contains("alignment") })
        assertTrue(errors.any { it.contains("scrollDirection") })
    }

    @Test
    fun `validate should return empty list when no optional properties are set`() {
        // Given
        val validator = provideLayoutPropertyValidator()
        val descriptor = provideLayoutDescriptor()

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertTrue(errors.isEmpty())
    }

    // ========== Provider Functions ==========

    private fun provideLayoutPropertyValidator() = LayoutPropertyValidator()

    private fun provideLayoutDescriptor(
        id: String = "test_layout",
        type: ComponentType = ComponentType.CONTENT_VERTICAL,
        arrangement: String? = null,
        alignment: String? = null,
        scrollDirection: String? = null,
        fabPosition: String? = null
    ) = LayoutDescriptor(
        id = id,
        type = type,
        arrangement = arrangement,
        alignment = alignment,
        scrollDirection = scrollDirection,
        fabPosition = fabPosition
    )
}
