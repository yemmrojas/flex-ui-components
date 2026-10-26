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
import com.libs.flex.ui.parser.internal.model.CommonProperties

/**
 * Contrato abstracto para la lectura defensiva y tipada de propiedades de componentes (DIP e ISP).
 *
 * Desacopla los mapeadores ([com.libs.flex.ui.parser.internal.mapper.NodeMapper]) del formato técnico
 * crudo (JSON, YAML, Map, etc.) y facilita pruebas unitarias basadas en fakes puros en memoria.
 */
internal interface PropertyReader {

    /**
     * Ruta actual en el documento correspondiente al nodo que se está leyendo.
     */
    val path: String

    /**
     * Lee una cadena de texto requerida. Falla con [ParserError.MissingRequiredProperty] si no existe,
     * o con [ParserError.InvalidPropertyValue] si no es un tipo de texto válido.
     */
    fun requiredString(primaryKey: String, vararg aliases: String): Either<Nel<ParserError>, String>

    /**
     * Lee una cadena de texto opcional.
     */
    fun optionalString(
        primaryKey: String,
        vararg aliases: String,
        default: String? = null
    ): Either<Nel<ParserError>, String?>

    /**
     * Lee un booleano opcional. Convierte de forma segura primitivos booleanos o cadenas "true"/"false".
     */
    fun optionalBoolean(
        primaryKey: String,
        vararg aliases: String,
        default: Boolean = false
    ): Either<Nel<ParserError>, Boolean>

    /**
     * Lee un entero opcional.
     */
    fun optionalInt(
        primaryKey: String,
        vararg aliases: String,
        default: Int? = null
    ): Either<Nel<ParserError>, Int?>

    /**
     * Lee un número flotante opcional.
     */
    fun optionalFloat(
        primaryKey: String,
        vararg aliases: String,
        default: Float? = null
    ): Either<Nel<ParserError>, Float?>

    /**
     * Lee un color hexadecimal opcional validándolo contra [HexColor].
     */
    fun optionalColor(
        primaryKey: String,
        vararg aliases: String,
        default: HexColor? = null
    ): Either<Nel<ParserError>, HexColor?>

    /**
     * Lee una dimensión en Dp opcional validándola contra [Dimension.Dp].
     */
    fun optionalDimensionDp(
        primaryKey: String,
        vararg aliases: String,
        default: Dimension.Dp? = null
    ): Either<Nel<ParserError>, Dimension.Dp?>

    /**
     * Lee una dimensión genérica (MatchParent, WrapContent o Dp numérico).
     */
    fun optionalDimension(
        primaryKey: String,
        vararg aliases: String,
        default: Dimension? = null
    ): Either<Nel<ParserError>, Dimension?>

    /**
     * Extrae las propiedades comunes del componente: identificador saneado y estilos.
     */
    fun readCommonProperties(): Either<Nel<ParserError>, CommonProperties>

    /**
     * Lee un tamaño de fuente tipado [FontSize] opcional.
     */
    fun optionalFontSize(
        primaryKey: String = "fontSize",
        vararg aliases: String
    ): Either<Nel<ParserError>, FontSize?>

    /**
     * Lee una lista de reglas de validación [ValidationRule].
     */
    fun readValidationRules(
        primaryKey: String = "validationRules",
        vararg aliases: String = arrayOf("validations", "rules")
    ): Either<Nel<ParserError>, List<ValidationRule>>

    /**
     * Lee una lista no vacía de opciones [SelectOption].
     */
    fun readSelectOptions(
        primaryKey: String = "options",
        vararg aliases: String = arrayOf("items")
    ): Either<Nel<ParserError>, Nel<SelectOption>>

    /**
     * Lee un rango numérico [NumberRange] a partir de min, max y step.
     */
    fun readNumberRange(
        minKey: String = "min",
        maxKey: String = "max",
        stepKey: String = "step",
        defaultMin: Double = 0.0,
        defaultMax: Double = 100.0,
        defaultStep: Double = 1.0
    ): Either<Nel<ParserError>, NumberRange>
}
