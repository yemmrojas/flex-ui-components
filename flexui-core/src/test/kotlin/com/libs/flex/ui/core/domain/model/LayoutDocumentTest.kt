package com.libs.flex.ui.core.domain.model

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.model.node.ButtonNode
import com.libs.flex.ui.core.domain.model.node.FloatingButtonLayoutNode
import com.libs.flex.ui.core.domain.model.node.InputNode
import com.libs.flex.ui.core.domain.model.node.ListLayoutNode
import com.libs.flex.ui.core.domain.model.node.TextNode
import com.libs.flex.ui.core.domain.model.node.VerticalLayoutNode
import com.libs.flex.ui.core.domain.value.ActionId
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.SchemaVersion
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

public class LayoutDocumentTest {

    @Test
    public fun `valid document builds indexes in natural order`() {
        val rootId = ComponentId.of("root").getOrNull()!!
        val textId = ComponentId.of("title").getOrNull()!!
        val input1Id = ComponentId.of("first_name").getOrNull()!!
        val input2Id = ComponentId.of("last_name").getOrNull()!!
        val buttonId = ComponentId.of("submit_btn").getOrNull()!!
        val actionId = ActionId.of("submit_form").getOrNull()!!

        val textNode = TextNode(id = textId, text = "Welcome")
        val input1 = InputNode(id = input1Id, label = "First")
        val input2 = InputNode(id = input2Id, label = "Last")
        val buttonNode = ButtonNode(id = buttonId, text = "Submit", actionId = actionId)

        val root = VerticalLayoutNode(
            id = rootId,
            children = listOf(textNode, input1, input2, buttonNode)
        )

        val result = LayoutDocument.create(SchemaVersion.CURRENT, root)

        assertTrue(result.isRight)
        val document = result.getOrNull()!!

        assertEquals(5, document.totalNodesCount)
        assertEquals("Welcome", (document.findNode(textId) as? TextNode)?.text)
        assertNotNull(document.findInteractiveNode(buttonId))

        // Verifica orden natural de inputs en el documento
        val inputs = document.allInputNodes()
        assertEquals(2, inputs.size)
        assertEquals(input1Id, inputs[0].id)
        assertEquals(input2Id, inputs[1].id)
    }

    @Test
    public fun `iterative traversal does not throw StackOverflowError on deeply nested tree`() {
        // Árbol lineal de 50.000 niveles
        var current: VerticalLayoutNode = VerticalLayoutNode(id = ComponentId.of("leaf").getOrNull()!!)
        for (i in 50_000 downTo 1) {
            val id = ComponentId.of("n_$i").getOrNull()!!
            current = VerticalLayoutNode(id = id, children = listOf(current))
        }

        val limits = LayoutLimits(maxDepth = 10, maxNodes = 500, maxErrors = 5)
        val result = LayoutDocument.create(SchemaVersion.CURRENT, current, limits = limits)

        assertTrue(result.isLeft)
        val errors = result.leftOrNull()!!
        assertTrue(errors.toList().any { it is DomainError.Structural.MaxNestingDepthExceeded })
    }

    @Test
    public fun `duplicate ids are pruned and don't flood stack`() {
        val rootId = ComponentId.of("root").getOrNull()!!
        val dupId = ComponentId.of("dup").getOrNull()!!

        val child1 = TextNode(id = dupId, text = "A")
        val child2 = TextNode(id = dupId, text = "B")

        val root = VerticalLayoutNode(id = rootId, children = listOf(child1, child2))
        val result = LayoutDocument.create(SchemaVersion.CURRENT, root)

        assertTrue(result.isLeft)
        val errors = result.leftOrNull()!!
        assertEquals(1, errors.toList().filterIsInstance<DomainError.Structural.DuplicateComponentId>().size)
    }

    @Test
    public fun `unsupported schema version short-circuits immediately`() {
        val root = TextNode(id = ComponentId.of("txt").getOrNull()!!, text = "Hi")
        val result = LayoutDocument.create(SchemaVersion("99.0.0"), root)

        assertTrue(result.isLeft)
        val errors = result.leftOrNull()!!
        assertEquals(1, errors.size)
        assertTrue(errors.head is DomainError.Structural.UnsupportedSchemaVersion)
    }

    @Test
    public fun `template map validates references and maps templateScopeOf`() {
        val rootId = ComponentId.of("root").getOrNull()!!
        val listId = ComponentId.of("user_list").getOrNull()!!
        val templateId = ComponentId.of("user_row_template").getOrNull()!!
        val rowTextId = ComponentId.of("user_row_text").getOrNull()!!

        val listNode = ListLayoutNode(
            id = listId,
            itemTemplateId = templateId,
            itemsCount = 10
        )
        val root = VerticalLayoutNode(id = rootId, children = listOf(listNode))

        val templateRoot = TextNode(id = rowTextId, text = "Row")
        val templates = mapOf(templateId to templateRoot)

        val result = LayoutDocument.create(SchemaVersion.CURRENT, root, templates = templates)

        assertTrue(result.isRight)
        val document = result.getOrNull()!!

        assertEquals(templateId, document.templateScopeOf[rowTextId])
        assertNotNull(document.findNode(rowTextId))
    }

    @Test
    public fun `floating button layout node indexes children and interactive capability`() {
        val fabId = ComponentId.of("fab").getOrNull()!!
        val actionId = ActionId.of("click_fab").getOrNull()!!
        val textId = ComponentId.of("inner_text").getOrNull()!!

        val fab = FloatingButtonLayoutNode(
            id = fabId,
            fabIcon = "plus",
            actionId = actionId,
            children = listOf(TextNode(id = textId, text = "FAB content"))
        )

        val result = LayoutDocument.create(SchemaVersion.CURRENT, fab)

        assertTrue(result.isRight)
        val document = result.getOrNull()!!

        assertNotNull(document.findInteractiveNode(fabId))
        assertNotNull(document.findNode(textId))
    }
}
