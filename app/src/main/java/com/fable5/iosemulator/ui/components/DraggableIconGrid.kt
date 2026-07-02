package com.fable5.iosemulator.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.fable5.iosemulator.model.HomeItem
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * Сетка иконок домашнего экрана с drag & drop как в iOS:
 * - долгое нажатие поднимает иконку (haptic + увеличение),
 *   остальные иконки начинают «покачиваться» (режим редактирования);
 * - сброс над центром другой иконки объединяет их в папку;
 * - сброс в другой ячейке переставляет иконку с пружинной анимацией;
 * - обычное нажатие слегка «продавливает» иконку (эффект нажатия iOS).
 *
 * Тап отдаёт наружу центр иконки в координатах корня — из этой точки
 * анимируется запуск приложения.
 */
@Composable
fun DraggableIconGrid(
    items: List<HomeItem>,
    onMove: (from: Int, to: Int) -> Unit,
    onMerge: (from: Int, target: Int) -> Unit,
    onItemTap: (index: Int, center: Offset) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 4,
    cellHeight: Dp = 100.dp
) {
    val currentOnMove by rememberUpdatedState(onMove)
    val currentOnMerge by rememberUpdatedState(onMerge)
    val currentOnTap by rememberUpdatedState(onItemTap)
    val haptics = LocalHapticFeedback.current

    BoxWithConstraints(modifier.fillMaxWidth()) {
        val cellWidth = maxWidth / columns
        val density = LocalDensity.current
        val cellWidthPx = with(density) { cellWidth.toPx() }
        val cellHeightPx = with(density) { cellHeight.toPx() }
        val rows = if (items.isEmpty()) 0 else (items.size + columns - 1) / columns

        var draggedKey by remember { mutableStateOf<String?>(null) }
        var dragOffset by remember { mutableStateOf(Offset.Zero) }

        // «Покачивание» иконок, пока идёт перетаскивание (jiggle mode)
        val jiggleTransition = rememberInfiniteTransition(label = "jiggle")
        val jiggleAngle by jiggleTransition.animateFloat(
            initialValue = -1.7f,
            targetValue = 1.7f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 140, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "jiggleAngle"
        )

        Box(Modifier.fillMaxWidth().height(cellHeight * rows)) {
            items.forEachIndexed { index, item ->
                key(item.key) {
                    val col = index % columns
                    val row = index / columns
                    val restingOffset = IntOffset(
                        (col * cellWidthPx).roundToInt(),
                        (row * cellHeightPx).roundToInt()
                    )
                    val animatedOffset by animateIntOffsetAsState(
                        targetValue = restingOffset,
                        animationSpec = spring(
                            dampingRatio = 0.8f,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "cellOffset"
                    )
                    val isDragged = draggedKey == item.key
                    val editMode = draggedKey != null

                    // Эффект нажатия iOS: иконка слегка уменьшается под пальцем
                    val pressInteraction = remember { MutableInteractionSource() }
                    val pressed by pressInteraction.collectIsPressedAsState()
                    val pressScale by animateFloatAsState(
                        targetValue = if (pressed) 0.88f else 1f,
                        animationSpec = tween(120),
                        label = "pressScale"
                    )

                    // Центр иконки в координатах корня — для анимации запуска
                    var itemCenter by remember { mutableStateOf(Offset.Zero) }

                    Box(
                        modifier = Modifier
                            .offset {
                                if (isDragged) {
                                    restingOffset + IntOffset(
                                        dragOffset.x.roundToInt(),
                                        dragOffset.y.roundToInt()
                                    )
                                } else {
                                    animatedOffset
                                }
                            }
                            .size(cellWidth, cellHeight)
                            .zIndex(if (isDragged) 1f else 0f)
                            .onGloballyPositioned { coordinates ->
                                val position = coordinates.positionInRoot()
                                itemCenter = Offset(
                                    position.x + coordinates.size.width / 2f,
                                    position.y + coordinates.size.height / 2f
                                )
                            }
                            .graphicsLayer {
                                when {
                                    isDragged -> {
                                        scaleX = 1.12f
                                        scaleY = 1.12f
                                        alpha = 0.92f
                                    }
                                    editMode -> {
                                        // Соседи покачиваются в противофазе
                                        rotationZ = if (index % 2 == 0) jiggleAngle else -jiggleAngle
                                    }
                                    else -> {
                                        scaleX = pressScale
                                        scaleY = pressScale
                                    }
                                }
                            }
                            .pointerInput(item.key, index, items.size) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        draggedKey = item.key
                                        dragOffset = Offset.Zero
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffset += dragAmount
                                    },
                                    onDragCancel = {
                                        draggedKey = null
                                        dragOffset = Offset.Zero
                                    },
                                    onDragEnd = {
                                        // Куда «прилетела» середина перетаскиваемой иконки
                                        val centerX = col * cellWidthPx + cellWidthPx / 2f + dragOffset.x
                                        val centerY = row * cellHeightPx + cellHeightPx / 2f + dragOffset.y
                                        val targetCol = (centerX / cellWidthPx).toInt()
                                            .coerceIn(0, columns - 1)
                                        val targetRow = (centerY / cellHeightPx).toInt()
                                            .coerceIn(0, maxOf(rows - 1, 0))
                                        val target = (targetRow * columns + targetCol)
                                            .coerceIn(0, items.lastIndex)
                                        if (target != index) {
                                            val targetCenterX = targetCol * cellWidthPx + cellWidthPx / 2f
                                            val targetCenterY = targetRow * cellHeightPx + cellHeightPx / 2f
                                            val distance = hypot(
                                                centerX - targetCenterX,
                                                centerY - targetCenterY
                                            )
                                            // Точно над центром чужой иконки → папка
                                            if (distance < cellWidthPx * 0.35f) {
                                                currentOnMerge(index, target)
                                            } else {
                                                currentOnMove(index, target)
                                            }
                                        }
                                        draggedKey = null
                                        dragOffset = Offset.Zero
                                    }
                                )
                            },
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            Modifier.clickable(
                                interactionSource = pressInteraction,
                                indication = null
                            ) { currentOnTap(index, itemCenter) }
                        ) {
                            HomeItemView(item)
                        }
                    }
                }
            }
        }
    }
}
