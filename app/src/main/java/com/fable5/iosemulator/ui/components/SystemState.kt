package com.fable5.iosemulator.ui.components

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Единый источник «живого» времени для статус-бара, виджетов,
 * локскрина и иконки часов — вместо дублирующихся циклов
 * `while(true) { delay(1000) }` в каждом компоненте.
 */
@Composable
fun rememberCurrentTime(): State<LocalDateTime> = produceState(LocalDateTime.now()) {
    while (true) {
        value = LocalDateTime.now()
        // Просыпаемся на границе секунды, чтобы часы не «плыли»
        delay(1000L - System.currentTimeMillis() % 1000L)
    }
}

private val timeFormatter = DateTimeFormatter.ofPattern("H:mm")

/** Часы в формате iOS (например, «9:41»). */
fun LocalDateTime.asIosClock(): String = format(timeFormatter)

/** Дата вида «Понедельник, 1 июля» на языке системы. */
fun LocalDateTime.asIosDate(pattern: String): String {
    val locale = Locale.getDefault()
    return format(DateTimeFormatter.ofPattern(pattern, locale))
        .replaceFirstChar { it.uppercase(locale) }
}

/**
 * Реальный уровень заряда устройства (0..1) из sticky-бродкаста
 * ACTION_BATTERY_CHANGED; обновляется раз в минуту.
 */
@Composable
fun rememberBatteryLevel(): State<Float> {
    val context = LocalContext.current
    return produceState(0.82f) {
        while (true) {
            val intent: Intent? = context.registerReceiver(
                null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) {
                value = level.toFloat() / scale
            }
            delay(60_000L)
        }
    }
}
