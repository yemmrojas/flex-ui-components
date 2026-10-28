package com.libs.flex.ui.parser.internal.mapper.atomic

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.functional.zip
import com.libs.flex.ui.core.domain.model.node.ButtonNode
import com.libs.flex.ui.core.domain.value.ActionId
import com.libs.flex.ui.core.domain.value.ButtonVariant
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.internal.mapper.NodeMapper
import com.libs.flex.ui.parser.internal.model.MappedNode
import com.libs.flex.ui.parser.internal.reader.PropertyReader
import com.libs.flex.ui.parser.internal.scope.MappingScope
import com.libs.flex.ui.parser.internal.util.Quad
import com.libs.flex.ui.parser.internal.util.Quint

/**
 * Mapeador atómico para botones interactivos ([ButtonNode]).
 *
 * Soporta el tipo oficial `flexButton` y los aliases heredados `button` y `componentButton`.
 */
internal class ButtonNodeMapper : NodeMapper {

    override val types: Set<String> = setOf("flexButton", "button", "componentButton")

    override fun map(
        reader: PropertyReader,
        scope: MappingScope
    ): Either<Nel<ParserError>, MappedNode> {
        val commonResult = reader.readCommonProperties()
        val textResult = reader.requiredString("text", "label")
        val actionIdResult = reader.requiredString("actionId", "action").flatMap { rawAction ->
            ActionId.of(rawAction).fold(
                onLeft = { domainError ->
                    Either.Left(
                        Nel.of(
                            ParserError.InvalidPropertyValue(
                                path = reader.path,
                                property = "actionId",
                                reason = "Invalid identifier: '${domainError.raw}'",
                                domainError = domainError
                            )
                        )
                    )
                },
                onRight = { Either.Right(it) }
            )
        }
        val variantResult = reader.optionalString("buttonVariant", "buttonStyle", "variant", default = "primary")
        val isEnabledResult = reader.optionalBoolean("isEnabled", "enabled", default = true)
        val requiresValidationResult = reader.optionalBoolean("requiresValidation", default = false)

        return commonResult
            .zip(textResult) { c, t -> Pair(c, t) }
            .zip(actionIdResult) { (c, t), a -> Triple(c, t, a) }
            .zip(variantResult) { (c, t, a), v -> Quad(c, t, a, parseVariant(v)) }
            .zip(isEnabledResult) { (c, t, a, v), e -> Quint(c, t, a, v, e) }
            .zip(requiresValidationResult) { (c, t, a, v, e), r ->
                MappedNode(
                    ButtonNode(
                        id = c.id,
                        style = c.style,
                        text = t,
                        actionId = a,
                        buttonVariant = v,
                        isEnabled = e,
                        requiresValidation = r
                    )
                )
            }
    }

    private fun parseVariant(raw: String?): ButtonVariant = when (raw?.lowercase()) {
        "secondary" -> ButtonVariant.Secondary
        "outlined", "outline" -> ButtonVariant.Outlined
        "text" -> ButtonVariant.Text
        else -> ButtonVariant.Primary
    }
}

