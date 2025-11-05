package com.libs.flex.ui.flexui.parser.infrastructure.validator

import com.libs.flex.ui.flexui.model.AtomicDescriptor
import com.libs.flex.ui.flexui.model.ComponentType
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for SliderCheckValidator.
 *
 * Tests follow the Given-When-Then (BDD) pattern and use provider functions
 * instead of setup methods for better test isolation and readability.
 */
class SliderCheckValidatorTest {

    @After
    fun tearDown() {
        unmockkAll()
        clearAllMocks()
    }

    // ========== canValidate Tests ==========

    @Test
    fun `canValidate should return true when descriptor is COMPONENT_SLIDER_CHECK`() {
        // Given
        val validator = provideSliderCheckValidator()
        val descriptor = provideSliderCheckDescriptor()

        // When
        val result = validator.canValidate(descriptor)

        // Then
        assertTrue(result)
    }

    @Test
    fun `canValidate should return false when descriptor is not COMPONENT_SLIDER_CHECK`() {
        // Given
        val validator = provideSliderCheckValidator()
        val descriptor = provideAtomicDescriptor(type = ComponentType.COMPONENT_BUTTON)

        // When
        val result = validator.canValidate(descriptor)

        // Then
        assertFalse(result)
    }

    // ========== validate Tests ==========

    @Test
    fun `validate should return empty list when slider has valid min and max`() {
        // Given
        val validator = provideSliderCheckValidator()
        val descriptor = provideSliderCheckDescriptor(min = 0f, max = 100f)

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate should return error when min is null`() {
        // Given
        val validator = provideSliderCheckValidator()
        val descriptor = provideSliderCheckDescriptor(min = null, max = 100f)

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("requires 'min' property"))
        assertTrue(errors[0].contains("componentSliderCheck"))
    }

    @Test
    fun `validate should return error when max is null`() {
        // Given
        val validator = provideSliderCheckValidator()
        val descriptor = provideSliderCheckDescriptor(min = 0f, max = null)

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("requires 'max' property"))
    }

    @Test
    fun `validate should return two errors when both min and max are null`() {
        // Given
        val validator = provideSliderCheckValidator()
        val descriptor = provideSliderCheckDescriptor(min = null, max = null)

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(2, errors.size)
        assertTrue(errors.any { it.contains("min") })
        assertTrue(errors.any { it.contains("max") })
    }

    @Test
    fun `validate should return error when min is greater than max`() {
        // Given
        val validator = provideSliderCheckValidator()
        val descriptor = provideSliderCheckDescriptor(min = 100f, max = 50f)

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("invalid range"))
        assertTrue(errors[0].contains("min (100.0)"))
        assertTrue(errors[0].contains("max (50.0)"))
    }

    @Test
    fun `validate should return error when min equals max`() {
        // Given
        val validator = provideSliderCheckValidator()
        val descriptor = provideSliderCheckDescriptor(min = 50f, max = 50f)

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("invalid range"))
    }

    // ========== Provider Functions ==========

    private fun provideSliderCheckValidator() = SliderCheckValidator()

    private fun provideSliderCheckDescriptor(
        id: String = "test_slider",
        min: Float? = 0f,
        max: Float? = 100f
    ) = AtomicDescriptor(
        id = id,
        type = ComponentType.COMPONENT_SLIDER_CHECK,
        min = min,
        max = max
    )

    private fun provideAtomicDescriptor(
        id: String = "test_atomic",
        type: ComponentType = ComponentType.COMPONENT_INPUT
    ) = AtomicDescriptor(
        id = id,
        type = type
    )
}
