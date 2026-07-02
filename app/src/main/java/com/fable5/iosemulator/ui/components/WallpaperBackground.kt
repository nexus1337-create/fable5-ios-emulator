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
 * Обои домашнего экрана: диагональный градиент + мягкие световые
 * пятна, придающие глубину (в духе стандартных обоев iOS 18).
 */
@Composable
fun WallpaperBackground(wallpaper: Wallpaper, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.linearGradient(wallpaper.colors))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            // Светлое пятно в правой верхней части
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.16f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.2f),
                    radius = size.width * 0.75f
                ),
                radius = size.width * 0.75f,
                center = Offset(size.width * 0.85f, size.height * 0.2f)
            )
            // Цветное пятно внизу слева
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        wallpaper.colors.last().copy(alpha = 0.55f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.1f, size.height * 0.9f),
                    radius = size.width * 0.9f
                ),
                radius = size.width * 0.9f,
                center = Offset(size.width * 0.1f, size.height * 0.9f)
            )
        }
    }
}
