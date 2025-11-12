package com.libs.flex.ui.flexui.components.infrastructure.adapter.atomic

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.libs.flex.ui.flexui.R
import com.libs.flex.ui.flexui.components.infrastructure.util.toContentScale
import com.libs.flex.ui.flexui.model.AtomicDescriptor

/**
 * Renders an image component using Coil's AsyncImage.
 *
 * This composable creates an AsyncImage that loads images efficiently with
 * caching, crossfade animation, and error handling. The content scale mode
 * determines how the image fits within its bounds.
 *
 * Supported contentScale values:
 * - "fillBounds": Scale to fill bounds, may distort aspect ratio
 * - "fit": Scale to fit within bounds maintaining aspect ratio (default)
 * - "crop": Scale to fill bounds and crop to maintain aspect ratio
 * - "inside": Scale down to fit if larger, otherwise display at original size
 * - "none": Display at original size without scaling
 *
 * @param descriptor Atomic descriptor containing image URL and scale properties
 * @param modifier Modifier to be applied to the AsyncImage
 *
 * Requirements: 15.1, 15.2, 15.3, 15.4, 15.5, 20.5
 */
@Composable
fun ImageComponent(
    descriptor: AtomicDescriptor,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(descriptor.imageUrl)
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build(),
        contentDescription = descriptor.text,
        modifier = modifier,
        contentScale = descriptor.contentScale.toContentScale(),
        placeholder = painterResource(R.drawable.ic_outline_error),
        error = painterResource(R.drawable.ic_outline_error)
    )
}
