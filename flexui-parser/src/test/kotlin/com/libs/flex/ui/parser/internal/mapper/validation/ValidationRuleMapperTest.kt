package com.libs.flex.ui.parser.internal.mapper.validation

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.validation.MaxLengthRule
import com.libs.flex.ui.core.domain.validation.MinLengthRule
import com.libs.flex.ui.core.domain.validation.NumberRangeRule
import com.libs.flex.ui.core.domain.validation.RegexRule
import com.libs.flex.ui.core.domain.validation.RequiredRule
import com.libs.flex.ui.parser.internal.reader.StrictErrorPolicy
import com.libs.flex.ui.parser.internal.reader.TolerantErrorPolicy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ValidationRuleMapperTest {

    private fun parseJsonObject(json: String): JsonObject =
        Json.parseToJsonElement(json).jsonObject

    private fun parseJsonArray(json: String): JsonArray =
        Json.parseToJsonElement(json).jsonArray

    @Test
    fun `maps required rule with custom message`() {
        val json = parseJsonObject("""{"type": "required", "message": "Email is required"}""")
        val result = ValidationRuleMapper.mapRule(json, "root.input.validations[0]", StrictErrorPolicy()) {}

        val rule = assertIs<Either.Right<RequiredRule>>(result).value
        assertEquals("Email is required", rule.customMessage)
    }

    @Test
    fun `maps minLength rule with min property`() {
        val json = parseJsonObject("""{"type": "minLength", "min": 5, "message": "Min 5 chars"}""")
        val result = ValidationRuleMapper.mapRule(json, "root.input.validations[0]", StrictErrorPolicy()) {}

        val rule = assertIs<Either.Right<MinLengthRule>>(result).value
        assertEquals(5, rule.min)
        assertEquals("Min 5 chars", rule.customMessage)
    }

    @Test
    fun `maps maxLength rule with max property`() {
        val json = parseJsonObject("""{"type": "maxLength", "max": 20}""")
        val result = ValidationRuleMapper.mapRule(json, "root.input.validations[0]", StrictErrorPolicy()) {}

        val rule = assertIs<Either.Right<MaxLengthRule>>(result).value
        assertEquals(20, rule.max)
    }

    @Test
    fun `maps regex pattern rule`() {
        val json = parseJsonObject("""{"type": "regex", "pattern": "^[0-9]+$", "message": "Only numbers"}""")
        val result = ValidationRuleMapper.mapRule(json, "root.input.validations[0]", StrictErrorPolicy()) {}

        val rule = assertIs<Either.Right<RegexRule>>(result).value
        assertEquals("^[0-9]+$", rule.pattern)
    }

    @Test
    fun `regex with invalid pattern returns Left in STRICT mode`() {
        val json = parseJsonObject("""{"type": "regex", "pattern": "["}""")
        val result = ValidationRuleMapper.mapRule(json, "root.input.validations[0]", StrictErrorPolicy()) {}

        assertIs<Either.Left<*>>(result)
    }

    @Test
    fun `maps numberRange rule`() {
        val json = parseJsonObject("""{"type": "numberRange", "min": 0.0, "max": 100.0, "step": 1.0}""")
        val result = ValidationRuleMapper.mapRule(json, "root.slider.validations[0]", StrictErrorPolicy()) {}

        val rule = assertIs<Either.Right<NumberRangeRule>>(result).value
        assertEquals(0.0, rule.range.min)
        assertEquals(100.0, rule.range.max)
        assertEquals(1.0, rule.range.step)
    }

    @Test
    fun `mapRules maps array of multiple validation rules`() {
        val jsonArray = parseJsonArray(
            """
            [
                {"type": "required"},
                {"type": "minLength", "min": 3},
                {"type": "maxLength", "max": 10}
            ]
            """.trimIndent()
        )

        val result = ValidationRuleMapper.mapRules(jsonArray, "root.input.validations", StrictErrorPolicy()) {}
        val rules = assertIs<Either.Right<List<*>>>(result).value

        assertEquals(3, rules.size)
        assertIs<RequiredRule>(rules[0])
        assertIs<MinLengthRule>(rules[1])
        assertIs<MaxLengthRule>(rules[2])
    }

    @Test
    fun `unknown rule type in TOLERANT mode emits warning and omits rule`() {
        val json = parseJsonObject("""{"type": "magicValidationRule"}""")
        val warnings = mutableListOf<com.libs.flex.ui.parser.ParserWarning>()

        val result = ValidationRuleMapper.mapRule(json, "root.input.validations[0]", TolerantErrorPolicy()) {
            warnings.add(it)
        }

        val right = assertIs<Either.Right<*>>(result)
        assertEquals(null, right.value)
        assertEquals(1, warnings.size)
        assertTrue(warnings.first().message.contains("magicValidationRule"))
    }

    @Test
    fun `minLength with malformed min returns Left in STRICT mode`() {
        val json = parseJsonObject("""{"type": "minLength", "min": "invalid_number"}""")
        val result = ValidationRuleMapper.mapRule(json, "root.input.validations[0]", StrictErrorPolicy()) {}

        val left = assertIs<Either.Left<*>>(result)
        val error = assertIs<com.libs.flex.ui.parser.ParserError.InvalidPropertyValue>((left.value as com.libs.flex.ui.core.domain.functional.Nel<*>).head)
        assertEquals("min", error.property)
    }

    @Test
    fun `maxLength with malformed max returns Left in STRICT mode`() {
        val json = parseJsonObject("""{"type": "maxLength", "max": "invalid_number"}""")
        val result = ValidationRuleMapper.mapRule(json, "root.input.validations[0]", StrictErrorPolicy()) {}

        val left = assertIs<Either.Left<*>>(result)
        val error = assertIs<com.libs.flex.ui.parser.ParserError.InvalidPropertyValue>((left.value as com.libs.flex.ui.core.domain.functional.Nel<*>).head)
        assertEquals("max", error.property)
    }

    @Test
    fun `minLength with malformed min in TOLERANT mode emits warning and continues`() {
        val json = parseJsonObject("""{"type": "minLength", "min": "invalid_number"}""")
        val warnings = mutableListOf<com.libs.flex.ui.parser.ParserWarning>()
        val result = ValidationRuleMapper.mapRule(json, "root.input.validations[0]", TolerantErrorPolicy()) {
            warnings.add(it)
        }

        val right = assertIs<Either.Right<*>>(result)
        val rule = assertIs<MinLengthRule>(right.value)
        assertEquals(0, rule.min)
        assertEquals(1, warnings.size)
    }
}
