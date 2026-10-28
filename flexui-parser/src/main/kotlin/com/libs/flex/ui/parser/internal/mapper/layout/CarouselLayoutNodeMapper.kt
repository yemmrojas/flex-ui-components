package com.libs.flex.ui.parser.internal.mapper.layout

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.functional.zip
import com.libs.flex.ui.core.domain.model.node.ImageNode
import com.libs.flex.ui.core.domain.model.node.SliderLayoutNode
import com.libs.flex.ui.core.domain.model.node.UIComponentNode
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.internal.mapper.NodeMapper
import com.libs.flex.ui.parser.internal.model.MappedNode
import com.libs.flex.ui.parser.internal.reader.PropertyReader
import com.libs.flex.ui.parser.internal.scope.MappingScope

private const val PRIMARY_TYPE = "flexCarousel"
private val ALIASES = setOf("flexSlider", "contentSlider", "imageSlider")

private const val KEY_AUTOPLAY = "autoPlay"
private const val KEY_INTERVAL_MS = "intervalMs"
private const val ALIAS_AUTOPLAY_INTERVAL = "autoPlayInterval"
private const val KEY_ITEMS = "items"
private const val DEFAULT_AUTOPLAY = false
private const val DEFAULT_INTERVAL_MS = 3000L

/**
 * Mapeador para carruseles y sliders ([SliderLayoutNode]).
 *
 * Soporta nodos hijos declarativos directos o una lista de URLs de imágenes bajo `items`.
 */
internal class CarouselLayoutNodeMapper : NodeMapper {

    override val types: Set<String> = setOf(PRIMARY_TYPE) + ALIASES

    override fun map(
        reader: PropertyReader,
        scope: MappingScope
    ): Either<Nel<ParserError>, MappedNode> {
        val commonResult = reader.readCommonProperties()
        val autoPlayResult = reader.optionalBoolean(KEY_AUTOPLAY, default = DEFAULT_AUTOPLAY)
        val intervalResult = reader.optionalInt(KEY_INTERVAL_MS, ALIAS_AUTOPLAY_INTERVAL)
        val childrenResult = reader.readChildren(scope)

        return commonResult
            .zip(autoPlayResult) { common, autoPlay -> Pair(common, autoPlay) }
            .zip(intervalResult) { (common, autoPlay), interval ->
                Triple(common, autoPlay, interval?.toLong() ?: DEFAULT_INTERVAL_MS)
            }
            .zip(childrenResult) { (common, autoPlay, interval), children ->
                val finalChildren: List<UIComponentNode> = if (children.children.isNotEmpty()) {
                    children.children
                } else {
                    buildImageNodesFromItems(reader.optionalStringList(KEY_ITEMS), common.id)
                }

                MappedNode(
                    node = SliderLayoutNode(
                        id = common.id,
                        style = common.style,
                        autoPlay = autoPlay,
                        intervalMs = interval,
                        children = finalChildren
                    ),
                    templates = children.templates
                )
            }
    }

    private fun buildImageNodesFromItems(urls: List<String>, parentId: ComponentId): List<UIComponentNode> {
        return urls.mapIndexed { index, url ->
            val itemId = ComponentId.of("${parentId.value}_item_$index").fold(
                onLeft = { parentId },
                onRight = { it }
            )
            ImageNode(
                id = itemId,
                url = url
            )
        }
    }
}

