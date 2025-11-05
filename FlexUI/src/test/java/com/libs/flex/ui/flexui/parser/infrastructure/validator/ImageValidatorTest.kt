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
 * Unit tests for ImageValidator.
 *
 * Tests follow the Given-When-Then (BDD) pattern and use provider functions
 * instead of setup methods for better test isolation and readability.
 */
class ImageValidatorTest {

    @After
    fun tearDown() {
        unmockkAll()
        clearAllMocks()
    }

    // ========== canValidate Tests ==========

    @Test
    fun `canValidate should return true when descriptor is COMPONENT_IMAGE`() {
        // Given
        val validator = provideImageValidator()
        val descriptor = provideImageDescriptor()

        // When
        val result = validator.canValidate(descriptor)

        // Then
        assertTrue(result)
    }

    @Test
    fun `canValidate should return false when descriptor is not COMPONENT_IMAGE`() {
        // Given
        val validator = provideImageValidator()
        val descriptor = provideAtomicDescriptor(type = ComponentType.COMPONENT_BUTTON)

        // When
        val result = validator.canValidate(descriptor)

        // Then
        assertFalse(result)
    }

    // ========== validate Tests ==========

    @Test
    fun `validate should return empty list when image has imageUrl`() {
        // Given
        val validator = provideImageValidator()
        val descriptor = provideImageDescriptor(imageUrl = "https://example.com/image.jpg")

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate should return error when image has no imageUrl`() {
        // Given
        val validator = provideImageValidator()
        val descriptor = provideImageDescriptor(imageUrl = null)

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("requires 'imageUrl' property"))
        assertTrue(errors[0].contains("componentImage"))
        assertTrue(errors[0].contains("test_image"))
    }

    // ========== Provider Functions ==========

    private fun provideImageValidator() = ImageValidator()

    private fun provideImageDescriptor(
        id: String = "test_image",
        imageUrl: String? = "https://example.com/default.jpg"
    ) = AtomicDescriptor(
        id = id,
        type = ComponentType.COMPONENT_IMAGE,
        imageUrl = imageUrl
    )

    private fun provideAtomicDescriptor(
        id: String = "test_atomic",
        type: ComponentType = ComponentType.COMPONENT_INPUT
    ) = AtomicDescriptor(
        id = id,
        type = type
    )
}
