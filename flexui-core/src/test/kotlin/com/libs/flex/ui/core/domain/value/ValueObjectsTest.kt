package com.libs.flex.ui.core.domain.value

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

 class ValueObjectsTest {

    @Test
     fun `SessionId of validates non-empty and trims`() {
        val valid = SessionId.of("  sess_1  ")
        assertTrue(valid.isRight)
        assertEquals("sess_1", valid.getOrNull()?.value)

        val invalid = SessionId.of("   ")
        assertTrue(invalid.isLeft)
    }

    @Test
     fun `LayoutId of validates non-empty`() {
        val valid = LayoutId.of("layout_login")
        assertTrue(valid.isRight)
        assertEquals("layout_login", valid.getOrNull()?.value)

        val invalid = LayoutId.of("")
        assertTrue(invalid.isLeft)
    }

    @Test
     fun `ComponentId of validates allowed characters and length`() {
        val valid = ComponentId.of("input-email_1")
        assertTrue(valid.isRight)
        assertEquals("input-email_1", valid.getOrNull()?.value)

        val withSpaces = ComponentId.of("invalid name")
        assertTrue(withSpaces.isLeft)

        val withSymbols = ComponentId.of("input@name")
        assertTrue(withSymbols.isLeft)
    }

    @Test
     fun `ActionId of validates non-empty`() {
        val valid = ActionId.of("submit_form")
        assertTrue(valid.isRight)
        assertEquals("submit_form", valid.getOrNull()?.value)

        val invalid = ActionId.of("")
        assertTrue(invalid.isLeft)
    }

    @Test
     fun `HexColor of validates 6, 8, and 3 digit hex codes`() {
        assertTrue(HexColor.of("#FFF").isRight)
        assertTrue(HexColor.of("#FFFFFF").isRight)
        assertTrue(HexColor.of("#FFFFFFFF").isRight)

        assertTrue(HexColor.of("FFF").isLeft)
        assertTrue(HexColor.of("#GGG").isLeft)
        assertTrue(HexColor.of("#12345").isLeft)
    }

    @Test
     fun `Dimension Dp validates finite and non-negative values`() {
        val valid = Dimension.Dp.of(16.5f)
        assertTrue(valid.isRight)
        assertEquals(16.5f, (valid.getOrNull() as Dimension.Dp).value)

        val negative = Dimension.Dp.of(-1f)
        assertTrue(negative.isLeft)

        val nan = Dimension.Dp.of(Float.NaN)
        assertTrue(nan.isLeft)

        val infinity = Dimension.Dp.of(Float.POSITIVE_INFINITY)
        assertTrue(infinity.isLeft)
    }

    @Test
     fun `FontSize validates finite and positive values`() {
        val valid = FontSize.of(14f)
        assertTrue(valid.isRight)
        assertEquals(14f, valid.getOrNull()?.value)

        val zero = FontSize.of(0f)
        assertTrue(zero.isLeft)

        val negative = FontSize.of(-10f)
        assertTrue(negative.isLeft)

        val nan = FontSize.of(Float.NaN)
        assertTrue(nan.isLeft)
    }

    @Test
    fun `NumberRange of validates finite numbers and bounds`() {
        val valid = NumberRange.of(0.0, 100.0, 1.0)
        assertTrue(valid.isRight)

        val inverted = NumberRange.of(100.0, 0.0, 1.0)
        assertTrue(inverted.isLeft)

        val stepTooLarge = NumberRange.of(0.0, 10.0, 20.0)
        assertTrue(stepTooLarge.isLeft)

        val nan = NumberRange.of(Double.NaN, 10.0, 1.0)
        assertTrue(nan.isLeft)
    }
}
