package com.libs.flex.ui.flexui.components.infrastructure.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.FabPosition
import androidx.compose.ui.Alignment
import com.libs.flex.ui.flexui.components.domain.model.LayoutEnum
import org.junit.Test

/**
 * Unit tests for LayoutMappers extension functions.
 *
 * Tests the mapping of string values to Compose layout properties,
 * ensuring correct conversions and default values.
 */
class LayoutMappersTest {

    @Test
    fun `toFabPosition should map center to FabPosition Center`() {
        // Given
        val position = LayoutEnum.CENTER.value

        // When
        val result = position.toFabPosition()

        // Then
        assert(result == FabPosition.Center)
    }

    @Test
    fun `toFabPosition should map start to FabPosition Start`() {
        // Given
        val position = LayoutEnum.START.value

        // When
        val result = position.toFabPosition()

        // Then
        assert(result == FabPosition.Start)
    }

    @Test
    fun `toFabPosition should map end to FabPosition End`() {
        // Given
        val position = LayoutEnum.END.value

        // When
        val result = position.toFabPosition()

        // Then
        assert(result == FabPosition.End)
    }

    @Test
    fun `toFabPosition should default to End for null`() {
        // Given
        val position: String? = null

        // When
        val result = position.toFabPosition()

        // Then
        assert(result == FabPosition.End)
    }

    @Test
    fun `toFabPosition should default to End for unknown value`() {
        // Given
        val position = "unknown"

        // When
        val result = position.toFabPosition()

        // Then
        assert(result == FabPosition.End)
    }

    @Test
    fun `toVerticalArrangement should map top to Arrangement Top`() {
        // Given
        val arrangement = LayoutEnum.TOP.value

        // When
        val result = arrangement.toVerticalArrangement()

        // Then
        assert(result == Arrangement.Top)
    }

    @Test
    fun `toVerticalArrangement should map center to Arrangement Center`() {
        // Given
        val arrangement = LayoutEnum.CENTER.value

        // When
        val result = arrangement.toVerticalArrangement()

        // Then
        assert(result == Arrangement.Center)
    }

    @Test
    fun `toVerticalArrangement should map bottom to Arrangement Bottom`() {
        // Given
        val arrangement = LayoutEnum.BOTTOM.value

        // When
        val result = arrangement.toVerticalArrangement()

        // Then
        assert(result == Arrangement.Bottom)
    }

    @Test
    fun `toVerticalArrangement should map spaceBetween to Arrangement SpaceBetween`() {
        // Given
        val arrangement = LayoutEnum.SPACE_BETWEEN.value

        // When
        val result = arrangement.toVerticalArrangement()

        // Then
        assert(result == Arrangement.SpaceBetween)
    }

    @Test
    fun `toVerticalArrangement should map spaceAround to Arrangement SpaceAround`() {
        // Given
        val arrangement = LayoutEnum.SPACE_AROUND.value

        // When
        val result = arrangement.toVerticalArrangement()

        // Then
        assert(result == Arrangement.SpaceAround)
    }

    @Test
    fun `toVerticalArrangement should map spaceEvenly to Arrangement SpaceEvenly`() {
        // Given
        val arrangement = LayoutEnum.SPACE_EVENLY.value

        // When
        val result = arrangement.toVerticalArrangement()

        // Then
        assert(result == Arrangement.SpaceEvenly)
    }

    @Test
    fun `toVerticalArrangement should default to Top for null`() {
        // Given
        val arrangement: String? = null

        // When
        val result = arrangement.toVerticalArrangement()

        // Then
        assert(result == Arrangement.Top)
    }

    @Test
    fun `toHorizontalArrangement should map start to Arrangement Start`() {
        // Given
        val arrangement = LayoutEnum.START.value

        // When
        val result = arrangement.toHorizontalArrangement()

        // Then
        assert(result == Arrangement.Start)
    }

    @Test
    fun `toHorizontalArrangement should map center to Arrangement Center`() {
        // Given
        val arrangement = LayoutEnum.CENTER.value

        // When
        val result = arrangement.toHorizontalArrangement()

        // Then
        assert(result == Arrangement.Center)
    }

    @Test
    fun `toHorizontalArrangement should map end to Arrangement End`() {
        // Given
        val arrangement = LayoutEnum.END.value

        // When
        val result = arrangement.toHorizontalArrangement()

        // Then
        assert(result == Arrangement.End)
    }

    @Test
    fun `toHorizontalArrangement should default to Start for null`() {
        // Given
        val arrangement: String? = null

        // When
        val result = arrangement.toHorizontalArrangement()

        // Then
        assert(result == Arrangement.Start)
    }

    @Test
    fun `toHorizontalAlignment should map start to Alignment Start`() {
        // Given
        val alignment = LayoutEnum.START.value

        // When
        val result = alignment.toHorizontalAlignment()

        // Then
        assert(result == Alignment.Start)
    }

    @Test
    fun `toHorizontalAlignment should map center to Alignment CenterHorizontally`() {
        // Given
        val alignment = LayoutEnum.CENTER.value

        // When
        val result = alignment.toHorizontalAlignment()

        // Then
        assert(result == Alignment.CenterHorizontally)
    }

    @Test
    fun `toHorizontalAlignment should map end to Alignment End`() {
        // Given
        val alignment = LayoutEnum.END.value

        // When
        val result = alignment.toHorizontalAlignment()

        // Then
        assert(result == Alignment.End)
    }

    @Test
    fun `toHorizontalAlignment should default to Start for null`() {
        // Given
        val alignment: String? = null

        // When
        val result = alignment.toHorizontalAlignment()

        // Then
        assert(result == Alignment.Start)
    }

    @Test
    fun `toVerticalAlignment should map top to Alignment Top`() {
        // Given
        val alignment = LayoutEnum.TOP.value

        // When
        val result = alignment.toVerticalAlignment()

        // Then
        assert(result == Alignment.Top)
    }

    @Test
    fun `toVerticalAlignment should map center to Alignment CenterVertically`() {
        // Given
        val alignment = LayoutEnum.CENTER.value

        // When
        val result = alignment.toVerticalAlignment()

        // Then
        assert(result == Alignment.CenterVertically)
    }

    @Test
    fun `toVerticalAlignment should map bottom to Alignment Bottom`() {
        // Given
        val alignment = LayoutEnum.BOTTOM.value

        // When
        val result = alignment.toVerticalAlignment()

        // Then
        assert(result == Alignment.Bottom)
    }

    @Test
    fun `toVerticalAlignment should default to CenterVertically for null`() {
        // Given
        val alignment: String? = null

        // When
        val result = alignment.toVerticalAlignment()

        // Then
        assert(result == Alignment.CenterVertically)
    }
}
