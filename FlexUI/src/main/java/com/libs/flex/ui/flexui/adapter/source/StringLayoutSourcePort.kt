package com.libs.flex.ui.flexui.adapter.source

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.model.LayoutDocument
import com.libs.flex.ui.core.domain.ports.LayoutSourcePort
import com.libs.flex.ui.flexui.model.FlexUiError

/**
 * Presentation port extending [LayoutSourcePort] with the capability
 * to ingest and parse layouts directly from raw string payloads.
 */
interface StringLayoutSourcePort : LayoutSourcePort {

    /**
     * Parses the given [source] string into a domain [LayoutDocument].
     */
    suspend fun load(source: String): Either<FlexUiError.InvalidJson, LayoutDocument>
}

