package com.fable5.iosemulator.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fable5.iosemulator.model.HomeItem
import com.fable5.iosemulator.model.Wallpapers
import com.fable5.iosemulator.ui.components.AppIconVisual
import com.fable5.iosemulator.ui.components.DraggableIconGrid
import com.fable5.iosemulator.ui.components.StatusBarHeight
import com.fable5.iosemulator.ui.components.WallpaperBackground
import com.fable5.iosemulator.ui.components.asIosClock
import com.fable5.iosemulator.ui.components.asIosDate
import com.fable5.iosemulator.ui.components.rememberCurrentTime
import com.fable5.iosemulator.viewmodel.EmulatorViewModel
import kotlin.math.abs

/**
 * Домашний экран iOS: обои, виджеты, страницы иконок с drag & drop,
 * точки-индикаторы страниц, док и оверлей открытой папки.
 */
@Composable
fun HomeScreen(vm: EmulatorViewModel, modifier: Modifier = Modifier) {
    val wallpaper = Wallpapers.all[vm.wallpaperIndex.coerceIn(0, Wallpapers.all.lastIndex)]
    val folderOpen = vm.openedFolderKey != null

    // Блюр контента, когда открыта папка (реальный blur на Android 12+)
    val contentBlur by animateDpAsState(
        targetValue = if (folderOpen) 24.dp else 0.dp,
        animationSpec = tween(300),
        label = "homeBlur"
    )

    Box(modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().blur(contentBlur)) {
            WallpaperBackground(wallpaper)
            Column(Modifier.fillMaxSize()) {
                Spacer(Modifier.height(StatusBarHeight + 10.dp))

                val pagerState = rememberPagerState(pageCount = { vm.pages.size })
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    // Параллакс: контент страницы движется медленнее свайпа
                    // и слегка тает на краях — как перелистывание в iOS.
                    // Внешний Box клипует сдвинутый контент по границам страницы,
                    // иначе иконки наезжают на соседнюю страницу.
                    val pageOffset =
                        (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    Box(Modifier.fillMaxSize().clipToBounds()) {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp)
                                .graphicsLayer {
                                    translationX = pageOffset * size.width * 0.25f
                                    alpha = 1f - abs(pageOffset).coerceIn(0f, 1f) * 0.4f
                                }
                        ) {
                        if (page == 0) {
                            HomeWidgets()
                            Spacer(Modifier.height(20.dp))
                        }
                        DraggableIconGrid(
                            items = vm.pages[page],
                            onMove = { from, to -> vm.moveItem(page, from, to) },
                            onMerge = { from, to -> vm.mergeItems(page, from, to) },
                            onItemTap = { index, center ->
                                when (val item = vm.pages[page].getOrNull(index)) {
                                    is HomeItem.App -> vm.openApp(item.app.id, center)
                                    is HomeItem.Folder -> vm.openFolder(item.key)
                                    null -> Unit
                                }
                            },
                            badgeFor = vm::badgeFor
                        )
                        }
                    }
                }

                SearchPill(onTap = { vm.showSpotlight() })
                Spacer(Modifier.height(12.dp))
                Dock(vm)
                Spacer(Modifier.height(34.dp)) // место под Home Indicator
            }
        }

        // ---------- Оверлей открытой папки ----------
        // Запоминаем последнюю открытую папку: во время exit-анимации
        // openedFolderKey уже null, и без этого папка исчезала бы рывком
        var lastFolder by remember { mutableStateOf<HomeItem.Folder?>(null) }
        val currentFolder = vm.openedFolderKey?.let { vm.findFolder(it) }
        if (currentFolder != null) lastFolder = currentFolder
        AnimatedVisibility(
            visible = folderOpen,
            enter = fadeIn(tween(220)) + scaleIn(initialScale = 0.9f, animationSpec = tween(260)),
            exit = fadeOut(tween(180)) + scaleOut(targetScale = 0.92f, animationSpec = tween(200))
        ) {
            val folder = currentFolder ?: lastFolder
            if (folder != null) {
                FolderOverlay(
                    folder = folder,
                    onAppClick = { vm.openApp(it) },
                    onDismiss = { vm.closeFolder() }
                )
            }
        }
    }
}

/** Виджет-пара в верхней части первой страницы: часы/дата и погода. */
@Composable
private fun HomeWidgets() {
    val now by rememberCurrentTime()
    val time = now.asIosClock()
    val weekday = now.asIosDate("EEEE")
    val date = now.asIosDate("d MMMM")

    Row(
        Modifier
            .fillMaxWidth()
            .height(150.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Виджет часов и даты
        WidgetCard(Modifier.weight(1f)) {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Text(weekday, color = Color(0xFFFF6961), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(time, color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text(date, color = Color.White.copy(alpha = 0.75f), fontSize = 15.sp)
            }
        }
        // Виджет погоды
        WidgetCard(Modifier.weight(1f)) {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Text("Купертино", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text("21°", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Light)
                Spacer(Modifier.weight(1f))
                Text("⛅ Переменная облачность", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                Text("Макс.: 24°  мин.: 16°", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
            }
        }
    }
}

/** Полупрозрачная «стеклянная» карточка-виджет (материал iOS). */
@Composable
private fun WidgetCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(Color.Black.copy(alpha = 0.34f), Color.Black.copy(alpha = 0.22f))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.16f), shape)
    ) {
        content()
    }
}

/** Капсула «Поиск» над доком — как в iOS 26/27. */
@Composable
private fun SearchPill(onTap: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Row(
            Modifier
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.24f))
                .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onTap() }
                .padding(horizontal = 16.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(5.dp))
            Text("Поиск", color = Color.White, fontSize = 13.sp)
        }
    }
}

/** «Стеклянный» док с четырьмя основными приложениями. */
@Composable
private fun Dock(vm: EmulatorViewModel) {
    val shape = RoundedCornerShape(38.dp)
    Row(
        Modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.30f), Color.White.copy(alpha = 0.16f))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.22f), shape)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        vm.dockApps.forEach { app ->
            DockIcon(
                app = app,
                badge = vm.badgeFor(app.id),
                onTap = { center -> vm.openApp(app.id, center) }
            )
        }
    }
}

/** Иконка дока: эффект нажатия + передача позиции для анимации запуска. */
@Composable
private fun DockIcon(
    app: com.fable5.iosemulator.model.IosApp,
    badge: Int?,
    onTap: (Offset) -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = tween(120),
        label = "dockPress"
    )
    var center by remember { mutableStateOf(Offset.Zero) }
    Box(
        Modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInRoot()
                center = Offset(
                    position.x + coordinates.size.width / 2f,
                    position.y + coordinates.size.height / 2f
                )
            }
            .clickable(interactionSource = interaction, indication = null) { onTap(center) }
    ) {
        AppIconVisual(app, badge = badge)
    }
}

/** Открытая папка: затемнение, название и крупная сетка приложений. */
@Composable
private fun FolderOverlay(
    folder: HomeItem.Folder,
    onAppClick: (com.fable5.iosemulator.model.AppId) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = folder.name,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(22.dp))
            val folderShape = RoundedCornerShape(40.dp)
            Box(
                Modifier
                    .clip(folderShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.38f), Color.White.copy(alpha = 0.24f))
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.25f), folderShape)
                    .padding(26.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    folder.apps.chunked(3).forEach { rowApps ->
                        Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                            rowApps.forEach { app ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .width(66.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) { onAppClick(app.id) }
                                ) {
                                    AppIconVisual(app, 62.dp)
                                    Spacer(Modifier.height(5.dp))
                                    Text(
                                        app.name,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
