package com.neonull.protocol.assets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawImage
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Draws a manifest-backed still image or sprite animation.
 *
 * Missing planned assets render the caller-provided fallback instead of crashing.
 * Bitmap decoding happens off the main thread; the catalog owns bitmap caching.
 */
@Composable
fun GameAssetSprite(
    catalog: GameAssetCatalog,
    assetKey: String,
    modifier: Modifier = Modifier,
    playing: Boolean = true,
    fallback: @Composable () -> Unit = {}
) {
    val spec = remember(catalog, assetKey) { catalog.find(assetKey) }
    val image by produceState<ImageBitmap?>(initialValue = null, catalog, assetKey) {
        value = withContext(Dispatchers.IO) {
            catalog.loadBitmap(assetKey)?.asImageBitmap()
        }
    }

    if (spec == null || image == null) {
        Box(modifier = modifier) { fallback() }
        return
    }

    val frameCount = if (
        spec.category == GameAssetCategory.SPRITESHEET ||
        spec.category == GameAssetCategory.EFFECT
    ) spec.frameCount else 1
    var frameIndex by remember(assetKey) { mutableIntStateOf(0) }

    LaunchedEffect(assetKey, playing, frameCount, spec.framesPerSecond, spec.loop) {
        frameIndex = 0
        if (!playing || frameCount <= 1) return@LaunchedEffect
        val frameDelay = (1000L / spec.framesPerSecond.coerceAtLeast(1)).coerceAtLeast(16L)
        while (true) {
            delay(frameDelay)
            if (frameIndex < frameCount - 1) {
                frameIndex += 1
            } else if (spec.loop) {
                frameIndex = 0
            } else {
                break
            }
        }
    }

    Canvas(modifier = modifier) {
        val current = image ?: return@Canvas
        val column = frameIndex % spec.columns
        val row = frameIndex / spec.columns
        val sourceOffset = IntOffset(column * spec.frameWidth, row * spec.frameHeight)
        val sourceSize = IntSize(spec.frameWidth, spec.frameHeight)
        drawImage(
            image = current,
            srcOffset = sourceOffset,
            srcSize = sourceSize,
            dstSize = IntSize(size.width.toInt().coerceAtLeast(1), size.height.toInt().coerceAtLeast(1)),
            filterQuality = FilterQuality.Medium
        )
    }
}
