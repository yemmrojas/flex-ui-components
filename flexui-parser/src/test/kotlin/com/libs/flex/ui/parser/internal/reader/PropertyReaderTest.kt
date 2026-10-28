package com.libs.flex.ui.parser.internal.reader

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserMode
import com.libs.flex.ui.parser.ParserWarning
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PropertyReaderTest {

    private fun parseJsonObject(json: String): JsonObject =
        Json.parseToJsonElement(json).jsonObject

    @Test
    fun `requiredString resolves with primary key or alias`() {
        val json = parseJsonObject("""{"label": "Submit Form"}""")
        val reader: PropertyReader = JsonPropertyReader(json, "root.btn", StrictErrorPolicy()) {}

        val result = reader.requiredString("text", "label")
        val value = assertIs<Either.Right<String>>(result).value
        assertEquals("Submit Form", value)
    }

    @Test
    fun `requiredString returns Left when missing`() {
        val json = parseJsonObject("""{"id": "btn_1"}""")
        val reader: PropertyReader = JsonPropertyReader(json, "root.btn", StrictErrorPolicy()) {}

        val result = reader.requiredString("text", "label")
        val left = assertIs<Either.Left<*>>(result)
        val error = assertIs<ParserError.MissingRequiredProperty>((left.value as com.libs.flex.ui.core.domain.functional.Nel<*>).head)
        assertEquals("text", error.property)
        assertEquals("root.btn", error.path)
    }

    @Test
    fun `requiredString returns Left without exception when value is not a primitive string`() {
        val json = parseJsonObject("""{"text": [1, 2, 3]}""")
        val reader: PropertyReader = JsonPropertyReader(json, "root.btn", StrictErrorPolicy()) {}

        val result = reader.requiredString("text")
        val left = assertIs<Either.Left<*>>(result)
        val error = assertIs<ParserError.InvalidPropertyValue>((left.value as com.libs.flex.ui.core.domain.functional.Nel<*>).head)
        assertEquals("text", error.property)
    }

    @Test
    fun `requiredString and optionalString reject non-string primitives like number or boolean`() {
        val jsonNumber = parseJsonObject("""{"text": 123, "label": true}""")
        val reader: PropertyReader = JsonPropertyReader(jsonNumber, "root.btn", StrictErrorPolicy()) {}

        val requiredRes = reader.requiredString("text")
        val requiredLeft = assertIs<Either.Left<*>>(requiredRes)
        val reqError = assertIs<ParserError.InvalidPropertyValue>((requiredLeft.value as com.libs.flex.ui.core.domain.functional.Nel<*>).head)
        assertEquals("text", reqError.property)
        assertTrue(reqError.reason.contains("number"))

        val optionalRes = reader.optionalString("label")
        val optionalLeft = assertIs<Either.Left<*>>(optionalRes)
        val optError = assertIs<ParserError.InvalidPropertyValue>((optionalLeft.value as com.libs.flex.ui.core.domain.functional.Nel<*>).head)
        assertEquals("label", optError.property)
        assertTrue(optError.reason.contains("string primitive"))
    }

    @Test
    fun `optionalBoolean resolves alias checked and defaultChecked`() {
        val jsonChecked = parseJsonObject("""{"checked": true}""")
        val reader1: PropertyReader = JsonPropertyReader(jsonChecked, "root.check", StrictErrorPolicy()) {}
        val val1 = assertIs<Either.Right<Boolean>>(reader1.optionalBoolean("defaultChecked", "checked")).value
        assertTrue(val1)

        val jsonDefaultChecked = parseJsonObject("""{"defaultChecked": true}""")
        val reader2: PropertyReader = JsonPropertyReader(jsonDefaultChecked, "root.check", StrictErrorPolicy()) {}
        val val2 = assertIs<Either.Right<Boolean>>(reader2.optionalBoolean("defaultChecked", "checked")).value
        assertTrue(val2)
    }

    @Test
    fun `optionalColor in STRICT mode returns Left on invalid hex`() {
        val json = parseJsonObject("""{"color": "not-a-color"}""")
        val reader: PropertyReader = JsonPropertyReader(json, "root.text", StrictErrorPolicy()) {}

        val result = reader.optionalColor("color")
        assertIs<Either.Left<*>>(result)
    }

    @Test
    fun `optionalColor in TOLERANT mode returns default and emits warning`() {
        val json = parseJsonObject("""{"color": "not-a-color"}""")
        val warnings = mutableListOf<ParserWarning>()
        val reader: PropertyReader = JsonPropertyReader(json, "root.text", TolerantErrorPolicy()) { warnings.add(it) }

        val result = reader.optionalColor("color")
        val right = assertIs<Either.Right<*>>(result)
        assertEquals(null, right.value)
        assertEquals(1, warnings.size)
        assertEquals("INVALID_OPTIONAL_PROPERTY", warnings.first().code)
    }

    @Test
    fun `readCommonProperties reads id and nested style object`() {
        val json = parseJsonObject(
            """
            {
                "id": "header.title",
                "style": {
                    "backgroundColor": "#FF0000",
                    "padding": 16.0,
                    "borderRadius": 8.0
                }
            }
            """.trimIndent()
        )
        val reader: PropertyReader = JsonPropertyReader(json, "root.header", StrictErrorPolicy()) {}

        val result = reader.readCommonProperties()
        val common = assertIs<Either.Right<*>>(result).value as com.libs.flex.ui.parser.internal.model.CommonProperties

        assertEquals("header_title", common.id.value) // id saneado
        assertEquals("#FF0000", common.style.backgroundColor?.value)
        assertEquals(16f, common.style.padding?.start?.value)
        assertEquals(8f, common.style.borderRadius?.value)
    }

    @Test
    fun `readCommonProperties generates safe id when missing`() {
        val json = parseJsonObject("""{}""")
        val reader: PropertyReader = JsonPropertyReader(json, "root.children_0", StrictErrorPolicy()) {}

        val result = reader.readCommonProperties()
        val common = assertIs<Either.Right<*>>(result).value as com.libs.flex.ui.parser.internal.model.CommonProperties

        assertNotNull(common.id)
        assertTrue(common.id.value.startsWith("auto_root_children_0"))
    }

    @Test
    fun `FakePropertyReader allows testing without any JsonObject`() {
        val fakeReader: PropertyReader = com.libs.flex.ui.parser.fakes.FakePropertyReader(
            path = "root.fake",
            strings = mapOf("text" to "Click Me", "color" to "#00FF00"),
            booleans = mapOf("isEnabled" to true)
        )

        assertEquals("Click Me", assertIs<Either.Right<String>>(fakeReader.requiredString("text")).value)
        assertTrue(assertIs<Either.Right<Boolean>>(fakeReader.optionalBoolean("isEnabled")).value)
        assertEquals("test_id", assertIs<Either.Right<*>>(fakeReader.readCommonProperties()).value.let {
            (it as com.libs.flex.ui.parser.internal.model.CommonProperties).id.value
        })
    }

    @Test
    fun `optionalFontSize returns FontSize on valid float`() {
        val json = parseJsonObject("""{"fontSize": 16.5}""")
        val reader: PropertyReader = JsonPropertyReader(json, "root.text", StrictErrorPolicy()) {}

        val result = reader.optionalFontSize("fontSize")
        val fontSize = assertIs<Either.Right<com.libs.flex.ui.core.domain.value.FontSize?>>(result).value
        assertNotNull(fontSize)
        assertEquals(16.5f, fontSize.value)
    }

    @Test
    fun `readSelectOptions parses array of options`() {
        val json = parseJsonObject(
            """
            {
                "options": [
                    {"label": "Option 1", "value": "opt_1"},
                    {"label": "Option 2", "value": "opt_2"}
                ]
            }
            """.trimIndent()
        )
        val reader: PropertyReader = JsonPropertyReader(json, "root.select", StrictErrorPolicy()) {}

        val result = reader.readSelectOptions("options")
        val options = assertIs<Either.Right<*>>(result).value as com.libs.flex.ui.core.domain.functional.Nel<*>
        assertEquals(2, options.size)
        val first = options.head as com.libs.flex.ui.core.domain.model.node.SelectOption
        assertEquals("opt_1", first.value)
        assertEquals("Option 1", first.label)
    }

    @Test
    fun `readSelectOptions in STRICT mode propagates invalid option label error`() {
        val json = parseJsonObject(
            """
            {
                "options": [
                    {"value": "opt_1", "label": 123}
                ]
            }
            """.trimIndent()
        )
        val reader: PropertyReader = JsonPropertyReader(json, "root.select", StrictErrorPolicy()) {}

        val result = reader.readSelectOptions("options")
        val left = assertIs<Either.Left<*>>(result)
        val error = assertIs<ParserError.InvalidPropertyValue>((left.value as com.libs.flex.ui.core.domain.functional.Nel<*>).head)
        assertEquals("label", error.property)
    }

    @Test
    fun `readSelectOptions in TOLERANT mode emits warning and falls back to value for invalid label`() {
        val json = parseJsonObject(
            """
            {
                "options": [
                    {"value": "opt_1", "label": 123}
                ]
            }
            """.trimIndent()
        )
        val warnings = mutableListOf<ParserWarning>()
        val reader: PropertyReader = JsonPropertyReader(json, "root.select", TolerantErrorPolicy()) { warnings.add(it) }

        val result = reader.readSelectOptions("options")
        val options = assertIs<Either.Right<*>>(result).value as com.libs.flex.ui.core.domain.functional.Nel<*>
        assertEquals(1, options.size)
        val first = options.head as com.libs.flex.ui.core.domain.model.node.SelectOption
        assertEquals("opt_1", first.value)
        assertEquals("opt_1", first.label)
        assertEquals(1, warnings.size)
        assertEquals("INVALID_OPTIONAL_PROPERTY", warnings.first().code)
    }

    @Test
    fun `readNumberRange parses min max step`() {
        val json = parseJsonObject("""{"min": 10.0, "max": 50.0, "step": 5.0}""")
        val reader: PropertyReader = JsonPropertyReader(json, "root.slider", StrictErrorPolicy()) {}

        val result = reader.readNumberRange("min", "max", "step")
        val range = assertIs<Either.Right<com.libs.flex.ui.core.domain.value.NumberRange>>(result).value
        assertEquals(10.0, range.min)
        assertEquals(50.0, range.max)
        assertEquals(5.0, range.step)
    }

    @Test
    fun `readNumberRange in STRICT mode propagates malformed range bound error`() {
        val json = parseJsonObject("""{"range": {"min": "invalid_min", "max": 100.0}}""")
        val reader: PropertyReader = JsonPropertyReader(json, "root.slider", StrictErrorPolicy()) {}

        val result = reader.readNumberRange("min", "max", "step")
        val left = assertIs<Either.Left<*>>(result)
        val error = assertIs<ParserError.InvalidPropertyValue>((left.value as com.libs.flex.ui.core.domain.functional.Nel<*>).head)
        assertEquals("min", error.property)
    }

    @Test
    fun `readNumberRange in TOLERANT mode emits warning and uses fallback for malformed bound`() {
        val json = parseJsonObject("""{"range": {"min": "invalid_min", "max": 100.0}}""")
        val warnings = mutableListOf<ParserWarning>()
        val reader: PropertyReader = JsonPropertyReader(json, "root.slider", TolerantErrorPolicy()) { warnings.add(it) }

        val result = reader.readNumberRange("min", "max", "step", defaultMin = 0.0)
        val range = assertIs<Either.Right<com.libs.flex.ui.core.domain.value.NumberRange>>(result).value
        assertEquals(0.0, range.min)
        assertEquals(100.0, range.max)
        assertEquals(1, warnings.size)
    }

    @Test
    fun `readValidationRules parses rules array`() {
        val json = parseJsonObject(
            """
            {
                "validations": [
                    {"type": "required", "message": "Field required"}
                ]
            }
            """.trimIndent()
        )
        val reader: PropertyReader = JsonPropertyReader(json, "root.input", StrictErrorPolicy()) {}

        val result = reader.readValidationRules("validations")
        val rules = assertIs<Either.Right<List<*>>>(result).value
        assertEquals(1, rules.size)
        assertIs<com.libs.flex.ui.core.domain.validation.RequiredRule>(rules.first())
    }

    @Test
    fun `readCommonProperties in STRICT mode propagates invalid style property error`() {
        val json = parseJsonObject(
            """
            {
                "id": "btn_1",
                "style": {
                    "backgroundColor": "invalid_color_not_hex"
                }
            }
            """.trimIndent()
        )
        val reader: PropertyReader = JsonPropertyReader(json, "root.btn", StrictErrorPolicy()) {}

        val result = reader.readCommonProperties()
        val left = assertIs<Either.Left<*>>(result)
        val error = assertIs<ParserError.InvalidPropertyValue>((left.value as com.libs.flex.ui.core.domain.functional.Nel<*>).head)
        assertEquals("backgroundColor", error.property)
    }

    @Test
    fun `readCommonProperties in TOLERANT mode emits warning and succeeds with null for invalid style`() {
        val json = parseJsonObject(
            """
            {
                "id": "btn_1",
                "style": {
                    "backgroundColor": "invalid_color_not_hex"
                }
            }
            """.trimIndent()
        )
        val warnings = mutableListOf<ParserWarning>()
        val reader: PropertyReader = JsonPropertyReader(json, "root.btn", TolerantErrorPolicy()) { warnings.add(it) }

        val result = reader.readCommonProperties()
        val common = assertIs<Either.Right<com.libs.flex.ui.parser.internal.model.CommonProperties>>(result).value
        assertEquals("btn_1", common.id.value)
        assertEquals(null, common.style.backgroundColor)
        assertEquals(1, warnings.size)
    }
}
