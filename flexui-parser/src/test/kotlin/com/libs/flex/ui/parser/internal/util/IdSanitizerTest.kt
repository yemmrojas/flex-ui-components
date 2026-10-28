package com.libs.flex.ui.parser.internal.util

import com.libs.flex.ui.core.domain.value.ComponentId
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdSanitizerTest {

    @Test
    fun `sanitize preserves valid identifier`() {
        val validId = "valid-id_123"
        val result = IdSanitizer.sanitize(validId)

        assertEquals("valid-id_123", result)
        assertTrue(ComponentId.of(result).isRight)
    }

    @Test
    fun `sanitize replaces invalid characters like dots and spaces with underscore`() {
        val raw = "login.button submit 1"
        val result = IdSanitizer.sanitize(raw)

        assertEquals("login_button_submit_1", result)
        assertTrue(ComponentId.of(result).isRight)
    }

    @Test
    fun `sanitize handles json pointer paths with brackets and slashes`() {
        val raw = "root/children[2]/item"
        val result = IdSanitizer.sanitize(raw)

        assertEquals("root_children_2_item", result)
        assertTrue(ComponentId.of(result).isRight)
    }

    @Test
    fun `sanitize uses fallbackPrefix when raw id is null or blank`() {
        val resultFromNull = IdSanitizer.sanitize(null, fallbackPrefix = "auto_root_c0")
        val resultFromBlank = IdSanitizer.sanitize("   ", fallbackPrefix = "auto_root_c1")

        assertEquals("auto_root_c0", resultFromNull)
        assertEquals("auto_root_c1", resultFromBlank)
        assertTrue(ComponentId.of(resultFromNull).isRight)
        assertTrue(ComponentId.of(resultFromBlank).isRight)
    }

    @Test
    fun `sanitize truncates identifiers exceeding 64 characters`() {
        val superLongId = "a".repeat(100)
        val result = IdSanitizer.sanitize(superLongId)

        assertEquals(64, result.length)
        assertTrue(ComponentId.of(result).isRight)
    }

    @Test
    fun `sanitizeToComponentId returns validated ComponentId directly`() {
        val componentId = IdSanitizer.sanitizeToComponentId("login.field[0]")

        assertEquals("login_field_0", componentId.value)
    }
}
