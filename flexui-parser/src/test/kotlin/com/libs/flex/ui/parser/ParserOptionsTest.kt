package com.libs.flex.ui.parser

import com.libs.flex.ui.core.domain.model.LayoutDocument
import com.libs.flex.ui.core.domain.model.LayoutLimits
import com.libs.flex.ui.core.domain.model.node.TextNode
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.SchemaVersion
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParserOptionsTest {

    @Test
    fun `ParserOptions has safe default values`() {
        val defaultOptions = ParserOptions.DEFAULT

        assertEquals(ParserMode.STRICT, defaultOptions.mode)
        assertEquals(10 * 1024 * 1024L, defaultOptions.maxPayloadBytes)
        assertEquals(LayoutLimits.DEFAULT_MAX_DEPTH, defaultOptions.maxDepth)
        assertEquals(SchemaVersion.CURRENT, defaultOptions.defaultSchemaVersion)
    }

    @Test
    fun `ParserOptions allows custom configuration`() {
        val customOptions = ParserOptions(
            mode = ParserMode.TOLERANT,
            maxPayloadBytes = 512 * 1024L,
            maxDepth = 15,
            defaultSchemaVersion = SchemaVersion("1.0.0")
        )

        assertEquals(ParserMode.TOLERANT, customOptions.mode)
        assertEquals(512 * 1024L, customOptions.maxPayloadBytes)
        assertEquals(15, customOptions.maxDepth)
        assertEquals(SchemaVersion("1.0.0"), customOptions.defaultSchemaVersion)
    }

    @Test
    fun `ParseResult holds document and warnings`() {
        val id = ComponentId.of("txt_1").fold({ error("invalid id") }, { it })
        val rootNode = TextNode(id = id, text = "Hello")
        val document = LayoutDocument.create(SchemaVersion.CURRENT, rootNode).fold({ error("doc failure") }, { it })
        val warning = ParserWarning("root", "CODE", "msg")

        val result = ParseResult(
            document = document,
            warnings = listOf(warning)
        )

        assertEquals(document, result.document)
        assertEquals(1, result.warnings.size)
        assertEquals("CODE", result.warnings.first().code)
    }
}
