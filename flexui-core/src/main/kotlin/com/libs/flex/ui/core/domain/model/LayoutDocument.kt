package com.libs.flex.ui.core.domain.model

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.model.node.InputCapableNode
import com.libs.flex.ui.core.domain.model.node.InteractiveNode
import com.libs.flex.ui.core.domain.model.node.LayoutNode
import com.libs.flex.ui.core.domain.model.node.ListLayoutNode
import com.libs.flex.ui.core.domain.model.node.UIComponentNode
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.SchemaVersion

public class LayoutDocument private constructor(
    public val schemaVersion: SchemaVersion,
    public val root: UIComponentNode,
    public val templates: Map<ComponentId, UIComponentNode>,
    private val nodeIndex: Map<ComponentId, UIComponentNode>,
    private val inputNodesList: List<InputCapableNode>,
    public val parentOf: Map<ComponentId, ComponentId>,
    public val templateScopeOf: Map<ComponentId, ComponentId>
) {
    public fun findNode(id: ComponentId): UIComponentNode? = nodeIndex[id]

    public fun findInteractiveNode(id: ComponentId): InteractiveNode? =
        nodeIndex[id] as? InteractiveNode

    public fun allInputNodes(): List<InputCapableNode> = inputNodesList

    public fun isTemplateNode(id: ComponentId): Boolean = templateScopeOf.containsKey(id)

    public fun standaloneInputNodes(): List<InputCapableNode> =
        inputNodesList.filter { !isTemplateNode(it.id) }

    public val totalNodesCount: Int get() = nodeIndex.size

    public companion object {
        public fun create(
            schemaVersion: SchemaVersion,
            root: UIComponentNode,
            templates: Map<ComponentId, UIComponentNode> = emptyMap(),
            limits: LayoutLimits = LayoutLimits.DEFAULT
        ): Either<Nel<DomainError.Structural>, LayoutDocument> {
            // Cortocircuito inmediato de versión no soportada
            if (schemaVersion.value !in SchemaVersion.SUPPORTED_VERSIONS) {
                return Either.Left(
                    Nel.of(
                        DomainError.Structural.UnsupportedSchemaVersion(
                            version = schemaVersion.value,
                            supportedMin = SchemaVersion.SUPPORTED_MIN,
                            supportedMax = SchemaVersion.SUPPORTED_MAX
                        )
                    )
                )
            }

            val scan = LayoutTreeScan.execute(root, templates, limits)

            val errors = mutableListOf<DomainError.Structural>()
            errors.addAll(scan.scanErrors)

            // Validar límites globales y referencias si no se alcanzó el tope de errores
            if (errors.size < limits.maxErrors) {
                // Validar que los itemTemplateId de listas existan en el mapa de templates y no sean reutilizados
                val usedTemplates = mutableSetOf<ComponentId>()
                scan.listNodes.forEach { listNode ->
                    if (errors.size >= limits.maxErrors) return@forEach
                    if (!templates.containsKey(listNode.itemTemplateId)) {
                        errors.add(
                            DomainError.Structural.InvalidComponentReference(
                                id = listNode.id,
                                referenceId = listNode.itemTemplateId
                            )
                        )
                    } else if (!usedTemplates.add(listNode.itemTemplateId)) {
                        errors.add(
                            DomainError.Structural.DuplicateTemplateUsage(
                                listId = listNode.id,
                                templateId = listNode.itemTemplateId
                            )
                        )
                    }
                }
            }

            val nelErrors = Nel.fromListOrNull(errors)
            return if (nelErrors != null) {
                Either.Left(nelErrors)
            } else {
                Either.Right(
                    LayoutDocument(
                        schemaVersion = schemaVersion,
                        root = root,
                        templates = templates.toMap(),
                        nodeIndex = scan.nodeMap.toMap(),
                        inputNodesList = scan.inputList.toList(),
                        parentOf = scan.parentMap.toMap(),
                        templateScopeOf = scan.templateScopeMap.toMap()
                    )
                )
            }
        }
    }
}

private data class Visit(
    val node: UIComponentNode,
    val depth: Int,
    val parentId: ComponentId?,
    val listScopeId: ComponentId?
)

internal class LayoutTreeScan private constructor(
    val nodeMap: Map<ComponentId, UIComponentNode>,
    val inputList: List<InputCapableNode>,
    val parentMap: Map<ComponentId, ComponentId>,
    val templateScopeMap: Map<ComponentId, ComponentId>,
    val listNodes: List<ListLayoutNode>,
    val scanErrors: List<DomainError.Structural>
) {
    companion object {
        fun execute(
            root: UIComponentNode,
            templates: Map<ComponentId, UIComponentNode>,
            limits: LayoutLimits
        ): LayoutTreeScan {
            val nodeMap = mutableMapOf<ComponentId, UIComponentNode>()
            val inputList = mutableListOf<InputCapableNode>()
            val parentMap = mutableMapOf<ComponentId, ComponentId>()
            val templateScopeMap = mutableMapOf<ComponentId, ComponentId>()
            val listNodes = mutableListOf<ListLayoutNode>()
            val errors = mutableListOf<DomainError.Structural>()

            val stack = ArrayDeque<Visit>()
            stack.addLast(Visit(root, depth = 1, parentId = null, listScopeId = null))

            // Agregar también los templates al stack para indexar sus nodos y detectar duplicados
            templates.forEach { (templateId, templateRoot) ->
                stack.addLast(Visit(templateRoot, depth = 1, parentId = null, listScopeId = templateId))
            }

            while (stack.isNotEmpty() && errors.size < limits.maxErrors) {
                val (node, depth, parentId, listScopeId) = stack.removeLast()

                // Si ya existe el ID: podar la rama y no indexar
                if (nodeMap.containsKey(node.id)) {
                    errors.add(DomainError.Structural.DuplicateComponentId(node.id))
                    continue
                }

                if (nodeMap.size >= limits.maxNodes) {
                    errors.add(DomainError.Structural.MaxNodesExceeded(nodeMap.size + 1, limits.maxNodes))
                    break
                }

                nodeMap[node.id] = node
                if (parentId != null) parentMap[node.id] = parentId
                if (listScopeId != null) templateScopeMap[node.id] = listScopeId

                if (node is InputCapableNode) {
                    inputList.add(node)
                }

                if (node is ListLayoutNode) {
                    listNodes.add(node)
                }

                if (node is LayoutNode) {
                    if (depth >= limits.maxDepth && node.children.isNotEmpty()) {
                        // Podar rama excedida: un solo error sin descender
                        errors.add(DomainError.Structural.MaxNestingDepthExceeded(node.id, depth + 1))
                    } else {
                        // asReversed para garantizar orden natural LIFO
                        node.children.asReversed().forEach { child ->
                            stack.addLast(Visit(child, depth + 1, node.id, listScopeId))
                        }
                    }
                }
            }

            return LayoutTreeScan(
                nodeMap = nodeMap,
                inputList = inputList,
                parentMap = parentMap,
                templateScopeMap = templateScopeMap,
                listNodes = listNodes,
                scanErrors = errors
            )
        }
    }
}
