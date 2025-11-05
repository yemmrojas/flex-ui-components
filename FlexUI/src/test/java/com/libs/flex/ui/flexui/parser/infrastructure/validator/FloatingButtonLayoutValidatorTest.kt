package com.libs.flex.ui.flexui.parser.infrastructure.validator

import com.libs.flex.ui.flexui.model.ComponentType
import com.libs.flex.ui.flexui.model.LayoutDescriptor
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for FloatingButtonLayoutValidator.
 *
 * Tests follow the Given-When-Then (BDD) pattern and use provider functions
 * instead of setup methods for better test isolation and readability.
 */
class FloatingButtonLayoutValidatorTest {

    @After
    fun tearDown() {
        unmockkAll()
        clearAllMocks()
    }

    // ========== canValidate Tests ==========

    @Test
    fun `canValidate should return true when descriptor is CONTENT_WITH_FLOATING_BUTTON`() {
        // Given
        val validator = provideFloatingButtonLayoutValidator()
        val descriptor = provideFloatingButtonLayoutDescriptor()

        // When
        val result = validator.canValidate(descriptor)

        // Then
        assertTrue(result)
    }

    @Test
    fun `canValidate should return false when descriptor is not CONTENT_WITH_FLOATING_BUTTON`() {
        // Given
        val validator = provideFloatingButtonLayoutValidator()
        val descriptor = provideLayoutDescriptor(type = ComponentType.CONTENT_VERTICAL)

        // When
        val result = validator.canValidate(descriptor)

        // Then
        assertFalse(result)
    }

    // ========== validate Tests ==========

    @Test
    fun `validate should return empty list when fabIcon is present`() {
        // Given
        val validator = provideFloatingButtonLayoutValidator()
        val descriptor = provideFloatingButtonLayoutDescriptor(fabIcon = "icon_add")

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate should return error when fabIcon is null`() {
        // Given
        val validator = provideFloatingButtonLayoutValidator()
        val descriptor = provideFloatingButtonLayoutDescriptor(fabIcon = null)

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("requires 'fabIcon' property"))
        assertTrue(errors[0].contains("contentWithFloatingButton"))
        assertTrue(errors[0].contains("test_fab"))
    }

    // ========== Provider Functions ==========

    private fun provideFloatingButtonLayoutValidator() = FloatingButtonLayoutValidator()

    private fun provideFloatingButtonLayoutDescriptor(
        id: String = "test_fab",
        fabIcon: String? = "icon_default"
    ) = LayoutDescriptor(
        id = id,
        type = ComponentType.CONTENT_WITH_FLOATING_BUTTON,
        fabIcon = fabIcon
    )

    private fun provideLayoutDescriptor(
        id: String = "test_layout",
        type: ComponentType = ComponentType.CONTENT_VERTICAL
    ) = LayoutDescriptor(
        id = id,
        type = type
    )
}
