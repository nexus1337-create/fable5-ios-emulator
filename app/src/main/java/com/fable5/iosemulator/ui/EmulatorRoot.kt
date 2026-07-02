package com.fable5.iosemulator.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
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
import com.fable5.iosemulator.ui.screens.ControlCenterOverlay
import com.fable5.iosemulator.ui.screens.HomeScreen
import com.fable5.iosemulator.ui.screens.LockScreen
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
        // Прогресс блокировки: параллельно уезжающему локскрину
        // домашний экран «приезжает» из глубины (как в iOS)
        val lockProgress by animateFloatAsState(
            targetValue = if (vm.locked) 1f else 0f,
            animationSpec = tween(420, easing = FastOutSlowInEasing),
            label = "lockProgress"
        )

        Box(Modifier.fillMaxSize().background(Color.Black)) {
            // ---------- 1. Домашний экран ----------
            // Параллельные анимации: зум при запуске приложения
            // и «выплывание» из глубины при разблокировке
            HomeScreen(
                vm,
                Modifier
                    .graphicsLayer {
                        val zoom = 1f + 0.1f * appProgress + 0.14f * lockProgress
                        scaleX = zoom
                        scaleY = zoom
                        alpha = (1f - 0.4f * appProgress) * (1f - 0.45f * lockProgress)
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

            // ---------- 3.5 Пункт управления ----------
            AnimatedVisibility(
                visible = vm.controlCenterVisible,
                enter = slideInVertically(
                    initialOffsetY = { -it / 2 },
                    animationSpec = tween(260, easing = FastOutSlowInEasing)
                ) + fadeIn(tween(200)),
                exit = slideOutVertically(
                    targetOffsetY = { -it / 2 },
                    animationSpec = tween(220, easing = FastOutSlowInEasing)
                ) + fadeOut(tween(180))
            ) {
                ControlCenterOverlay(vm) { vm.controlCenterVisible = false }
            }

            // ---------- 3.7 Экран блокировки ----------
            AnimatedVisibility(
                visible = vm.locked,
                enter = fadeIn(tween(250)),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(380, easing = FastOutSlowInEasing)
                ) + fadeOut(tween(320))
            ) {
                LockScreen(vm)
            }

            // ---------- 4. Статус-бар + Dynamic Island ----------
            // Тёмный контент статус-бара — только в приложениях при светлой теме
            val darkStatusContent =
                vm.openedApp != null && !vm.darkTheme && !vm.locked && !vm.controlCenterVisible
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

            // ---------- 4.5 Затемнение от ползунка яркости ----------
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = (1f - vm.brightness) * 0.55f))
            )

            // ---------- 5. Жесты и Home Indicator ----------
            HomeGestureArea(
                onShortSwipeUp = {
                    when {
                        vm.locked -> vm.unlock()
                        vm.controlCenterVisible -> vm.controlCenterVisible = false
                        vm.openedApp != null || vm.switcherVisible -> vm.goHome()
                        else -> vm.showSwitcher()
                    }
                },
                onLongSwipeUp = {
                    when {
                        vm.locked -> vm.unlock()
                        vm.controlCenterVisible -> vm.controlCenterVisible = false
                        else -> vm.showSwitcher()
                    }
                },
                indicatorColor = statusColor,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // ---------- 6. Жест Пункта управления ----------
            // Свайп вниз от правого верхнего угла, как в iOS
            if (!vm.locked && !vm.controlCenterVisible) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .fillMaxWidth(0.42f)
                        .height(44.dp)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onVerticalDrag = { change, dragAmount ->
                                    if (dragAmount > 10f) {
                                        change.consume()
                                        vm.controlCenterVisible = true
                                    }
                                }
                            )
                        }
                )
            }

            // Системная кнопка «Назад» ведёт себя как жест «домой»
            BackHandler(
                enabled = vm.openedApp != null || vm.switcherVisible ||
                    vm.openedFolderKey != null || vm.controlCenterVisible
            ) {
                when {
                    vm.controlCenterVisible -> vm.controlCenterVisible = false
                    vm.openedFolderKey != null -> vm.openedFolderKey = null
                    else -> vm.goHome()
                }
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
