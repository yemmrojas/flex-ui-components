package com.libs.flex.ui.parser.internal.reader

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserMode
import com.libs.flex.ui.parser.ParserWarning
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ErrorPolicyTest {

    private val sampleError = ParserError.InvalidPropertyValue(
        path = "root.style.backgroundColor",
        property = "backgroundColor",
        reason = "Invalid hex color format"
    )

    @Test
    fun `StrictErrorPolicy returns Left with Nel containing error`() {
        val policy = ErrorPolicy.of(ParserMode.STRICT)
        var warningEmitted: ParserWarning? = null

        val result = policy.handleOptionalError<String>(
            path = "root.style.backgroundColor",
            property = "backgroundColor",
            error = sampleError,
            default = "#FFFFFF",
            onWarn = { warningEmitted = it }
        )

        val left = assertIs<Either.Left<*>>(result)
        assertEquals(sampleError, (left.value as com.libs.flex.ui.core.domain.functional.Nel<*>).head)
        assertEquals(null, warningEmitted)
    }

    @Test
    fun `TolerantErrorPolicy returns Right with default and emits warning`() {
        val policy = ErrorPolicy.of(ParserMode.TOLERANT)
        var warningEmitted: ParserWarning? = null

        val result = policy.handleOptionalError(
            path = "root.style.backgroundColor",
            property = "backgroundColor",
            error = sampleError,
            default = "#FFFFFF",
            onWarn = { warningEmitted = it }
        )

        val right = assertIs<Either.Right<String?>>(result)
        assertEquals("#FFFFFF", right.value)
        assertEquals("root.style.backgroundColor", warningEmitted?.path)
        assertEquals("INVALID_OPTIONAL_PROPERTY", warningEmitted?.code)
        assertTrue(warningEmitted?.message?.contains("Invalid hex color format") == true)
    }

    @Test
    fun `TolerantErrorPolicy handles null default value gracefully`() {
        val policy = ErrorPolicy.of(ParserMode.TOLERANT)
        var warningEmitted: ParserWarning? = null

        val result = policy.handleOptionalError<Int?>(
            path = "root.elevation",
            property = "elevation",
            error = sampleError,
            default = null,
            onWarn = { warningEmitted = it }
        )

        val right = assertIs<Either.Right<Int?>>(result)
        assertEquals(null, right.value)
        assertEquals("root.elevation", warningEmitted?.path)
    }
}
