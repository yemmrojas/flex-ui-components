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
 * Unit tests for TextViewValidator.
 *
 * Tests follow the Given-When-Then (BDD) pattern and use provider functions
 * instead of setup methods for better test isolation and readability.
 */
class TextViewValidatorTest {

    @After
    fun tearDown() {
        unmockkAll()
        clearAllMocks()
    }

    // ========== canValidate Tests ==========

    @Test
    fun `canValidate should return true when descriptor is COMPONENT_TEXT_VIEW`() {
        // Given
        val validator = provideTextViewValidator()
        val descriptor = provideTextViewDescriptor()

        // When
        val result = validator.canValidate(descriptor)

        // Then
        assertTrue(result)
    }

    @Test
    fun `canValidate should return false when descriptor is not COMPONENT_TEXT_VIEW`() {
        // Given
        val validator = provideTextViewValidator()
        val descriptor = provideAtomicDescriptor(type = ComponentType.COMPONENT_BUTTON)

        // When
        val result = validator.canValidate(descriptor)

        // Then
        assertFalse(result)
    }

    // ========== validate Tests ==========

    @Test
    fun `validate should return empty list when text view has text`() {
        // Given
        val validator = provideTextViewValidator()
        val descriptor = provideTextViewDescriptor(text = "Hello World")

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate should return error when text view has no text`() {
        // Given
        val validator = provideTextViewValidator()
        val descriptor = provideTextViewDescriptor(text = null)

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("requires 'text' property"))
        assertTrue(errors[0].contains("componentTextView"))
        assertTrue(errors[0].contains("test_textview"))
    }

    // ========== Provider Functions ==========

    private fun provideTextViewValidator() = TextViewValidator()

    private fun provideTextViewDescriptor(
        id: String = "test_textview",
        text: String? = "Default Text"
    ) = AtomicDescriptor(
        id = id,
        type = ComponentType.COMPONENT_TEXT_VIEW,
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
