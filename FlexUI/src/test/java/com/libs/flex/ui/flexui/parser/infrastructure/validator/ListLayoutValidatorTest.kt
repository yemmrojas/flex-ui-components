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
 * Unit tests for ListLayoutValidator.
 *
 * Tests follow the Given-When-Then (BDD) pattern and use provider functions
 * instead of setup methods for better test isolation and readability.
 */
class ListLayoutValidatorTest {

    @After
    fun tearDown() {
        unmockkAll()
        clearAllMocks()
    }

    // ========== canValidate Tests ==========

    @Test
    fun `canValidate should return true when descriptor is CONTENT_LIST layout`() {
        // Given
        val validator = provideListLayoutValidator()
        val descriptor = provideListLayoutDescriptor()

        // When
        val result = validator.canValidate(descriptor)

        // Then
        assertTrue(result)
    }

    @Test
    fun `canValidate should return false when descriptor is not CONTENT_LIST`() {
        // Given
        val validator = provideListLayoutValidator()
        val descriptor = provideLayoutDescriptor(type = ComponentType.CONTENT_VERTICAL)

        // When
        val result = validator.canValidate(descriptor)

        // Then
        assertFalse(result)
    }

    // ========== validate Tests ==========

    @Test
    fun `validate should return empty list when list has items`() {
        // Given
        val validator = provideListLayoutValidator()
        val descriptor = provideListLayoutDescriptor(
            items = listOf(JsonPrimitive("item1"), JsonPrimitive("item2"))
        )

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate should return error when items is null`() {
        // Given
        val validator = provideListLayoutValidator()
        val descriptor = provideListLayoutDescriptor(items = null)

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("requires 'items' property"))
        assertTrue(errors[0].contains("test_list"))
    }

    @Test
    fun `validate should return error when items is empty`() {
        // Given
        val validator = provideListLayoutValidator()
        val descriptor = provideListLayoutDescriptor(items = emptyList())

        // When
        val errors = validator.validate(descriptor)

        // Then
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("empty 'items' array"))
        assertTrue(errors[0].contains("test_list"))
    }

    // ========== Provider Functions ==========

    private fun provideListLayoutValidator() = ListLayoutValidator()

    private fun provideListLayoutDescriptor(
        id: String = "test_list",
        items: List<kotlinx.serialization.json.JsonElement>? = listOf(JsonPrimitive("item1"))
    ) = LayoutDescriptor(
        id = id,
        type = ComponentType.CONTENT_LIST,
        items = items
    )

    private fun provideLayoutDescriptor(
        id: String = "test_layout",
        type: ComponentType = ComponentType.CONTENT_VERTICAL
    ) = LayoutDescriptor(
        id = id,
        type = type
    )
}
