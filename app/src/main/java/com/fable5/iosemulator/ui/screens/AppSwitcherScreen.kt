package com.fable5.iosemulator.ui.screens

import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
                items(vm.recentApps.toList(), key = { it.name }) { appId ->
                    SwitcherCard(appId = appId, vm = vm)
                }
            }
        }
    }
}

/** Карточка одного приложения в переключателе. */
@Composable
private fun SwitcherCard(appId: AppId, vm: EmulatorViewModel) {
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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .offset { IntOffset(0, offsetY.value.roundToInt()) }
            .graphicsLayer {
                alpha = 1f - (-offsetY.value / 1400f).coerceIn(0f, 0.8f)
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
                                    if (offsetY.value < -240f) {
                                        // Улетает вверх и удаляется из недавних
                                        offsetY.animateTo(-2200f, tween(240))
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
