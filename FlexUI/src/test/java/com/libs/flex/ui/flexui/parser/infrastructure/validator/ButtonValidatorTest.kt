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
 * Unit tests for ButtonValidator.
 *
 * Tests follow the Given-When-Then (BDD) pattern and use provider functions
 * instead of setup methods for better test isolation and readability.
 */
class ButtonValidatorTest {

    @After
    fun tearDown() {
        unmockkAll()
        clearAllMocks()
    }

    // ========== canValidate Tests ==========

    @Test
    fun `canValidate should return true when descriptor is COMPONENT_BUTTON`() {
        // Given
        val validator = provideButtonValidator()
        val descriptor = provideButtonDescriptor()

        // When
        val result = validator.canValidate(descriptor)

        // Then
        assertTrue(result)
    }

    @Test
    fun `canValidate should return false when descriptor is not COMPONENT_BUTTON`() {
        // Given
        val validator = provideButtonValidator()
        val descriptor = provideAtomicDescriptor(type = ComponentType.COMPONENT_TEXT_VIEW)

        // When
        val result = validator.canValidate(descriptor)

        // Then
        assertFalse(result)
    }

    // ========== validate Tests ==========

    @Test
    fun `validate should return empty list when button has text`() {
        // Given
        val validator = provideButtonValidator()
        val descriptor = provideButtonDescriptor(text = "Click Me")

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate should return error when button has no text`() {
        // Given
        val validator = provideButtonValidator()
        val descriptor = provideButtonDescriptor(text = null)

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("requires 'text' property"))
        assertTrue(errors[0].contains("componentButton"))
        assertTrue(errors[0].contains("test_button"))
    }

    // ========== Provider Functions ==========

    private fun provideButtonValidator() = ButtonValidator()

    private fun provideButtonDescriptor(
        id: String = "test_button",
        text: String? = "Default Text"
    ) = AtomicDescriptor(
        id = id,
        type = ComponentType.COMPONENT_BUTTON,
        text = text
    )

    private fun provideAtomicDescriptor(
        id: String = "test_atomic",
        type: ComponentType = ComponentType.COMPONENT_INPUT
    ) = AtomicDescriptor(
        id = id,
        type = type
    )
}
