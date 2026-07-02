package com.fable5.iosemulator.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fable5.iosemulator.ui.components.StatusBarHeight
import com.fable5.iosemulator.ui.theme.IosBlue
import com.fable5.iosemulator.viewmodel.EmulatorViewModel

/** Палитра «фотографий»: пары цветов для градиентов. */
private val photoGradients = listOf(
    listOf(Color(0xFFFF9A8B), Color(0xFFFF6A88)),
    listOf(Color(0xFF667EEA), Color(0xFF764BA2)),
    listOf(Color(0xFF43E97B), Color(0xFF38F9D7)),
    listOf(Color(0xFFFA709A), Color(0xFFFEE140)),
    listOf(Color(0xFF30CFD0), Color(0xFF330867)),
    listOf(Color(0xFFA8EDEA), Color(0xFFFED6E3)),
    listOf(Color(0xFFFF758C), Color(0xFFFF7EB3)),
    listOf(Color(0xFF08AEEA), Color(0xFF2AF598)),
    listOf(Color(0xFFFEC163), Color(0xFFDE4313)),
    listOf(Color(0xFF0BA360), Color(0xFF3CBA92)),
    listOf(Color(0xFFB721FF), Color(0xFF21D4FD)),
    listOf(Color(0xFF3B41C5), Color(0xFFA981BB))
)

private val photoEmojis = listOf(
    "🌄", "🌊", "🌸", "🏔️", "🌅", "🦋",
    "🌴", "🍂", "⛺", "🌌", "🐚", "🌿"
)

private const val PHOTO_COUNT = 48

/**
 * Mock Photos: сетка «фотографий» (детерминированные градиенты + эмодзи),
 * сегментированный переключатель масштаба и полноэкранный просмотр
 * с листанием.
 */
@Composable
fun PhotosScreen(vm: EmulatorViewModel) {
    val dark = vm.darkTheme
    val background = if (dark) Color.Black else Color.White
    val textColor = if (dark) Color.White else Color.Black

    var tab by remember { mutableStateOf(2) } // 0 — Годы, 1 — Месяцы, 2 — Все фото
    var viewerIndex by remember { mutableStateOf<Int?>(null) }

    Box(Modifier.fillMaxSize().background(background)) {
        Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.height(StatusBarHeight))
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Медиатека", color = textColor, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("Выбрать", color = IosBlue, fontSize = 17.sp)
            }

            val columns = when (tab) {
                0 -> 1
                1 -> 2
                else -> 3
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items((0 until PHOTO_COUNT).toList()) { index ->
                    PhotoTile(
                        index = index,
                        emojiSize = if (columns == 3) 34 else 56,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { viewerIndex = index }
                    )
                }
            }
        }

        // ---------- Сегментированный переключатель внизу ----------
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 52.dp)
                .clip(CircleShape)
                .background(
                    if (dark) Color(0xFF2C2C2E).copy(alpha = 0.95f)
                    else Color(0xFFE9E9EB).copy(alpha = 0.95f)
                )
                .padding(3.dp)
        ) {
            listOf("Годы", "Месяцы", "Все фото").forEachIndexed { index, title ->
                Box(
                    Modifier
                        .clip(CircleShape)
                        .background(
                            if (tab == index) {
                                if (dark) Color(0xFF636366) else Color.White
                            } else Color.Transparent
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { tab = index }
                        .padding(horizontal = 16.dp, vertical = 7.dp)
                ) {
                    Text(
                        title,
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = if (tab == index) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        // ---------- Полноэкранный просмотр ----------
        AnimatedVisibility(
            visible = viewerIndex != null,
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f)
        ) {
            val startIndex = viewerIndex ?: 0
            PhotoViewer(startIndex = startIndex, onClose = { viewerIndex = null })
        }
    }
}

/** Одна «фотография»: градиент + эмодзи, детерминированные по индексу. */
@Composable
private fun PhotoTile(index: Int, emojiSize: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(
            Brush.linearGradient(photoGradients[index % photoGradients.size])
        ),
        contentAlignment = Alignment.Center
    ) {
        Text(photoEmojis[index % photoEmojis.size], fontSize = emojiSize.sp)
    }
}

/** Полноэкранный просмотрщик с горизонтальным листанием. */
@Composable
private fun PhotoViewer(startIndex: Int, onClose: () -> Unit) {
    val pagerState = rememberPagerState(
        initialPage = startIndex,
        pageCount = { PHOTO_COUNT }
    )
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    PhotoTile(index = page, emojiSize = 110, modifier = Modifier.fillMaxSize())
                }
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = StatusBarHeight + 4.dp, start = 20.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Готово",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClose() }
            )
            Spacer(Modifier.weight(1f))
            Text(
                "${pagerState.currentPage + 1} из $PHOTO_COUNT",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 15.sp
            )
        }
    }
}
