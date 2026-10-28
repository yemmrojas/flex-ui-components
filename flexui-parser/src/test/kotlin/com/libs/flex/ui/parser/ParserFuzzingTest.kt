package com.libs.flex.ui.parser

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.model.node.CheckNode
import com.libs.flex.ui.core.domain.model.node.ScrollLayoutNode
import com.libs.flex.ui.core.domain.model.node.TextNode
import com.libs.flex.ui.core.domain.model.node.UnsupportedFallbackNode
import com.libs.flex.ui.core.domain.model.node.VerticalLayoutNode
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Pruebas de resiliencia, robustez, protección contra DoS y fuzzing para el parser ([LayoutParser]).
 *
 * Valida los 4 vectores principales de ataque y corrupción:
 * 1. Payloads masivos (> 10 MB) rechazados inmediatamente por tamaño UTF-8 ([ParserError.PayloadTooLarge]).
 * 2. JSONs truncados y sintaxis corrupta gestionados funcionalmente sin excepciones ([ParserError.InvalidJson]).
 * 3. Anidación profunda extrema (50.000 niveles) cortada en tiempo lineal $O(N)$ sin [StackOverflowError] ([ParserError.MaxDepthExceeded]).
 * 4. Mutaciones de tipos (arrays por strings, strings por números) con rechazo estricto en STRICT y recuperación segura en TOLERANT.
 */
class ParserFuzzingTest {

    // === MÉTODOS PROVEEDORES (Object Mother Pattern) ===

    private fun givenParser(): LayoutParser = LayoutParserFactory.create()

    private fun givenStrictOptions(
        maxPayloadBytes: Long = ParserOptions.DEFAULT_MAX_PAYLOAD_BYTES,
        maxDepth: Int = 20
    ): ParserOptions = ParserOptions(
        mode = ParserMode.STRICT,
        maxPayloadBytes = maxPayloadBytes,
        maxDepth = maxDepth
    )

    private fun givenTolerantOptions(
        maxPayloadBytes: Long = ParserOptions.DEFAULT_MAX_PAYLOAD_BYTES,
        maxDepth: Int = 20
    ): ParserOptions = ParserOptions(
        mode = ParserMode.TOLERANT,
        maxPayloadBytes = maxPayloadBytes,
        maxDepth = maxDepth
    )

    // =========================================================================
    // 1. Payloads Mayores a 10 MB (Protección contra Saturación de Memoria / DoS)
    // =========================================================================

    @Test
    fun `payload exceeding 10 MB default limit is rejected with PayloadTooLarge error`() {
        // Arrange: tamaño > 10 MB (10 * 1024 * 1024 + 1024 bytes)
        val parser = givenParser()
        val tenMbPlusBytes = (10 * 1024 * 1024) + 1024
        val largePadding = "x".repeat(tenMbPlusBytes)
        val json = """{"id":"text_1","type":"flexText","text":"$largePadding"}"""

        // Act
        val result = parser.parse(json, ParserOptions.DEFAULT)

        // Assert
        val left = assertIs<Either.Left<Nel<ParserError>>>(result)
        val error = assertIs<ParserError.PayloadTooLarge>(left.value.head)
        assertTrue(error.sizeBytes >= tenMbPlusBytes)
        assertEquals(ParserOptions.DEFAULT_MAX_PAYLOAD_BYTES, error.maxBytes)
    }

    @Test
    fun `payload exceeding custom maxPayloadBytes is rejected immediately`() {
        // Arrange
        val parser = givenParser()
        val limit = 5_000L
        val oversizedJson = """{"id":"text_1","type":"flexText","text":"${"a".repeat(6_000)}"}"""
        val options = givenStrictOptions(maxPayloadBytes = limit)

        // Act
        val result = parser.parse(oversizedJson, options)

        // Assert
        val left = assertIs<Either.Left<Nel<ParserError>>>(result)
        val error = assertIs<ParserError.PayloadTooLarge>(left.value.head)
        assertEquals(limit, error.maxBytes)
        assertTrue(error.sizeBytes > limit)
    }

    // =========================================================================
    // 2. JSONs Truncados y Sintaxis Corrupta (Resiliencia ante Stream Incompleto)
    // =========================================================================

    @Test
    fun `truncated JSON variations return InvalidJson error without crashing or throwing exceptions`() {
        val parser = givenParser()
        val options = givenStrictOptions()

        val truncatedPayloads = listOf(
            "",                                                                             // Vacío total
            "   \n\t  ",                                                                    // Solo espacios en blanco
            "{",                                                                            // Solo apertura
            """{ "id": """,                                                                 // Truncado en valor de clave
            """{ "id": "root", "type": """,                                                 // Truncado en tipo
            """{ "id": "root", "type": "flexVertical", "children": [""",                    // Truncado en apertura de array
            """{ "id": "root", "type": "flexVertical", "children": [{"id": "c1"""",         // Truncado dentro de hijo
            """{ "id": "root", "type": "flexText", "text": "unclosed string""",             // String sin comilla de cierre
            """{ "id": "root", "type": "flexSliderCheck", "min": 1""",                      // Truncado tras número
            """{ "id": "root", "type": "flexCheck", "label": "test", "checked": fa""",      // Truncado a mitad de literal booleano
            """{ "id": "root", "type": "flexVertical", "children": []""",                   // Falta llave de cierre
            """{ "id": "root", "type": "flexVertical", "children": [ { } ]""",              // Faltan llaves de cierre
            "null",                                                                         // Literal null raíz
            "12345",                                                                        // Primitivo número raíz
            "true",                                                                         // Primitivo booleano raíz
            """"just a string"""",                                                          // Primitivo string raíz
            "[1, 2, 3]",                                                                    // Array primitivo raíz
            """@#${'$'}%^&*!~""",                                                           // Caracteres no JSON
            """{"id": "root", "type": "flexText", "text": "\u123"}"""                       // Secuencia unicode truncada (solo 3 hex)
        )

        for (payload in truncatedPayloads) {
            val result = parser.parse(payload, options)
            val left = assertIs<Either.Left<Nel<ParserError>>>(
                result,
                message = "Se esperaba fallo controlado con InvalidJson para: $payload"
            )
            assertIs<ParserError.InvalidJson>(
                left.value.head,
                message = "Se esperaba ParserError.InvalidJson pero fue ${left.value.head}"
            )
        }
    }

    // =========================================================================
    // 3. Anidación Profunda Extrema (50.000 Niveles - Protección contra StackOverflow)
    // =========================================================================

    @Test
    fun `payload with 50000 nested opening brackets is rejected with MaxDepthExceeded in linear time`() {
        // Arrange: 50.000 llaves de apertura y cierre anidadas
        val parser = givenParser()
        val depthCount = 50_000
        val deepJson = "{".repeat(depthCount) + "}".repeat(depthCount)
        val options = givenStrictOptions(maxDepth = 20)

        // Act
        val result = parser.parse(deepJson, options)

        // Assert
        val left = assertIs<Either.Left<Nel<ParserError>>>(
            result,
            message = "Se esperaba rechazo por MaxDepthExceeded ante 50.000 niveles"
        )
        val error = assertIs<ParserError.MaxDepthExceeded>(left.value.head)
        assertEquals(20, error.maxDepth)
        assertTrue(error.currentDepth > 20)
    }

    @Test
    fun `payload with 50000 nested array brackets is rejected with MaxDepthExceeded in linear time`() {
        // Arrange: 50.000 corchetes de apertura y cierre anidados
        val parser = givenParser()
        val depthCount = 50_000
        val deepJson = "[".repeat(depthCount) + "]".repeat(depthCount)
        val options = givenStrictOptions(maxDepth = 20)

        // Act
        val result = parser.parse(deepJson, options)

        // Assert
        val left = assertIs<Either.Left<Nel<ParserError>>>(result)
        val error = assertIs<ParserError.MaxDepthExceeded>(left.value.head)
        assertEquals(20, error.maxDepth)
        assertTrue(error.currentDepth > 20)
    }

    @Test
    fun `deeply nested valid component tree exceeding maxDepth returns MaxDepthExceeded`() {
        // Arrange: 30 niveles de componentes cuando maxDepth = 10
        val parser = givenParser()
        val options = givenStrictOptions(maxDepth = 10)

        var jsonBuilder = """{"id":"leaf","type":"flexText","text":"End"}"""
        for (i in 29 downTo 1) {
            jsonBuilder = """{"id":"level_$i","type":"flexVertical","children":[$jsonBuilder]}"""
        }

        // Act
        val result = parser.parse(jsonBuilder, options)

        // Assert
        val left = assertIs<Either.Left<Nel<ParserError>>>(result)
        val error = assertIs<ParserError.MaxDepthExceeded>(left.value.head)
        assertEquals(10, error.maxDepth)
    }

    // =========================================================================
    // 4. Mutaciones de Tipos (Sustitución de Tipos de Datos en STRICT y TOLERANT)
    // =========================================================================

    @Test
    fun `type mutations in STRICT mode are rejected with InvalidPropertyValue error`() {
        val parser = givenParser()
        val options = givenStrictOptions()

        // 1. children mutado a String en vez de Array
        val childrenAsString = """{ "id": "v1", "type": "flexVertical", "children": "not_an_array" }"""
        val res1 = parser.parse(childrenAsString, options)
        val left1 = assertIs<Either.Left<Nel<ParserError>>>(res1)
        val err1 = assertIs<ParserError.InvalidPropertyValue>(left1.value.head)
        assertEquals("children", err1.property)

        // 2. children mutado a Objeto en vez de Array
        val childrenAsObject = """{ "id": "v1", "type": "flexVertical", "children": { "id": "c1", "type": "flexText" } }"""
        val res2 = parser.parse(childrenAsObject, options)
        val left2 = assertIs<Either.Left<Nel<ParserError>>>(res2)
        val err2 = assertIs<ParserError.InvalidPropertyValue>(left2.value.head)
        assertEquals("children", err2.property)

        // 3. label en flexInput mutado a Array en vez de String
        val inputWithArrayLabel = """{ "id": "in1", "type": "flexInput", "label": ["not_a_string"] }"""
        val res3 = parser.parse(inputWithArrayLabel, options)
        val left3 = assertIs<Either.Left<Nel<ParserError>>>(res3)
        val err3 = assertIs<ParserError.InvalidPropertyValue>(left3.value.head)
        assertEquals("label", err3.property)

        // 4. options en flexSelect mutado a String primitivo
        val selectStringOptions = """{ "id": "sel1", "type": "flexSelect", "label": "Choose", "options": "invalid_options" }"""
        val res4 = parser.parse(selectStringOptions, options)
        val left4 = assertIs<Either.Left<Nel<ParserError>>>(res4)
        val err4 = assertIs<ParserError.InvalidPropertyValue>(left4.value.head)
        assertEquals("options", err4.property)

        // 5. options en flexSelect como Array vacío
        val selectEmptyOptions = """{ "id": "sel1", "type": "flexSelect", "label": "Choose", "options": [] }"""
        val res5 = parser.parse(selectEmptyOptions, options)
        val left5 = assertIs<Either.Left<Nel<ParserError>>>(res5)
        val err5 = assertIs<ParserError.InvalidPropertyValue>(left5.value.head)
        assertEquals("options", err5.property)

        // 6. checked en flexCheck mutado a Array en vez de Boolean
        val checkArrayChecked = """{ "id": "chk1", "type": "flexCheck", "label": "Agree", "checked": ["invalid"] }"""
        val res6 = parser.parse(checkArrayChecked, options)
        val left6 = assertIs<Either.Left<Nel<ParserError>>>(res6)
        val err6 = assertIs<ParserError.InvalidPropertyValue>(left6.value.head)
        assertEquals("checked", err6.property)

        // 7. actionId en flexButton mutado a Objeto en vez de String
        val buttonObjectAction = """{ "id": "btn1", "type": "flexButton", "text": "Click", "actionId": {"nested": "obj"} }"""
        val res7 = parser.parse(buttonObjectAction, options)
        val left7 = assertIs<Either.Left<Nel<ParserError>>>(res7)
        val err7 = assertIs<ParserError.InvalidPropertyValue>(left7.value.head)
        assertEquals("actionId", err7.property)

        // 8. schemaVersion mutado a Array en documento envoltorio
        val schemaVersionAsArray = """{ "schemaVersion": ["1.0.0"], "root": { "id": "t1", "type": "flexText", "text": "Hi" } }"""
        val res8 = parser.parse(schemaVersionAsArray, options)
        val left8 = assertIs<Either.Left<Nel<ParserError>>>(res8)
        val err8 = assertIs<ParserError.InvalidPropertyValue>(left8.value.head)
        assertEquals("schemaVersion", err8.property)
    }

    @Test
    fun `type mutations in TOLERANT mode recover safely with warnings and fallbacks`() {
        val parser = givenParser()
        val options = givenTolerantOptions()

        // 1. children como String: en TOLERANT emite warning y el contenedor tiene lista vacía de hijos
        val childrenAsString = """{ "id": "v1", "type": "flexVertical", "children": "not_an_array" }"""
        val res1 = parser.parse(childrenAsString, options)
        val ok1 = assertIs<Either.Right<ParseResult>>(res1).value
        val vertNode = assertIs<VerticalLayoutNode>(ok1.document.root)
        assertTrue(vertNode.children.isEmpty())
        assertTrue(ok1.warnings.isNotEmpty())

        // 2. checked como Array: en TOLERANT cae al valor predeterminado (false) con warning
        val checkArrayChecked = """{ "id": "chk1", "type": "flexCheck", "label": "Agree", "checked": ["invalid"] }"""
        val res2 = parser.parse(checkArrayChecked, options)
        val ok2 = assertIs<Either.Right<ParseResult>>(res2).value
        val checkNode = assertIs<CheckNode>(ok2.document.root)
        assertEquals(false, checkNode.defaultChecked)
        assertTrue(ok2.warnings.isNotEmpty())

        // 3. backgroundColor como String no hexadecimal: en TOLERANT cae a null con warning
        val colorInvalid = """{ "id": "t1", "type": "flexText", "text": "Hi", "style": { "backgroundColor": "not_a_hex_color" } }"""
        val res3 = parser.parse(colorInvalid, options)
        val ok3 = assertIs<Either.Right<ParseResult>>(res3).value
        val textNode = assertIs<TextNode>(ok3.document.root)
        assertEquals("t1", textNode.id.value)
        assertEquals("Hi", textNode.text)
        assertTrue(ok3.warnings.isNotEmpty())

        // 4. Componente con tipo defectuoso insalvable (ej. select con options como string):
        // En TOLERANT se convierte en UnsupportedFallbackNode manteniendo la estabilidad del árbol
        val selectStringOptions = """
            {
                "id": "container",
                "type": "flexVertical",
                "children": [
                    { "id": "sel1", "type": "flexSelect", "label": "Choose", "options": "invalid_options" },
                    { "id": "btn1", "type": "flexButton", "text": "Next", "actionId": "next" }
                ]
            }
        """.trimIndent()
        val res4 = parser.parse(selectStringOptions, options)
        val ok4 = assertIs<Either.Right<ParseResult>>(res4).value
        val parent = assertIs<VerticalLayoutNode>(ok4.document.root)
        assertEquals(2, parent.children.size)

        // El primer hijo defectuoso fue degradado a UnsupportedFallbackNode
        val fallbackChild = assertIs<UnsupportedFallbackNode>(parent.children[0])
        assertEquals("sel1", fallbackChild.id.value)

        // El segundo hijo sano se parseó correctamente
        assertEquals("btn1", parent.children[1].id.value)
        assertTrue(ok4.warnings.isNotEmpty())
    }

    @Test
    fun `fuzzing random mutations across complex document does not crash or throw unhandled exceptions`() {
        val parser = givenParser()
        val options = givenTolerantOptions()

        // Documento con múltiples mutaciones simultáneas de tipos y valores corruptos
        val corruptDocument = """
            {
                "schemaVersion": "1.0.0",
                "root": {
                    "id": "root_fuzz",
                    "type": "flexScroll",
                    "style": {
                        "backgroundColor": 123456,
                        "padding": "invalid_padding"
                    },
                    "children": [
                        {
                            "id": "bad_input",
                            "type": "flexInput",
                            "label": 9999,
                            "isSensitive": "not_a_bool"
                        },
                        {
                            "id": "bad_slider",
                            "type": "flexSliderCheck",
                            "min": ["array_not_number"],
                            "max": "NaN",
                            "step": false
                        },
                        {
                            "id": "valid_text",
                            "type": "flexText",
                            "text": "I survived fuzzing!"
                        }
                    ]
                }
            }
        """.trimIndent()

        // Act
        val result = parser.parse(corruptDocument, options)

        // Assert: en TOLERANT se recupera, degradando lo defectuoso y manteniendo lo sano
        val parseResult = assertIs<Either.Right<ParseResult>>(result).value
        val root = assertIs<ScrollLayoutNode>(parseResult.document.root)
        assertEquals("root_fuzz", root.id.value)
        assertEquals(3, root.children.size)

        // El tercer nodo válido se conservó intacto
        val validText = assertIs<TextNode>(root.children[2])
        assertEquals("valid_text", validText.id.value)
        assertEquals("I survived fuzzing!", validText.text)
        assertTrue(parseResult.warnings.isNotEmpty())
    }
}
