package com.libs.flex.ui.parser.internal.reader

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.model.node.SelectOption
import com.libs.flex.ui.core.domain.validation.ValidationRule
import com.libs.flex.ui.core.domain.value.Dimension
import com.libs.flex.ui.core.domain.value.FontSize
import com.libs.flex.ui.core.domain.value.HexColor
import com.libs.flex.ui.core.domain.value.NumberRange
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserWarning
import com.libs.flex.ui.parser.internal.mapper.validation.ValidationRuleMapper
import com.libs.flex.ui.parser.internal.model.ChildrenResult
import com.libs.flex.ui.parser.internal.model.CommonProperties
import com.libs.flex.ui.parser.internal.model.MappedNode
import com.libs.flex.ui.parser.internal.scope.MappingScope
import com.libs.flex.ui.parser.internal.util.kindName
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull

/**
 * Implementación coordinada y desacoplada de [PropertyReader] que opera sobre [JsonObject].
 *
 * Sigue el principio de Inversión de Dependencias (DIP) delegando a contratos abstractos:
 * - [DomainValueReader]: Colores, Dimensiones, FontSize y NumberRange.
 * - [StyleReader]: Estilos comunes y PaddingValues.
 * - [SelectOptionsReader]: Opciones de selección.
 * - [ChildrenReader]: Jerarquías de nodos hijos y templates.
 */
internal class JsonPropertyReader(
    val json: JsonObject,
    override val path: String,
    val policy: ErrorPolicy,
    private val domainValueReader: DomainValueReader = DefaultDomainValueReader(),
    private val styleReader: StyleReader = DefaultStyleReader(),
    private val selectOptionsReader: SelectOptionsReader = DefaultSelectOptionsReader(),
    private val childrenReader: ChildrenReader = DefaultChildrenReader(),
    val onWarn: (ParserWarning) -> Unit = {}
) : PropertyReader {

    constructor(
        json: JsonObject,
        path: String,
        policy: ErrorPolicy,
        onWarn: (ParserWarning) -> Unit
    ) : this(
        json = json,
        path = path,
        policy = policy,
        domainValueReader = DefaultDomainValueReader(),
        styleReader = DefaultStyleReader(),
        selectOptionsReader = DefaultSelectOptionsReader(),
        childrenReader = DefaultChildrenReader(),
        onWarn = onWarn
    )


    // =========================================================================
    // 1. Lecturas Primitivas
    // =========================================================================

    override fun requiredString(primaryKey: String, vararg aliases: String): Either<Nel<ParserError>, String> {
        val entry = findElement(primaryKey, *aliases)
            ?: return Either.Left(Nel.of(ParserError.MissingRequiredProperty(path, primaryKey)))

        val content = (entry.second as? JsonPrimitive)?.contentOrNull
        return if (content != null) {
            Either.Right(content)
        } else {
            Either.Left(
                Nel.of(
                    ParserError.InvalidPropertyValue(
                        path = path,
                        property = entry.first,
                        reason = "Expected string primitive, but got ${entry.second.kindName()}"
                    )
                )
            )
        }
    }

    override fun optionalString(
        primaryKey: String,
        vararg aliases: String,
        default: String?
    ): Either<Nel<ParserError>, String?> = readOptionalPrimitive(
        primaryKey = primaryKey,
        aliases = aliases,
        default = default,
        expectedType = "string primitive"
    ) { it.contentOrNull }

    override fun optionalBoolean(
        primaryKey: String,
        vararg aliases: String,
        default: Boolean
    ): Either<Nel<ParserError>, Boolean> = readOptionalPrimitive(
        primaryKey = primaryKey,
        aliases = aliases,
        default = default,
        expectedType = "boolean"
    ) { primitive ->
        primitive.booleanOrNull ?: primitive.contentOrNull?.toBooleanStrictOrNull()
    }.map { it ?: default }

    override fun optionalInt(
        primaryKey: String,
        vararg aliases: String,
        default: Int?
    ): Either<Nel<ParserError>, Int?> = readOptionalPrimitive(
        primaryKey = primaryKey,
        aliases = aliases,
        default = default,
        expectedType = "integer"
    ) { primitive ->
        primitive.intOrNull ?: primitive.contentOrNull?.toIntOrNull()
    }

    override fun optionalFloat(
        primaryKey: String,
        vararg aliases: String,
        default: Float?
    ): Either<Nel<ParserError>, Float?> = readOptionalPrimitive(
        primaryKey = primaryKey,
        aliases = aliases,
        default = default,
        expectedType = "finite float"
    ) { primitive ->
        val value = primitive.floatOrNull ?: primitive.contentOrNull?.toFloatOrNull()
        if (value != null && value.isFinite()) value else null
    }

    override fun optionalListSize(primaryKey: String, vararg aliases: String): Int? {
        val entry = findElement(primaryKey, *aliases) ?: return null
        return (entry.second as? JsonArray)?.size
    }

    override fun optionalStringList(primaryKey: String, vararg aliases: String): List<String> {
        val entry = findElement(primaryKey, *aliases) ?: return emptyList()
        val array = entry.second as? JsonArray ?: return emptyList()
        return array.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
    }

    // =========================================================================
    // 2. Delegación a Contratos Abstractos
    // =========================================================================

    override fun optionalColor(
        primaryKey: String,
        vararg aliases: String,
        default: HexColor?
    ): Either<Nel<ParserError>, HexColor?> = domainValueReader.readColor(
        entry = findElement(primaryKey, *aliases),
        path = path,
        policy = policy,
        onWarn = onWarn,
        default = default
    )

    override fun optionalDimensionDp(
        primaryKey: String,
        vararg aliases: String,
        default: Dimension.Dp?
    ): Either<Nel<ParserError>, Dimension.Dp?> = domainValueReader.readDimensionDp(
        entry = findElement(primaryKey, *aliases),
        path = path,
        policy = policy,
        onWarn = onWarn,
        default = default
    )

    override fun optionalFontSize(
        primaryKey: String,
        vararg aliases: String
    ): Either<Nel<ParserError>, FontSize?> = domainValueReader.readFontSize(
        entry = findElement(primaryKey, *aliases),
        path = path,
        policy = policy,
        onWarn = onWarn
    )

    override fun optionalDimension(
        primaryKey: String,
        vararg aliases: String,
        default: Dimension?
    ): Either<Nel<ParserError>, Dimension?> = domainValueReader.readDimension(
        entry = findElement(primaryKey, *aliases),
        path = path,
        policy = policy,
        onWarn = onWarn,
        default = default
    )

    override fun readNumberRange(
        minKey: String,
        maxKey: String,
        stepKey: String,
        defaultMin: Double,
        defaultMax: Double,
        defaultStep: Double
    ): Either<Nel<ParserError>, NumberRange> = domainValueReader.readNumberRange(
        json = json,
        path = path,
        policy = policy,
        onWarn = onWarn,
        minKey = minKey,
        maxKey = maxKey,
        stepKey = stepKey,
        defaultMin = defaultMin,
        defaultMax = defaultMax,
        defaultStep = defaultStep
    )

    override fun readCommonProperties(): Either<Nel<ParserError>, CommonProperties> =
        styleReader.readCommonProperties(json, path, policy, onWarn)

    override fun readValidationRules(
        primaryKey: String,
        vararg aliases: String
    ): Either<Nel<ParserError>, List<ValidationRule>> {
        val entry = findElement(primaryKey, *aliases) ?: return Either.Right(emptyList())
        return ValidationRuleMapper.mapRules(entry.second, "$path.${entry.first}", policy, onWarn)
    }

    override fun readSelectOptions(
        primaryKey: String,
        vararg aliases: String
    ): Either<Nel<ParserError>, Nel<SelectOption>> = selectOptionsReader.readSelectOptions(
        entry = findElement(primaryKey, *aliases),
        path = path,
        policy = policy,
        onWarn = onWarn
    )

    override fun readChildren(
        scope: MappingScope,
        primaryKey: String,
        vararg aliases: String
    ): Either<Nel<ParserError>, ChildrenResult> = childrenReader.readChildren(
        entry = findElement(primaryKey, *aliases),
        path = path,
        scope = scope,
        policy = policy,
        onWarn = onWarn
    )

    override fun readChild(
        scope: MappingScope,
        primaryKey: String,
        vararg aliases: String
    ): Either<Nel<ParserError>, MappedNode?> = childrenReader.readChild(
        entry = findElement(primaryKey, *aliases),
        scope = scope
    )

    // =========================================================================
    // 3. Utilidades Internas
    // =========================================================================

    internal fun findElement(primaryKey: String, vararg aliases: String): Pair<String, JsonElement>? {
        json[primaryKey]?.let { return Pair(primaryKey, it) }
        aliases.forEach { alias ->
            json[alias]?.let { return Pair(alias, it) }
        }
        return null
    }

    private inline fun <T> readOptionalPrimitive(
        primaryKey: String,
        aliases: Array<out String>,
        default: T?,
        expectedType: String,
        convert: (JsonPrimitive) -> T?
    ): Either<Nel<ParserError>, T?> {
        val entry = findElement(primaryKey, *aliases) ?: return Either.Right(default)
        val primitive = entry.second as? JsonPrimitive

        if (primitive == null) {
            val error = ParserError.InvalidPropertyValue(path, entry.first, "Expected $expectedType")
            return policy.handleOptionalError(path, entry.first, error, default, onWarn)
        }

        val value = convert(primitive)
        if (value == null) {
            val error = ParserError.InvalidPropertyValue(
                path = path,
                property = entry.first,
                reason = "Cannot convert '${primitive.contentOrNull}' to $expectedType"
            )
            return policy.handleOptionalError(path, entry.first, error, default, onWarn)
        }

        return Either.Right(value)
    }
}
