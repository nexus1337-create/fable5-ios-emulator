package com.fable5.iosemulator.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fable5.iosemulator.model.AppCatalog
import com.fable5.iosemulator.model.AppId
import com.fable5.iosemulator.ui.AppScreenHost
import com.fable5.iosemulator.ui.components.AppIconVisual
import com.fable5.iosemulator.ui.theme.IosDarkCard
import com.fable5.iosemulator.viewmodel.EmulatorViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * App Switcher — переключатель приложений iOS.
 * Горизонтальная лента карточек с живыми миниатюрами экранов:
 * тап открывает приложение, свайп карточки вверх убирает её из недавних.
 */
@Composable
fun AppSwitcherScreen(vm: EmulatorViewModel, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        if (vm.recentApps.isEmpty()) {
            Text(
                text = "Нет недавних приложений",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 17.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyRow(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(
                    vm.recentApps.toList(),
                    key = { _, id -> id.name }
                ) { index, appId ->
                    SwitcherCard(appId = appId, index = index, vm = vm)
                }
            }
        }
    }
}

/** Карточка одного приложения в переключателе. */
@Composable
private fun SwitcherCard(appId: AppId, index: Int, vm: EmulatorViewModel) {
    val app = AppCatalog[appId]
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val cardWidth = 230.dp
    val scale = cardWidth / screenWidth          // Dp / Dp -> Float
    val cardHeight = screenHeight * scale

    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val currentAppId by rememberUpdatedState(appId)

    // Пороги жеста в dp: в «сырых» пикселях поведение свайпа
    // отличалось бы в разы между mdpi- и xxhdpi-экранами
    val density = LocalDensity.current
    val dismissThresholdPx = with(density) { 90.dp.toPx() }
    val flyAwayPx = with(density) { 800.dp.toPx() }
    val fadeDistancePx = with(density) { 500.dp.toPx() }

    // Каскадное появление: карточки «подплывают» одна за другой
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index * 60L)
        appear.animateTo(1f, tween(280, easing = FastOutSlowInEasing))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .offset { IntOffset(0, offsetY.value.roundToInt()) }
            .graphicsLayer {
                val enter = appear.value
                scaleX = 0.88f + 0.12f * enter
                scaleY = 0.88f + 0.12f * enter
                translationY += 60f * (1f - enter)
                alpha = enter * (1f - (-offsetY.value / fadeDistancePx).coerceIn(0f, 0.8f))
            }
    ) {
        // Заголовок карточки: иконка + имя
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIconVisual(app, 24.dp)
            Spacer(Modifier.width(8.dp))
            Text(
                text = app.name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.height(10.dp))

        Box(
            Modifier
                .size(cardWidth, cardHeight)
                .clip(RoundedCornerShape(28.dp))
                .background(if (vm.darkTheme) IosDarkCard else Color.White)
        ) {
            // Живая миниатюра: экран приложения, отрисованный в полном
            // размере и уменьшенный graphicsLayer-масштабом
            Box(
                Modifier
                    .requiredSize(screenWidth, screenHeight)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
            ) {
                AppScreenHost(appId = appId, vm = vm)
            }
            // Прозрачный слой: перехватывает тапы и свайп-вверх
            Box(
                Modifier
                    .matchParentSize()
                    .pointerInput(appId) {
                        detectTapGestures { vm.openApp(currentAppId) }
                    }
                    .pointerInput(appId) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                scope.launch {
                                    offsetY.snapTo(
                                        (offsetY.value + dragAmount).coerceAtMost(0f)
                                    )
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    if (offsetY.value < -dismissThresholdPx) {
                                        // Улетает вверх и удаляется из недавних
                                        offsetY.animateTo(-flyAwayPx, tween(240))
                                        vm.removeFromRecents(currentAppId)
                                    } else {
                                        offsetY.animateTo(0f, spring())
                                    }
                                }
                            },
                            onDragCancel = {
                                scope.launch { offsetY.animateTo(0f, spring()) }
                            }
                        )
                    }
            )
        }
    }
}
