package com.libs.flex.ui.core.domain.value

import com.libs.flex.ui.core.domain.error.DomainError
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ValueObjectsTest {

    @Nested
    inner class SessionIdTests {
        @Test
        fun `valid raw creates SessionId`() {
            val result = SessionId.of("sess-123")
            assertTrue(result.isRight)
            assertEquals("sess-123", result.getOrNull()?.value)
        }

        @Test
        fun `empty or blank raw returns InvalidIdentifier`() {
            val emptyRes = SessionId.of("")
            assertTrue(emptyRes.isLeft)
            assertTrue(emptyRes.leftOrNull() is DomainError.Value.InvalidIdentifier)

            val blankRes = SessionId.of("   ")
            assertTrue(blankRes.isLeft)
            assertTrue(blankRes.leftOrNull() is DomainError.Value.InvalidIdentifier)
        }
    }

    @Nested
    inner class LayoutIdTests {
        @Test
        fun `valid raw creates LayoutId`() {
            val result = LayoutId.of("layout_home")
            assertTrue(result.isRight)
            assertEquals("layout_home", result.getOrNull()?.value)
        }

        @Test
        fun `blank raw returns InvalidIdentifier`() {
            val result = LayoutId.of("  ")
            assertTrue(result.isLeft)
        }
    }

    @Nested
    inner class ComponentIdTests {
        @Test
        fun `valid identifier creates ComponentId`() {
            val result = ComponentId.of("btn_submit-01")
            assertTrue(result.isRight)
            assertEquals("btn_submit-01", result.getOrNull()?.value)
        }

        @Test
        fun `invalid chars returns InvalidIdentifier`() {
            val result = ComponentId.of("btn.submit@1")
            assertTrue(result.isLeft)
        }

        @Test
        fun `too long returns InvalidIdentifier`() {
            val longId = "a".repeat(65)
            val result = ComponentId.of(longId)
            assertTrue(result.isLeft)
        }

        @Test
        fun `empty returns InvalidIdentifier`() {
            val result = ComponentId.of("")
            assertTrue(result.isLeft)
        }
    }

    @Nested
    inner class ActionIdTests {
        @Test
        fun `valid raw creates ActionId`() {
            val result = ActionId.of("action.submit")
            assertTrue(result.isRight)
            assertEquals("action.submit", result.getOrNull()?.value)
        }

        @Test
        fun `blank raw returns InvalidIdentifier`() {
            val result = ActionId.of("   ")
            assertTrue(result.isLeft)
        }
    }

    @Nested
    inner class HexColorTests {
        @Test
        fun `valid 6-digit hex color`() {
            val result = HexColor.of("#FFFFFF")
            assertTrue(result.isRight)
            assertEquals("#FFFFFF", result.getOrNull()?.value)
        }

        @Test
        fun `valid 8-digit hex color`() {
            val result = HexColor.of("#FF00FF00")
            assertTrue(result.isRight)
            assertEquals("#FF00FF00", result.getOrNull()?.value)
        }

        @Test
        fun `valid 3-digit hex color`() {
            val result = HexColor.of("#FFF")
            assertTrue(result.isRight)
            assertEquals("#FFF", result.getOrNull()?.value)
        }

        @Test
        fun `invalid hex color returns error`() {
            val result = HexColor.of("blue")
            assertTrue(result.isLeft)
            assertTrue(result.leftOrNull() is DomainError.Value.InvalidColorFormat)

            val invalidHex = HexColor.of("#12345")
            assertTrue(invalidHex.isLeft)
        }
    }

    @Nested
    inner class FontSizeTests {
        @Test
        fun `valid positive finite value creates Sp`() {
            val result = FontSize.Sp.of(16.0f)
            assertTrue(result.isRight)
            assertEquals(16.0f, result.getOrNull()?.value)
        }

        @Test
        fun `negative or zero returns InvalidDimension`() {
            val zeroRes = FontSize.Sp.of(0f)
            assertTrue(zeroRes.isLeft)

            val negRes = FontSize.Sp.of(-5f)
            assertTrue(negRes.isLeft)
        }

        @Test
        fun `NaN and Infinity return InvalidDimension`() {
            assertTrue(FontSize.Sp.of(Float.NaN).isLeft)
            assertTrue(FontSize.Sp.of(Float.POSITIVE_INFINITY).isLeft)
        }
    }

    @Nested
    inner class DimensionTests {
        @Test
        fun `valid non-negative finite value creates Dp`() {
            val result0 = Dimension.Dp.of(0f)
            assertTrue(result0.isRight)
            assertEquals(0f, result0.getOrNull()?.value)

            val result10 = Dimension.Dp.of(10.5f)
            assertTrue(result10.isRight)
            assertEquals(10.5f, result10.getOrNull()?.value)
        }

        @Test
        fun `negative value returns InvalidDimension`() {
            val result = Dimension.Dp.of(-1f)
            assertTrue(result.isLeft)
        }

        @Test
        fun `non-finite values return InvalidDimension`() {
            assertTrue(Dimension.Dp.of(Float.NaN).isLeft)
            assertTrue(Dimension.Dp.of(Float.NEGATIVE_INFINITY).isLeft)
        }

        @Test
        fun `MatchParent and WrapContent constants exist`() {
            assertNotNull(Dimension.MatchParent)
            assertNotNull(Dimension.WrapContent)
            assertEquals("MatchParent", Dimension.MatchParent.toString())
            assertEquals("WrapContent", Dimension.WrapContent.toString())
        }
    }

    @Nested
    inner class NumberRangeTests {
        @Test
        fun `valid range creates NumberRange`() {
            val result = NumberRange.of(0.0, 100.0, 1.0)
            assertTrue(result.isRight)
            val range = result.getOrNull()!!
            assertEquals(0.0, range.min)
            assertEquals(100.0, range.max)
            assertEquals(1.0, range.step)
        }

        @Test
        fun `min equal to or greater than max returns error`() {
            val res1 = NumberRange.of(100.0, 100.0, 1.0)
            assertTrue(res1.isLeft)

            val res2 = NumberRange.of(105.0, 100.0, 1.0)
            assertTrue(res2.isLeft)
        }

        @Test
        fun `step less than or equal to zero returns error`() {
            val resZero = NumberRange.of(0.0, 10.0, 0.0)
            assertTrue(resZero.isLeft)

            val resNeg = NumberRange.of(0.0, 10.0, -1.0)
            assertTrue(resNeg.isLeft)
        }

        @Test
        fun `step greater than diff returns error`() {
            val res = NumberRange.of(0.0, 10.0, 11.0)
            assertTrue(res.isLeft)
        }

        @Test
        fun `non-finite values return error`() {
            assertTrue(NumberRange.of(Double.NaN, 10.0, 1.0).isLeft)
            assertTrue(NumberRange.of(0.0, Double.POSITIVE_INFINITY, 1.0).isLeft)
            assertTrue(NumberRange.of(0.0, 10.0, Double.NaN).isLeft)
        }
    }

    @Nested
    inner class SchemaVersionAndPropertiesTests {
        @Test
        fun `SchemaVersion constants`() {
            assertEquals("1.0.0", SchemaVersion.CURRENT.value)
            assertTrue(SchemaVersion.SUPPORTED_VERSIONS.contains("1.0.0"))
        }

        @Test
        fun `PropertyValue hierarchy works`() {
            val str: PropertyValue = PropertyValue.Str("hello")
            val num: PropertyValue = PropertyValue.Num(42.0)
            val bool: PropertyValue = PropertyValue.Bool(true)
            val list: PropertyValue = PropertyValue.ListVal(listOf(str, num))
            val map: PropertyValue = PropertyValue.Obj(mapOf("key" to bool))

            assertTrue(str is PropertyValue.Str)
            assertTrue(num is PropertyValue.Num)
            assertTrue(bool is PropertyValue.Bool)
            assertTrue(list is PropertyValue.ListVal)
            assertTrue(map is PropertyValue.Obj)
        }

        @Test
        fun `ButtonVariant and InputVariant factory`() {
            assertEquals(ButtonVariant.Primary, ButtonVariant.of("primary"))
            assertEquals(ButtonVariant.Secondary, ButtonVariant.of("SECONDARY"))
            assertEquals(ButtonVariant.Text, ButtonVariant.of("Text"))
            assertEquals(ButtonVariant.Custom("outline"), ButtonVariant.of("outline"))

            assertEquals(InputVariant.Outlined, InputVariant.of("outlined"))
            assertEquals(InputVariant.Filled, InputVariant.of("FILLED"))
            assertEquals(InputVariant.Underlined, InputVariant.of("Underlined"))
            assertEquals(InputVariant.Custom("rounded"), InputVariant.of("rounded"))
        }

        @Test
        fun `NodeKey equality`() {
            val compId = ComponentId.of("comp-1").getOrNull()!!
            val key1 = NodeKey(compId, 0)
            val key2 = NodeKey(compId, 0)
            val key3 = NodeKey(compId, 1)

            assertEquals(key1, key2)
            assertNotEquals(key1, key3)
        }
    }
}
