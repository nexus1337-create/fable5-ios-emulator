package com.fable5.iosemulator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fable5.iosemulator.model.AppId
import com.fable5.iosemulator.ui.components.StatusBarHeight
import com.fable5.iosemulator.ui.theme.IosBlue
import com.fable5.iosemulator.ui.theme.IosGreen
import com.fable5.iosemulator.ui.theme.IosIndigo
import com.fable5.iosemulator.ui.theme.IosOrange
import com.fable5.iosemulator.viewmodel.EmulatorViewModel

/**
 * Пункт управления iOS (свайп вниз от правого верхнего угла):
 * тумблеры связи, mock-плеер, рабочие ползунки яркости и громкости
 * (яркость реально затемняет экран эмулятора), быстрые действия.
 */
@Composable
fun ControlCenterOverlay(vm: EmulatorViewModel, onDismiss: () -> Unit) {
    // Плотное затемнение: на устройствах без RenderEffect-блюра (до Android 12)
    // именно оно отделяет Пункт управления от контента позади
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() }
    ) {
        Column(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 14.dp)
                .padding(horizontal = 14.dp)
                // Тапы внутри панели не закрывают Пункт управления
                .pointerInput(Unit) { detectTapGestures { } },
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ---------- Верхний ряд: «+» и питание, как в iOS 27 ----------
            Row(Modifier.fillMaxWidth()) {
                SmallCircleButton(Icons.Filled.Add)
                Spacer(Modifier.weight(1f))
                SmallCircleButton(Icons.Filled.PowerSettingsNew)
            }
            // ---------- Строка статуса внутри Пункта управления ----------
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.SignalCellularAlt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.size(6.dp))
                Icon(
                    imageVector = Icons.Filled.Wifi,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.weight(1f))
                Text("100 %", color = Color.White, fontSize = 14.sp)
                Spacer(Modifier.size(5.dp))
                Icon(
                    imageVector = Icons.Filled.BatteryFull,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp).graphicsLayer { rotationZ = 90f }
                )
            }
            // ---------- Связь + плеер ----------
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                GlassModule(Modifier.weight(1f).aspectRatio(1f)) {
                    ConnectivityModule(vm)
                }
                GlassModule(Modifier.weight(1f).aspectRatio(1f)) {
                    MusicModule(vm)
                }
            }
            // ---------- Камера/без звука/фонарик + Фокус + ползунки ----------
            Row(
                Modifier.height(178.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(
                    Modifier.weight(1.5f).fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        CcRoundButton(icon = Icons.Filled.CameraAlt, active = false) {
                            vm.openApp(AppId.CAMERA)
                        }
                        CcRoundButton(
                            icon = Icons.Filled.NotificationsOff,
                            active = vm.muted,
                            activeColor = Color(0xFFE85D5D)
                        ) { vm.muted = !vm.muted }
                        CcRoundButton(
                            icon = Icons.Filled.FlashlightOn,
                            active = vm.flashlightOn,
                            activeColor = Color.White
                        ) { vm.flashlightOn = !vm.flashlightOn }
                    }
                    // Модуль «Фокусирование», как в ките (Title/Detail)
                    GlassModule(Modifier.fillMaxWidth().height(64.dp)) {
                        Row(
                            Modifier.fillMaxSize().padding(horizontal = 14.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { vm.focusMode = !vm.focusMode },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (vm.focusMode) IosIndigo
                                        else Color.White.copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Bedtime,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.size(10.dp))
                            Column {
                                Text(
                                    "Фокусирование",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    if (vm.focusMode) "Вкл." else "Выкл.",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
                VerticalSlider(
                    value = vm.brightness,
                    onChange = { vm.brightness = it },
                    icon = Icons.Filled.WbSunny,
                    modifier = Modifier.weight(0.75f)
                )
                VerticalSlider(
                    value = vm.volume,
                    onChange = { vm.volume = it },
                    icon = Icons.Filled.VolumeUp,
                    modifier = Modifier.weight(0.75f)
                )
            }
        }
    }
}

/** Маленькая круглая кнопка верхнего ряда («+», питание). */
@Composable
private fun SmallCircleButton(icon: ImageVector) {
    Box(
        Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(19.dp)
        )
    }
}

/**
 * «Матовый» модуль Пункта управления. Фон почти непрозрачный,
 * чтобы модули читались и на устройствах без настоящего блюра.
 */
@Composable
private fun GlassModule(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(26.dp)
    Box(
        modifier
            .clip(shape)
            .background(Color(0xFF2E2E30).copy(alpha = 0.88f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), shape)
    ) {
        content()
    }
}

/** Модуль связи: авиарежим, сотовые данные, Wi-Fi, Bluetooth. */
@Composable
private fun ConnectivityModule(vm: EmulatorViewModel) {
    Column(
        Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            CcRoundButton(
                icon = Icons.Filled.AirplanemodeActive,
                active = vm.airplaneMode,
                activeColor = IosOrange
            ) { vm.airplaneMode = !vm.airplaneMode }
            CcRoundButton(
                icon = Icons.Filled.SignalCellularAlt,
                active = vm.cellularData && !vm.airplaneMode,
                activeColor = IosGreen
            ) { vm.cellularData = !vm.cellularData }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            CcRoundButton(
                icon = Icons.Filled.Wifi,
                active = vm.wifiEnabled && !vm.airplaneMode,
                activeColor = IosBlue
            ) { vm.wifiEnabled = !vm.wifiEnabled }
            CcRoundButton(
                icon = Icons.Filled.Bluetooth,
                active = vm.bluetoothEnabled,
                activeColor = IosBlue
            ) { vm.bluetoothEnabled = !vm.bluetoothEnabled }
        }
    }
}

/** Mock-плеер «Сейчас играет». */
@Composable
private fun MusicModule(vm: EmulatorViewModel) {
    Column(
        Modifier.fillMaxSize().padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Cosmic Drift",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "Fable Waves",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            maxLines = 1
        )
        Spacer(Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.SkipPrevious,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = if (vm.musicPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(40.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { vm.musicPlaying = !vm.musicPlaying }
            )
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Filled.SkipNext,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.weight(1f))
    }
}

/** Круглая кнопка-тумблер Пункта управления. */
@Composable
private fun CcRoundButton(
    icon: ImageVector,
    active: Boolean,
    activeColor: Color = IosBlue,
    onClick: () -> Unit
) {
    val background = if (active) activeColor else Color.White.copy(alpha = 0.2f)
    val tint = if (active && activeColor == Color.White) Color.Black else Color.White
    Box(
        Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Вертикальный ползунок iOS (яркость/громкость):
 * белая «заливка» растёт снизу, управляется перетаскиванием.
 */
@Composable
private fun VerticalSlider(
    value: Float,
    onChange: (Float) -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val currentValue by rememberUpdatedState(value)
    val currentOnChange by rememberUpdatedState(onChange)
    val shape = RoundedCornerShape(34.dp)
    Box(
        modifier
            .fillMaxHeight()
            .clip(shape)
            .background(Color(0xFF3A3A3C).copy(alpha = 0.88f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), shape)
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    change.consume()
                    currentOnChange(
                        (currentValue - dragAmount / size.height).coerceIn(0.02f, 1f)
                    )
                }
            }
    ) {
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(value)
                .background(Color.White)
        )
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF3A3A3C),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .size(22.dp)
        )
    }
}
