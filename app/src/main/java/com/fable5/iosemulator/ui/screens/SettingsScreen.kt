package com.fable5.iosemulator.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fable5.iosemulator.model.Wallpapers
import com.fable5.iosemulator.ui.components.IosSwitch
import com.fable5.iosemulator.ui.components.StatusBarHeight
import com.fable5.iosemulator.ui.components.WallpaperBackground
import com.fable5.iosemulator.ui.theme.IosBlue
import com.fable5.iosemulator.ui.theme.IosGray
import com.fable5.iosemulator.ui.theme.IosGreen
import com.fable5.iosemulator.ui.theme.IosIndigo
import com.fable5.iosemulator.ui.theme.IosOrange
import com.fable5.iosemulator.ui.theme.IosPink
import com.fable5.iosemulator.ui.theme.IosRed
import com.fable5.iosemulator.viewmodel.EmulatorViewModel

/** Разделы приложения «Настройки». */
private enum class SettingsSection { MAIN, WIFI, APPEARANCE, ABOUT }

/**
 * Mock Settings: группированные списки в стиле iOS,
 * рабочие переключатели (авиарежим, Wi-Fi, тёмная тема),
 * выбор обоев и экран «Об этом устройстве».
 */
@Composable
fun SettingsScreen(vm: EmulatorViewModel) {
    var section by remember { mutableStateOf(SettingsSection.MAIN) }

    AnimatedContent(
        targetState = section,
        transitionSpec = {
            if (targetState != SettingsSection.MAIN) {
                (slideInHorizontally { it } + fadeIn()) togetherWith
                    (slideOutHorizontally { -it / 3 } + fadeOut())
            } else {
                (slideInHorizontally { -it / 3 } + fadeIn()) togetherWith
                    (slideOutHorizontally { it } + fadeOut())
            }
        },
        label = "settingsNav"
    ) { current ->
        when (current) {
            SettingsSection.MAIN -> MainSettings(vm) { section = it }
            SettingsSection.WIFI -> WifiSettings(vm) { section = SettingsSection.MAIN }
            SettingsSection.APPEARANCE -> AppearanceSettings(vm) { section = SettingsSection.MAIN }
            SettingsSection.ABOUT -> AboutSettings(vm) { section = SettingsSection.MAIN }
        }
    }
}

// ---------------------------------------------------------------------
// Главный экран настроек
// ---------------------------------------------------------------------

@Composable
private fun MainSettings(vm: EmulatorViewModel, onNavigate: (SettingsSection) -> Unit) {
    val dark = vm.darkTheme
    val background = if (dark) Color.Black else Color(0xFFF2F2F7)
    val textColor = if (dark) Color.White else Color.Black

    Column(
        Modifier
            .fillMaxSize()
            .background(background)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(StatusBarHeight))
        Text(
            "Настройки",
            color = textColor,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        // Поиск
        Row(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (dark) Color(0xFF1C1C1E) else Color(0xFFE3E3E8))
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Search,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.45f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text("Поиск", color = textColor.copy(alpha = 0.45f), fontSize = 16.sp)
        }
        Spacer(Modifier.height(16.dp))

        // Профиль
        SettingsGroup(dark) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Ф", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Fable Пользователь", color = textColor, fontSize = 19.sp)
                    Text(
                        "Apple ID, iCloud+ и другое",
                        color = textColor.copy(alpha = 0.55f),
                        fontSize = 13.sp
                    )
                }
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = textColor.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(Modifier.height(20.dp))

        // Связь
        SettingsGroup(dark) {
            SettingsRow(
                icon = Icons.Filled.AirplanemodeActive, iconBg = IosOrange,
                title = "Авиарежим", dark = dark, showChevron = false,
                trailing = { IosSwitch(vm.airplaneMode, { vm.setAirplaneModeEnabled(it) }) }
            )
            SettingsDivider(dark)
            SettingsRow(
                icon = Icons.Filled.Wifi, iconBg = IosBlue,
                title = "Wi-Fi",
                value = if (vm.wifiEnabled && !vm.airplaneMode) vm.wifiNetwork else "Выкл.",
                dark = dark, onClick = { onNavigate(SettingsSection.WIFI) }
            )
            SettingsDivider(dark)
            SettingsRow(
                icon = Icons.Filled.Bluetooth, iconBg = IosBlue,
                title = "Bluetooth",
                value = if (vm.bluetoothEnabled) "Вкл." else "Выкл.",
                dark = dark, showChevron = false,
                trailing = { IosSwitch(vm.bluetoothEnabled, { vm.setBluetoothState(it) }) }
            )
            SettingsDivider(dark)
            SettingsRow(
                icon = Icons.Filled.SignalCellularAlt, iconBg = IosGreen,
                title = "Сотовая связь", dark = dark
            )
        }
        Spacer(Modifier.height(20.dp))

        // Оформление
        SettingsGroup(dark) {
            SettingsRow(
                icon = Icons.Filled.DarkMode, iconBg = IosIndigo,
                title = "Оформление и обои",
                value = if (dark) "Тёмное" else "Светлое",
                dark = dark, onClick = { onNavigate(SettingsSection.APPEARANCE) }
            )
        }
        Spacer(Modifier.height(20.dp))

        // Уведомления и звуки
        SettingsGroup(dark) {
            SettingsRow(
                icon = Icons.Filled.Notifications, iconBg = IosRed,
                title = "Уведомления", dark = dark
            )
            SettingsDivider(dark)
            SettingsRow(
                icon = Icons.Filled.VolumeUp, iconBg = IosPink,
                title = "Звуки, тактильные сигналы", dark = dark
            )
            SettingsDivider(dark)
            SettingsRow(
                icon = Icons.Filled.Bedtime, iconBg = IosIndigo,
                title = "Фокусирование", dark = dark
            )
            SettingsDivider(dark)
            SettingsRow(
                icon = Icons.Filled.HourglassEmpty, iconBg = IosIndigo,
                title = "Экранное время", dark = dark
            )
        }
        Spacer(Modifier.height(20.dp))

        // Основные
        SettingsGroup(dark) {
            SettingsRow(
                icon = Icons.Filled.Settings, iconBg = IosGray,
                title = "Основные", dark = dark,
                onClick = { onNavigate(SettingsSection.ABOUT) }
            )
            SettingsDivider(dark)
            SettingsRow(
                icon = Icons.Filled.Tune, iconBg = IosGray,
                title = "Пункт управления", dark = dark
            )
            SettingsDivider(dark)
            SettingsRow(
                icon = Icons.Filled.Lock, iconBg = IosBlue,
                title = "Конфиденциальность и безопасность", dark = dark
            )
        }
        Spacer(Modifier.height(20.dp))

        // Блокировка экрана (демонстрация экрана блокировки)
        SettingsGroup(dark) {
            SettingsRow(
                icon = Icons.Filled.Lock, iconBg = IosGray,
                title = "Заблокировать экран", dark = dark,
                showChevron = false,
                onClick = { vm.lockScreen() }
            )
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "Fable 5 iOS Emulator · версия 1.0",
            color = textColor.copy(alpha = 0.4f),
            fontSize = 13.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 60.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// ---------------------------------------------------------------------
// Подэкран Wi-Fi
// ---------------------------------------------------------------------

@Composable
private fun WifiSettings(vm: EmulatorViewModel, onBack: () -> Unit) {
    val dark = vm.darkTheme
    val background = if (dark) Color.Black else Color(0xFFF2F2F7)
    val textColor = if (dark) Color.White else Color.Black
    val networks = listOf("FableNet", "Coffee House", "Home_5G", "iPhone (Аня)")

    Column(
        Modifier
            .fillMaxSize()
            .background(background)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(StatusBarHeight))
        SubScreenHeader(title = "Wi-Fi", dark = dark, onBack = onBack)

        SettingsGroup(dark) {
            SettingsRow(
                icon = Icons.Filled.Wifi, iconBg = IosBlue,
                title = "Wi-Fi", dark = dark, showChevron = false,
                trailing = { IosSwitch(vm.wifiEnabled, { vm.setWifiState(it) }) }
            )
        }
        Spacer(Modifier.height(20.dp))

        if (vm.wifiEnabled) {
            Text(
                "МОИ СЕТИ",
                color = textColor.copy(alpha = 0.5f),
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 6.dp)
            )
            SettingsGroup(dark) {
                networks.forEachIndexed { index, network ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { vm.selectWifiNetwork(network) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (vm.wifiNetwork == network) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "Подключено",
                                tint = IosBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Spacer(Modifier.width(18.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(network, color = textColor, fontSize = 17.sp, modifier = Modifier.weight(1f))
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = null,
                            tint = textColor.copy(alpha = 0.4f),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Filled.Wifi,
                            contentDescription = null,
                            tint = textColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    if (index != networks.lastIndex) SettingsDivider(dark)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
// Подэкран «Оформление и обои»
// ---------------------------------------------------------------------

@Composable
private fun AppearanceSettings(vm: EmulatorViewModel, onBack: () -> Unit) {
    val dark = vm.darkTheme
    val background = if (dark) Color.Black else Color(0xFFF2F2F7)
    val textColor = if (dark) Color.White else Color.Black

    Column(
        Modifier
            .fillMaxSize()
            .background(background)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(StatusBarHeight))
        SubScreenHeader(title = "Оформление и обои", dark = dark, onBack = onBack)

        Text(
            "ТЕМА",
            color = textColor.copy(alpha = 0.5f),
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 6.dp)
        )
        SettingsGroup(dark) {
            SettingsRow(
                icon = Icons.Filled.DarkMode, iconBg = IosIndigo,
                title = "Тёмная тема", dark = dark, showChevron = false,
                trailing = { IosSwitch(vm.darkTheme, { vm.setDarkThemeEnabled(it) }) }
            )
        }
        Spacer(Modifier.height(24.dp))

        Text(
            "ОБОИ",
            color = textColor.copy(alpha = 0.5f),
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 6.dp)
        )
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(Wallpapers.all.size) { index ->
                val wallpaper = Wallpapers.all[index]
                val selected = vm.wallpaperIndex == index
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .width(86.dp)
                            .height(170.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .then(
                                if (selected) Modifier.border(
                                    3.dp, IosBlue, RoundedCornerShape(18.dp)
                                ) else Modifier
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { vm.selectWallpaper(index) }
                    ) {
                        WallpaperBackground(wallpaper, Modifier.fillMaxSize())
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        wallpaper.name,
                        color = if (selected) IosBlue else textColor.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
        Spacer(Modifier.height(40.dp))
    }
}

// ---------------------------------------------------------------------
// Подэкран «Об этом устройстве»
// ---------------------------------------------------------------------

@Composable
private fun AboutSettings(vm: EmulatorViewModel, onBack: () -> Unit) {
    val dark = vm.darkTheme
    val background = if (dark) Color.Black else Color(0xFFF2F2F7)

    Column(
        Modifier
            .fillMaxSize()
            .background(background)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(StatusBarHeight))
        SubScreenHeader(title = "Об этом устройстве", dark = dark, onBack = onBack)

        SettingsGroup(dark) {
            AboutRow("Название", "Fable 5", dark)
            SettingsDivider(dark)
            AboutRow("Версия ПО", "iOS 19.0 (Fable)", dark)
            SettingsDivider(dark)
            AboutRow("Модель", "iPhone 17 Pro (виртуальный)", dark)
            SettingsDivider(dark)
            AboutRow("Хранилище", "256 ГБ", dark)
            SettingsDivider(dark)
            AboutRow("Процессор", "F5 Bionic", dark)
        }
    }
}

@Composable
private fun AboutRow(title: String, value: String, dark: Boolean) {
    val textColor = if (dark) Color.White else Color.Black
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(title, color = textColor, fontSize = 17.sp)
        Spacer(Modifier.weight(1f))
        Text(value, color = textColor.copy(alpha = 0.5f), fontSize = 17.sp)
    }
}

// ---------------------------------------------------------------------
// Общие строительные блоки
// ---------------------------------------------------------------------

/** Шапка подэкрана с кнопкой «Назад». */
@Composable
private fun SubScreenHeader(title: String, dark: Boolean, onBack: () -> Unit) {
    val textColor = if (dark) Color.White else Color.Black
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onBack() }
        ) {
            Icon(
                Icons.Filled.ArrowBackIosNew,
                contentDescription = "Назад",
                tint = IosBlue,
                modifier = Modifier.size(20.dp)
            )
            Text("Настройки", color = IosBlue, fontSize = 17.sp)
        }
        Spacer(Modifier.weight(1f))
        Text(title, color = textColor, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.width(90.dp))
    }
}

/** Группа настроек: скруглённая карточка. */
@Composable
private fun SettingsGroup(dark: Boolean, content: @Composable () -> Unit) {
    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (dark) Color(0xFF1C1C1E) else Color.White)
    ) {
        content()
    }
}

/** Разделитель строк внутри группы (с отступом под иконку). */
@Composable
private fun SettingsDivider(dark: Boolean) {
    Box(
        Modifier
            .padding(start = 54.dp)
            .fillMaxWidth()
            .height(0.5.dp)
            .background(if (dark) Color(0xFF38383A) else Color(0xFFE0E0E5))
    )
}

/** Строка настроек: цветная иконка, заголовок, значение/переключатель. */
@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    dark: Boolean,
    value: String? = null,
    showChevron: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val textColor = if (dark) Color.White else Color.Black
    Row(
        Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onClick() }
                } else Modifier
            )
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(title, color = textColor, fontSize = 17.sp, modifier = Modifier.weight(1f))
        if (value != null) {
            Text(value, color = textColor.copy(alpha = 0.5f), fontSize = 17.sp)
            Spacer(Modifier.width(6.dp))
        }
        if (trailing != null) {
            trailing()
        } else if (showChevron) {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
