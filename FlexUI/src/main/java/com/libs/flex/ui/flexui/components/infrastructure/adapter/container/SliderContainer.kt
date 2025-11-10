package com.libs.flex.ui.flexui.components.infrastructure.adapter.container

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.libs.flex.ui.flexui.components.ComponentFactory
import com.libs.flex.ui.flexui.model.AtomicDescriptor
import com.libs.flex.ui.flexui.model.ComponentEvent
import com.libs.flex.ui.flexui.model.ComponentType
import com.libs.flex.ui.flexui.model.LayoutDescriptor
import kotlinx.coroutines.delay
import kotlinx.serialization.json.jsonPrimitive

/**
 * Renders an image slider/carousel with page indicators.
 *
 * This composable creates a HorizontalPager that displays images from the items array.
 * It includes animated page indicators and optional auto-play functionality.
 *
 * Features:
 * - Horizontal paging with swipe gestures
 * - Animated page indicators
 * - Auto-play with configurable interval
 * - Smooth page transitions
 *
 * @param descriptor Layout descriptor containing items array, autoPlay, and autoPlayInterval
 * @param onEvent Callback invoked when child components trigger events
 * @param modifier Modifier to be applied to the container
 * @param componentFactory Factory for creating image components
 *
 * Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 20.2
 */
@Composable
fun SliderContainer(
    descriptor: LayoutDescriptor,
    onEvent: (ComponentEvent) -> Unit,
    modifier: Modifier = Modifier,
    componentFactory: ComponentFactory
) {
    val items = descriptor.items ?: emptyList()
    val pagerState = rememberPagerState(pageCount = { items.size })
    val autoPlay = descriptor.autoPlay ?: false
    val interval = descriptor.autoPlayInterval ?: 3000L

    // Auto-play logic
    LaunchedEffect(autoPlay, pagerState.currentPage) {
        if (autoPlay && items.isNotEmpty()) {
            delay(interval)
            val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column(modifier = modifier) {
        // Horizontal pager for images
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val imageUrl = items.getOrNull(page)?.jsonPrimitive?.content

            // Create an image component descriptor for each page
            val imageDescriptor = AtomicDescriptor(
                id = "${descriptor.id}_image_$page",
                type = ComponentType.COMPONENT_IMAGE,
                imageUrl = imageUrl,
                contentScale = "crop"
            )

            componentFactory.CreateComponent(
                descriptor = imageDescriptor,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Page indicators
        if (items.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pagerState.pageCount) { index ->
                    val color by animateColorAsState(
                        targetValue = if (pagerState.currentPage == index)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        label = "indicator_color_$index"
                    )
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(8.dp)
                            .background(color, CircleShape)
                    )
                }
            }
        }
    }
}
