package com.fable5.iosemulator.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * Фирменные иконки iOS, нарисованные вручную на Canvas —
 * максимально близко к оригиналам: компас Safari, цветок «Фото»,
 * живые «Часы» и «Календарь», конверт «Почты» и т.д.
 * Каждая иконка рисует свой фон; скругление задаёт родитель (AppIconVisual).
 */

// ---------------------------------------------------------------
// Safari: белый фон, синий циферблат с делениями, красно-белая стрелка
// ---------------------------------------------------------------
@Composable
fun SafariIcon() {
    Box(Modifier.fillMaxSize().background(Color.White)) {
        Canvas(Modifier.fillMaxSize()) {
            val c = center
            val r = size.minDimension * 0.4f
            drawCircle(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF52B4FA), Color(0xFF1667E8))
                ),
                radius = r,
                center = c
            )
            // Деления по кругу (каждое шестое — крупнее)
            for (i in 0 until 72) {
                val major = i % 6 == 0
                rotate(degrees = i * 5f, pivot = c) {
                    drawLine(
                        color = Color.White.copy(alpha = if (major) 0.95f else 0.5f),
                        start = Offset(c.x, c.y - r * 0.93f),
                        end = Offset(c.x, c.y - r * (if (major) 0.78f else 0.85f)),
                        strokeWidth = size.minDimension * 0.012f
                    )
                }
            }
            // Стрелка компаса под 45°: белая половина + красная
            rotate(degrees = 45f, pivot = c) {
                val needle = r * 0.62f
                val halfWidth = r * 0.16f
                val white = Path().apply {
                    moveTo(c.x, c.y - needle)
                    lineTo(c.x - halfWidth, c.y)
                    lineTo(c.x + halfWidth, c.y)
                    close()
                }
                val red = Path().apply {
                    moveTo(c.x, c.y + needle)
                    lineTo(c.x - halfWidth, c.y)
                    lineTo(c.x + halfWidth, c.y)
                    close()
                }
                drawPath(white, Color.White)
                drawPath(red, Color(0xFFFF3B30))
            }
        }
    }
}

// ---------------------------------------------------------------
// Фото: белый фон и цветок из восьми полупрозрачных лепестков
// ---------------------------------------------------------------
private val petalColors = listOf(
    Color(0xFFFFCC00), Color(0xFFFF9500), Color(0xFFFF3B30), Color(0xFFFF2D55),
    Color(0xFFAF52DE), Color(0xFF007AFF), Color(0xFF5AC8FA), Color(0xFF34C759)
)

@Composable
fun PhotosIcon() {
    Box(Modifier.fillMaxSize().background(Color.White)) {
        Canvas(Modifier.fillMaxSize()) {
            val c = center
            val petalWidth = size.width * 0.19f
            val petalHeight = size.height * 0.36f
            petalColors.forEachIndexed { index, color ->
                rotate(degrees = index * 45f, pivot = c) {
                    drawOval(
                        color = color.copy(alpha = 0.72f),
                        topLeft = Offset(c.x - petalWidth / 2f, size.height * 0.12f),
                        size = Size(petalWidth, petalHeight)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------
// Часы: белый циферблат с живыми стрелками (тикают каждую секунду)
// ---------------------------------------------------------------
@Composable
fun ClockIcon() {
    var now by remember { mutableStateOf(Calendar.getInstance()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = Calendar.getInstance()
            delay(1000L)
        }
    }
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Canvas(Modifier.fillMaxSize()) {
            val c = center
            val r = size.minDimension * 0.44f
            drawCircle(Color.White, radius = r, center = c)
            // Часовые метки
            for (i in 0 until 12) {
                rotate(degrees = i * 30f, pivot = c) {
                    drawLine(
                        color = Color.Black,
                        start = Offset(c.x, c.y - r * 0.92f),
                        end = Offset(c.x, c.y - r * 0.8f),
                        strokeWidth = size.minDimension * 0.02f,
                        cap = StrokeCap.Round
                    )
                }
            }
            val hours = now.get(Calendar.HOUR)
            val minutes = now.get(Calendar.MINUTE)
            val seconds = now.get(Calendar.SECOND)

            fun hand(angleDeg: Float, length: Float, width: Float, color: Color) {
                val rad = Math.toRadians(angleDeg.toDouble() - 90.0)
                drawLine(
                    color = color,
                    start = c,
                    end = Offset(
                        c.x + (length * cos(rad)).toFloat(),
                        c.y + (length * sin(rad)).toFloat()
                    ),
                    strokeWidth = width,
                    cap = StrokeCap.Round
                )
            }
            hand((hours + minutes / 60f) * 30f, r * 0.45f, size.minDimension * 0.035f, Color.Black)
            hand(minutes * 6f, r * 0.68f, size.minDimension * 0.028f, Color.Black)
            hand(seconds * 6f, r * 0.72f, size.minDimension * 0.012f, Color(0xFFFF9500))
            drawCircle(Color.Black, radius = size.minDimension * 0.028f, center = c)
        }
    }
}

// ---------------------------------------------------------------
// Календарь: белый фон, красный день недели и крупное число (живые)
// ---------------------------------------------------------------
@Composable
fun CalendarIcon(iconSize: Dp) {
    val locale = Locale("ru")
    val weekday = remember { SimpleDateFormat("EE", locale).format(Date()).uppercase(locale) }
    val day = remember { SimpleDateFormat("d", locale).format(Date()) }
    val scale = iconSize.value
    Column(
        Modifier.fillMaxSize().background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = weekday,
            color = Color(0xFFFF3B30),
            fontSize = (scale * 0.14f).sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = iconSize * 0.12f)
        )
        Text(
            text = day,
            color = Color.Black,
            fontSize = (scale * 0.42f).sp,
            fontWeight = FontWeight.Light
        )
    }
}

// ---------------------------------------------------------------
// Заметки: жёлтая полоса сверху и «строчки» текста
// ---------------------------------------------------------------
@Composable
fun NotesIcon() {
    Box(Modifier.fillMaxSize().background(Color.White)) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFFCE38A), Color(0xFFF7D14C))
                ),
                topLeft = Offset.Zero,
                size = Size(size.width, size.height * 0.26f)
            )
            val lineColor = Color(0xFFC7C7CC)
            listOf(0.45f, 0.62f, 0.79f).forEach { fy ->
                drawLine(
                    color = lineColor,
                    start = Offset(size.width * 0.16f, size.height * fy),
                    end = Offset(size.width * 0.84f, size.height * fy),
                    strokeWidth = size.height * 0.035f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

// ---------------------------------------------------------------
// Калькулятор: чёрный фон, «дисплей» и кнопки (оранжевый столбец)
// ---------------------------------------------------------------
@Composable
fun CalculatorIcon() {
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Canvas(Modifier.fillMaxSize()) {
            drawRoundRect(
                color = Color.White.copy(alpha = 0.9f),
                topLeft = Offset(size.width * 0.2f, size.height * 0.16f),
                size = Size(size.width * 0.6f, size.height * 0.13f),
                cornerRadius = CornerRadius(size.width * 0.04f)
            )
            val columns = listOf(0.3f, 0.5f, 0.7f)
            val rows = listOf(0.5f, 0.72f)
            rows.forEach { fy ->
                columns.forEach { fx ->
                    drawCircle(
                        color = if (fx == 0.7f) Color(0xFFFF9500) else Color(0xFF636366),
                        radius = size.minDimension * 0.085f,
                        center = Offset(size.width * fx, size.height * fy)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------
// App Store: синий градиент и белая буква «А» из палочек
// ---------------------------------------------------------------
@Composable
fun AppStoreIcon() {
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF44C0FF), Color(0xFF1470E1)))
        )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val strokeWidth = size.minDimension * 0.09f
            val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            val path = Path().apply {
                moveTo(size.width * 0.3f, size.height * 0.72f)
                lineTo(size.width * 0.5f, size.height * 0.28f)
                lineTo(size.width * 0.7f, size.height * 0.72f)
            }
            drawPath(path, Color.White, style = stroke)
            drawLine(
                color = Color.White,
                start = Offset(size.width * 0.36f, size.height * 0.585f),
                end = Offset(size.width * 0.64f, size.height * 0.585f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

// ---------------------------------------------------------------
// Почта: синий градиент и белый конверт
// ---------------------------------------------------------------
@Composable
fun MailIcon() {
    val bgTop = Color(0xFF6FC5FF)
    val bgBottom = Color(0xFF1D77EF)
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(bgTop, bgBottom)))) {
        Canvas(Modifier.fillMaxSize()) {
            val left = size.width * 0.16f
            val right = size.width * 0.84f
            val top = size.height * 0.3f
            val bottom = size.height * 0.7f
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                cornerRadius = CornerRadius(size.width * 0.05f)
            )
            // Клапан конверта
            val flap = Path().apply {
                moveTo(left + size.width * 0.015f, top + size.height * 0.03f)
                lineTo(size.width * 0.5f, size.height * 0.53f)
                lineTo(right - size.width * 0.015f, top + size.height * 0.03f)
            }
            drawPath(
                flap,
                Color(0xFF2E86F5),
                style = Stroke(width = size.width * 0.035f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

// ---------------------------------------------------------------
// Погода: голубое небо, солнце и облако
// ---------------------------------------------------------------
@Composable
fun WeatherIcon() {
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF4098FF), Color(0xFF0F5AD7)))
        )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            // Солнце
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFE45C), Color(0xFFFFB300)),
                    center = Offset(size.width * 0.38f, size.height * 0.34f),
                    radius = size.width * 0.18f
                ),
                radius = size.width * 0.18f,
                center = Offset(size.width * 0.38f, size.height * 0.34f)
            )
            // Облако из окружностей + основание
            val cloud = Color.White
            drawCircle(cloud, size.width * 0.13f, Offset(size.width * 0.47f, size.height * 0.58f))
            drawCircle(cloud, size.width * 0.16f, Offset(size.width * 0.62f, size.height * 0.53f))
            drawCircle(cloud, size.width * 0.12f, Offset(size.width * 0.74f, size.height * 0.6f))
            drawRoundRect(
                color = cloud,
                topLeft = Offset(size.width * 0.4f, size.height * 0.56f),
                size = Size(size.width * 0.46f, size.height * 0.16f),
                cornerRadius = CornerRadius(size.width * 0.08f)
            )
        }
    }
}

// ---------------------------------------------------------------
// Карты: земля, парк, вода, дороги и синяя точка геолокации
// ---------------------------------------------------------------
@Composable
fun MapsIcon() {
    Box(Modifier.fillMaxSize().background(Color(0xFFEDE8DC))) {
        Canvas(Modifier.fillMaxSize()) {
            // Парк (зелёная зона) сверху слева
            drawRoundRect(
                color = Color(0xFFA8DC8C),
                topLeft = Offset(-size.width * 0.1f, -size.height * 0.1f),
                size = Size(size.width * 0.55f, size.height * 0.5f),
                cornerRadius = CornerRadius(size.width * 0.1f)
            )
            // Вода снизу справа
            drawRoundRect(
                color = Color(0xFF8FD1F2),
                topLeft = Offset(size.width * 0.6f, size.height * 0.62f),
                size = Size(size.width * 0.55f, size.height * 0.55f),
                cornerRadius = CornerRadius(size.width * 0.12f)
            )
            // Жёлтая магистраль по диагонали
            drawLine(
                color = Color(0xFFF7C948),
                start = Offset(-size.width * 0.05f, size.height * 0.78f),
                end = Offset(size.width * 1.05f, size.height * 0.3f),
                strokeWidth = size.height * 0.1f
            )
            // Белая улица
            drawLine(
                color = Color.White,
                start = Offset(size.width * 0.3f, -size.height * 0.05f),
                end = Offset(size.width * 0.62f, size.height * 1.05f),
                strokeWidth = size.height * 0.06f
            )
            // Точка геолокации
            drawCircle(Color.White, size.width * 0.11f, center)
            drawCircle(Color(0xFF2E86F5), size.width * 0.08f, center)
        }
    }
}

// ---------------------------------------------------------------
// Wallet: чёрный фон, стопка цветных карт и «карман»
// ---------------------------------------------------------------
private val walletCards = listOf(
    Color(0xFF3FBF7F), Color(0xFFFFC531), Color(0xFFFF7A45), Color(0xFF3E8BFF)
)

@Composable
fun WalletIcon() {
    Box(Modifier.fillMaxSize().background(Color(0xFF17181C))) {
        Canvas(Modifier.fillMaxSize()) {
            walletCards.forEachIndexed { index, color ->
                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width * 0.17f, size.height * (0.14f + index * 0.09f)),
                    size = Size(size.width * 0.66f, size.height * 0.3f),
                    cornerRadius = CornerRadius(size.width * 0.05f)
                )
            }
            // Передний карман
            drawRoundRect(
                color = Color(0xFF3A3D45),
                topLeft = Offset(size.width * 0.12f, size.height * 0.52f),
                size = Size(size.width * 0.76f, size.height * 0.34f),
                cornerRadius = CornerRadius(size.width * 0.07f)
            )
            drawLine(
                color = Color.White.copy(alpha = 0.25f),
                start = Offset(size.width * 0.12f, size.height * 0.53f),
                end = Offset(size.width * 0.88f, size.height * 0.53f),
                strokeWidth = size.height * 0.015f
            )
        }
    }
}

// ---------------------------------------------------------------
// Камера: светло-серый фон, тёмный корпус с объективом
// ---------------------------------------------------------------
@Composable
fun CameraIcon() {
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFFE9E9EE), Color(0xFFBDBEC4)))
        )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val body = Color(0xFF3A3A3C)
            // «Горб» видоискателя
            drawRoundRect(
                color = body,
                topLeft = Offset(size.width * 0.36f, size.height * 0.26f),
                size = Size(size.width * 0.28f, size.height * 0.12f),
                cornerRadius = CornerRadius(size.width * 0.04f)
            )
            // Корпус
            drawRoundRect(
                color = body,
                topLeft = Offset(size.width * 0.18f, size.height * 0.32f),
                size = Size(size.width * 0.64f, size.height * 0.4f),
                cornerRadius = CornerRadius(size.width * 0.08f)
            )
            // Объектив
            drawCircle(Color(0xFFD9D9DE), size.width * 0.13f, center = Offset(size.width * 0.5f, size.height * 0.52f))
            drawCircle(Color(0xFF2C2C2E), size.width * 0.085f, center = Offset(size.width * 0.5f, size.height * 0.52f))
        }
    }
}

// ---------------------------------------------------------------
// Настройки: серый фон и тёмная шестерёнка
// ---------------------------------------------------------------
@Composable
fun SettingsGearIcon() {
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFFDBDCE1), Color(0xFF9EA1A8)))
        )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val c = center
            val gear = Color(0xFF4A4D55)
            // Зубья шестерёнки
            for (i in 0 until 8) {
                rotate(degrees = i * 45f, pivot = c) {
                    drawRoundRect(
                        color = gear,
                        topLeft = Offset(c.x - size.width * 0.06f, size.height * 0.16f),
                        size = Size(size.width * 0.12f, size.height * 0.2f),
                        cornerRadius = CornerRadius(size.width * 0.03f)
                    )
                }
            }
            // Тело и отверстие
            drawCircle(gear, size.width * 0.25f, c)
            drawCircle(Color(0xFFC7C9CF), size.width * 0.11f, c)
        }
    }
}

// ---------------------------------------------------------------
// Сообщения: зелёный градиент и белый пузырь с хвостиком
// ---------------------------------------------------------------
@Composable
fun MessagesIcon() {
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF6BE07A), Color(0xFF15BD31)))
        )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            // Пузырь
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(size.width * 0.17f, size.height * 0.22f),
                size = Size(size.width * 0.66f, size.height * 0.46f),
                cornerRadius = CornerRadius(size.width * 0.21f)
            )
            // Хвостик слева снизу
            val tail = Path().apply {
                moveTo(size.width * 0.34f, size.height * 0.62f)
                lineTo(size.width * 0.24f, size.height * 0.8f)
                lineTo(size.width * 0.46f, size.height * 0.66f)
                close()
            }
            drawPath(tail, Color.White)
        }
    }
}

// ---------------------------------------------------------------
// Акции: чёрный фон и белый график
// ---------------------------------------------------------------
@Composable
fun StocksIcon() {
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Canvas(Modifier.fillMaxSize()) {
            val points = listOf(
                Offset(0.16f, 0.66f), Offset(0.34f, 0.5f), Offset(0.48f, 0.58f),
                Offset(0.66f, 0.34f), Offset(0.84f, 0.42f)
            ).map { Offset(size.width * it.x, size.height * it.y) }
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(
                path,
                Color.White,
                style = Stroke(
                    width = size.minDimension * 0.06f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}
