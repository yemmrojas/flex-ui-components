package com.libs.flex.ui.core.domain.value

public data class PaddingValues(
    public val start: Dimension.Dp? = null,
    public val top: Dimension.Dp? = null,
    public val end: Dimension.Dp? = null,
    public val bottom: Dimension.Dp? = null
)

public data class StyleProperties(
    public val padding: PaddingValues? = null,
    public val margin: PaddingValues? = null,
    public val backgroundColor: HexColor? = null,
    public val borderRadius: Dimension.Dp? = null,
    public val elevation: Dimension.Dp? = null,
    public val width: Dimension? = null,
    public val height: Dimension? = null,
    public val weight: Float? = null
)
