package com.libs.flex.ui.flexui.components.infrastructure.adapter.atomic

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.libs.flex.ui.flexui.R
import com.libs.flex.ui.flexui.model.AtomicDescriptor

/**
 * Renders an image component from an AtomicDescriptor using Coil's AsyncImage.
 *
 * Supports:
 * - AsyncImage from Coil library for efficient image loading
 * - Image loading from imageUrl property
 * - ContentScale mapping (fit, crop, fillWidth, fillHeight)
 * - ImageRequest configuration with crossfade, memory cache, and disk cache
 * - Placeholder display while loading and error image on failure
 *
 * @param descriptor The atomic descriptor containing image properties
 * @param modifier Modifier to be applied to the AsyncImage
 *
 * Requirements: 15.1, 15.2, 15.3, 15.4, 15.5, 20.5
 */
@Composable
fun ImageComponent(
    descriptor: AtomicDescriptor,
    modifier: Modifier = Modifier
) {
    val contentScale = when (descriptor.contentScale) {
        "fit" -> ContentScale.Fit
        "crop" -> ContentScale.Crop
        "fillWidth" -> ContentScale.FillWidth
        "fillHeight" -> ContentScale.FillHeight
        else -> ContentScale.Fit
    }

    val context = LocalContext.current

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(descriptor.imageUrl)
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build(),
        contentDescription = descriptor.text,
        modifier = modifier,
        contentScale = contentScale,
        placeholder = painterResource(R.drawable.ic_outline_error),
        error = painterResource(R.drawable.ic_outline_error)
    )
}
