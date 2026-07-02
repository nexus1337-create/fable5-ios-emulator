package com.fable5.iosemulator.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.fable5.iosemulator.model.Wallpaper

/** Фиксированные позиции и радиусы цветных «пятен» (доли ширины/высоты). */
private val blobLayout = listOf(
    Triple(0.15f, 0.18f, 0.72f),
    Triple(0.88f, 0.32f, 0.78f),
    Triple(0.42f, 0.62f, 0.85f),
    Triple(0.85f, 0.88f, 0.80f),
    Triple(0.10f, 0.92f, 0.70f)
)

/**
 * Обои в стиле iOS 18: вертикальный базовый градиент, поверх которого
 * лежат крупные мягкие цветные пятна — имитация mesh-градиента.
 */
@Composable
fun WallpaperBackground(wallpaper: Wallpaper, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(wallpaper.base))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            blobLayout.forEachIndexed { index, (fx, fy, fr) ->
                val color = wallpaper.blobs[index % wallpaper.blobs.size]
                val center = Offset(size.width * fx, size.height * fy)
                val radius = size.width * fr
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color.copy(alpha = 0.55f), Color.Transparent),
                        center = center,
                        radius = radius
                    ),
                    radius = radius,
                    center = center
                )
            }
            // Верхний светлый «дымчатый» отблеск для глубины
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(size.width * 0.7f, size.height * 0.06f),
                    radius = size.width * 0.9f
                ),
                radius = size.width * 0.9f,
                center = Offset(size.width * 0.7f, size.height * 0.06f)
            )
        }
    }
}
