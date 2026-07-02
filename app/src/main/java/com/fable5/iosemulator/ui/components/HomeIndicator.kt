package com.fable5.iosemulator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/** Полоска Home Indicator внизу экрана. */
@Composable
fun HomeIndicator(color: Color = Color.White, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(134.dp)
            .height(5.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.85f))
    )
}

/**
 * Зона жестов внизу экрана (как у iPhone без кнопки Home):
 * короткий свайп вверх — «домой», длинный — App Switcher.
 */
@Composable
fun HomeGestureArea(
    onShortSwipeUp: () -> Unit,
    onLongSwipeUp: () -> Unit,
    indicatorColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    // rememberUpdatedState — чтобы pointerInput не захватывал устаревшие колбэки
    val shortSwipe by rememberUpdatedState(onShortSwipeUp)
    val longSwipe by rememberUpdatedState(onLongSwipeUp)
    var totalDrag by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp)
            .pointerInput(Unit) {
                val shortThreshold = -40.dp.toPx()
                val longThreshold = -170.dp.toPx()
                detectVerticalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        totalDrag += dragAmount
                    },
                    onDragEnd = {
                        when {
                            totalDrag < longThreshold -> longSwipe()
                            totalDrag < shortThreshold -> shortSwipe()
                        }
                    }
                )
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        HomeIndicator(indicatorColor, Modifier.padding(bottom = 9.dp))
    }
}
