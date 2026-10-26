package com.libs.flex.ui.parser.internal.mapper

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.model.node.TextNode
import com.libs.flex.ui.core.domain.model.node.UnsupportedFallbackNode
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserOptions
import com.libs.flex.ui.parser.ParserWarning
import com.libs.flex.ui.parser.fakes.FakePropertyReader
import com.libs.flex.ui.parser.internal.model.MappedNode
import com.libs.flex.ui.parser.internal.scope.DefaultMappingScope
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TolerantNodeMapperDecoratorTest {

    // === TEST FIXTURE PROVIDERS (Object Mother Pattern) ===

    private fun givenComponentId(id: String = "test_node"): ComponentId =
        ComponentId.of(id).fold({ throw IllegalStateException("Invalid fixture ID") }, { it })

    private fun givenTextNode(id: String = "text_node", text: String = "Hello"): TextNode =
        TextNode(id = givenComponentId(id), text = text)

    private fun givenScope(
        path: String = "root.node",
        warnings: MutableList<ParserWarning> = mutableListOf()
    ): DefaultMappingScope = DefaultMappingScope(
        path = path,
        depth = 1,
        options = ParserOptions.DEFAULT,
        nodeResolver = { _, _ -> Either.Right(MappedNode(givenTextNode())) },
        onWarn = { warnings.add(it) }
    )

    // =========================================================================
    // Tests
    // =========================================================================

    @Test
    fun `when delegate succeeds, returns Right with delegate node untouched and no warnings`() {
        val expectedNode = givenTextNode("title", "Flex UI")
        val delegate = NodeMapper { _, _ -> Either.Right(MappedNode(expectedNode)) }
        val decorator = TolerantNodeMapperDecorator(delegate)

        val warnings = mutableListOf<ParserWarning>()
        val scope = givenScope(warnings = warnings)
        val reader = FakePropertyReader(id = "title")

        val result = decorator.map(reader, scope)

        val right = assertIs<Either.Right<MappedNode>>(result)
        assertEquals(expectedNode, right.value.node)
        assertTrue(warnings.isEmpty())
    }

    @Test
    fun `when delegate fails, returns Right with UnsupportedFallbackNode and emits warning`() {
        val delegate = NodeMapper { reader, _ ->
            Either.Left(Nel.of(ParserError.MissingRequiredProperty(reader.path, "requiredField")))
        }
        val decorator = TolerantNodeMapperDecorator(delegate)

        val warnings = mutableListOf<ParserWarning>()
        val scope = givenScope(path = "root.broken_node", warnings = warnings)
        val reader = FakePropertyReader(
            path = "root.broken_node",
            id = "custom_id",
            strings = mapOf("type" to "specialButton")
        )

        val result = decorator.map(reader, scope)

        val right = assertIs<Either.Right<MappedNode>>(result)
        val fallback = assertIs<UnsupportedFallbackNode>(right.value.node)
        assertEquals("custom_id", fallback.id.value)
        assertEquals("specialButton", fallback.rawType)
        assertTrue(fallback.reason.contains("requiredField"))

        assertEquals(1, warnings.size)
        val warning = warnings.first()
        assertEquals("NODE_FALLBACK", warning.code)
        assertEquals("root.broken_node", warning.path)
        assertTrue(warning.message.contains("specialButton"))
    }

    @Test
    fun `when delegate fails and ID is missing or blank, generates safe fallback ID from scope path`() {
        val delegate = NodeMapper { _, _ ->
            Either.Left(Nel.of(ParserError.InvalidPropertyValue("root.section.item", "value", "Corrupt value")))
        }
        val decorator = TolerantNodeMapperDecorator(delegate)

        val warnings = mutableListOf<ParserWarning>()
        val scope = givenScope(path = "root.section.item", warnings = warnings)
        val reader = FakePropertyReader(
            path = "root.section.item",
            id = null,
            strings = mapOf("type" to "corruptWidget")
        )

        val result = decorator.map(reader, scope)

        val right = assertIs<Either.Right<MappedNode>>(result)
        val fallback = assertIs<UnsupportedFallbackNode>(right.value.node)
        assertEquals("auto_root_section_item", fallback.id.value)
        assertEquals("corruptWidget", fallback.rawType)
        assertTrue(fallback.reason.contains("Corrupt value"))
        assertEquals(1, warnings.size)
    }

    @Test
    fun `when delegate fails with multiple errors, combines reasons in fallback node`() {
        val delegate = NodeMapper { _, _ ->
            Either.Left(
                Nel.of(
                    ParserError.MissingRequiredProperty("root.node", "field1"),
                    ParserError.MissingRequiredProperty("root.node", "field2")
                )
            )
        }
        val decorator = TolerantNodeMapperDecorator(delegate)

        val warnings = mutableListOf<ParserWarning>()
        val scope = givenScope(warnings = warnings)
        val reader = FakePropertyReader(id = "multi_err_node")

        val result = decorator.map(reader, scope)

        val right = assertIs<Either.Right<MappedNode>>(result)
        val fallback = assertIs<UnsupportedFallbackNode>(right.value.node)
        assertTrue(fallback.reason.contains("field1"))
        assertTrue(fallback.reason.contains("field2"))
        assertEquals(1, warnings.size)
    }

    @Test
    fun `when delegate throws unexpected exception, captures it safely without crashing`() {
        val delegate = NodeMapper { _, _ ->
            throw IllegalStateException("Unexpected crash in third party mapper")
        }
        val decorator = TolerantNodeMapperDecorator(delegate)

        val warnings = mutableListOf<ParserWarning>()
        val scope = givenScope(path = "root.crashing_node", warnings = warnings)
        val reader = FakePropertyReader(
            path = "root.crashing_node",
            id = "crashing_id",
            strings = mapOf("type" to "crasher")
        )

        val result = decorator.map(reader, scope)

        val right = assertIs<Either.Right<MappedNode>>(result)
        val fallback = assertIs<UnsupportedFallbackNode>(right.value.node)
        assertEquals("crashing_id", fallback.id.value)
        assertEquals("crasher", fallback.rawType)
        assertTrue(fallback.reason.contains("Unexpected crash in third party mapper"))
        assertEquals(1, warnings.size)
    }

    @Test
    fun `when raw ID contains invalid characters, sanitizes it to valid ComponentId`() {
        val delegate = NodeMapper { _, _ ->
            Either.Left(Nel.of(ParserError.MissingRequiredProperty("root.node", "text")))
        }
        val decorator = TolerantNodeMapperDecorator(delegate)

        val reader = FakePropertyReader(
            id = "invalid id with spaces & symbols!",
            strings = mapOf("type" to "flexText")
        )

        val result = decorator.map(reader, givenScope())

        val right = assertIs<Either.Right<MappedNode>>(result)
        val fallback = assertIs<UnsupportedFallbackNode>(right.value.node)
        assertEquals("invalid_id_with_spaces_symbols", fallback.id.value)
    }

    @Test
    fun `when delegate succeeds with templates, preserves templates list in MappedNode`() {
        val childTemplate = givenTextNode("tpl_item", "Template Item")
        val mainNode = givenTextNode("main_list", "List")
        val expectedMapped = MappedNode(node = mainNode, templates = listOf(childTemplate))

        val delegate = NodeMapper { _, _ -> Either.Right(expectedMapped) }
        val decorator = TolerantNodeMapperDecorator(delegate)

        val result = decorator.map(FakePropertyReader(), givenScope())

        val right = assertIs<Either.Right<MappedNode>>(result)
        assertEquals(expectedMapped, right.value)
        assertEquals(listOf(childTemplate), right.value.templates)
    }
}

