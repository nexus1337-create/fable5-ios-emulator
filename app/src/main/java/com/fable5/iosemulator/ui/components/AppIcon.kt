package com.fable5.iosemulator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fable5.iosemulator.model.AppBadges
import com.fable5.iosemulator.model.AppId
import com.fable5.iosemulator.model.HomeItem
import com.fable5.iosemulator.model.IosApp

/**
 * Визуал иконки приложения: скруглённый квадрат
 * (радиус ≈ 22,37% стороны — как суперэллипс иконок iOS).
 * Самые узнаваемые иконки нарисованы вручную на Canvas (IosAppIcons.kt),
 * остальные — Material-вектор на вертикальном градиенте.
 */
@Composable
fun AppIconVisual(app: IosApp, size: Dp = 60.dp, showBadge: Boolean = false) {
    Box(Modifier.size(size)) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(size * 0.2237f)),
            contentAlignment = Alignment.Center
        ) {
            when (app.id) {
                AppId.SAFARI -> SafariIcon()
                AppId.PHOTOS -> PhotosIcon()
                AppId.CLOCK -> ClockIcon()
                AppId.CALENDAR -> CalendarIcon(size)
                AppId.NOTES -> NotesIcon()
                AppId.CALCULATOR -> CalculatorIcon()
                AppId.APP_STORE -> AppStoreIcon()
                AppId.MAIL -> MailIcon()
                AppId.WEATHER -> WeatherIcon()
                AppId.MAPS -> MapsIcon()
                AppId.WALLET -> WalletIcon()
                AppId.STOCKS -> StocksIcon()
                else -> DefaultAppIcon(app, size)
            }
        }
        // Красный бейдж непрочитанных, как в iOS
        val badgeCount = if (showBadge) AppBadges.counts[app.id] else null
        if (badgeCount != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = size * 0.1f, y = -size * 0.1f)
                    .size(size * 0.34f)
                    .clip(CircleShape)
                    .background(Color(0xFFFF3B30)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeCount.toString(),
                    color = Color.White,
                    fontSize = (size.value * 0.2f).sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/** Иконка по умолчанию: вертикальный градиент + векторный символ. */
@Composable
private fun DefaultAppIcon(app: IosApp, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(Brush.verticalGradient(app.gradient)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = app.icon,
            contentDescription = app.name,
            tint = app.iconTint,
            modifier = Modifier.size(size * 0.52f)
        )
    }
}

/** Иконка папки: полупрозрачная подложка с мини-иконками внутри. */
@Composable
fun FolderIconVisual(folder: HomeItem.Folder, size: Dp = 60.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.2237f))
            .background(Color.White.copy(alpha = 0.28f)),
        contentAlignment = Alignment.Center
    ) {
        val mini = size * 0.22f
        val gap = size * 0.06f
        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
            folder.apps.take(9).chunked(3).forEach { rowApps ->
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    rowApps.forEach { app -> AppIconVisual(app, mini) }
                }
            }
        }
    }
}

/**
 * Элемент домашнего экрана: иконка (приложение или папка) + подпись
 * с лёгкой тенью, чтобы читалась на любых обоях.
 */
@Composable
fun HomeItemView(item: HomeItem, iconSize: Dp = 60.dp, labelColor: Color = Color.White) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        when (item) {
            is HomeItem.App -> AppIconVisual(item.app, iconSize, showBadge = true)
            is HomeItem.Folder -> FolderIconVisual(item, iconSize)
        }
        Spacer(Modifier.height(5.dp))
        val label = when (item) {
            is HomeItem.App -> item.app.name
            is HomeItem.Folder -> item.name
        }
        Text(
            text = label,
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = labelColor,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.35f),
                    blurRadius = 6f
                )
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}
