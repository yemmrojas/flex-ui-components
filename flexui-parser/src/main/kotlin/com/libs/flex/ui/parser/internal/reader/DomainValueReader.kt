package com.libs.flex.ui.parser.internal.reader

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.value.Dimension
import com.libs.flex.ui.core.domain.value.FontSize
import com.libs.flex.ui.core.domain.value.HexColor
import com.libs.flex.ui.core.domain.value.NumberRange
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserWarning
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * Contrato abstracto para la deserialización y validación de Value Objects del Core
 * ([HexColor], [Dimension], [Dimension.Dp], [FontSize] y [NumberRange]).
 */
internal interface DomainValueReader {

    fun readColor(
        entry: Pair<String, JsonElement>?,
        path: String,
        policy: ErrorPolicy,
        onWarn: (ParserWarning) -> Unit,
        default: HexColor?
    ): Either<Nel<ParserError>, HexColor?>

    fun readDimensionDp(
        entry: Pair<String, JsonElement>?,
        path: String,
        policy: ErrorPolicy,
        onWarn: (ParserWarning) -> Unit,
        default: Dimension.Dp?
    ): Either<Nel<ParserError>, Dimension.Dp?>

    fun readFontSize(
        entry: Pair<String, JsonElement>?,
        path: String,
        policy: ErrorPolicy,
        onWarn: (ParserWarning) -> Unit
    ): Either<Nel<ParserError>, FontSize?>

    fun readDimension(
        entry: Pair<String, JsonElement>?,
        path: String,
        policy: ErrorPolicy,
        onWarn: (ParserWarning) -> Unit,
        default: Dimension?
    ): Either<Nel<ParserError>, Dimension?>

    fun readNumberRange(
        json: JsonObject,
        path: String,
        policy: ErrorPolicy,
        onWarn: (ParserWarning) -> Unit,
        minKey: String,
        maxKey: String,
        stepKey: String,
        defaultMin: Double,
        defaultMax: Double,
        defaultStep: Double
    ): Either<Nel<ParserError>, NumberRange>
}
