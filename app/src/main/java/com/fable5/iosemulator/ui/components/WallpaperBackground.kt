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

/**
 * Обои в стиле iOS 18/26: вертикальный базовый градиент, поверх
 * которого лежат крупные мягкие цветные пятна с индивидуальной
 * для каждого пресета раскладкой — имитация mesh-градиента.
 */
@Composable
fun WallpaperBackground(wallpaper: Wallpaper, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(wallpaper.base))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            wallpaper.blobs.forEach { blob ->
                val center = Offset(size.width * blob.x, size.height * blob.y)
                val radius = size.width * blob.radius
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(blob.color.copy(alpha = blob.alpha), Color.Transparent),
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
