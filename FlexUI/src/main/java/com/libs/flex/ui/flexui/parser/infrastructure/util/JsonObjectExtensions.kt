package com.libs.flex.ui.flexui.parser.infrastructure.util

import com.libs.flex.ui.flexui.exceptions.MissingPropertyException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

/**
 * Extension functions for JsonObject to simplify property extraction and reduce code duplication.
 */

/**
 * Extracts a required string property from JsonObject.
 *
 * @param key The property key
 * @param componentType The component type for error messaging
 * @return The string value
 * @throws MissingPropertyException if the property is missing
 */
fun JsonObject.getRequiredString(key: String, componentType: String): String {
    return this[key]?.jsonPrimitive?.content
        ?: throw MissingPropertyException(key, componentType)
}

/**
 * Extracts an optional string property from JsonObject.
 *
 * @param key The property key
 * @return The string value or null if not present
 */
fun JsonObject.getOptionalString(key: String): String? {
    return this[key]?.jsonPrimitive?.content
}

/**
 * Extracts an optional integer property from JsonObject.
 *
 * @param key The property key
 * @return The integer value or null if not present
 */
fun JsonObject.getOptionalInt(key: String): Int? {
    return this[key]?.jsonPrimitive?.intOrNull
}

/**
 * Extracts an optional float property from JsonObject.
 *
 * @param key The property key
 * @return The float value or null if not present
 */
fun JsonObject.getOptionalFloat(key: String): Float? {
    return this[key]?.jsonPrimitive?.floatOrNull
}

/**
 * Extracts an optional long property from JsonObject.
 *
 * @param key The property key
 * @return The long value or null if not present
 */
fun JsonObject.getOptionalLong(key: String): Long? {
    return this[key]?.jsonPrimitive?.longOrNull
}

/**
 * Extracts an optional boolean property from JsonObject.
 *
 * @param key The property key
 * @param defaultValue The default value if not present
 * @return The boolean value or default if not present
 */
fun JsonObject.getOptionalBoolean(key: String, defaultValue: Boolean = false): Boolean {
    return this[key]?.jsonPrimitive?.booleanOrNull ?: defaultValue
}

/**
 * Extracts an optional JsonObject property.
 *
 * @param key The property key
 * @return The JsonObject or null if not present
 */
fun JsonObject.getOptionalObject(key: String): JsonObject? {
    return this[key]?.jsonObject
}

/**
 * Extracts an optional JsonArray property.
 *
 * @param key The property key
 * @return The JsonArray or null if not present
 */
fun JsonObject.getOptionalArray(key: String): JsonArray? {
    return this[key]?.jsonArray
}

/**
 * Extracts an optional JsonElement property.
 *
 * @param key The property key
 * @return The JsonElement or null if not present
 */
fun JsonObject.getOptionalElement(key: String): JsonElement? {
    return this[key]
}

/**
 * Extracts an optional boolean property from JsonObject, returning null if not present.
 *
 * @param key The property key
 * @return The boolean value or null if not present
 */
fun JsonObject.getOptionalBooleanOrNull(key: String): Boolean? {
    return this[key]?.jsonPrimitive?.booleanOrNull
}
