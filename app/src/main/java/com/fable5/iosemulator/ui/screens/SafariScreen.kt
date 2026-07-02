package com.fable5.iosemulator.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterNone
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fable5.iosemulator.ui.components.StatusBarHeight
import com.fable5.iosemulator.ui.theme.IosBlue
import com.fable5.iosemulator.viewmodel.EmulatorViewModel

/** Заглушка сайта для mock-браузера. */
private data class MockSite(
    val name: String,
    val domain: String,
    val emoji: String,
    val colors: List<Color>,
    val tagline: String
)

private val mockSites = listOf(
    MockSite("Apple", "apple.com", "🍎", listOf(Color(0xFF232526), Color(0xFF414345)),
        "iPhone 17 Pro. Титан. Мощь. Магия."),
    MockSite("Anthropic", "anthropic.com", "✳️", listOf(Color(0xFFCC785C), Color(0xFF8D5A44)),
        "Безопасный ИИ для всего человечества"),
    MockSite("GitHub", "github.com", "🐙", listOf(Color(0xFF24292F), Color(0xFF0D1117)),
        "Где живёт код миллионов разработчиков"),
    MockSite("YouTube", "youtube.com", "▶️", listOf(Color(0xFFFF0000), Color(0xFF8B0000)),
        "Смотрите, что нравится"),
    MockSite("Википедия", "wikipedia.org", "🌐", listOf(Color(0xFF636E72), Color(0xFF2D3436)),
        "Свободная энциклопедия"),
    MockSite("Telegram", "telegram.org", "✈️", listOf(Color(0xFF2AABEE), Color(0xFF229ED9)),
        "Быстрый и безопасный мессенджер")
)

/**
 * Mock Safari: стартовая страница с «Избранным», адресная строка снизу
 * (как в iOS 15+) и генерируемые страницы-заглушки для введённых адресов.
 */
@Composable
fun SafariScreen(vm: EmulatorViewModel) {
    val dark = vm.darkTheme
    var addressText by remember { mutableStateOf("") }
    var currentSite by remember { mutableStateOf<MockSite?>(null) }
    val focusManager = LocalFocusManager.current

    val background = if (dark) Color(0xFF1C1C1E) else Color(0xFFF7F7F9)
    val barColor = if (dark) Color(0xFF2C2C2E) else Color.White
    val textColor = if (dark) Color.White else Color.Black

    fun navigate() {
        val query = addressText.trim().lowercase()
        if (query.isEmpty()) return
        currentSite = mockSites.firstOrNull { site ->
            query.contains(site.domain) || query.contains(site.name.lowercase())
        } ?: MockSite(
            name = addressText.trim(),
            domain = query.substringBefore('/'),
            emoji = "🔎",
            colors = listOf(Color(0xFF5B86E5), Color(0xFF36D1DC)),
            tagline = "Результаты для «${addressText.trim()}»"
        )
        focusManager.clearFocus()
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(background)
    ) {
        Spacer(Modifier.height(StatusBarHeight))

        Box(Modifier.weight(1f)) {
            val site = currentSite
            if (site == null) {
                SafariStartPage(dark = dark, textColor = textColor) { selected ->
                    currentSite = selected
                    addressText = selected.domain
                }
            } else {
                MockSitePage(site = site, dark = dark)
            }
        }

        // ---------- Нижняя панель с адресной строкой ----------
        Column(
            Modifier
                .fillMaxWidth()
                .background(barColor.copy(alpha = 0.97f))
                .imePadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (dark) Color(0xFF3A3A3C) else Color(0xFFE9E9EB))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = addressText,
                    onValueChange = { addressText = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = textColor,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    ),
                    cursorBrush = SolidColor(IosBlue),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { navigate() }),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.Center) {
                            if (addressText.isEmpty()) {
                                Text(
                                    "Поиск или адрес сайта",
                                    color = textColor.copy(alpha = 0.4f),
                                    fontSize = 16.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Обновить",
                    tint = textColor.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { navigate() }
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToolbarIcon(Icons.Filled.ChevronLeft, enabled = currentSite != null) {
                    currentSite = null
                    addressText = ""
                }
                ToolbarIcon(Icons.Filled.ChevronRight, enabled = false) {}
                ToolbarIcon(Icons.Filled.IosShare, enabled = true) {}
                ToolbarIcon(Icons.Filled.Book, enabled = true) {}
                ToolbarIcon(Icons.Filled.FilterNone, enabled = true) {}
            }
        }
    }
}

@Composable
private fun ToolbarIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (enabled) IosBlue else IosBlue.copy(alpha = 0.35f),
        modifier = Modifier
            .size(26.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled
            ) { onClick() }
    )
}

/** Стартовая страница Safari: «Избранное» и отчёт о конфиденциальности. */
@Composable
private fun SafariStartPage(
    dark: Boolean,
    textColor: Color,
    onOpen: (MockSite) -> Unit
) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        item {
            Spacer(Modifier.height(14.dp))
            Text("Избранное", color = textColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            mockSites.chunked(4).forEach { rowSites ->
                Row(Modifier.fillMaxWidth()) {
                    rowSites.forEach { site ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onOpen(site) }
                        ) {
                            Box(
                                Modifier
                                    .size(58.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Brush.linearGradient(site.colors)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(site.emoji, fontSize = 26.sp)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                site.name,
                                color = textColor.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }
                    // Заполнители, чтобы неполный ряд не растягивался
                    repeat(4 - rowSites.size) { Spacer(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(18.dp))
            }
        }
        item {
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (dark) Color(0xFF2C2C2E) else Color.White)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        "Отчёт о конфиденциальности",
                        color = textColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "🛡️ Safari заблокировал 12 трекеров за последние 7 дней",
                        color = textColor.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

/** Страница-заглушка сайта: «шапка» с градиентом и скелетон статьи. */
@Composable
private fun MockSitePage(site: MockSite, dark: Boolean) {
    val lineColor = if (dark) Color(0xFF3A3A3C) else Color(0xFFE3E3E8)
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Brush.linearGradient(site.colors)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(site.emoji, fontSize = 44.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(site.name, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(site.domain, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                }
            }
        }
        item {
            Column(Modifier.padding(20.dp)) {
                Text(
                    site.tagline,
                    color = if (dark) Color.White else Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(18.dp))
                // Скелетон текста: строки разной ширины
                val widths = listOf(1f, 0.92f, 0.97f, 0.7f, 1f, 0.85f, 0.94f, 0.6f)
                widths.forEach { fraction ->
                    Box(
                        Modifier
                            .fillMaxWidth(fraction)
                            .height(13.dp)
                            .clip(CircleShape)
                            .background(lineColor)
                    )
                    Spacer(Modifier.height(10.dp))
                }
                Spacer(Modifier.height(10.dp))
                // «Изображения» статьи
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(2) { index ->
                        Box(
                            Modifier
                                .weight(1f)
                                .height(110.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        if (index == 0) site.colors
                                        else site.colors.reversed()
                                    )
                                )
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                widths.take(5).forEach { fraction ->
                    Box(
                        Modifier
                            .fillMaxWidth(fraction)
                            .height(13.dp)
                            .clip(CircleShape)
                            .background(lineColor)
                    )
                    Spacer(Modifier.height(10.dp))
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}
