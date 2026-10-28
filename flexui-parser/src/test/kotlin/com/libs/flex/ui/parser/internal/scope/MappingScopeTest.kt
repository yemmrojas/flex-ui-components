package com.libs.flex.ui.parser.internal.scope

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.model.node.TextNode
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserOptions
import com.libs.flex.ui.parser.ParserWarning
import com.libs.flex.ui.parser.internal.model.MappedNode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MappingScopeTest {

    private fun parseJsonObject(json: String): JsonObject =
        Json.parseToJsonElement(json).jsonObject

    @Test
    fun `mapChild increments depth and tracks path`() {
        var observedPath = ""
        var observedDepth = -1

        val rootScope = DefaultMappingScope(
            path = "root",
            depth = 1,
            options = ParserOptions(maxDepth = 5),
            nodeResolver = { _, childScope ->
                observedPath = childScope.path
                observedDepth = childScope.depth
                val id = ComponentId.of("txt_1").fold({ error("invalid") }, { it })
                Either.Right(MappedNode(TextNode(id = id, text = "Child")))
            },
            onWarn = {}
        )

        val childJson = parseJsonObject("""{"type": "text"}""")
        val result = rootScope.mapChild(childJson, "children[0]")

        assertIs<Either.Right<MappedNode>>(result)
        assertEquals("root.children[0]", observedPath)
        assertEquals(2, observedDepth)
    }

    @Test
    fun `mapChild returns MaxDepthExceeded error when depth exceeds limit`() {
        val rootScope = DefaultMappingScope(
            path = "root",
            depth = 5,
            options = ParserOptions(maxDepth = 5),
            nodeResolver = { _, _ -> error("Should not be called") },
            onWarn = {}
        )

        val childJson = parseJsonObject("""{"type": "text"}""")
        val result = rootScope.mapChild(childJson, "children[0]")

        val left = assertIs<Either.Left<*>>(result)
        val error = assertIs<ParserError.MaxDepthExceeded>((left.value as com.libs.flex.ui.core.domain.functional.Nel<*>).head)
        assertEquals("root.children[0]", error.path)
        assertEquals(6, error.currentDepth)
        assertEquals(5, error.maxDepth)
    }

    @Test
    fun `mapChild returns InvalidPropertyValue when element is not a JsonObject`() {
        val rootScope = DefaultMappingScope(
            path = "root",
            depth = 1,
            options = ParserOptions(maxDepth = 5),
            nodeResolver = { _, _ -> error("Should not be called") },
            onWarn = {}
        )

        val result = rootScope.mapChild(Json.parseToJsonElement("\"primitive string\""), "item")

        val left = assertIs<Either.Left<*>>(result)
        val error = assertIs<ParserError.InvalidPropertyValue>((left.value as com.libs.flex.ui.core.domain.functional.Nel<*>).head)
        assertEquals("root.item", error.path)
    }

    @Test
    fun `warn registers warning with scope path`() {
        val warnings = mutableListOf<ParserWarning>()
        val scope = DefaultMappingScope(
            path = "root.btn",
            depth = 2,
            options = ParserOptions.DEFAULT,
            nodeResolver = { _, _ -> error("unused") },
            onWarn = { warnings.add(it) }
        )

        scope.warn("DEPRECATED", "Button property is deprecated")

        assertEquals(1, warnings.size)
        assertEquals("root.btn", warnings.first().path)
        assertEquals("DEPRECATED", warnings.first().code)
    }
}
