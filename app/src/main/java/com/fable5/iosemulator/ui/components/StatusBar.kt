package com.fable5.iosemulator.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Высота статус-бара iPhone с Dynamic Island. */
val StatusBarHeight: Dp = 54.dp

/**
 * Точная имитация статус-бара iOS: время слева,
 * сотовый сигнал / Wi-Fi / батарея справа. Центр остаётся
 * свободным под Dynamic Island.
 */
@Composable
fun IosStatusBar(
    contentColor: Color,
    airplaneMode: Boolean,
    wifiEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    // Живое время, обновляется каждую секунду
    var time by remember { mutableStateOf(currentTime()) }
    LaunchedEffect(Unit) {
        while (true) {
            time = currentTime()
            delay(1000L)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(StatusBarHeight)
            .padding(horizontal = 28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = time,
            color = contentColor,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.weight(1f))
        if (airplaneMode) {
            Icon(
                imageVector = Icons.Filled.AirplanemodeActive,
                contentDescription = "Авиарежим",
                tint = contentColor,
                modifier = Modifier.size(17.dp)
            )
        } else {
            CellularBars(contentColor)
            if (wifiEnabled) {
                Spacer(Modifier.width(7.dp))
                Icon(
                    imageVector = Icons.Filled.Wifi,
                    contentDescription = "Wi-Fi",
                    tint = contentColor,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
        Spacer(Modifier.width(7.dp))
        BatteryIndicator(contentColor, level = 0.82f)
    }
}

/** Четыре столбика сотового сигнала (последний — приглушён). */
@Composable
private fun CellularBars(color: Color) {
    Canvas(Modifier.size(width = 19.dp, height = 12.dp)) {
        val barWidth = size.width / 4f * 0.68f
        val gap = (size.width - barWidth * 4f) / 3f
        val heights = listOf(0.4f, 0.6f, 0.8f, 1f)
        heights.forEachIndexed { index, fraction ->
            val barHeight = size.height * fraction
            drawRoundRect(
                color = if (index == 3) color.copy(alpha = 0.35f) else color,
                topLeft = Offset(index * (barWidth + gap), size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2.5f)
            )
        }
    }
}

/** Батарея как в iOS: контур, заряд внутри и «носик» справа. */
@Composable
private fun BatteryIndicator(color: Color, level: Float) {
    Canvas(Modifier.size(width = 27.dp, height = 13.dp)) {
        val bodyWidth = size.width * 0.88f
        val strokeWidth = 1.dp.toPx()
        // Контур корпуса
        drawRoundRect(
            color = color.copy(alpha = 0.4f),
            topLeft = Offset(0f, 0f),
            size = Size(bodyWidth, size.height),
            cornerRadius = CornerRadius(3.5.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
        )
        // Уровень заряда
        val inset = 2.dp.toPx()
        drawRoundRect(
            color = color,
            topLeft = Offset(inset, inset),
            size = Size((bodyWidth - inset * 2f) * level, size.height - inset * 2f),
            cornerRadius = CornerRadius(1.8.dp.toPx())
        )
        // «Носик» батареи
        drawRoundRect(
            color = color.copy(alpha = 0.4f),
            topLeft = Offset(bodyWidth + 1.5.dp.toPx(), size.height * 0.33f),
            size = Size(size.width - bodyWidth - 1.5.dp.toPx(), size.height * 0.34f),
            cornerRadius = CornerRadius(1.dp.toPx())
        )
    }
}

private fun currentTime(): String =
    SimpleDateFormat("H:mm", Locale.getDefault()).format(Date())
