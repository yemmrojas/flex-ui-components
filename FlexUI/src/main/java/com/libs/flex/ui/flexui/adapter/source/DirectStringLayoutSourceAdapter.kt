package com.libs.flex.ui.flexui.adapter.source

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.model.LayoutDocument
import com.libs.flex.ui.core.domain.ports.LayoutRequest
import com.libs.flex.ui.core.domain.ports.LayoutSourcePort
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.flexui.model.FlexUiError
import com.libs.flex.ui.parser.LayoutParser
import com.libs.flex.ui.parser.ParserOptions

/**
 * Adapter implementing [StringLayoutSourcePort] that encapsulates JSON deserialization via [LayoutParser].
 *
 * Responsibilities are strictly limited to parsing layout documents and caching
 * the most recently loaded layout for domain use cases.
 *
 * @property jsonString Optional pre-seeded raw JSON schema.
 * @property parser The parser facade from :flexui-parser.
 * @property options Parser options (mode, limits, schema version).
 */
class DirectStringLayoutSourceAdapter(
    private var jsonString: String = "",
    private val parser: LayoutParser,
    private val options: ParserOptions = ParserOptions.DEFAULT
) : StringLayoutSourcePort {

    @Volatile
    private var cachedDocument: LayoutDocument? = null

    /**
     * Ingests and parses the given [source] string into a [LayoutDocument],
     * caching it for subsequent domain requests.
     */
    override suspend fun load(source: String): Either<FlexUiError.InvalidJson, LayoutDocument> {
        this.jsonString = source
        return when (val parseResult = parser.parse(source, options)) {
            is Either.Left -> {
                val head = parseResult.value.head
                Either.Left(FlexUiError.InvalidJson(message = head.message, path = head.path))
            }
            is Either.Right -> {
                val doc = parseResult.value.document
                cachedDocument = doc
                Either.Right(doc)
            }
        }
    }

    /**
     * Backward-compatible convenience alias for [load].
     */
    suspend fun loadFromJson(jsonString: String): Either<FlexUiError.InvalidJson, LayoutDocument> =
        load(jsonString)

    override suspend fun getLayout(request: LayoutRequest): Either<Nel<DomainError.Structural>, LayoutDocument> {
        val cached = cachedDocument
        if (cached != null) {
            return Either.Right(cached)
        }

        if (jsonString.isNotBlank()) {
            return when (val result = load(jsonString)) {
                is Either.Left -> {
                    val fallbackId = ComponentId.of("root").getOrNull()!!
                    val structuralError = DomainError.Structural.InvalidComponentReference(
                        id = fallbackId,
                        referenceId = ComponentId.of("malformed_payload").getOrNull()!!
                    )
                    Either.Left(Nel.of(structuralError))
                }
                is Either.Right -> Either.Right(result.value)
            }
        }

        val fallbackId = ComponentId.of("root").getOrNull()!!
        return Either.Left(
            Nel.of(
                DomainError.Structural.InvalidComponentReference(
                    id = fallbackId,
                    referenceId = ComponentId.of("not_found").getOrNull()!!
                )
            )
        )
    }
}
