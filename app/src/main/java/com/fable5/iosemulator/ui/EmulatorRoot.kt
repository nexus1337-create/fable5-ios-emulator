package com.fable5.iosemulator.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fable5.iosemulator.model.AppCatalog
import com.fable5.iosemulator.model.AppId
import com.fable5.iosemulator.ui.components.AppIconVisual
import com.fable5.iosemulator.ui.components.DynamicIsland
import com.fable5.iosemulator.ui.components.HomeGestureArea
import com.fable5.iosemulator.ui.components.IosStatusBar
import com.fable5.iosemulator.ui.screens.AppSwitcherScreen
import com.fable5.iosemulator.ui.screens.HomeScreen
import com.fable5.iosemulator.ui.screens.MessagesScreen
import com.fable5.iosemulator.ui.screens.PhotosScreen
import com.fable5.iosemulator.ui.screens.SafariScreen
import com.fable5.iosemulator.ui.screens.SettingsScreen
import com.fable5.iosemulator.ui.theme.Fable5Theme
import com.fable5.iosemulator.viewmodel.EmulatorViewModel

/**
 * Корневой композабл эмулятора. Слои снизу вверх:
 * 1) домашний экран (всегда отрисован, блюрится под App Switcher);
 * 2) App Switcher;
 * 3) открытое приложение (анимация масштаба, как при запуске в iOS);
 * 4) статус-бар + Dynamic Island;
 * 5) зона жестов с Home Indicator.
 */
@Composable
fun EmulatorRoot(vm: EmulatorViewModel) {
    Fable5Theme(darkTheme = vm.darkTheme) {
        // Блюр домашнего экрана, когда открыт App Switcher
        val homeBlur by animateDpAsState(
            targetValue = if (vm.switcherVisible) 24.dp else 0.dp,
            animationSpec = tween(300),
            label = "switcherBlur"
        )
        // Прогресс запуска приложения: 0 — закрыто, 1 — открыто
        val appProgress by animateFloatAsState(
            targetValue = if (vm.openedApp != null) 1f else 0f,
            animationSpec = tween(
                durationMillis = if (vm.openedApp != null) 380 else 300,
                easing = FastOutSlowInEasing
            ),
            label = "appLaunch"
        )

        Box(Modifier.fillMaxSize().background(Color.Black)) {
            // ---------- 1. Домашний экран ----------
            // При запуске приложения home слегка «улетает» вглубь, как в iOS
            HomeScreen(
                vm,
                Modifier
                    .graphicsLayer {
                        val zoom = 1f + 0.1f * appProgress
                        scaleX = zoom
                        scaleY = zoom
                        alpha = 1f - 0.4f * appProgress
                    }
                    .blur(homeBlur)
            )

            // ---------- 2. App Switcher ----------
            AnimatedVisibility(
                visible = vm.switcherVisible,
                enter = fadeIn(tween(250)),
                exit = fadeOut(tween(200))
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                ) {
                    AppSwitcherScreen(vm)
                }
            }

            // ---------- 3. Открытое приложение ----------
            // Запоминаем последнее приложение, чтобы exit-анимация
            // показывала его экран, а не пустоту
            var lastApp by remember { mutableStateOf<AppId?>(null) }
            SideEffect {
                if (vm.openedApp != null) lastApp = vm.openedApp
            }
            // Приложение «вырастает» из иконки, по которой нажали,
            // и «сворачивается» обратно в неё при выходе — как в iOS
            if (appProgress > 0.001f) {
                val appToShow = vm.openedApp ?: lastApp
                if (appToShow != null) {
                    BoxWithConstraints(Modifier.fillMaxSize()) {
                        val rootWidth = constraints.maxWidth.toFloat()
                        val rootHeight = constraints.maxHeight.toFloat()
                        val origin = vm.launchOrigin
                        Box(
                            Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    val scale = 0.15f + 0.85f * appProgress
                                    scaleX = scale
                                    scaleY = scale
                                    if (origin != null) {
                                        translationX = (origin.x - rootWidth / 2f) * (1f - appProgress)
                                        translationY = (origin.y - rootHeight / 2f) * (1f - appProgress)
                                    }
                                    alpha = (appProgress * 2.5f).coerceAtMost(1f)
                                    clip = true
                                    shape = RoundedCornerShape(((1f - appProgress) * 44f).dp)
                                }
                        ) {
                            AppScreenHost(appId = appToShow, vm = vm, modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }

            // ---------- 4. Статус-бар + Dynamic Island ----------
            // Тёмный контент статус-бара — только в приложениях при светлой теме
            val darkStatusContent = vm.openedApp != null && !vm.darkTheme
            val statusColor = if (darkStatusContent) Color.Black else Color.White

            IosStatusBar(
                contentColor = statusColor,
                airplaneMode = vm.airplaneMode,
                wifiEnabled = vm.wifiEnabled,
                modifier = Modifier.align(Alignment.TopCenter)
            )
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
            ) {
                DynamicIsland(
                    expanded = vm.islandExpanded,
                    playing = vm.musicPlaying,
                    onTap = { vm.islandExpanded = !vm.islandExpanded },
                    onTogglePlay = { vm.musicPlaying = !vm.musicPlaying }
                )
            }

            // ---------- 5. Жесты и Home Indicator ----------
            HomeGestureArea(
                onShortSwipeUp = {
                    if (vm.openedApp != null || vm.switcherVisible) vm.goHome()
                    else vm.showSwitcher()
                },
                onLongSwipeUp = { vm.showSwitcher() },
                indicatorColor = statusColor,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // Системная кнопка «Назад» ведёт себя как жест «домой»
            BackHandler(
                enabled = vm.openedApp != null || vm.switcherVisible || vm.openedFolderKey != null
            ) {
                if (vm.openedFolderKey != null) vm.openedFolderKey = null else vm.goHome()
            }
        }
    }
}

/** Выбор экрана по идентификатору приложения. */
@Composable
fun AppScreenHost(appId: AppId, vm: EmulatorViewModel, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        when (appId) {
            AppId.SAFARI -> SafariScreen(vm)
            AppId.MESSAGES -> MessagesScreen(vm)
            AppId.PHOTOS -> PhotosScreen(vm)
            AppId.SETTINGS -> SettingsScreen(vm)
            else -> GenericAppScreen(appId, vm)
        }
    }
}

/** Экран-заглушка для приложений без собственной реализации. */
@Composable
private fun GenericAppScreen(appId: AppId, vm: EmulatorViewModel) {
    val app = AppCatalog[appId]
    val dark = vm.darkTheme
    Column(
        Modifier
            .fillMaxSize()
            .background(if (dark) Color.Black else Color(0xFFF2F2F7)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))
        AppIconVisual(app, 92.dp)
        Spacer(Modifier.height(20.dp))
        Text(
            app.name,
            color = if (dark) Color.White else Color.Black,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Mock-приложение эмулятора Fable 5",
            color = (if (dark) Color.White else Color.Black).copy(alpha = 0.5f),
            fontSize = 15.sp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.weight(1.3f))
    }
}
