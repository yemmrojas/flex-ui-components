package com.libs.flex.ui.core.domain.model

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
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

        val limits = LayoutLimits.of(maxDepth = 10, maxNodes = 500, maxErrors = 5).getOrNull()!!
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

    @Test
    public fun `standaloneInputNodes separates root inputs from template inputs`() {
        val rootInputId = ComponentId.of("root_input").getOrNull()!!
        val templateId = ComponentId.of("row_tmpl").getOrNull()!!
        val templateInputId = ComponentId.of("tmpl_input").getOrNull()!!
        val listId = ComponentId.of("list").getOrNull()!!

        val rootInput = InputNode(id = rootInputId, label = "Root")
        val listNode = ListLayoutNode(id = listId, itemTemplateId = templateId)
        val templateRoot = InputNode(id = templateInputId, label = "Template Input")

        val root = VerticalLayoutNode(
            id = ComponentId.of("root").getOrNull()!!,
            children = listOf(rootInput, listNode)
        )

        val doc = LayoutDocument.create(
            schemaVersion = SchemaVersion.CURRENT,
            root = root,
            templates = mapOf(templateId to templateRoot)
        ).getOrNull()!!

        assertEquals(2, doc.allInputNodes().size)
        assertEquals(1, doc.standaloneInputNodes().size)
        assertEquals(rootInputId, doc.standaloneInputNodes().first().id)
        assertTrue(doc.isTemplateNode(templateInputId))
        kotlin.test.assertFalse(doc.isTemplateNode(rootInputId))
    }

    @Test
    public fun `create returns DuplicateTemplateUsage when multiple lists reuse the same templateId`() {
        val list1Id = ComponentId.of("list_1").getOrNull()!!
        val list2Id = ComponentId.of("list_2").getOrNull()!!
        val sharedTemplateId = ComponentId.of("shared_tmpl").getOrNull()!!
        val templateInputId = ComponentId.of("tmpl_input").getOrNull()!!

        val list1 = ListLayoutNode(id = list1Id, itemTemplateId = sharedTemplateId)
        val list2 = ListLayoutNode(id = list2Id, itemTemplateId = sharedTemplateId)
        val templateRoot = InputNode(id = templateInputId, label = "Template Input")

        val root = VerticalLayoutNode(
            id = ComponentId.of("root").getOrNull()!!,
            children = listOf(list1, list2)
        )

        val result = LayoutDocument.create(
            schemaVersion = SchemaVersion.CURRENT,
            root = root,
            templates = mapOf(sharedTemplateId to templateRoot)
        )

        assertTrue(result.isLeft)
        val errors = (result as Either.Left).value
        val duplicateError = errors.head as? DomainError.Structural.DuplicateTemplateUsage
        assertNotNull(duplicateError)
        assertEquals(list2Id, duplicateError?.listId)
        assertEquals(sharedTemplateId, duplicateError?.templateId)
    }

    @Test
    public fun `create succeeds when multiple lists reference distinct templates`() {
        val list1Id = ComponentId.of("list_1").getOrNull()!!
        val list2Id = ComponentId.of("list_2").getOrNull()!!
        val tmpl1Id = ComponentId.of("tmpl_1").getOrNull()!!
        val tmpl2Id = ComponentId.of("tmpl_2").getOrNull()!!
        val input1Id = ComponentId.of("input_1").getOrNull()!!
        val input2Id = ComponentId.of("input_2").getOrNull()!!

        val list1 = ListLayoutNode(id = list1Id, itemTemplateId = tmpl1Id)
        val list2 = ListLayoutNode(id = list2Id, itemTemplateId = tmpl2Id)

        val root = VerticalLayoutNode(
            id = ComponentId.of("root").getOrNull()!!,
            children = listOf(list1, list2)
        )

        val result = LayoutDocument.create(
            schemaVersion = SchemaVersion.CURRENT,
            root = root,
            templates = mapOf(
                tmpl1Id to InputNode(id = input1Id, label = "Input 1"),
                tmpl2Id to InputNode(id = input2Id, label = "Input 2")
            )
        )

        assertTrue(result.isRight)
    }

    @Test
    public fun `create returns MaxNodesExceeded and stops traversal when maxNodes is reached`() {
        val rootId = ComponentId.of("root").getOrNull()!!
        val text1Id = ComponentId.of("text_1").getOrNull()!!
        val text2Id = ComponentId.of("text_2").getOrNull()!!
        val text3Id = ComponentId.of("text_3").getOrNull()!!

        val root = VerticalLayoutNode(
            id = rootId,
            children = listOf(
                TextNode(id = text1Id, text = "1"),
                TextNode(id = text2Id, text = "2"),
                TextNode(id = text3Id, text = "3")
            )
        )

        // root + 3 children = 4 nodes in total. Setting maxNodes = 2 should trigger MaxNodesExceeded and stop traversal
        val limits = LayoutLimits.of(maxNodes = 2).getOrNull()!!
        val result = LayoutDocument.create(
            schemaVersion = SchemaVersion.CURRENT,
            root = root,
            limits = limits
        )

        assertTrue(result.isLeft)
        val errors = (result as Either.Left).value
        val maxNodesError = errors.head as? DomainError.Structural.MaxNodesExceeded
        assertNotNull(maxNodesError)
        assertEquals(2, maxNodesError?.max)
    }
}
