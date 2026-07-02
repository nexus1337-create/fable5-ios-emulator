package com.fable5.iosemulator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fable5.iosemulator.model.AppCatalog
import com.fable5.iosemulator.model.AppId
import com.fable5.iosemulator.model.IosApp
import com.fable5.iosemulator.model.Wallpapers
import com.fable5.iosemulator.ui.components.AppIconVisual
import com.fable5.iosemulator.ui.components.StatusBarHeight
import com.fable5.iosemulator.ui.components.WallpaperBackground
import com.fable5.iosemulator.ui.components.asIosClock
import com.fable5.iosemulator.ui.components.asIosDate
import com.fable5.iosemulator.ui.components.rememberCurrentTime
import com.fable5.iosemulator.viewmodel.EmulatorViewModel

/**
 * Экран блокировки iOS: большие часы, дата, уведомления,
 * кнопки фонарика и камеры. Разблокировка — свайпом вверх
 * (в любом месте экрана или от Home Indicator).
 */
@Composable
fun LockScreen(vm: EmulatorViewModel, modifier: Modifier = Modifier) {
    val wallpaper = Wallpapers.all[vm.wallpaperIndex.coerceIn(0, Wallpapers.all.lastIndex)]

    // Живые часы (общий тикер — см. components/SystemState.kt)
    val now by rememberCurrentTime()
    val time = now.asIosClock()
    val date = now.asIosDate("EEEE, d MMMM")

    var dragTotal by remember { mutableFloatStateOf(0f) }

    Box(
        modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                val threshold = -120.dp.toPx()
                detectVerticalDragGestures(
                    onDragStart = { dragTotal = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        dragTotal += dragAmount
                    },
                    onDragEnd = { if (dragTotal < threshold) vm.unlock() }
                )
            }
    ) {
        WallpaperBackground(wallpaper)
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.18f)))

        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(StatusBarHeight + 18.dp))
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(19.dp)
            )
            Spacer(Modifier.height(10.dp))
            // Компоновка как в ките iOS 27: небольшая дата
            // над огромными полупрозрачными часами
            Text(
                text = date,
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 21.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = time,
                color = Color.White.copy(alpha = 0.95f),
                fontSize = 96.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-3).sp
            )
            Spacer(Modifier.height(6.dp))
            // Мини-виджеты под часами (погода и будильник)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LockWidgetChip("⛅ 21°  Макс. 24°")
                LockWidgetChip("⏰ 7:30")
            }
        }

        // Уведомления — внизу, над кнопками (как в iOS)
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 176.dp)
        ) {
            LockNotification(
                app = AppCatalog[AppId.MESSAGES],
                title = "Аня",
                message = "Dynamic Island вообще огонь 🔥",
                time = "9:43"
            )
            Spacer(Modifier.height(8.dp))
            LockNotification(
                app = AppCatalog[AppId.MAIL],
                title = "Команда Fable",
                message = "Релиз 1.0 готов к демо 🚀",
                time = "8:15"
            )
        }

        // Кнопки фонарика и камеры
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 52.dp, vertical = 78.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LockRoundButton(
                icon = Icons.Filled.FlashlightOn,
                active = vm.flashlightOn,
                onClick = { vm.flashlightOn = !vm.flashlightOn }
            )
            LockRoundButton(
                icon = Icons.Filled.CameraAlt,
                active = false,
                onClick = {
                    vm.unlock()
                    vm.openApp(AppId.CAMERA)
                }
            )
        }
    }
}

/** Мини-виджет под часами (капсула со «стеклом»). */
@Composable
private fun LockWidgetChip(text: String) {
    Box(
        Modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.18f))
            .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
    }
}

/** Уведомление на экране блокировки: «матовая» карточка. */
@Composable
private fun LockNotification(app: IosApp, title: String, message: String, time: String) {
    val shape = RoundedCornerShape(22.dp)
    Row(
        Modifier
            .padding(horizontal = 14.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White.copy(alpha = 0.22f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), shape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIconVisual(app, 36.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = message,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = time,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
    }
}

/** Круглая кнопка экрана блокировки (фонарик/камера). */
@Composable
private fun LockRoundButton(icon: ImageVector, active: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(if (active) Color.White else Color.Black.copy(alpha = 0.34f))
            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (active) Color.Black else Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}
