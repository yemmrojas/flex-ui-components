package com.libs.flex.ui.parser.internal.guard

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserOptions
import com.libs.flex.ui.parser.internal.util.kindName
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * Guardián de frontera responsable de:
 * 1. Validar el tamaño en bytes del payload JSON de forma eficiente.
 * 2. Pre-escanear la profundidad de anidación para evitar StackOverflowError.
 * 3. Parsear el string a [JsonObject] capturando excepciones de serialización.
 */
internal class PayloadGuard(
    private val json: Json = Json
) {

    fun validateAndParse(
        jsonString: String,
        options: ParserOptions
    ): Either<Nel<ParserError>, JsonObject> {
        val len = jsonString.length.toLong()

        // Cota superior: si la longitud de caracteres ya supera el máximo en bytes, excede seguro
        if (len > options.maxPayloadBytes) {
            return Either.Left(
                Nel.of(
                    ParserError.PayloadTooLarge(
                        sizeBytes = len,
                        maxBytes = options.maxPayloadBytes
                    )
                )
            )
        }

        // Medir exactamente en UTF-8 solo si UTF-16 chars * 3 puede exceder el límite
        if (len * 3L > options.maxPayloadBytes) {
            val payloadBytes = jsonString.toByteArray(Charsets.UTF_8).size.toLong()
            if (payloadBytes > options.maxPayloadBytes) {
                return Either.Left(
                    Nel.of(
                        ParserError.PayloadTooLarge(
                            sizeBytes = payloadBytes,
                            maxBytes = options.maxPayloadBytes
                        )
                    )
                )
            }
        }

        // Pre-escaneo lineal O(N) para prevenir anidaciones extremas antes de la deserialización recursiva
        val depthError = preScanMaxDepth(jsonString, options.maxDepth)
        if (depthError != null) {
            return Either.Left(Nel.of(depthError))
        }

        val rootElement = try {
            json.parseToJsonElement(jsonString)
        } catch (e: StackOverflowError) {
            return Either.Left(
                Nel.of(
                    ParserError.MaxDepthExceeded(
                        path = "$",
                        currentDepth = options.maxDepth + 1,
                        maxDepth = options.maxDepth
                    )
                )
            )
        } catch (e: SerializationException) {
            return Either.Left(
                Nel.of(
                    ParserError.InvalidJson(
                        path = "$",
                        reason = e.message ?: "Invalid JSON syntax"
                    )
                )
            )
        } catch (e: IllegalArgumentException) {
            return Either.Left(
                Nel.of(
                    ParserError.InvalidJson(
                        path = "$",
                        reason = e.message ?: "Illegal JSON argument"
                    )
                )
            )
        } catch (e: Exception) {
            return Either.Left(
                Nel.of(
                    ParserError.InvalidJson(
                        path = "$",
                        reason = e.message ?: "Unexpected error during JSON parsing"
                    )
                )
            )
        }

        if (rootElement !is JsonObject) {
            return Either.Left(
                Nel.of(
                    ParserError.InvalidJson(
                        path = "$",
                        reason = "Root JSON payload must be an object, but found ${rootElement.kindName()}"
                    )
                )
            )
        }

        return Either.Right(rootElement)
    }

    private fun preScanMaxDepth(jsonString: String, maxDepth: Int): ParserError.MaxDepthExceeded? {
        var depth = 0
        var inString = false
        var escape = false
        val safetyThreshold = maxOf(maxDepth * 3 + 20, 200)

        for (i in 0 until jsonString.length) {
            val c = jsonString[i]
            if (escape) {
                escape = false
                continue
            }
            if (c == '\\' && inString) {
                escape = true
                continue
            }
            if (c == '"') {
                inString = !inString
                continue
            }
            if (!inString) {
                if (c == '{' || c == '[') {
                    depth++
                    if (depth > safetyThreshold) {
                        return ParserError.MaxDepthExceeded(
                            path = "$",
                            currentDepth = depth,
                            maxDepth = maxDepth
                        )
                    }
                } else if (c == '}' || c == ']') {
                    if (depth > 0) depth--
                }
            }
        }
        return null
    }
}
