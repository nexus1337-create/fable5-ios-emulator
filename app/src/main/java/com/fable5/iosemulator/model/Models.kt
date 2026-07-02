package com.fable5.iosemulator.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.fable5.iosemulator.ui.theme.IosBlue
import com.fable5.iosemulator.ui.theme.IosRed

/** Идентификаторы всех приложений внутри эмулятора. */
enum class AppId {
    PHONE, SAFARI, MESSAGES, PHOTOS, SETTINGS,
    CAMERA, MAIL, MUSIC, MAPS, NOTES, CALENDAR, WEATHER,
    WALLET, FACETIME, APP_STORE, CLOCK, HEALTH, CALCULATOR, STOCKS, PODCASTS
}

/**
 * Описание одного приложения: имя, иконка (Material-вектор, стилизованный
 * под SF Symbols) и градиент подложки иконки — как на домашнем экране iOS.
 */
data class IosApp(
    val id: AppId,
    val name: String,
    val icon: ImageVector,
    val iconTint: Color = Color.White,
    val gradient: List<Color>
)

/** Элемент домашнего экрана: приложение или папка. */
sealed class HomeItem {
    abstract val key: String

    data class App(val app: IosApp) : HomeItem() {
        override val key: String get() = "app_${app.id.name}"
    }

    data class Folder(
        val id: Long,
        val name: String,
        val apps: List<IosApp>
    ) : HomeItem() {
        override val key: String get() = "folder_$id"
    }
}

/**
 * Обои домашнего экрана: вертикальный базовый градиент + цветные
 * «пятна» (имитация mesh-градиентов стандартных обоев iOS 18).
 */
data class Wallpaper(
    val name: String,
    val base: List<Color>,
    val blobs: List<Color>
)

object Wallpapers {
    val all = listOf(
        Wallpaper(
            "Fable",
            base = listOf(Color(0xFF0A1442), Color(0xFF35156B), Color(0xFF6A2C91)),
            blobs = listOf(Color(0xFFFF6AC1), Color(0xFF45C4FF), Color(0xFFFF9F5A), Color(0xFF7B61FF))
        ),
        Wallpaper(
            "Закат",
            base = listOf(Color(0xFF23104F), Color(0xFF7A2E62), Color(0xFFDF6B4F)),
            blobs = listOf(Color(0xFFFF4E8E), Color(0xFFFFC371), Color(0xFFFF7A59), Color(0xFF8E4EC6))
        ),
        Wallpaper(
            "Лагуна",
            base = listOf(Color(0xFF04293F), Color(0xFF0A4D6E), Color(0xFF0E7490)),
            blobs = listOf(Color(0xFF34D3C8), Color(0xFF7DE1FF), Color(0xFF1F6FEB), Color(0xFF37F5C6))
        ),
        Wallpaper(
            "Аврора",
            base = listOf(Color(0xFF061A2B), Color(0xFF0D3242), Color(0xFF123B2F)),
            blobs = listOf(Color(0xFF3EE58F), Color(0xFF37C3FF), Color(0xFF7B61FF), Color(0xFF1FE0C4))
        ),
        Wallpaper(
            "Графит",
            base = listOf(Color(0xFF0B0C0F), Color(0xFF1B1D22), Color(0xFF2E3138)),
            blobs = listOf(Color(0xFF5A5F6A), Color(0xFF8A93A5), Color(0xFF3C414B), Color(0xFF6E7684))
        )
    )
}

/** Каталог всех приложений эмулятора. */
object AppCatalog {

    private val white = Color.White
    private val lightBg = listOf(Color(0xFFFFFFFF), Color(0xFFE9E9EE))
    private val darkBg = listOf(Color(0xFF3A3A3C), Color(0xFF111113))

    val apps: Map<AppId, IosApp> = listOf(
        IosApp(AppId.PHONE, "Телефон", Icons.Filled.Phone, white,
            listOf(Color(0xFF67E374), Color(0xFF12B72C))),
        IosApp(AppId.SAFARI, "Safari", Icons.Filled.Explore, IosBlue, lightBg),
        IosApp(AppId.MESSAGES, "Сообщения", Icons.Filled.ChatBubble, white,
            listOf(Color(0xFF6BE07A), Color(0xFF15BD31))),
        IosApp(AppId.PHOTOS, "Фото", Icons.Filled.FilterVintage, Color(0xFFE8467C), lightBg),
        IosApp(AppId.SETTINGS, "Настройки", Icons.Filled.Settings, Color(0xFF4B4E57),
            listOf(Color(0xFFD9DADF), Color(0xFF9EA1A8))),
        IosApp(AppId.CAMERA, "Камера", Icons.Filled.CameraAlt, Color(0xFF3A3A3C),
            listOf(Color(0xFFE5E5EA), Color(0xFFB9BAC0))),
        IosApp(AppId.MAIL, "Почта", Icons.Filled.Email, white,
            listOf(Color(0xFF6FC5FF), Color(0xFF1D77EF))),
        IosApp(AppId.MUSIC, "Музыка", Icons.Filled.MusicNote, white,
            listOf(Color(0xFFFC5C7D), Color(0xFFEB2749))),
        IosApp(AppId.MAPS, "Карты", Icons.Filled.NearMe, white,
            listOf(Color(0xFF6BD5FA), Color(0xFF2E9BF0))),
        IosApp(AppId.NOTES, "Заметки", Icons.Filled.StickyNote2, Color(0xFFFFB300), lightBg),
        IosApp(AppId.CALENDAR, "Календарь", Icons.Filled.CalendarMonth, IosRed, lightBg),
        IosApp(AppId.WEATHER, "Погода", Icons.Filled.WbSunny, Color(0xFFFFD60A),
            listOf(Color(0xFF54A8F5), Color(0xFF1B6DE0))),
        IosApp(AppId.WALLET, "Wallet", Icons.Filled.AccountBalanceWallet, white, darkBg),
        IosApp(AppId.FACETIME, "FaceTime", Icons.Filled.Videocam, white,
            listOf(Color(0xFF67E374), Color(0xFF12B72C))),
        IosApp(AppId.APP_STORE, "App Store", Icons.Filled.Apps, white,
            listOf(Color(0xFF41C8F5), Color(0xFF1470E1))),
        IosApp(AppId.CLOCK, "Часы", Icons.Filled.AccessTime, white, darkBg),
        IosApp(AppId.HEALTH, "Здоровье", Icons.Filled.Favorite, Color(0xFFFF2D55), lightBg),
        IosApp(AppId.CALCULATOR, "Калькулятор", Icons.Filled.Calculate, white,
            listOf(Color(0xFF48484A), Color(0xFF1C1C1E))),
        IosApp(AppId.STOCKS, "Акции", Icons.Filled.ShowChart, white, darkBg),
        IosApp(AppId.PODCASTS, "Подкасты", Icons.Filled.Podcasts, white,
            listOf(Color(0xFFC97BF2), Color(0xFF8944AB)))
    ).associateBy { it.id }

    operator fun get(id: AppId): IosApp = apps.getValue(id)
}
