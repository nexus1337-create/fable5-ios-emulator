package com.fable5.iosemulator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.fable5.iosemulator.model.HomeItem
import com.fable5.iosemulator.model.IosApp

/**
 * Визуал иконки приложения: скруглённый квадрат с градиентом
 * (радиус ≈ 22,37% стороны — как суперэллипс иконок iOS).
 */
@Composable
fun AppIconVisual(app: IosApp, size: Dp = 60.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.2237f))
            .background(Brush.linearGradient(app.gradient)),
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
            is HomeItem.App -> AppIconVisual(item.app, iconSize)
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
                fontSize = 12.sp,
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
