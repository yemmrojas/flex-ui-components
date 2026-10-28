package com.libs.flex.ui.parser.internal.envelope

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.value.SchemaVersion
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserOptions
import com.libs.flex.ui.parser.ParserWarning
import com.libs.flex.ui.parser.WarningCode
import com.libs.flex.ui.parser.internal.util.kindName
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Datos extraídos de la envoltura del documento JSON.
 */
internal data class EnvelopeData(
    val rootJson: JsonObject,
    val schemaVersion: SchemaVersion,
    val rootPath: String,
    val warnings: List<ParserWarning>
)

/**
 * Analizador semántico de la envoltura del documento JSON.
 *
 * Determina si el payload es un documento estructurado (con clave `root`) o un nodo directo,
 * valida el tipo y coherencia de `schemaVersion`, y previene ambigüedades entre `root` y `rootNode`.
 */
internal class DocumentEnvelope {

    private val keyRoot = "root"
    private val keyRootNode = "rootNode"
    private val keySchemaVersion = "schemaVersion"
    private val keyTemplates = "templates"

    fun extract(
        rootElement: JsonObject,
        options: ParserOptions
    ): Either<Nel<ParserError>, EnvelopeData> {
        val hasRoot = rootElement.containsKey(keyRoot)
        val hasRootNode = rootElement.containsKey(keyRootNode)
        val hasSchemaVersion = rootElement.containsKey(keySchemaVersion)
        val hasTemplates = rootElement.containsKey(keyTemplates)

        // 1. Error si vienen 'root' y 'rootNode' simultáneamente
        if (hasRoot && hasRootNode) {
            return Either.Left(
                Nel.of(
                    ParserError.InvalidPropertyValue(
                        path = "$",
                        property = keyRoot,
                        reason = "Ambiguous root definition: both '$keyRoot' and '$keyRootNode' are present"
                    )
                )
            )
        }

        // 2. Si declara schemaVersion o templates pero no tiene root/rootNode: envoltura mal formada
        if ((hasSchemaVersion || hasTemplates) && !hasRoot && !hasRootNode) {
            return Either.Left(
                Nel.of(
                    ParserError.MissingRequiredProperty(
                        path = "$",
                        property = keyRoot
                    )
                )
            )
        }

        // 3. Validar tipo de root o rootNode si existen
        if (hasRoot && rootElement[keyRoot] !is JsonObject) {
            return Either.Left(
                Nel.of(
                    ParserError.InvalidPropertyValue(
                        path = "$",
                        property = keyRoot,
                        reason = "Expected JSON object for '$keyRoot', but found ${rootElement[keyRoot]?.kindName()}"
                    )
                )
            )
        }

        if (hasRootNode && rootElement[keyRootNode] !is JsonObject) {
            return Either.Left(
                Nel.of(
                    ParserError.InvalidPropertyValue(
                        path = "$",
                        property = keyRootNode,
                        reason = "Expected JSON object for '$keyRootNode', but found ${rootElement[keyRootNode]?.kindName()}"
                    )
                )
            )
        }

        // 4. Validar schemaVersion si está presente
        val schemaVersion: SchemaVersion
        if (hasSchemaVersion) {
            val versionElem = rootElement[keySchemaVersion]
            if (versionElem !is JsonPrimitive || !versionElem.isString) {
                return Either.Left(
                    Nel.of(
                        ParserError.InvalidPropertyValue(
                            path = "$",
                            property = keySchemaVersion,
                            reason = "Expected string for '$keySchemaVersion', but found ${versionElem?.kindName()}"
                        )
                    )
                )
            }
            schemaVersion = SchemaVersion(versionElem.content)
        } else {
            schemaVersion = options.defaultSchemaVersion
        }

        val warnings = mutableListOf<ParserWarning>()

        // 5. Determinar nodo raíz y su ruta JSONPath
        val structuredRoot = (rootElement[keyRoot] as? JsonObject) ?: (rootElement[keyRootNode] as? JsonObject)

        return if (structuredRoot != null) {
            val path = if (hasRoot) "$.$keyRoot" else {
                warnings.add(
                    ParserWarning(
                        path = "$.$keyRootNode",
                        code = WarningCode.LEGACY_ROOT_KEY,
                        message = "Found legacy '$keyRootNode' key; prefer '$keyRoot'"
                    )
                )
                "$.$keyRootNode"
            }
            Either.Right(EnvelopeData(structuredRoot, schemaVersion, path, warnings))
        } else {
            // Modo nodo directo
            Either.Right(EnvelopeData(rootElement, schemaVersion, "$", warnings))
        }
    }
}
