package com.libs.flex.ui.core.domain.validation

import com.libs.flex.ui.core.domain.value.FieldValue
import com.libs.flex.ui.core.domain.value.NumberRange
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ValidationRulesTest {

    @Nested
    inner class RequiredRuleTests {
        private val rule = RequiredRule()

        @Test
        fun `null value fails`() {
            val result = rule.validate(null)
            assertTrue(result.isLeft)
            assertTrue(result.leftOrNull() is ValidationError.Required)
        }

        @Test
        fun `empty text fails`() {
            val result = rule.validate(FieldValue.Text(""))
            assertTrue(result.isLeft)

            val blankResult = rule.validate(FieldValue.Text("   "))
            assertTrue(blankResult.isLeft)
        }

        @Test
        fun `non-empty text passes`() {
            val result = rule.validate(FieldValue.Text("valid"))
            assertTrue(result.isRight)
        }

        @Test
        fun `empty selection fails`() {
            val result = rule.validate(FieldValue.Selection(emptyList()))
            assertTrue(result.isLeft)
        }

        @Test
        fun `non-empty selection passes`() {
            val result = rule.validate(FieldValue.Selection(listOf(1)))
            assertTrue(result.isRight)
        }

        @Test
        fun `bool and number pass`() {
            assertTrue(rule.validate(FieldValue.Bool(false)).isRight)
            assertTrue(rule.validate(FieldValue.Number(0.0)).isRight)
        }
    }

    @Nested
    inner class MinLengthRuleTests {
        private val rule = MinLengthRule.of(min = 3).getOrNull()!!

        @Test
        fun `invalid min length fails creation`() {
            val invalid = MinLengthRule.of(min = -1)
            assertTrue(invalid.isLeft)
        }

        @Test
        fun `null value passes (optional if not required)`() {
            val result = rule.validate(null)
            assertTrue(result.isRight)
        }

        @Test
        fun `short text fails`() {
            val result = rule.validate(FieldValue.Text("ab"))
            assertTrue(result.isLeft)
            val error = result.leftOrNull() as ValidationError.MinLength
            assertEquals(3, error.min)
            assertEquals(2, error.actual)
        }

        @Test
        fun `exact or longer text passes`() {
            assertTrue(rule.validate(FieldValue.Text("abc")).isRight)
            assertTrue(rule.validate(FieldValue.Text("abcd")).isRight)
        }

        @Test
        fun `non-text value fails with InvalidType`() {
            val result = rule.validate(FieldValue.Number(123.0))
            assertTrue(result.isLeft)
            assertTrue(result.leftOrNull() is ValidationError.InvalidType)
        }
    }

    @Nested
    inner class MaxLengthRuleTests {
        private val rule = MaxLengthRule.of(max = 5).getOrNull()!!

        @Test
        fun `negative max length fails creation`() {
            val invalid = MaxLengthRule.of(max = -1)
            assertTrue(invalid.isLeft)
            val error = invalid.leftOrNull()!!
            assertTrue(error.reason.contains("max length"))
        }

        @Test
        fun `null value passes`() {
            assertTrue(rule.validate(null).isRight)
        }

        @Test
        fun `too long text fails`() {
            val result = rule.validate(FieldValue.Text("123456"))
            assertTrue(result.isLeft)
            val error = result.leftOrNull() as ValidationError.MaxLength
            assertEquals(5, error.max)
            assertEquals(6, error.actual)
        }

        @Test
        fun `exact or shorter text passes`() {
            assertTrue(rule.validate(FieldValue.Text("12345")).isRight)
            assertTrue(rule.validate(FieldValue.Text("123")).isRight)
        }
    }

    @Nested
    inner class RegexRuleTests {
        private val rule = RegexRule.of(pattern = "^[0-9]+$").getOrNull()!!

        @Test
        fun `invalid regex pattern fails creation`() {
            val invalid = RegexRule.of(pattern = "[unclosed")
            assertTrue(invalid.isLeft)
        }

        @Test
        fun `null value passes`() {
            assertTrue(rule.validate(null).isRight)
        }

        @Test
        fun `matching text passes`() {
            assertTrue(rule.validate(FieldValue.Text("12345")).isRight)
        }

        @Test
        fun `non-matching text fails`() {
            val result = rule.validate(FieldValue.Text("123a45"))
            assertTrue(result.isLeft)
            assertTrue(result.leftOrNull() is ValidationError.PatternMismatch)
        }
    }

    @Nested
    inner class NumberRangeRuleTests {
        private val range = NumberRange.of(10.0, 50.0, 1.0).getOrNull()!!
        private val rule = NumberRangeRule(range = range)

        @Test
        fun `null value passes`() {
            assertTrue(rule.validate(null).isRight)
        }

        @Test
        fun `in-range number passes`() {
            assertTrue(rule.validate(FieldValue.Number(10.0)).isRight)
            assertTrue(rule.validate(FieldValue.Number(30.0)).isRight)
            assertTrue(rule.validate(FieldValue.Number(50.0)).isRight)
        }

        @Test
        fun `out-of-range number fails`() {
            val tooLow = rule.validate(FieldValue.Number(9.9))
            assertTrue(tooLow.isLeft)
            assertTrue(tooLow.leftOrNull() is ValidationError.NumberOutOfRange)

            val tooHigh = rule.validate(FieldValue.Number(50.1))
            assertTrue(tooHigh.isLeft)
            assertTrue(tooHigh.leftOrNull() is ValidationError.NumberOutOfRange)
        }

        @Test
        fun `non-number field fails with InvalidType`() {
            val result = rule.validate(FieldValue.Text("30"))
            assertTrue(result.isLeft)
            assertTrue(result.leftOrNull() is ValidationError.InvalidType)
        }
    }
}
