package com.fable5.iosemulator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = IosBlue,
    onPrimary = Color.White,
    background = IosLightBackground,
    onBackground = Color.Black,
    surface = IosLightCard,
    onSurface = Color.Black,
    secondary = IosGreen,
    error = IosRed
)

private val DarkColors = darkColorScheme(
    primary = IosBlueDark,
    onPrimary = Color.White,
    background = IosDarkBackground,
    onBackground = Color.White,
    surface = IosDarkCard,
    onSurface = Color.White,
    secondary = IosGreen,
    error = IosRed
)

/**
 * Тема эмулятора. Переключение светлая/тёмная управляется вручную
 * из настроек эмулятора (как в iOS), а не системной темой.
 */
@Composable
fun Fable5Theme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = IosTypography,
        content = content
    )
}
