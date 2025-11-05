package com.libs.flex.ui.flexui.parser.infrastructure.validator

import com.libs.flex.ui.flexui.model.ComponentType
import com.libs.flex.ui.flexui.model.LayoutDescriptor
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import kotlinx.serialization.json.JsonPrimitive
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for SliderLayoutValidator.
 *
 * Tests follow the Given-When-Then (BDD) pattern and use provider functions
 * instead of setup methods for better test isolation and readability.
 */
class SliderLayoutValidatorTest {

    @After
    fun tearDown() {
        unmockkAll()
        clearAllMocks()
    }

    // ========== canValidate Tests ==========

    @Test
    fun `canValidate should return true when descriptor is CONTENT_SLIDER layout`() {
        // Given
        val validator = provideSliderLayoutValidator()
        val descriptor = provideSliderLayoutDescriptor()

        // When
        val result = validator.canValidate(descriptor)

        // Then
        assertTrue(result)
    }

    @Test
    fun `canValidate should return false when descriptor is not CONTENT_SLIDER`() {
        // Given
        val validator = provideSliderLayoutValidator()
        val descriptor = provideLayoutDescriptor(type = ComponentType.CONTENT_VERTICAL)

        // When
        val result = validator.canValidate(descriptor)

        // Then
        assertFalse(result)
    }

    // ========== validate Tests ==========

    @Test
    fun `validate should return empty list when slider has items and no autoPlay`() {
        // Given
        val validator = provideSliderLayoutValidator()
        val descriptor = provideSliderLayoutDescriptor(
            items = listOf(JsonPrimitive("image1.jpg"), JsonPrimitive("image2.jpg")),
            autoPlay = false
        )

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate should return empty list when slider has items and autoPlay with interval`() {
        // Given
        val validator = provideSliderLayoutValidator()
        val descriptor = provideSliderLayoutDescriptor(
            items = listOf(JsonPrimitive("image1.jpg")),
            autoPlay = true,
            autoPlayInterval = 3000L
        )

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate should return error when items is null`() {
        // Given
        val validator = provideSliderLayoutValidator()
        val descriptor = provideSliderLayoutDescriptor(items = null)

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("requires 'items' property"))
    }

    @Test
    fun `validate should return error when items is empty`() {
        // Given
        val validator = provideSliderLayoutValidator()
        val descriptor = provideSliderLayoutDescriptor(items = emptyList())

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("empty 'items' array"))
    }

    @Test
    fun `validate should return error when autoPlay is true but no interval`() {
        // Given
        val validator = provideSliderLayoutValidator()
        val descriptor = provideSliderLayoutDescriptor(
            items = listOf(JsonPrimitive("image1.jpg")),
            autoPlay = true,
            autoPlayInterval = null
        )

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("autoPlay=true"))
        assertTrue(errors[0].contains("autoPlayInterval"))
    }

    @Test
    fun `validate should return multiple errors when items is null and autoPlay without interval`() {
        // Given
        val validator = provideSliderLayoutValidator()
        val descriptor = provideSliderLayoutDescriptor(
            items = null,
            autoPlay = true,
            autoPlayInterval = null
        )

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(2, errors.size)
        assertTrue(errors.any { it.contains("items") })
        assertTrue(errors.any { it.contains("autoPlayInterval") })
    }

    // ========== Provider Functions ==========

    private fun provideSliderLayoutValidator() = SliderLayoutValidator()

    private fun provideSliderLayoutDescriptor(
        id: String = "test_slider",
        items: List<kotlinx.serialization.json.JsonElement>? = listOf(JsonPrimitive("image1.jpg")),
        autoPlay: Boolean? = null,
        autoPlayInterval: Long? = null
    ) = LayoutDescriptor(
        id = id,
        type = ComponentType.CONTENT_SLIDER,
        items = items,
        autoPlay = autoPlay,
        autoPlayInterval = autoPlayInterval
    )

    private fun provideLayoutDescriptor(
        id: String = "test_layout",
        type: ComponentType = ComponentType.CONTENT_VERTICAL
    ) = LayoutDescriptor(
        id = id,
        type = type
    )
}
