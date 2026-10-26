package com.libs.flex.ui.parser.internal.mapper.layout

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.model.node.FloatingButtonLayoutNode
import com.libs.flex.ui.core.domain.model.node.HorizontalLayoutNode
import com.libs.flex.ui.core.domain.model.node.ImageNode
import com.libs.flex.ui.core.domain.model.node.ListLayoutNode
import com.libs.flex.ui.core.domain.model.node.ScrollLayoutNode
import com.libs.flex.ui.core.domain.model.node.SliderLayoutNode
import com.libs.flex.ui.core.domain.model.node.TextNode
import com.libs.flex.ui.core.domain.model.node.UIComponentNode
import com.libs.flex.ui.core.domain.model.node.VerticalLayoutNode
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserOptions
import com.libs.flex.ui.parser.fakes.FakePropertyReader
import com.libs.flex.ui.parser.internal.model.ChildrenResult
import com.libs.flex.ui.parser.internal.model.MappedNode
import com.libs.flex.ui.parser.internal.reader.JsonPropertyReader
import com.libs.flex.ui.parser.internal.reader.StrictErrorPolicy
import com.libs.flex.ui.parser.internal.scope.DefaultMappingScope
import com.libs.flex.ui.parser.internal.scope.MappingScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ContainerNodeMappersTest {

    // === TEST FIXTURE PROVIDERS (Object Mother Pattern) ===

    private fun givenComponentId(id: String = "test_node"): ComponentId =
        ComponentId.of(id).fold({ throw IllegalStateException("Invalid ID in test fixture") }, { it })

    private fun givenTextNode(id: String = "text_1", text: String = "Hello"): TextNode =
        TextNode(id = givenComponentId(id), text = text)

    private fun givenScope(
        path: String = "root",
        maxDepth: Int = 20,
        resolver: (JsonObject, MappingScope) -> Either<Nel<ParserError>, MappedNode> = { _, _ ->
            Either.Right(MappedNode(givenTextNode()))
        }
    ): DefaultMappingScope = DefaultMappingScope(
        path = path,
        depth = 1,
        options = ParserOptions(maxDepth = maxDepth),
        nodeResolver = resolver,
        onWarn = {}
    )

    private fun givenJsonReader(jsonStr: String, path: String = "root"): JsonPropertyReader {
        val json = Json.parseToJsonElement(jsonStr).jsonObject
        return JsonPropertyReader(json, path, StrictErrorPolicy) {}
    }

    // =========================================================================
    // 1. VerticalLayoutNodeMapper Tests
    // =========================================================================

    @Test
    fun `VerticalLayoutNodeMapper maps children and bubbles up child templates`() {
        val mapper = VerticalLayoutNodeMapper()
        val child1 = givenTextNode("child_1", "One")
        val child2 = givenTextNode("child_2", "Two")
        val template = givenTextNode("tpl_1", "Template")

        val reader = FakePropertyReader(
            path = "root.vertical",
            id = "v_container",
            childrenResult = Either.Right(
                ChildrenResult(
                    children = listOf(child1, child2),
                    templates = listOf(template)
                )
            )
        )

        val result = mapper.map(reader, givenScope())

        val right = assertIs<Either.Right<MappedNode>>(result)
        val node = assertIs<VerticalLayoutNode>(right.value.node)
        assertEquals("v_container", node.id.value)
        assertEquals(2, node.children.size)
        assertEquals(child1, node.children[0])
        assertEquals(child2, node.children[1])
        assertEquals(listOf(template), right.value.templates)
    }

    @Test
    fun `VerticalLayoutNodeMapper supported types match spec`() {
        assertEquals(
            setOf("flexVertical", "contentVertical", "vertical"),
            VerticalLayoutNodeMapper.SUPPORTED_TYPES
        )
    }

    // =========================================================================
    // 2. HorizontalLayoutNodeMapper Tests
    // =========================================================================

    @Test
    fun `HorizontalLayoutNodeMapper maps horizontal container with children`() {
        val mapper = HorizontalLayoutNodeMapper()
        val child = givenTextNode("child_h", "Horizontal Item")

        val reader = FakePropertyReader(
            path = "root.horizontal",
            id = "h_container",
            childrenResult = Either.Right(ChildrenResult(children = listOf(child)))
        )

        val result = mapper.map(reader, givenScope())

        val right = assertIs<Either.Right<MappedNode>>(result)
        val node = assertIs<HorizontalLayoutNode>(right.value.node)
        assertEquals("h_container", node.id.value)
        assertEquals(1, node.children.size)
        assertEquals(child, node.children[0])
    }

    @Test
    fun `HorizontalLayoutNodeMapper supported types match spec`() {
        assertEquals(
            setOf("flexHorizontal", "contentHorizontal", "horizontal"),
            HorizontalLayoutNodeMapper.SUPPORTED_TYPES
        )
    }

    // =========================================================================
    // 3. ScrollLayoutNodeMapper Tests
    // =========================================================================

    @Test
    fun `ScrollLayoutNodeMapper maps orientation and children correctly`() {
        val mapper = ScrollLayoutNodeMapper()
        val child = givenTextNode("scroll_child", "Scroll Item")

        val reader = FakePropertyReader(
            path = "root.scroll",
            id = "scroll_container",
            strings = mapOf("orientation" to "horizontal"),
            childrenResult = Either.Right(ChildrenResult(children = listOf(child)))
        )

        val result = mapper.map(reader, givenScope())

        val right = assertIs<Either.Right<MappedNode>>(result)
        val node = assertIs<ScrollLayoutNode>(right.value.node)
        assertEquals("scroll_container", node.id.value)
        assertEquals("horizontal", node.orientation)
        assertEquals(1, node.children.size)
    }

    @Test
    fun `ScrollLayoutNodeMapper defaults to vertical orientation when unspecified`() {
        val mapper = ScrollLayoutNodeMapper()
        val reader = FakePropertyReader(
            path = "root.scroll",
            id = "scroll_container"
        )

        val result = mapper.map(reader, givenScope())

        val right = assertIs<Either.Right<MappedNode>>(result)
        val node = assertIs<ScrollLayoutNode>(right.value.node)
        assertEquals("vertical", node.orientation)
    }

    @Test
    fun `ScrollLayoutNodeMapper supported types match spec`() {
        assertEquals(
            setOf("flexScroll", "contentScroll", "scroll"),
            ScrollLayoutNodeMapper.SUPPORTED_TYPES
        )
    }

    // =========================================================================
    // 4. CardContainerNodeMapper Tests
    // =========================================================================

    @Test
    fun `CardContainerNodeMapper maps to VerticalLayoutNode`() {
        val mapper = CardContainerNodeMapper()
        val child = givenTextNode("card_text", "Card content")

        val reader = FakePropertyReader(
            path = "root.card",
            id = "card_1",
            childrenResult = Either.Right(ChildrenResult(children = listOf(child)))
        )

        val result = mapper.map(reader, givenScope())

        val right = assertIs<Either.Right<MappedNode>>(result)
        val node = assertIs<VerticalLayoutNode>(right.value.node)
        assertEquals("card_1", node.id.value)
        assertEquals(1, node.children.size)
    }

    @Test
    fun `CardContainerNodeMapper supported types match spec`() {
        assertEquals(
            setOf("flexCard", "cardContainer", "card"),
            CardContainerNodeMapper.SUPPORTED_TYPES
        )
    }

    // =========================================================================
    // 5. GridLayoutNodeMapper Tests
    // =========================================================================

    @Test
    fun `GridLayoutNodeMapper maps to VerticalLayoutNode`() {
        val mapper = GridLayoutNodeMapper()
        val child = givenTextNode("grid_text", "Grid item")

        val reader = FakePropertyReader(
            path = "root.grid",
            id = "grid_1",
            childrenResult = Either.Right(ChildrenResult(children = listOf(child)))
        )

        val result = mapper.map(reader, givenScope())

        val right = assertIs<Either.Right<MappedNode>>(result)
        val node = assertIs<VerticalLayoutNode>(right.value.node)
        assertEquals("grid_1", node.id.value)
        assertEquals(1, node.children.size)
    }

    @Test
    fun `GridLayoutNodeMapper supported types match spec`() {
        assertEquals(
            setOf("flexGrid", "grid"),
            GridLayoutNodeMapper.SUPPORTED_TYPES
        )
    }

    // =========================================================================
    // 6. CarouselLayoutNodeMapper Tests
    // =========================================================================

    @Test
    fun `CarouselLayoutNodeMapper maps autoPlay and intervalMs with explicit children`() {
        val mapper = CarouselLayoutNodeMapper()
        val child = givenTextNode("slide_1", "Slide")

        val reader = FakePropertyReader(
            path = "root.slider",
            id = "slider_1",
            booleans = mapOf("autoPlay" to true),
            ints = mapOf("intervalMs" to 5000),
            childrenResult = Either.Right(ChildrenResult(children = listOf(child)))
        )

        val result = mapper.map(reader, givenScope())

        val right = assertIs<Either.Right<MappedNode>>(result)
        val node = assertIs<SliderLayoutNode>(right.value.node)
        assertEquals("slider_1", node.id.value)
        assertTrue(node.autoPlay)
        assertEquals(5000L, node.intervalMs)
        assertEquals(1, node.children.size)
    }

    @Test
    fun `CarouselLayoutNodeMapper converts string items array into ImageNodes when children is empty`() {
        val mapper = CarouselLayoutNodeMapper()
        val reader = FakePropertyReader(
            path = "root.slider",
            id = "image_slider",
            booleans = mapOf("autoPlay" to true),
            ints = mapOf("autoPlayInterval" to 3000),
            stringLists = mapOf(
                "items" to listOf(
                    "https://picsum.photos/800/400?1",
                    "https://picsum.photos/800/400?2"
                )
            )
        )

        val result = mapper.map(reader, givenScope())

        val right = assertIs<Either.Right<MappedNode>>(result)
        val node = assertIs<SliderLayoutNode>(right.value.node)
        assertEquals(2, node.children.size)
        val img1 = assertIs<ImageNode>(node.children[0])
        assertEquals("image_slider_item_0", img1.id.value)
        assertEquals("https://picsum.photos/800/400?1", img1.url)
        val img2 = assertIs<ImageNode>(node.children[1])
        assertEquals("image_slider_item_1", img2.id.value)
        assertEquals("https://picsum.photos/800/400?2", img2.url)
    }

    @Test
    fun `CarouselLayoutNodeMapper supported types match spec`() {
        assertEquals(
            setOf("flexCarousel", "flexSlider", "contentSlider", "imageSlider"),
            CarouselLayoutNodeMapper.SUPPORTED_TYPES
        )
    }

    // =========================================================================
    // 7. FloatingButtonLayoutNodeMapper Tests
    // =========================================================================

    @Test
    fun `FloatingButtonLayoutNodeMapper maps required properties and action correctly`() {
        val mapper = FloatingButtonLayoutNodeMapper()
        val content = givenTextNode("inner_content", "Screen content")

        val reader = FakePropertyReader(
            path = "root.fab",
            id = "fab_screen",
            strings = mapOf(
                "fabIcon" to "add",
                "actionId" to "on_add_clicked"
            ),
            booleans = mapOf(
                "isEnabled" to true,
                "requiresValidation" to true
            ),
            childrenResult = Either.Right(ChildrenResult(children = listOf(content)))
        )

        val result = mapper.map(reader, givenScope())

        val right = assertIs<Either.Right<MappedNode>>(result)
        val node = assertIs<FloatingButtonLayoutNode>(right.value.node)
        assertEquals("fab_screen", node.id.value)
        assertEquals("add", node.fabIcon)
        assertEquals("on_add_clicked", node.actionId.value)
        assertTrue(node.isEnabled)
        assertTrue(node.requiresValidation)
        assertEquals(1, node.children.size)
    }

    @Test
    fun `FloatingButtonLayoutNodeMapper fails when actionId is invalid or blank`() {
        val mapper = FloatingButtonLayoutNodeMapper()
        val reader = FakePropertyReader(
            path = "root.fab",
            id = "fab_screen",
            strings = mapOf(
                "fabIcon" to "add",
                "actionId" to "   "
            )
        )

        val result = mapper.map(reader, givenScope())

        val left = assertIs<Either.Left<Nel<ParserError>>>(result)
        assertTrue(left.value.toList().any { it is ParserError.InvalidPropertyValue })
    }

    @Test
    fun `FloatingButtonLayoutNodeMapper supported types match spec`() {
        assertEquals(
            setOf("flexFloatingButton", "contentWithFloatingButton"),
            FloatingButtonLayoutNodeMapper.SUPPORTED_TYPES
        )
    }

    // =========================================================================
    // 8. ListLayoutNodeMapper Tests
    // =========================================================================

    @Test
    fun `ListLayoutNodeMapper extracts itemTemplate and sets itemTemplateId`() {
        val mapper = ListLayoutNodeMapper()
        val templateNode = givenTextNode("template_row", "Row Template")
        val mappedTemplate = MappedNode(node = templateNode)

        val reader = FakePropertyReader(
            path = "root.list",
            id = "user_list",
            ints = mapOf("itemsCount" to 10),
            childMap = mapOf("itemTemplate" to Either.Right(mappedTemplate))
        )

        val result = mapper.map(reader, givenScope())

        val right = assertIs<Either.Right<MappedNode>>(result)
        val node = assertIs<ListLayoutNode>(right.value.node)
        assertEquals("user_list", node.id.value)
        assertEquals(templateNode.id, node.itemTemplateId)
        assertEquals(10, node.itemsCount)
        assertEquals(listOf(templateNode), right.value.templates)
    }

    @Test
    fun `ListLayoutNodeMapper derives itemsCount from items list size when itemsCount not provided`() {
        val mapper = ListLayoutNodeMapper()
        val templateNode = givenTextNode("item_tpl", "Item")

        val reader = FakePropertyReader(
            path = "root.list",
            id = "product_list",
            listSizes = mapOf("items" to 5),
            childMap = mapOf("itemTemplate" to Either.Right(MappedNode(templateNode)))
        )

        val result = mapper.map(reader, givenScope())

        val right = assertIs<Either.Right<MappedNode>>(result)
        val node = assertIs<ListLayoutNode>(right.value.node)
        assertEquals(5, node.itemsCount)
    }

    @Test
    fun `ListLayoutNodeMapper supports itemTemplateId as reference when inline template is absent`() {
        val mapper = ListLayoutNodeMapper()
        val reader = FakePropertyReader(
            path = "root.list",
            id = "ref_list",
            strings = mapOf("itemTemplateId" to "external_template_id")
        )

        val result = mapper.map(reader, givenScope())

        val right = assertIs<Either.Right<MappedNode>>(result)
        val node = assertIs<ListLayoutNode>(right.value.node)
        assertEquals("external_template_id", node.itemTemplateId.value)
        assertTrue(right.value.templates.isEmpty())
    }

    @Test
    fun `ListLayoutNodeMapper fails when both itemTemplate and itemTemplateId are missing`() {
        val mapper = ListLayoutNodeMapper()
        val reader = FakePropertyReader(
            path = "root.list",
            id = "invalid_list"
        )

        val result = mapper.map(reader, givenScope())

        val left = assertIs<Either.Left<Nel<ParserError>>>(result)
        assertTrue(left.value.toList().any { it is ParserError.MissingRequiredProperty })
    }

    @Test
    fun `ListLayoutNodeMapper supported types match spec`() {
        assertEquals(
            setOf("flexList", "contentList", "list", "componentListView"),
            ListLayoutNodeMapper.SUPPORTED_TYPES
        )
    }

    // =========================================================================
    // 9. Recursive & Real JSON Integration Tests
    // =========================================================================

    @Test
    fun `JsonPropertyReader maps recursive container tree and accumulates templates from list`() {
        val json = """
            {
                "id": "screen_root",
                "type": "contentVertical",
                "children": [
                    {
                        "id": "header_text",
                        "type": "componentTextView",
                        "text": "Header"
                    },
                    {
                        "id": "product_list",
                        "type": "contentList",
                        "itemsCount": 3,
                        "itemTemplate": {
                            "id": "product_item",
                            "type": "componentTextView",
                            "text": "Row"
                        }
                    }
                ]
            }
        """.trimIndent()

        // Recursive resolver that delegates to our mappers
        val verticalMapper = VerticalLayoutNodeMapper()
        val listMapper = ListLayoutNodeMapper()

        fun resolveComponent(jsonObj: JsonObject, scope: MappingScope): Either<Nel<ParserError>, MappedNode> {
            val type = (jsonObj["type"] as? kotlinx.serialization.json.JsonPrimitive)?.content ?: ""
            val reader = JsonPropertyReader(jsonObj, scope.path, StrictErrorPolicy) {}
            return when {
                type in VerticalLayoutNodeMapper.SUPPORTED_TYPES -> verticalMapper.map(reader, scope)
                type in ListLayoutNodeMapper.SUPPORTED_TYPES -> listMapper.map(reader, scope)
                type == "componentTextView" -> {
                    val id = (jsonObj["id"] as? kotlinx.serialization.json.JsonPrimitive)?.content ?: "text"
                    val text = (jsonObj["text"] as? kotlinx.serialization.json.JsonPrimitive)?.content ?: ""
                    Either.Right(MappedNode(TextNode(id = ComponentId.of(id).getOrNull()!!, text = text)))
                }
                else -> Either.Left(Nel.of(ParserError.UnknownComponentType(scope.path, type)))
            }
        }

        val scope = givenScope(resolver = ::resolveComponent)
        val rootReader = givenJsonReader(json, path = "root")

        val result = verticalMapper.map(rootReader, scope)

        val right = assertIs<Either.Right<MappedNode>>(result)
        val rootNode = assertIs<VerticalLayoutNode>(right.value.node)
        assertEquals("screen_root", rootNode.id.value)
        assertEquals(2, rootNode.children.size)

        // Verify child 0 is TextNode
        val header = assertIs<TextNode>(rootNode.children[0])
        assertEquals("header_text", header.id.value)

        // Verify child 1 is ListLayoutNode
        val list = assertIs<ListLayoutNode>(rootNode.children[1])
        assertEquals("product_list", list.id.value)
        assertEquals("product_item", list.itemTemplateId.value)

        // Verify template bubbled up to root MappedNode.templates!
        assertEquals(1, right.value.templates.size)
        assertEquals("product_item", right.value.templates[0].id.value)
    }

    @Test
    fun `Recursive child mapping propagates error when child fails validation`() {
        val json = """
            {
                "id": "container",
                "type": "contentVertical",
                "children": [
                    {
                        "id": "child_with_error",
                        "type": "unknownType"
                    }
                ]
            }
        """.trimIndent()

        val verticalMapper = VerticalLayoutNodeMapper()
        val scope = givenScope(resolver = { _, childScope ->
            Either.Left(Nel.of(ParserError.UnknownComponentType(childScope.path, "unknownType")))
        })
        val rootReader = givenJsonReader(json, path = "root")

        val result = verticalMapper.map(rootReader, scope)

        val left = assertIs<Either.Left<Nel<ParserError>>>(result)
        assertTrue(left.value.toList().any { it is ParserError.UnknownComponentType })
    }

    @Test
    fun `Recursive child mapping respects max depth and fails when exceeded`() {
        val json = """
            {
                "id": "level_1",
                "type": "contentVertical",
                "children": [
                    {
                        "id": "level_2",
                        "type": "contentVertical"
                    }
                ]
            }
        """.trimIndent()

        val verticalMapper = VerticalLayoutNodeMapper()
        // Scope allows maxDepth = 1, so level_2 at depth 2 will exceed
        val scope = givenScope(maxDepth = 1)
        val rootReader = givenJsonReader(json, path = "root")

        val result = verticalMapper.map(rootReader, scope)

        val left = assertIs<Either.Left<Nel<ParserError>>>(result)
        assertTrue(left.value.toList().any { it is ParserError.MaxDepthExceeded })
    }
}
