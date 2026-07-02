package com.fable5.iosemulator.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fable5.iosemulator.model.AppCatalog
import com.fable5.iosemulator.model.AppId
import com.fable5.iosemulator.model.HomeItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Единственный ViewModel эмулятора (простой MVVM):
 * хранит всё глобальное состояние — навигацию, тему, обои,
 * раскладку домашнего экрана, недавние приложения и данные mock-приложений.
 */
class EmulatorViewModel : ViewModel() {

    // ---------------------------------------------------------------
    // Тема и обои
    // ---------------------------------------------------------------
    var darkTheme by mutableStateOf(false)
    var wallpaperIndex by mutableStateOf(0)

    // ---------------------------------------------------------------
    // Системные переключатели (отражаются в статус-баре)
    // ---------------------------------------------------------------
    var airplaneMode by mutableStateOf(false)
    var wifiEnabled by mutableStateOf(true)
    var bluetoothEnabled by mutableStateOf(true)
    var cellularData by mutableStateOf(true)
    var wifiNetwork by mutableStateOf("FableNet")

    // ---------------------------------------------------------------
    // Экран блокировки и Пункт управления
    // ---------------------------------------------------------------
    var locked by mutableStateOf(true)
        private set
    var controlCenterVisible by mutableStateOf(false)
    var spotlightVisible by mutableStateOf(false)
    var brightness by mutableStateOf(0.8f)
    var volume by mutableStateOf(0.55f)
    var flashlightOn by mutableStateOf(false)
    var focusMode by mutableStateOf(false)
    var muted by mutableStateOf(false)

    fun unlock() {
        locked = false
    }

    /** Блокировка экрана (кнопка в Настройках). */
    fun lockScreen() {
        goHome()
        controlCenterVisible = false
        locked = true
    }

    // ---------------------------------------------------------------
    // Dynamic Island (mock-плеер)
    // ---------------------------------------------------------------
    var islandExpanded by mutableStateOf(false)
    var musicPlaying by mutableStateOf(true)

    // ---------------------------------------------------------------
    // Навигация: открытое приложение, App Switcher, открытая папка
    // ---------------------------------------------------------------
    var openedApp by mutableStateOf<AppId?>(null)
        private set
    var switcherVisible by mutableStateOf(false)
        private set
    var openedFolderKey by mutableStateOf<String?>(null)

    /** Точка на экране, из которой «вырастает» открываемое приложение. */
    var launchOrigin by mutableStateOf<Offset?>(null)
        private set

    /** Недавние приложения для App Switcher (первый — самый свежий). */
    val recentApps = mutableStateListOf<AppId>()

    fun openApp(id: AppId, origin: Offset? = null) {
        launchOrigin = origin
        openedApp = id
        switcherVisible = false
        openedFolderKey = null
        controlCenterVisible = false
        spotlightVisible = false
        recentApps.remove(id)
        recentApps.add(0, id)
        // Держим не больше шести карточек в переключателе
        while (recentApps.size > 6) recentApps.removeAt(recentApps.lastIndex)
    }

    fun goHome() {
        openedApp = null
        switcherVisible = false
        openedFolderKey = null
        controlCenterVisible = false
        spotlightVisible = false
    }

    fun showSwitcher() {
        openedApp = null
        openedFolderKey = null
        switcherVisible = true
    }

    fun removeFromRecents(id: AppId) {
        recentApps.remove(id)
    }

    // ---------------------------------------------------------------
    // Домашний экран: страницы с иконками и папками
    // ---------------------------------------------------------------
    private var folderIdCounter = 1L

    val pages: List<SnapshotStateList<HomeItem>> = listOf(
        mutableStateListOf(
            HomeItem.App(AppCatalog[AppId.FACETIME]),
            HomeItem.App(AppCatalog[AppId.CALENDAR]),
            HomeItem.App(AppCatalog[AppId.NOTES]),
            HomeItem.App(AppCatalog[AppId.CAMERA]),
            HomeItem.App(AppCatalog[AppId.MAIL]),
            HomeItem.App(AppCatalog[AppId.WEATHER]),
            HomeItem.App(AppCatalog[AppId.CLOCK]),
            HomeItem.App(AppCatalog[AppId.MAPS]),
            HomeItem.App(AppCatalog[AppId.WALLET]),
            HomeItem.App(AppCatalog[AppId.HEALTH]),
            HomeItem.App(AppCatalog[AppId.MUSIC]),
            HomeItem.App(AppCatalog[AppId.SETTINGS])
        ),
        mutableStateListOf(
            HomeItem.App(AppCatalog[AppId.APP_STORE]),
            HomeItem.App(AppCatalog[AppId.PODCASTS]),
            HomeItem.Folder(
                id = 0L,
                name = "Утилиты",
                apps = listOf(AppCatalog[AppId.CALCULATOR], AppCatalog[AppId.STOCKS])
            )
        )
    )

    /** Приложения в доке (нижняя панель). */
    val dockApps = listOf(AppId.PHONE, AppId.SAFARI, AppId.MESSAGES, AppId.PHOTOS)
        .map { AppCatalog[it] }

    /** Перемещение иконки внутри страницы (drag & drop). */
    fun moveItem(page: Int, from: Int, to: Int) {
        val list = pages[page]
        if (from !in list.indices || from == to) return
        val item = list.removeAt(from)
        list.add(to.coerceIn(0, list.size), item)
    }

    /**
     * Объединение элементов: перетаскивание приложения на другое приложение
     * создаёт папку; перетаскивание на папку — добавляет приложение в неё.
     */
    fun mergeItems(page: Int, from: Int, to: Int) {
        val list = pages[page]
        if (from !in list.indices || to !in list.indices || from == to) return
        val source = list[from]
        if (source !is HomeItem.App) return // папки в папки не вкладываем
        when (val target = list[to]) {
            is HomeItem.App -> list[to] = HomeItem.Folder(
                id = folderIdCounter++,
                name = "Папка",
                apps = listOf(target.app, source.app)
            )
            is HomeItem.Folder -> list[to] = target.copy(apps = target.apps + source.app)
        }
        list.removeAt(from)
    }

    /** Поиск открытой папки по ключу (для оверлея папки). */
    fun findFolder(key: String): HomeItem.Folder? =
        pages.asSequence()
            .flatMap { it.asSequence() }
            .filterIsInstance<HomeItem.Folder>()
            .firstOrNull { it.key == key }

    // ---------------------------------------------------------------
    // Mock-данные приложения «Сообщения»
    // ---------------------------------------------------------------
    data class ChatMessage(val text: String, val fromMe: Boolean, val time: String)

    class Conversation(
        val id: Int,
        val name: String,
        val colorIndex: Int,
        initial: List<ChatMessage>,
        val cannedReplies: List<String>
    ) {
        val messages = mutableStateListOf<ChatMessage>().apply { addAll(initial) }
        var replyCursor = 0
    }

    val conversations = listOf(
        Conversation(
            id = 0, name = "Аня", colorIndex = 0,
            initial = listOf(
                ChatMessage("Привет! Видел новый эмулятор iOS? 😍", false, "9:41"),
                ChatMessage("Ага, Fable 5? Выглядит прямо как настоящий iPhone", true, "9:42"),
                ChatMessage("Dynamic Island вообще огонь 🔥", false, "9:43")
            ),
            cannedReplies = listOf(
                "Ахах, точно! 😄",
                "Кстати, обои там тоже можно менять",
                "Напиши мне ещё, я проверяю авто-ответы 🙂"
            )
        ),
        Conversation(
            id = 1, name = "Команда Fable", colorIndex = 1,
            initial = listOf(
                ChatMessage("Релиз 1.0 готов к демо 🚀", false, "8:15"),
                ChatMessage("App Switcher работает свайпом вверх", false, "8:16"),
                ChatMessage("Отлично, показываю заказчику", true, "8:30")
            ),
            cannedReplies = listOf(
                "Принято! Фиксируем в задачах ✅",
                "Не забудь про тёмную тему в настройках",
                "Скоро добавим ещё виджеты"
            )
        ),
        Conversation(
            id = 2, name = "Мама", colorIndex = 2,
            initial = listOf(
                ChatMessage("Ты покушал?", false, "Вчера"),
                ChatMessage("Да, мам 🙂", true, "Вчера")
            ),
            cannedReplies = listOf("Молодец! ❤️", "Позвони бабушке", "Хорошего дня!")
        ),
        Conversation(
            id = 3, name = "Дмитрий", colorIndex = 3,
            initial = listOf(
                ChatMessage("Скинь код Dynamic Island, пожалуйста", false, "Пн"),
                ChatMessage("Уже в репозитории, смотри components/", true, "Пн")
            ),
            cannedReplies = listOf("Спасибо, нашёл 👍", "Красиво сделано", "Анимации плавные, респект")
        )
    )

    /** Отправка сообщения + имитация ответа собеседника с задержкой. */
    fun sendMessage(conversationId: Int, text: String) {
        val convo = conversations.firstOrNull { it.id == conversationId } ?: return
        convo.messages.add(ChatMessage(text, fromMe = true, time = timeNow()))
        viewModelScope.launch {
            delay(1500)
            val reply = convo.cannedReplies[convo.replyCursor % convo.cannedReplies.size]
            convo.replyCursor++
            convo.messages.add(ChatMessage(reply, fromMe = false, time = timeNow()))
        }
    }

    private fun timeNow(): String =
        SimpleDateFormat("H:mm", Locale.getDefault()).format(Date())
}
