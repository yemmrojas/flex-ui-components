package com.libs.flex.ui.parser.internal.util

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

/**
 * Retorna un nombre legible y estable ante minificación para el tipo de elemento JSON.
 */
internal fun JsonElement.kindName(): String = when (this) {
    is JsonObject -> "object"
    is JsonArray -> "array"
    is JsonNull -> "null"
    is JsonPrimitive -> when {
        isString -> "string"
        booleanOrNull != null -> "boolean"
        else -> "number"
    }
}
