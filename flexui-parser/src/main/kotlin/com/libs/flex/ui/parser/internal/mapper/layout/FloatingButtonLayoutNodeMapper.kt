package com.libs.flex.ui.parser.internal.mapper.layout

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.functional.zip
import com.libs.flex.ui.core.domain.model.node.FloatingButtonLayoutNode
import com.libs.flex.ui.core.domain.value.ActionId
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.internal.mapper.NodeMapper
import com.libs.flex.ui.parser.internal.model.MappedNode
import com.libs.flex.ui.parser.internal.reader.PropertyReader
import com.libs.flex.ui.parser.internal.scope.MappingScope
import com.libs.flex.ui.parser.internal.util.Quad

private const val PRIMARY_TYPE = "flexFloatingButton"
private val ALIASES = setOf("contentWithFloatingButton")

private const val KEY_FAB_ICON = "fabIcon"
private const val ALIAS_ICON = "icon"
private const val KEY_ACTION_ID = "actionId"
private const val ALIAS_ACTION = "action"
private const val KEY_IS_ENABLED = "isEnabled"
private const val ALIAS_ENABLED = "enabled"
private const val KEY_REQUIRES_VALIDATION = "requiresValidation"
private const val DEFAULT_IS_ENABLED = true
private const val DEFAULT_REQUIRES_VALIDATION = false

/**
 * Mapeador para contenedores con botón de acción flotante ([FloatingButtonLayoutNode]).
 */
internal class FloatingButtonLayoutNodeMapper : NodeMapper {

    override fun map(
        reader: PropertyReader,
        scope: MappingScope
    ): Either<Nel<ParserError>, MappedNode> {
        val commonResult = reader.readCommonProperties()
        val fabIconResult = reader.requiredString(KEY_FAB_ICON, ALIAS_ICON)
        val actionIdResult = readActionId(reader)
        val isEnabledResult = reader.optionalBoolean(KEY_IS_ENABLED, ALIAS_ENABLED, default = DEFAULT_IS_ENABLED)
        val requiresValidationResult = reader.optionalBoolean(
            KEY_REQUIRES_VALIDATION,
            default = DEFAULT_REQUIRES_VALIDATION
        )
        val childrenResult = reader.readChildren(scope)

        return commonResult
            .zip(fabIconResult) { common, icon -> Pair(common, icon) }
            .zip(actionIdResult) { (common, icon), actionId -> Triple(common, icon, actionId) }
            .zip(isEnabledResult) { (common, icon, actionId), enabled -> Quad(common, icon, actionId, enabled) }
            .zip(requiresValidationResult) { (common, icon, actionId, enabled), reqVal ->
                Quad(common, icon, actionId, Pair(enabled, reqVal))
            }
            .zip(childrenResult) { (common, icon, actionId, flags), children ->
                MappedNode(
                    node = FloatingButtonLayoutNode(
                        id = common.id,
                        style = common.style,
                        fabIcon = icon,
                        actionId = actionId,
                        isEnabled = flags.first,
                        requiresValidation = flags.second,
                        children = children.children
                    ),
                    templates = children.templates
                )
            }
    }

    private fun readActionId(reader: PropertyReader): Either<Nel<ParserError>, ActionId> {
        return reader.requiredString(KEY_ACTION_ID, ALIAS_ACTION).flatMap { rawActionId ->
            ActionId.of(rawActionId).fold(
                onLeft = { domainError ->
                    Either.Left(
                        Nel.of(
                            ParserError.InvalidPropertyValue(
                                path = reader.path,
                                property = KEY_ACTION_ID,
                                reason = "Invalid action identifier: '${domainError.raw}'",
                                domainError = domainError
                            )
                        )
                    )
                },
                onRight = { Either.Right(it) }
            )
        }
    }

    companion object {
        val SUPPORTED_TYPES: Set<String> = setOf(PRIMARY_TYPE) + ALIASES
    }
}
