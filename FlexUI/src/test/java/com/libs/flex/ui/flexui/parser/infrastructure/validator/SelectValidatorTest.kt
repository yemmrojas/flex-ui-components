package com.libs.flex.ui.flexui.parser.infrastructure.validator

import com.libs.flex.ui.flexui.model.AtomicDescriptor
import com.libs.flex.ui.flexui.model.ComponentType
import com.libs.flex.ui.flexui.model.SelectOption
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for SelectValidator.
 *
 * Tests follow the Given-When-Then (BDD) pattern and use provider functions
 * instead of setup methods for better test isolation and readability.
 */
class SelectValidatorTest {

    @After
    fun tearDown() {
        unmockkAll()
        clearAllMocks()
    }

    // ========== canValidate Tests ==========

    @Test
    fun `canValidate should return true when descriptor is COMPONENT_SELECT`() {
        // Given
        val validator = provideSelectValidator()
        val descriptor = provideSelectDescriptor()

        // When
        val result = validator.canValidate(descriptor)

        // Then
        assertTrue(result)
    }

    @Test
    fun `canValidate should return false when descriptor is not COMPONENT_SELECT`() {
        // Given
        val validator = provideSelectValidator()
        val descriptor = provideAtomicDescriptor(type = ComponentType.COMPONENT_BUTTON)

        // When
        val result = validator.canValidate(descriptor)

        // Then
        assertFalse(result)
    }

    // ========== validate Tests ==========

    @Test
    fun `validate should return empty list when select has options`() {
        // Given
        val validator = provideSelectValidator()
        val descriptor = provideSelectDescriptor(
            options = listOf(
                SelectOption("1", "Option 1"),
                SelectOption("2", "Option 2")
            )
        )

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate should return error when select has no options`() {
        // Given
        val validator = provideSelectValidator()
        val descriptor = provideSelectDescriptor(options = null)

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("requires 'options' property"))
        assertTrue(errors[0].contains("componentSelect"))
    }

    @Test
    fun `validate should return error when select has empty options`() {
        // Given
        val validator = provideSelectValidator()
        val descriptor = provideSelectDescriptor(options = emptyList())

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("empty 'options' array"))
    }

    // ========== Provider Functions ==========

    private fun provideSelectValidator() = SelectValidator()

    private fun provideSelectDescriptor(
        id: String = "test_select",
        options: List<SelectOption>? = listOf(SelectOption("1", "Default Option"))
    ) = AtomicDescriptor(
        id = id,
        type = ComponentType.COMPONENT_SELECT,
        options = options
    )

    private fun provideAtomicDescriptor(
        id: String = "test_atomic",
        type: ComponentType = ComponentType.COMPONENT_INPUT
    ) = AtomicDescriptor(
        id = id,
        type = type
    )
}
