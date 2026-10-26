package com.libs.flex.ui.parser.internal.mapper.atomic

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.model.node.ButtonNode
import com.libs.flex.ui.core.domain.model.node.CheckNode
import com.libs.flex.ui.core.domain.model.node.CustomNode
import com.libs.flex.ui.core.domain.model.node.ImageNode
import com.libs.flex.ui.core.domain.model.node.InputNode
import com.libs.flex.ui.core.domain.model.node.LoaderNode
import com.libs.flex.ui.core.domain.model.node.SelectNode
import com.libs.flex.ui.core.domain.model.node.SelectOption
import com.libs.flex.ui.core.domain.model.node.SliderNode
import com.libs.flex.ui.core.domain.model.node.TextNode
import com.libs.flex.ui.core.domain.model.node.ToastNode
import com.libs.flex.ui.core.domain.value.ButtonVariant
import com.libs.flex.ui.core.domain.value.FontSize
import com.libs.flex.ui.core.domain.value.InputVariant
import com.libs.flex.ui.core.domain.value.NumberRange
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserOptions
import com.libs.flex.ui.parser.fakes.FakePropertyReader
import com.libs.flex.ui.parser.internal.reader.JsonPropertyReader
import com.libs.flex.ui.parser.internal.reader.StrictErrorPolicy
import com.libs.flex.ui.parser.internal.scope.DefaultMappingScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AtomicNodeMappersTest {

    // === TEST FIXTURE PROVIDERS (Object Mother Pattern) ===

    private fun givenScope(path: String = "root.node"): DefaultMappingScope =
        DefaultMappingScope(
            path = path,
            depth = 1,
            options = ParserOptions.DEFAULT,
            nodeResolver = { _, _ -> Either.Left(com.libs.flex.ui.core.domain.functional.Nel.of(ParserError.UnknownComponentType(path, "unknown"))) },
            onWarn = {}
        )

    private fun givenJsonReader(jsonStr: String, path: String = "root.node"): JsonPropertyReader {
        val json = Json.parseToJsonElement(jsonStr).jsonObject
        return JsonPropertyReader(json, path, StrictErrorPolicy) {}
    }

    // =========================================================================
    // 1. TextNodeMapper Tests
    // =========================================================================

    @Test
    fun `TextNodeMapper maps valid text with default values`() {
        val mapper = TextNodeMapper()
        val reader = FakePropertyReader(
            path = "root.title",
            strings = mapOf("text" to "Hello FlexUI")
        )

        val result = mapper.map(reader, givenScope("root.title"))
        val mappedNode = assertIs<Either.Right<*>>(result).value as com.libs.flex.ui.parser.internal.model.MappedNode
        val node = assertIs<TextNode>(mappedNode.node)

        assertEquals("Hello FlexUI", node.text)
        assertEquals("body1", node.typography)
        assertEquals(null, node.fontSize)
        assertEquals(null, node.maxLines)
    }

    @Test
    fun `TextNodeMapper maps all properties from JSON including aliases and fontSize`() {
        val mapper = TextNodeMapper()
        val reader = givenJsonReader(
            """
            {
                "id": "header_text",
                "text": "Header Title",
                "textStyle": "bold",
                "fontSize": 24.0,
                "maxLines": 2
            }
            """.trimIndent()
        )

        val result = mapper.map(reader, givenScope())
        val mappedNode = assertIs<Either.Right<*>>(result).value as com.libs.flex.ui.parser.internal.model.MappedNode
        val node = assertIs<TextNode>(mappedNode.node)

        assertEquals("header_text", node.id.value)
        assertEquals("Header Title", node.text)
        assertEquals("bold", node.typography)
        assertEquals(24.0f, node.fontSize?.value)
        assertEquals(2, node.maxLines)
    }

    @Test
    fun `TextNodeMapper fails when text is missing`() {
        val mapper = TextNodeMapper()
        val reader = FakePropertyReader(path = "root.text")

        val result = mapper.map(reader, givenScope("root.text"))
        val left = assertIs<Either.Left<*>>(result)
        val error = assertIs<ParserError.MissingRequiredProperty>((left.value as com.libs.flex.ui.core.domain.functional.Nel<*>).head)
        assertEquals("text", error.property)
    }

    @Test
    fun `TextNodeMapper declares supported types`() {
        assertTrue(TextNodeMapper.SUPPORTED_TYPES.contains("flexText"))
        assertTrue(TextNodeMapper.SUPPORTED_TYPES.contains("componentTextView"))
        assertTrue(TextNodeMapper.SUPPORTED_TYPES.contains("text"))
    }

    // =========================================================================
    // 2. ButtonNodeMapper Tests
    // =========================================================================

    @Test
    fun `ButtonNodeMapper maps button with primary variant and default flags`() {
        val mapper = ButtonNodeMapper()
        val reader = FakePropertyReader(
            path = "root.btn",
            strings = mapOf("text" to "Submit", "actionId" to "submit_action")
        )

        val result = mapper.map(reader, givenScope("root.btn"))
        val mapped = assertIs<Either.Right<*>>(result).value as com.libs.flex.ui.parser.internal.model.MappedNode
        val node = assertIs<ButtonNode>(mapped.node)

        assertEquals("Submit", node.text)
        assertEquals("submit_action", node.actionId.value)
        assertEquals(ButtonVariant.Primary, node.buttonVariant)
        assertTrue(node.isEnabled)
        assertFalse(node.requiresValidation)
    }

    @Test
    fun `ButtonNodeMapper maps secondary button variant and aliases from JSON`() {
        val mapper = ButtonNodeMapper()
        val reader = givenJsonReader(
            """
            {
                "id": "cancel_btn",
                "label": "Cancel",
                "action": "cancel_action",
                "buttonStyle": "secondary",
                "isEnabled": false,
                "requiresValidation": true
            }
            """.trimIndent()
        )

        val result = mapper.map(reader, givenScope())
        val mapped = assertIs<Either.Right<*>>(result).value as com.libs.flex.ui.parser.internal.model.MappedNode
        val node = assertIs<ButtonNode>(mapped.node)

        assertEquals("Cancel", node.text)
        assertEquals("cancel_action", node.actionId.value)
        assertEquals(ButtonVariant.Secondary, node.buttonVariant)
        assertFalse(node.isEnabled)
        assertTrue(node.requiresValidation)
    }

    @Test
    fun `ButtonNodeMapper accumulates multiple errors when text and actionId are both missing`() {
        val mapper = ButtonNodeMapper()
        val reader = FakePropertyReader(path = "root.btn")

        val result = mapper.map(reader, givenScope("root.btn"))
        val left = assertIs<Either.Left<*>>(result)
        val errors = (left.value as com.libs.flex.ui.core.domain.functional.Nel<*>).toList()

        assertEquals(2, errors.size)
        assertTrue(errors.any { it is ParserError.MissingRequiredProperty && it.property == "text" })
        assertTrue(errors.any { it is ParserError.MissingRequiredProperty && it.property == "actionId" })
    }

    @Test
    fun `ButtonNodeMapper fails when actionId is blank`() {
        val mapper = ButtonNodeMapper()
        val reader = FakePropertyReader(
            strings = mapOf("text" to "Click", "actionId" to "   ")
        )

        val result = mapper.map(reader, givenScope())
        val left = assertIs<Either.Left<*>>(result)
        val error = assertIs<ParserError.InvalidPropertyValue>((left.value as com.libs.flex.ui.core.domain.functional.Nel<*>).head)
        assertEquals("actionId", error.property)
    }

    @Test
    fun `ButtonNodeMapper declares supported types`() {
        assertTrue(ButtonNodeMapper.SUPPORTED_TYPES.contains("flexButton"))
        assertTrue(ButtonNodeMapper.SUPPORTED_TYPES.contains("button"))
        assertTrue(ButtonNodeMapper.SUPPORTED_TYPES.contains("componentButton"))
    }

    // =========================================================================
    // 3. InputNodeMapper Tests
    // =========================================================================

    @Test
    fun `InputNodeMapper maps text input with outlined variant and validation rules`() {
        val mapper = InputNodeMapper()
        val reader = givenJsonReader(
            """
            {
                "id": "email_input",
                "label": "Email",
                "placeholder": "user@example.com",
                "inputStyle": "outlined",
                "validations": [
                    {"type": "required", "message": "Email is required"}
                ]
            }
            """.trimIndent()
        )

        val result = mapper.map(reader, givenScope())
        val mapped = assertIs<Either.Right<*>>(result).value as com.libs.flex.ui.parser.internal.model.MappedNode
        val node = assertIs<InputNode>(mapped.node)

        assertEquals("Email", node.label)
        assertEquals("user@example.com", node.placeholder)
        assertEquals(InputVariant.Outlined, node.inputVariant)
        assertEquals(1, node.validationRules.size)
        assertFalse(node.isSensitive)
    }

    @Test
    fun `InputNodeMapper sets isSensitive true when inputStyle is password`() {
        val mapper = InputNodeMapper()
        val reader = givenJsonReader(
            """
            {
                "id": "pwd_input",
                "label": "Password",
                "inputStyle": "password"
            }
            """.trimIndent()
        )

        val result = mapper.map(reader, givenScope())
        val mapped = assertIs<Either.Right<*>>(result).value as com.libs.flex.ui.parser.internal.model.MappedNode
        val node = assertIs<InputNode>(mapped.node)

        assertEquals(InputVariant.Password, node.inputVariant)
        assertTrue(node.isSensitive)
    }

    @Test
    fun `InputNodeMapper declares supported types`() {
        assertTrue(InputNodeMapper.SUPPORTED_TYPES.contains("flexInput"))
        assertTrue(InputNodeMapper.SUPPORTED_TYPES.contains("input"))
        assertTrue(InputNodeMapper.SUPPORTED_TYPES.contains("componentTextInput"))
        assertTrue(InputNodeMapper.SUPPORTED_TYPES.contains("componentInput"))
    }

    // =========================================================================
    // 4. CheckNodeMapper Tests
    // =========================================================================

    @Test
    fun `CheckNodeMapper maps checkbox with default checked and label`() {
        val mapper = CheckNodeMapper()
        val reader = FakePropertyReader(
            strings = mapOf("label" to "Accept Terms"),
            booleans = mapOf("checked" to true)
        )

        val result = mapper.map(reader, givenScope())
        val mapped = assertIs<Either.Right<*>>(result).value as com.libs.flex.ui.parser.internal.model.MappedNode
        val node = assertIs<CheckNode>(mapped.node)

        assertEquals("Accept Terms", node.label)
        assertTrue(node.defaultChecked)
        assertFalse(node.isSensitive)
    }

    @Test
    fun `CheckNodeMapper fails when label is missing`() {
        val mapper = CheckNodeMapper()
        val reader = FakePropertyReader()

        val result = mapper.map(reader, givenScope())
        val left = assertIs<Either.Left<*>>(result)
        val error = assertIs<ParserError.MissingRequiredProperty>((left.value as com.libs.flex.ui.core.domain.functional.Nel<*>).head)
        assertEquals("label", error.property)
    }

    @Test
    fun `CheckNodeMapper declares supported types`() {
        assertTrue(CheckNodeMapper.SUPPORTED_TYPES.contains("flexCheck"))
        assertTrue(CheckNodeMapper.SUPPORTED_TYPES.contains("checkbox"))
        assertTrue(CheckNodeMapper.SUPPORTED_TYPES.contains("componentCheckbox"))
        assertTrue(CheckNodeMapper.SUPPORTED_TYPES.contains("componentCheck"))
        assertTrue(CheckNodeMapper.SUPPORTED_TYPES.contains("check"))
    }

    // =========================================================================
    // 5. SelectNodeMapper Tests
    // =========================================================================

    @Test
    fun `SelectNodeMapper maps select dropdown with options`() {
        val mapper = SelectNodeMapper()
        val reader = givenJsonReader(
            """
            {
                "id": "country_select",
                "label": "Country",
                "options": [
                    {"label": "Chile", "value": "cl"},
                    {"label": "Argentina", "value": "ar"}
                ]
            }
            """.trimIndent()
        )

        val result = mapper.map(reader, givenScope())
        val mapped = assertIs<Either.Right<*>>(result).value as com.libs.flex.ui.parser.internal.model.MappedNode
        val node = assertIs<SelectNode>(mapped.node)

        assertEquals("Country", node.label)
        assertEquals(2, node.options.size)
        assertEquals("cl", node.options.head.value)
        assertEquals("Chile", node.options.head.label)
    }

    @Test
    fun `SelectNodeMapper fails when options are missing`() {
        val mapper = SelectNodeMapper()
        val reader = FakePropertyReader(strings = mapOf("label" to "Country"))

        val result = mapper.map(reader, givenScope())
        val left = assertIs<Either.Left<*>>(result)
        val error = assertIs<ParserError.MissingRequiredProperty>((left.value as com.libs.flex.ui.core.domain.functional.Nel<*>).head)
        assertEquals("options", error.property)
    }

    @Test
    fun `SelectNodeMapper declares supported types`() {
        assertTrue(SelectNodeMapper.SUPPORTED_TYPES.contains("flexSelect"))
        assertTrue(SelectNodeMapper.SUPPORTED_TYPES.contains("select"))
        assertTrue(SelectNodeMapper.SUPPORTED_TYPES.contains("dropdown"))
        assertTrue(SelectNodeMapper.SUPPORTED_TYPES.contains("componentSelect"))
    }

    // =========================================================================
    // 6. RadioGroupNodeMapper Tests
    // =========================================================================

    @Test
    fun `RadioGroupNodeMapper maps radio group to SelectNode with options`() {
        val mapper = RadioGroupNodeMapper()
        val reader = FakePropertyReader(
            strings = mapOf("label" to "Delivery Option"),
            selectOptions = listOf(
                SelectOption("standard", "Standard"),
                SelectOption("express", "Express")
            )
        )

        val result = mapper.map(reader, givenScope())
        val mapped = assertIs<Either.Right<*>>(result).value as com.libs.flex.ui.parser.internal.model.MappedNode
        val node = assertIs<SelectNode>(mapped.node)

        assertEquals("Delivery Option", node.label)
        assertEquals(2, node.options.size)
    }

    @Test
    fun `RadioGroupNodeMapper declares supported types`() {
        assertTrue(RadioGroupNodeMapper.SUPPORTED_TYPES.contains("flexRadioGroup"))
        assertTrue(RadioGroupNodeMapper.SUPPORTED_TYPES.contains("radioGroup"))
        assertTrue(RadioGroupNodeMapper.SUPPORTED_TYPES.contains("componentRadioGroup"))
    }

    // =========================================================================
    // 7. SliderNodeMapper Tests
    // =========================================================================

    @Test
    fun `SliderNodeMapper maps slider with min, max, step`() {
        val mapper = SliderNodeMapper()
        val reader = givenJsonReader(
            """
            {
                "id": "volume_slider",
                "min": 0.0,
                "max": 100.0,
                "step": 5.0
            }
            """.trimIndent()
        )

        val result = mapper.map(reader, givenScope())
        val mapped = assertIs<Either.Right<*>>(result).value as com.libs.flex.ui.parser.internal.model.MappedNode
        val node = assertIs<SliderNode>(mapped.node)

        assertEquals(0.0, node.range.min)
        assertEquals(100.0, node.range.max)
        assertEquals(5.0, node.range.step)
        assertEquals(5.0, node.step)
    }

    @Test
    fun `SliderNodeMapper fails when min is greater than max`() {
        val mapper = SliderNodeMapper()
        val reader = givenJsonReader(
            """
            {
                "id": "invalid_slider",
                "min": 100.0,
                "max": 10.0
            }
            """.trimIndent()
        )

        val result = mapper.map(reader, givenScope())
        val left = assertIs<Either.Left<*>>(result)
        val error = assertIs<ParserError.InvalidPropertyValue>((left.value as com.libs.flex.ui.core.domain.functional.Nel<*>).head)
        assertEquals("range", error.property)
    }

    @Test
    fun `SliderNodeMapper declares supported types`() {
        assertTrue(SliderNodeMapper.SUPPORTED_TYPES.contains("flexSliderCheck"))
        assertTrue(SliderNodeMapper.SUPPORTED_TYPES.contains("componentSliderCheck"))
        assertTrue(SliderNodeMapper.SUPPORTED_TYPES.contains("slider"))
        assertTrue(SliderNodeMapper.SUPPORTED_TYPES.contains("flexSlider"))
    }

    // =========================================================================
    // 8. SwitchNodeMapper Tests
    // =========================================================================

    @Test
    fun `SwitchNodeMapper maps switch to CheckNode`() {
        val mapper = SwitchNodeMapper()
        val reader = FakePropertyReader(
            strings = mapOf("label" to "Dark Mode"),
            booleans = mapOf("checked" to true)
        )

        val result = mapper.map(reader, givenScope())
        val mapped = assertIs<Either.Right<*>>(result).value as com.libs.flex.ui.parser.internal.model.MappedNode
        val node = assertIs<CheckNode>(mapped.node)

        assertEquals("Dark Mode", node.label)
        assertTrue(node.defaultChecked)
    }

    @Test
    fun `SwitchNodeMapper declares supported types`() {
        assertTrue(SwitchNodeMapper.SUPPORTED_TYPES.contains("flexSwitch"))
        assertTrue(SwitchNodeMapper.SUPPORTED_TYPES.contains("switch"))
        assertTrue(SwitchNodeMapper.SUPPORTED_TYPES.contains("toggle"))
        assertTrue(SwitchNodeMapper.SUPPORTED_TYPES.contains("componentSwitch"))
    }

    // =========================================================================
    // 9. ImageNodeMapper Tests
    // =========================================================================

    @Test
    fun `ImageNodeMapper maps image with url and default scaleMode`() {
        val mapper = ImageNodeMapper()
        val reader = FakePropertyReader(
            strings = mapOf("url" to "https://example.com/banner.png", "alt" to "Banner Image")
        )

        val result = mapper.map(reader, givenScope())
        val mapped = assertIs<Either.Right<*>>(result).value as com.libs.flex.ui.parser.internal.model.MappedNode
        val node = assertIs<ImageNode>(mapped.node)

        assertEquals("https://example.com/banner.png", node.url)
        assertEquals("Banner Image", node.contentDescription)
        assertEquals("fit", node.scaleMode)
    }

    @Test
    fun `ImageNodeMapper fails when url is missing`() {
        val mapper = ImageNodeMapper()
        val reader = FakePropertyReader()

        val result = mapper.map(reader, givenScope())
        val left = assertIs<Either.Left<*>>(result)
        val error = assertIs<ParserError.MissingRequiredProperty>((left.value as com.libs.flex.ui.core.domain.functional.Nel<*>).head)
        assertEquals("url", error.property)
    }

    @Test
    fun `ImageNodeMapper declares supported types`() {
        assertTrue(ImageNodeMapper.SUPPORTED_TYPES.contains("flexImage"))
        assertTrue(ImageNodeMapper.SUPPORTED_TYPES.contains("image"))
        assertTrue(ImageNodeMapper.SUPPORTED_TYPES.contains("componentImageView"))
        assertTrue(ImageNodeMapper.SUPPORTED_TYPES.contains("componentImage"))
    }

    // =========================================================================
    // 10. Loader, Toast, and Custom Node Mappers Tests
    // =========================================================================

    @Test
    fun `LoaderNodeMapper maps loader with variant and size`() {
        val mapper = LoaderNodeMapper()
        val reader = FakePropertyReader(
            strings = mapOf("variant" to "linear", "size" to "large")
        )

        val result = mapper.map(reader, givenScope())
        val mapped = assertIs<Either.Right<*>>(result).value as com.libs.flex.ui.parser.internal.model.MappedNode
        val node = assertIs<LoaderNode>(mapped.node)

        assertEquals("linear", node.variant)
        assertEquals("large", node.size)
    }

    @Test
    fun `ToastNodeMapper maps toast with message, type and duration`() {
        val mapper = ToastNodeMapper()
        val reader = FakePropertyReader(
            strings = mapOf("message" to "Operation completed successfully", "type" to "success"),
            ints = mapOf("durationMs" to 5000)
        )

        val result = mapper.map(reader, givenScope())
        val mapped = assertIs<Either.Right<*>>(result).value as com.libs.flex.ui.parser.internal.model.MappedNode
        val node = assertIs<ToastNode>(mapped.node)

        assertEquals("Operation completed successfully", node.message)
        assertEquals("success", node.type)
        assertEquals(5000L, node.durationMs)
    }

    @Test
    fun `CustomNodeMapper maps custom node with type`() {
        val mapper = CustomNodeMapper()
        val reader = FakePropertyReader(strings = mapOf("type" to "customBarcodeScanner"))

        val result = mapper.map(reader, givenScope())
        val mapped = assertIs<Either.Right<*>>(result).value as com.libs.flex.ui.parser.internal.model.MappedNode
        val node = assertIs<CustomNode>(mapped.node)

        assertEquals("customBarcodeScanner", node.type)
    }
}
