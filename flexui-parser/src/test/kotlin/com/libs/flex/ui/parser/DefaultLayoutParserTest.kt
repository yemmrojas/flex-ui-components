package com.libs.flex.ui.parser

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.model.node.ButtonNode
import com.libs.flex.ui.core.domain.model.node.CheckNode
import com.libs.flex.ui.core.domain.model.node.InputNode
import com.libs.flex.ui.core.domain.model.node.ListLayoutNode
import com.libs.flex.ui.core.domain.model.node.TextNode
import com.libs.flex.ui.core.domain.model.node.UnsupportedFallbackNode
import com.libs.flex.ui.core.domain.model.node.VerticalLayoutNode
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.parser.internal.DefaultLayoutParser
import com.libs.flex.ui.parser.internal.mapper.NodeMapper
import com.libs.flex.ui.parser.internal.mapper.NodeMapperRegistry
import com.libs.flex.ui.parser.internal.model.MappedNode
import com.libs.flex.ui.parser.internal.reader.PropertyReader
import com.libs.flex.ui.parser.internal.scope.MappingScope
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class DefaultLayoutParserTest {

    // === TEST FIXTURE PROVIDERS (Object Mother Pattern) ===

    private fun givenParser(): LayoutParser = DefaultLayoutParser()

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
    // Paso 1: Protección de Ingesta y Tamaño en Bytes
    // =========================================================================

    @Test
    fun `parse rejects payload exceeding maxPayloadBytes with PayloadTooLarge error`() {
        val parser = givenParser()
        val json = """{"id": "text_1", "type": "flexText", "text": "Hello World"}"""
        val options = givenStrictOptions(maxPayloadBytes = 10)

        val result = parser.parse(json, options)

        val left = assertIs<Either.Left<Nel<ParserError>>>(result)
        val error = assertIs<ParserError.PayloadTooLarge>(left.value.head)
        assertTrue(error.sizeBytes > 10)
        assertEquals(10, error.maxBytes)
    }

    // =========================================================================
    // Paso 2: Protección de Sintaxis JSON y Estructura Raíz
    // =========================================================================

    @Test
    fun `parse rejects malformed JSON with InvalidJson error`() {
        val parser = givenParser()
        val truncatedJson = """{ "id": "text_1", "type": """

        val result = parser.parse(truncatedJson, givenStrictOptions())

        val left = assertIs<Either.Left<Nel<ParserError>>>(result)
        val error = assertIs<ParserError.InvalidJson>(left.value.head)
        assertEquals("$", error.path)
    }


    @Test
    fun `parse rejects non-object root payload with InvalidJson error`() {
        val parser = givenParser()
        val arrayJson = """[ { "id": "text_1", "type": "flexText", "text": "Hello" } ]"""

        val result = parser.parse(arrayJson, givenStrictOptions())

        val left = assertIs<Either.Left<Nel<ParserError>>>(result)
        val error = assertIs<ParserError.InvalidJson>(left.value.head)
        assertTrue(error.reason.contains("must be an object"))
    }

    // =========================================================================
    // Paso 3: Identificación de Formatos (Documento Envoltorio vs Nodo Directo)
    // =========================================================================

    @Test
    fun `parse direct root node JSON succeeds and constructs LayoutDocument with default version`() {
        val parser = givenParser()
        val directJson = """
            {
                "id": "direct_text",
                "type": "componentTextView",
                "text": "Direct root text"
            }
        """.trimIndent()

        val result = parser.parse(directJson, givenStrictOptions())

        val right = assertIs<Either.Right<ParseResult>>(result)
        val document = right.value.document
        assertEquals("1.0.0", document.schemaVersion.value)
        val rootNode = assertIs<TextNode>(document.root)
        assertEquals("direct_text", rootNode.id.value)
        assertEquals("Direct root text", rootNode.text)
        assertTrue(right.value.warnings.isEmpty())
    }

    @Test
    fun `parse structured document with root and explicit schemaVersion succeeds`() {
        val parser = givenParser()
        val structuredJson = """
            {
                "schemaVersion": "1.0.0",
                "root": {
                    "id": "welcome_btn",
                    "type": "flexButton",
                    "text": "Continue",
                    "actionId": "on_continue"
                }
            }
        """.trimIndent()

        val result = parser.parse(structuredJson, givenStrictOptions())

        val right = assertIs<Either.Right<ParseResult>>(result)
        val document = right.value.document
        assertEquals("1.0.0", document.schemaVersion.value)
        val rootNode = assertIs<ButtonNode>(document.root)
        assertEquals("welcome_btn", rootNode.id.value)
        assertEquals("on_continue", rootNode.actionId.value)
    }

    @Test
    fun `parse structured document with rootNode key succeeds`() {
        val parser = givenParser()
        val structuredJson = """
            {
                "schemaVersion": "1.0.0",
                "rootNode": {
                    "id": "checkbox_1",
                    "type": "flexCheck",
                    "label": "I accept the conditions",
                    "checked": true
                }
            }
        """.trimIndent()

        val result = parser.parse(structuredJson, givenStrictOptions())

        val right = assertIs<Either.Right<ParseResult>>(result)
        val rootNode = assertIs<CheckNode>(right.value.document.root)
        assertEquals("checkbox_1", rootNode.id.value)
        assertTrue(rootNode.defaultChecked)
    }

    @Test
    fun `parse rejects non-object root property with InvalidPropertyValue error`() {
        val parser = givenParser()
        val invalidWrapperJson = """
            {
                "schemaVersion": "1.0.0",
                "root": "not_an_object"
            }
        """.trimIndent()

        val result = parser.parse(invalidWrapperJson, givenStrictOptions())

        val left = assertIs<Either.Left<Nel<ParserError>>>(result)
        val error = assertIs<ParserError.InvalidPropertyValue>(left.value.head)
        assertEquals("root", error.property)
    }

    // =========================================================================
    // Paso 4: STRICT vs TOLERANT en Resolución de Componentes
    // =========================================================================

    @Test
    fun `in STRICT mode, unknown component type fails with UnknownComponentType error`() {
        val parser = givenParser()
        val json = """
            {
                "id": "unknown_comp",
                "type": "flexFutureWidget3D"
            }
        """.trimIndent()

        val result = parser.parse(json, givenStrictOptions())

        val left = assertIs<Either.Left<Nel<ParserError>>>(result)
        val error = assertIs<ParserError.UnknownComponentType>(left.value.head)
        assertEquals("flexFutureWidget3D", error.type)
    }

    @Test
    fun `in TOLERANT mode, unknown component type maps to UnsupportedFallbackNode with warning`() {
        val parser = givenParser()
        val json = """
            {
                "id": "unknown_comp",
                "type": "flexFutureWidget3D"
            }
        """.trimIndent()

        val result = parser.parse(json, givenTolerantOptions())

        val right = assertIs<Either.Right<ParseResult>>(result)
        val rootNode = assertIs<UnsupportedFallbackNode>(right.value.document.root)
        assertEquals("unknown_comp", rootNode.id.value)
        assertEquals("flexFutureWidget3D", rootNode.rawType)

        assertEquals(1, right.value.warnings.size)
        val warning = right.value.warnings.first()
        assertEquals("UNKNOWN_COMPONENT", warning.code)
    }

    @Test
    fun `in STRICT mode, missing required field fails with MissingRequiredProperty error`() {
        val parser = givenParser()
        val json = """
            {
                "id": "btn_invalid",
                "type": "flexButton",
                "text": "Click me"
            }
        """.trimIndent()

        val result = parser.parse(json, givenStrictOptions())

        val left = assertIs<Either.Left<Nel<ParserError>>>(result)
        val error = assertIs<ParserError.MissingRequiredProperty>(left.value.head)
        assertEquals("actionId", error.property)
    }

    @Test
    fun `in TOLERANT mode, missing required field maps to UnsupportedFallbackNode with warning`() {
        val parser = givenParser()
        val json = """
            {
                "id": "btn_invalid",
                "type": "flexButton",
                "text": "Click me"
            }
        """.trimIndent()

        val result = parser.parse(json, givenTolerantOptions())

        val right = assertIs<Either.Right<ParseResult>>(result)
        val rootNode = assertIs<UnsupportedFallbackNode>(right.value.document.root)
        assertEquals("btn_invalid", rootNode.id.value)
        assertEquals("flexButton", rootNode.rawType)
        assertTrue(right.value.warnings.isNotEmpty())
    }

    @Test
    fun `missing type in component in TOLERANT mode maps to fallback with MISSING_TYPE warning`() {
        val parser = givenParser()
        val json = """
            {
                "id": "missing_type_node",
                "text": "No type"
            }
        """.trimIndent()

        val result = parser.parse(json, givenTolerantOptions())

        val right = assertIs<Either.Right<ParseResult>>(result)
        val rootNode = assertIs<UnsupportedFallbackNode>(right.value.document.root)
        assertEquals("missing_type_node", rootNode.id.value)
        val warning = right.value.warnings.first()
        assertEquals("MISSING_TYPE", warning.code)
    }

    @Test
    fun `deep nesting exceeding maxDepth fails with MaxDepthExceeded error`() {
        val parser = givenParser()
        // Contenedor anidado a profundidad 3 con límite configurado en 2
        val nestedJson = """
            {
                "id": "level_1",
                "type": "flexVertical",
                "children": [
                    {
                        "id": "level_2",
                        "type": "flexVertical",
                        "children": [
                            {
                                "id": "level_3",
                                "type": "flexText",
                                "text": "Too deep"
                            }
                        ]
                    }
                ]
            }
        """.trimIndent()

        val result = parser.parse(nestedJson, givenStrictOptions(maxDepth = 2))

        val left = assertIs<Either.Left<Nel<ParserError>>>(result)
        val error = assertIs<ParserError.MaxDepthExceeded>(left.value.head)
        assertEquals(3, error.currentDepth)
        assertEquals(2, error.maxDepth)
    }

    // =========================================================================
    // Paso 5: Extracción y Acumulación de Templates (Embebidas y Top-Level)
    // =========================================================================

    @Test
    fun `parse extracts inline itemTemplate from flexList into LayoutDocument templates`() {
        val parser = givenParser()
        val listJson = """
            {
                "id": "user_list",
                "type": "flexList",
                "itemsCount": 5,
                "itemTemplate": {
                    "id": "user_row_template",
                    "type": "flexText",
                    "text": "User Item"
                }
            }
        """.trimIndent()

        val result = parser.parse(listJson, givenStrictOptions())

        val right = assertIs<Either.Right<ParseResult>>(result)
        val document = right.value.document
        val listNode = assertIs<ListLayoutNode>(document.root)
        assertEquals("user_row_template", listNode.itemTemplateId.value)

        val templateId = ComponentId.of("user_row_template").getOrNull()!!
        assertTrue(document.templates.containsKey(templateId))
        val templateNode = assertIs<TextNode>(document.templates[templateId])
        assertEquals("User Item", templateNode.text)
    }

    @Test
    fun `parse with top-level templates as object includes them in LayoutDocument templates`() {
        val parser = givenParser()
        val json = """
            {
                "schemaVersion": "1.0.0",
                "root": {
                    "id": "my_list",
                    "type": "flexList",
                    "itemsCount": 3,
                    "itemTemplateId": "shared_card_template"
                },
                "templates": {
                    "card": {
                        "id": "shared_card_template",
                        "type": "flexText",
                        "text": "Shared Card Template"
                    }
                }
            }
        """.trimIndent()

        val result = parser.parse(json, givenStrictOptions())

        val right = assertIs<Either.Right<ParseResult>>(result)
        val document = right.value.document
        val templateId = ComponentId.of("shared_card_template").getOrNull()!!
        assertTrue(document.templates.containsKey(templateId))
        val templateNode = assertIs<TextNode>(document.templates[templateId])
        assertEquals("Shared Card Template", templateNode.text)
    }

    @Test
    fun `parse with top-level templates as array includes them in LayoutDocument templates`() {
        val parser = givenParser()
        val json = """
            {
                "schemaVersion": "1.0.0",
                "root": {
                    "id": "my_list",
                    "type": "flexList",
                    "itemsCount": 3,
                    "itemTemplateId": "array_card_template"
                },
                "templates": [
                    {
                        "id": "array_card_template",
                        "type": "flexText",
                        "text": "Array Card Template"
                    }
                ]
            }
        """.trimIndent()

        val result = parser.parse(json, givenStrictOptions())

        val right = assertIs<Either.Right<ParseResult>>(result)
        val document = right.value.document
        val templateId = ComponentId.of("array_card_template").getOrNull()!!
        assertTrue(document.templates.containsKey(templateId))
    }

    // =========================================================================
    // Paso 6 y 7: Delegación e Invariantes Estructurales del Core
    // =========================================================================

    @Test
    fun `parse with unsupported schemaVersion returns CoreStructuralError`() {
        val parser = givenParser()
        val json = """
            {
                "schemaVersion": "99.0.0",
                "root": {
                    "id": "root_text",
                    "type": "flexText",
                    "text": "Future Version"
                }
            }
        """.trimIndent()

        val result = parser.parse(json, givenStrictOptions())

        val left = assertIs<Either.Left<Nel<ParserError>>>(result)
        val error = assertIs<ParserError.CoreStructuralError>(left.value.head)
        assertIs<DomainError.Structural.UnsupportedSchemaVersion>(error.structuralError)
    }

    @Test
    fun `parse with duplicate component IDs returns CoreStructuralError`() {
        val parser = givenParser()
        val jsonWithDuplicates = """
            {
                "id": "container_1",
                "type": "flexVertical",
                "children": [
                    {
                        "id": "duplicated_id",
                        "type": "flexText",
                        "text": "First"
                    },
                    {
                        "id": "duplicated_id",
                        "type": "flexText",
                        "text": "Second"
                    }
                ]
            }
        """.trimIndent()

        val result = parser.parse(jsonWithDuplicates, givenStrictOptions())

        val left = assertIs<Either.Left<Nel<ParserError>>>(result)
        val error = assertIs<ParserError.CoreStructuralError>(left.value.head)
        assertIs<DomainError.Structural.DuplicateComponentId>(error.structuralError)
    }

    // =========================================================================
    // Prueba Integral End-to-End
    // =========================================================================

    @Test
    fun `parse end-to-end multi-component form constructs valid LayoutDocument and standalone inputs`() {
        val parser = givenParser()
        val complexFormJson = """
            {
                "schemaVersion": "1.0.0",
                "root": {
                    "id": "form_screen",
                    "type": "contentVertical",
                    "children": [
                        {
                            "id": "header_label",
                            "type": "componentTextView",
                            "text": "Register Account"
                        },
                        {
                            "id": "user_email",
                            "type": "componentInput",
                            "label": "Email Address",
                            "validationRules": [
                                { "type": "required", "message": "Email is required" },
                                { "type": "regex", "pattern": "^[A-Za-z0-9+_.-]+@(.+)$", "message": "Invalid email" }
                            ]
                        },
                        {
                            "id": "terms_check",
                            "type": "componentCheck",
                            "label": "Agree to Terms",
                            "checked": false
                        },
                        {
                            "id": "submit_btn",
                            "type": "componentButton",
                            "text": "Submit",
                            "actionId": "submit_registration"
                        }
                    ]
                }
            }
        """.trimIndent()

        val result = parser.parse(complexFormJson, givenStrictOptions())

        val right = assertIs<Either.Right<ParseResult>>(result)
        val document = right.value.document

        val root = assertIs<VerticalLayoutNode>(document.root)
        assertEquals("form_screen", root.id.value)
        assertEquals(4, root.children.size)

        // Verify standalone input nodes recognized by :flexui-core
        val inputNodes = document.standaloneInputNodes()
        assertEquals(2, inputNodes.size) // user_email (InputNode) y terms_check (CheckNode)

        val emailNode = assertIs<InputNode>(document.findNode(ComponentId.of("user_email").getOrNull()!!))
        assertEquals("Email Address", emailNode.label)
        assertEquals(2, emailNode.validationRules.size)

        val buttonNode = assertIs<ButtonNode>(document.findNode(ComponentId.of("submit_btn").getOrNull()!!))
        assertEquals("submit_registration", buttonNode.actionId.value)
    }

    // =========================================================================
    // Pruebas de Resiliencia, Límites y Casos Borde (F)
    // =========================================================================

    @Test
    fun `two templates with duplicate id produce DuplicateComponentId error instead of silent overwrite`() {
        val parser = givenParser()
        val json = """
            {
                "schemaVersion": "1.0.0",
                "templates": [
                    { "id": "tmpl_dup", "type": "flexText", "text": "First" },
                    { "id": "tmpl_dup", "type": "flexText", "text": "Second" }
                ],
                "root": { "id": "root_1", "type": "flexVertical" }
            }
        """.trimIndent()

        val result = parser.parse(json, givenStrictOptions())

        val left = assertIs<Either.Left<Nel<ParserError>>>(result)
        val duplicateError = left.value.toList().filterIsInstance<ParserError.DuplicateComponentId>().firstOrNull()
        assertNotNull(duplicateError)
        assertEquals("tmpl_dup", duplicateError.id)
    }

    @Test
    fun `tolerant mode with invalid template records warning and continues`() {
        val parser = givenParser()
        val json = """
            {
                "schemaVersion": "1.0.0",
                "templates": {
                    "corrupt_tmpl": { "id": "tmpl_corrupt", "type": "unregisteredComponentType999" }
                },
                "root": { "id": "root_1", "type": "flexVertical" }
            }
        """.trimIndent()

        val result = parser.parse(json, givenTolerantOptions())

        val right = assertIs<Either.Right<ParseResult>>(result)
        val warnings = right.value.warnings
        assertTrue(warnings.isNotEmpty())
        assertTrue(warnings.any { it.code == WarningCode.UNKNOWN_COMPONENT || it.code == WarningCode.INVALID_TEMPLATE })
    }

    @Test
    fun `constructing ParserOptions with maxDepth 0 throws visible IllegalArgumentException`() {
        val exception = assertThrows<IllegalArgumentException> {
            ParserOptions(maxDepth = 0)
        }
        assertTrue(exception.message?.contains("maxDepth") == true)
    }

    @Test
    fun `payload containing both root and rootNode fails with ambiguous root error`() {
        val parser = givenParser()
        val json = """
            {
                "schemaVersion": "1.0.0",
                "root": { "id": "root_1", "type": "flexVertical" },
                "rootNode": { "id": "root_2", "type": "flexVertical" }
            }
        """.trimIndent()

        val result = parser.parse(json, givenStrictOptions())

        val left = assertIs<Either.Left<Nel<ParserError>>>(result)
        val error = assertIs<ParserError.InvalidPropertyValue>(left.value.head)
        assertEquals("root", error.property)
        assertTrue(error.reason.contains("Ambiguous root definition"))
    }

    @Test
    fun `document with schemaVersion as number or object fails with InvalidPropertyValue`() {
        val parser = givenParser()
        val numberVersionJson = """
            {
                "schemaVersion": 1.0,
                "root": { "id": "root_1", "type": "flexVertical" }
            }
        """.trimIndent()

        val numberResult = parser.parse(numberVersionJson, givenStrictOptions())
        val numberLeft = assertIs<Either.Left<Nel<ParserError>>>(numberResult)
        val numberError = assertIs<ParserError.InvalidPropertyValue>(numberLeft.value.head)
        assertEquals("schemaVersion", numberError.property)
        assertTrue(numberError.reason.contains("Expected string"))

        val objectVersionJson = """
            {
                "schemaVersion": { "major": 1, "minor": 0 },
                "root": { "id": "root_1", "type": "flexVertical" }
            }
        """.trimIndent()

        val objectResult = parser.parse(objectVersionJson, givenStrictOptions())
        val objectLeft = assertIs<Either.Left<Nel<ParserError>>>(objectResult)
        val objectError = assertIs<ParserError.InvalidPropertyValue>(objectLeft.value.head)
        assertEquals("schemaVersion", objectError.property)
        assertTrue(objectError.reason.contains("Expected string"))
    }

    @Test
    fun `envelope missing root property when declaring schemaVersion fails with MissingRequiredProperty`() {
        val parser = givenParser()
        val json = """
            {
                "schemaVersion": "1.0.0",
                "templates": []
            }
        """.trimIndent()

        val result = parser.parse(json, givenStrictOptions())

        val left = assertIs<Either.Left<Nel<ParserError>>>(result)
        val error = assertIs<ParserError.MissingRequiredProperty>(left.value.head)
        assertEquals("root", error.property)
    }

    @Test
    fun `extreme 50_000 nesting levels payload is rejected with MaxDepthExceeded without StackOverflowError`() {
        val parser = givenParser()
        val count = 50_000
        val extremeJson = StringBuilder().apply {
            repeat(count) { append("{\"a\":") }
            append("1")
            repeat(count) { append("}") }
        }.toString()

        val result = parser.parse(extremeJson, givenStrictOptions(maxPayloadBytes = 50 * 1024 * 1024L, maxDepth = 20))

        val left = assertIs<Either.Left<Nel<ParserError>>>(result)
        val error = assertIs<ParserError.MaxDepthExceeded>(left.value.head)
        assertEquals(20, error.maxDepth)
    }

    @Test
    fun `list nested three levels deep preserves its item template in document`() {
        val parser = givenParser()
        val json = """
            {
                "id": "root_vertical",
                "type": "flexVertical",
                "children": [
                    {
                        "id": "level_1_horizontal",
                        "type": "flexHorizontal",
                        "children": [
                            {
                                "id": "level_2_vertical",
                                "type": "flexVertical",
                                "children": [
                                    {
                                        "id": "level_3_list",
                                        "type": "flexList",
                                        "itemTemplate": {
                                            "id": "deep_template_card",
                                            "type": "flexText",
                                            "text": "Deep Card"
                                        }
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        """.trimIndent()

        val result = parser.parse(json, givenStrictOptions(maxDepth = 20))

        val right = assertIs<Either.Right<ParseResult>>(result)
        val document = right.value.document

        val templateId = ComponentId.of("deep_template_card").getOrNull()!!
        assertTrue(document.templates.containsKey(templateId))
        val templateNode = document.findNode(templateId)
        assertNotNull(templateNode)
        assertEquals("deep_template_card", templateNode.id.value)
    }

    @Test
    fun `same DefaultLayoutParser instance evaluates STRICT and TOLERANT dynamically across calls`() {
        val sharedParser = givenParser()
        val unknownComponentJson = """{ "id": "c1", "type": "customUnregisteredWidget" }"""

        // 1. STRICT call fails
        val strictResult = sharedParser.parse(unknownComponentJson, givenStrictOptions())
        val strictLeft = assertIs<Either.Left<Nel<ParserError>>>(strictResult)
        assertIs<ParserError.UnknownComponentType>(strictLeft.value.head)

        // 2. TOLERANT call succeeds with fallback
        val tolerantResult = sharedParser.parse(unknownComponentJson, givenTolerantOptions())
        val tolerantRight = assertIs<Either.Right<ParseResult>>(tolerantResult)
        assertIs<UnsupportedFallbackNode>(tolerantRight.value.document.root)

        // 3. Subsequent STRICT call fails again cleanly
        val secondStrictResult = sharedParser.parse(unknownComponentJson, givenStrictOptions())
        val secondStrictLeft = assertIs<Either.Left<Nel<ParserError>>>(secondStrictResult)
        assertIs<ParserError.UnknownComponentType>(secondStrictLeft.value.head)
    }

    @Test
    fun `unexpected exception in node mapper is safely captured as InternalError`() {
        val explodingMapper = object : NodeMapper {
            override val types: Set<String> = setOf("explodingNode")
            override fun map(reader: PropertyReader, scope: MappingScope): Either<Nel<ParserError>, MappedNode> {
                throw IllegalStateException("Unexpected mapper explosion!")
            }
        }
        val customRegistry = NodeMapperRegistry.create(listOf(explodingMapper))
        val parser = DefaultLayoutParser(registry = customRegistry)

        val json = """{ "id": "boom", "type": "explodingNode" }"""
        val result = parser.parse(json, givenStrictOptions())

        val left = assertIs<Either.Left<Nel<ParserError>>>(result)
        val error = assertIs<ParserError.InternalError>(left.value.head)
        assertIs<IllegalStateException>(error.cause)
        assertEquals("Unexpected mapper explosion!", error.cause.message)
    }

    @Test
    fun `payload of exact maxPayloadBytes succeeds while maxPayloadBytes minus one fails`() {
        val parser = givenParser()
        val json = """{"id":"text_1","type":"flexText","text":"Exact"}"""
        val exactBytes = json.toByteArray(Charsets.UTF_8).size.toLong()

        // Exacto: debe tener éxito
        val exactOptions = givenStrictOptions(maxPayloadBytes = exactBytes)
        val exactResult = parser.parse(json, exactOptions)
        assertIs<Either.Right<ParseResult>>(exactResult)

        // Exacto menos 1: debe fallar con PayloadTooLarge
        val smallerOptions = givenStrictOptions(maxPayloadBytes = exactBytes - 1)
        val smallerResult = parser.parse(json, smallerOptions)
        val left = assertIs<Either.Left<Nel<ParserError>>>(smallerResult)
        assertIs<ParserError.PayloadTooLarge>(left.value.head)
    }
}

