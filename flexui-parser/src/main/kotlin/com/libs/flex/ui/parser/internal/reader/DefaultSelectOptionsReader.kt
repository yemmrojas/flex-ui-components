package com.libs.flex.ui.parser.internal.reader

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.model.node.SelectOption
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserWarning
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * Implementación predeterminada e inmutable de [SelectOptionsReader].
 */
internal class DefaultSelectOptionsReader : SelectOptionsReader {

    override fun readSelectOptions(
        entry: Pair<String, JsonElement>?,
        path: String,
        policy: ErrorPolicy,
        onWarn: (ParserWarning) -> Unit
    ): Either<Nel<ParserError>, Nel<SelectOption>> {
        if (entry == null) {
            return Either.Left(Nel.of(ParserError.MissingRequiredProperty(path, "options")))
        }

        val array = entry.second as? JsonArray
            ?: return Either.Left(Nel.of(ParserError.InvalidPropertyValue(path, entry.first, "Expected JSON array of options")))

        if (array.isEmpty()) {
            return Either.Left(Nel.of(ParserError.InvalidPropertyValue(path, entry.first, "Options array cannot be empty")))
        }

        val options = mutableListOf<SelectOption>()
        val errors = mutableListOf<ParserError>()

        array.forEachIndexed { index, optionElement ->
            val optPath = "$path.${entry.first}[$index]"
            if (optionElement !is JsonObject) {
                errors.add(ParserError.InvalidPropertyValue(optPath, "option", "Expected JSON object for option"))
                return@forEachIndexed
            }
            val optReader = JsonPropertyReader(optionElement, optPath, policy, onWarn)
            val valRes = optReader.requiredString("value", "id")
            val labelRes = optReader.optionalString("label", "text", "title")

            if (valRes is Either.Left) {
                errors.addAll(valRes.value.toList())
            }
            if (labelRes is Either.Left) {
                errors.addAll(labelRes.value.toList())
            }
            if (valRes is Either.Right && labelRes is Either.Right) {
                options.add(
                    SelectOption(
                        value = valRes.value,
                        label = labelRes.value ?: valRes.value
                    )
                )
            }
        }

        return if (errors.isNotEmpty()) {
            Either.Left(Nel.fromListOrNull(errors)!!)
        } else {
            val nel = Nel.fromListOrNull(options)
            if (nel != null) Either.Right(nel)
            else Either.Left(Nel.of(ParserError.InvalidPropertyValue(path, entry.first, "Options list cannot be empty")))
        }
    }
}
