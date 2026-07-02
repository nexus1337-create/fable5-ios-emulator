package com.fable5.iosemulator.model

import androidx.compose.ui.graphics.Color

/** Идентификаторы всех приложений внутри эмулятора. */
enum class AppId {
    PHONE, SAFARI, MESSAGES, PHOTOS, SETTINGS,
    CAMERA, MAIL, MUSIC, MAPS, NOTES, CALENDAR, WEATHER,
    WALLET, FACETIME, APP_STORE, CLOCK, HEALTH, CALCULATOR, STOCKS, PODCASTS
}

/**
 * Описание одного приложения. Сама иконка рисуется вручную на Canvas
 * (ui/components/IosAppIcons.kt) — по своей отрисовке на каждый AppId,
 * максимально близко к оригинальным иконкам iOS.
 */
data class IosApp(
    val id: AppId,
    val name: String
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

/** Одно мягкое цветное «пятно» mesh-градиента (доли ширины/высоты и радиус). */
data class WallpaperBlob(
    val x: Float,
    val y: Float,
    val radius: Float,
    val color: Color,
    val alpha: Float = 0.55f
)

/**
 * Обои домашнего экрана: вертикальный базовый градиент + индивидуальная
 * раскладка цветных «пятен» — имитация mesh-градиентов стандартных
 * обоев iOS 18/26, у каждого пресета своя композиция.
 */
data class Wallpaper(
    val name: String,
    val base: List<Color>,
    val blobs: List<WallpaperBlob>
)

object Wallpapers {
    val all = listOf(
        // Классические тёплые «переливы шёлка» (iOS 18 default)
        Wallpaper(
            "Шёлк",
            base = listOf(Color(0xFF3E3730), Color(0xFF8F8478), Color(0xFFD8CDC0)),
            blobs = listOf(
                WallpaperBlob(0.20f, 0.15f, 0.75f, Color(0xFFEFE0C8), 0.60f),
                WallpaperBlob(0.85f, 0.30f, 0.70f, Color(0xFF9AA6B5), 0.45f),
                WallpaperBlob(0.40f, 0.58f, 0.90f, Color(0xFFC2A98C), 0.55f),
                WallpaperBlob(0.80f, 0.85f, 0.75f, Color(0xFF60564A), 0.65f),
                WallpaperBlob(0.10f, 0.90f, 0.65f, Color(0xFFEFE0C8), 0.35f)
            )
        ),
        // Фирменные обои Fable: глубокий космос с неоновыми всполохами
        Wallpaper(
            "Fable",
            base = listOf(Color(0xFF0A1442), Color(0xFF35156B), Color(0xFF6A2C91)),
            blobs = listOf(
                WallpaperBlob(0.75f, 0.12f, 0.65f, Color(0xFFFF6AC1), 0.55f),
                WallpaperBlob(0.15f, 0.35f, 0.80f, Color(0xFF45C4FF), 0.45f),
                WallpaperBlob(0.60f, 0.62f, 0.85f, Color(0xFF7B61FF), 0.55f),
                WallpaperBlob(0.90f, 0.80f, 0.60f, Color(0xFFFF9F5A), 0.45f),
                WallpaperBlob(0.25f, 0.90f, 0.70f, Color(0xFFFF6AC1), 0.35f)
            )
        ),
        // «Закат» — тёплый градиент к горизонту, как стоковые Sunset
        Wallpaper(
            "Закат",
            base = listOf(Color(0xFF23104F), Color(0xFF7A2E62), Color(0xFFDF6B4F)),
            blobs = listOf(
                WallpaperBlob(0.50f, 0.85f, 0.95f, Color(0xFFFFC371), 0.65f),
                WallpaperBlob(0.20f, 0.65f, 0.70f, Color(0xFFFF7A59), 0.55f),
                WallpaperBlob(0.85f, 0.55f, 0.65f, Color(0xFFFF4E8E), 0.50f),
                WallpaperBlob(0.35f, 0.20f, 0.75f, Color(0xFF8E4EC6), 0.45f)
            )
        ),
        // «Лагуна» — бирюзовая вода (стоковые Beach/Lagoon)
        Wallpaper(
            "Лагуна",
            base = listOf(Color(0xFF04293F), Color(0xFF0A4D6E), Color(0xFF0E7490)),
            blobs = listOf(
                WallpaperBlob(0.30f, 0.25f, 0.80f, Color(0xFF7DE1FF), 0.45f),
                WallpaperBlob(0.80f, 0.45f, 0.75f, Color(0xFF34D3C8), 0.55f),
                WallpaperBlob(0.45f, 0.75f, 0.90f, Color(0xFF37F5C6), 0.45f),
                WallpaperBlob(0.10f, 0.60f, 0.60f, Color(0xFF1F6FEB), 0.50f)
            )
        ),
        // «Аврора» — северное сияние на тёмном небе
        Wallpaper(
            "Аврора",
            base = listOf(Color(0xFF061A2B), Color(0xFF0D3242), Color(0xFF123B2F)),
            blobs = listOf(
                WallpaperBlob(0.30f, 0.12f, 0.85f, Color(0xFF3EE58F), 0.50f),
                WallpaperBlob(0.75f, 0.30f, 0.75f, Color(0xFF37C3FF), 0.45f),
                WallpaperBlob(0.50f, 0.55f, 0.80f, Color(0xFF1FE0C4), 0.40f),
                WallpaperBlob(0.20f, 0.80f, 0.70f, Color(0xFF7B61FF), 0.40f)
            )
        ),
        // «Ирис» — сине-фиолетовый градиент, как обои iOS 26
        Wallpaper(
            "Ирис",
            base = listOf(Color(0xFF101B4D), Color(0xFF3A2D8F), Color(0xFF7A55C9)),
            blobs = listOf(
                WallpaperBlob(0.70f, 0.18f, 0.70f, Color(0xFF9C7DFF), 0.55f),
                WallpaperBlob(0.20f, 0.40f, 0.80f, Color(0xFF4A8DFF), 0.45f),
                WallpaperBlob(0.60f, 0.72f, 0.85f, Color(0xFFCE8FFF), 0.45f),
                WallpaperBlob(0.90f, 0.90f, 0.60f, Color(0xFF6BE1FF), 0.40f)
            )
        ),
        // «Графит» — сдержанный тёмный монохром
        Wallpaper(
            "Графит",
            base = listOf(Color(0xFF0B0C0F), Color(0xFF1B1D22), Color(0xFF2E3138)),
            blobs = listOf(
                WallpaperBlob(0.15f, 0.18f, 0.70f, Color(0xFF5A5F6A), 0.50f),
                WallpaperBlob(0.85f, 0.35f, 0.75f, Color(0xFF8A93A5), 0.40f),
                WallpaperBlob(0.45f, 0.65f, 0.85f, Color(0xFF3C414B), 0.55f),
                WallpaperBlob(0.80f, 0.90f, 0.70f, Color(0xFF6E7684), 0.45f)
            )
        )
    )
}

/** Каталог всех приложений эмулятора. */
object AppCatalog {

    val apps: Map<AppId, IosApp> = listOf(
        IosApp(AppId.PHONE, "Телефон"),
        IosApp(AppId.SAFARI, "Safari"),
        IosApp(AppId.MESSAGES, "Сообщения"),
        IosApp(AppId.PHOTOS, "Фото"),
        IosApp(AppId.SETTINGS, "Настройки"),
        IosApp(AppId.CAMERA, "Камера"),
        IosApp(AppId.MAIL, "Почта"),
        IosApp(AppId.MUSIC, "Музыка"),
        IosApp(AppId.MAPS, "Карты"),
        IosApp(AppId.NOTES, "Заметки"),
        IosApp(AppId.CALENDAR, "Календарь"),
        IosApp(AppId.WEATHER, "Погода"),
        IosApp(AppId.WALLET, "Wallet"),
        IosApp(AppId.FACETIME, "FaceTime"),
        IosApp(AppId.APP_STORE, "App Store"),
        IosApp(AppId.CLOCK, "Часы"),
        IosApp(AppId.HEALTH, "Здоровье"),
        IosApp(AppId.CALCULATOR, "Калькулятор"),
        IosApp(AppId.STOCKS, "Акции"),
        IosApp(AppId.PODCASTS, "Подкасты")
    ).associateBy { it.id }

    operator fun get(id: AppId): IosApp = apps.getValue(id)
}
