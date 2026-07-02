package com.fable5.iosemulator.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fable5.iosemulator.data.SettingsRepository
import com.fable5.iosemulator.model.AppId
import com.fable5.iosemulator.model.AppCatalog
import com.fable5.iosemulator.model.ChatMessage
import com.fable5.iosemulator.model.Conversation
import com.fable5.iosemulator.model.HomeItem
import com.fable5.iosemulator.model.HomeLayout
import com.fable5.iosemulator.model.HomeLayoutCodec
import com.fable5.iosemulator.model.MockChats
import com.fable5.iosemulator.model.Wallpapers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Главный state-holder эмулятора: навигация, тема/обои, системные
 * тумблеры и раскладка домашнего экрана. Данные и логика раскладки
 * вынесены в model/ (HomeLayout, MockChats); тема, обои, тумблеры
 * и раскладка сохраняются в DataStore и переживают перезапуск.
 */
class EmulatorViewModel(application: Application) : AndroidViewModel(application) {

    private val settings = SettingsRepository(application)

    // ---------------------------------------------------------------
    // Тема и обои (персистентные)
    // ---------------------------------------------------------------
    var darkTheme by mutableStateOf(false)
        private set
    var wallpaperIndex by mutableStateOf(0)
        private set

    fun setDarkThemeEnabled(value: Boolean) {
        darkTheme = value
        viewModelScope.launch { settings.saveDarkTheme(value) }
    }

    fun selectWallpaper(index: Int) {
        wallpaperIndex = index.coerceIn(0, Wallpapers.all.lastIndex)
        viewModelScope.launch { settings.saveWallpaperIndex(wallpaperIndex) }
    }

    // ---------------------------------------------------------------
    // Системные переключатели (отражаются в статус-баре, персистентные)
    // ---------------------------------------------------------------
    var airplaneMode by mutableStateOf(false)
        private set
    var wifiEnabled by mutableStateOf(true)
        private set
    var bluetoothEnabled by mutableStateOf(true)
        private set
    var cellularData by mutableStateOf(true)
        private set
    var wifiNetwork by mutableStateOf("FableNet")
        private set

    fun setAirplaneModeEnabled(value: Boolean) {
        airplaneMode = value
        viewModelScope.launch { settings.saveAirplaneMode(value) }
    }

    fun setWifiState(value: Boolean) {
        wifiEnabled = value
        viewModelScope.launch { settings.saveWifiEnabled(value) }
    }

    fun setBluetoothState(value: Boolean) {
        bluetoothEnabled = value
        viewModelScope.launch { settings.saveBluetoothEnabled(value) }
    }

    fun setCellularDataEnabled(value: Boolean) {
        cellularData = value
        viewModelScope.launch { settings.saveCellularData(value) }
    }

    fun selectWifiNetwork(name: String) {
        wifiNetwork = name
        viewModelScope.launch { settings.saveWifiNetwork(name) }
    }

    // ---------------------------------------------------------------
    // Экран блокировки, Пункт управления, Spotlight
    // ---------------------------------------------------------------
    var locked by mutableStateOf(true)
        private set
    var controlCenterVisible by mutableStateOf(false)
        private set
    var spotlightVisible by mutableStateOf(false)
        private set
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
        locked = true
    }

    fun showControlCenter() {
        if (!locked) controlCenterVisible = true
    }

    fun hideControlCenter() {
        controlCenterVisible = false
    }

    fun showSpotlight() {
        spotlightVisible = true
    }

    fun hideSpotlight() {
        spotlightVisible = false
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
        private set

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
        while (recentApps.size > MAX_RECENTS) recentApps.removeAt(recentApps.lastIndex)
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

    fun openFolder(key: String) {
        openedFolderKey = key
    }

    fun closeFolder() {
        openedFolderKey = null
    }

    // ---------------------------------------------------------------
    // Домашний экран: страницы с иконками и папками (персистентные)
    // ---------------------------------------------------------------
    // mutableStateOf — чтобы подмена раскладки при восстановлении
    // из DataStore вызвала рекомпозицию домашнего экрана
    private var homeLayout by mutableStateOf(HomeLayout.default())

    val pages: List<SnapshotStateList<HomeItem>> get() = homeLayout.pages

    /** Приложения в доке (нижняя панель). */
    val dockApps = listOf(AppId.PHONE, AppId.SAFARI, AppId.MESSAGES, AppId.PHOTOS)
        .map { AppCatalog[it] }

    fun moveItem(page: Int, from: Int, to: Int) {
        homeLayout.moveItem(page, from, to)
        persistLayout()
    }

    fun mergeItems(page: Int, from: Int, to: Int) {
        homeLayout.mergeItems(page, from, to)
        persistLayout()
    }

    fun findFolder(key: String): HomeItem.Folder? = homeLayout.findFolder(key)

    private fun persistLayout() {
        val encoded = HomeLayoutCodec.encode(pages)
        viewModelScope.launch { settings.saveHomeLayout(encoded) }
    }

    // ---------------------------------------------------------------
    // «Сообщения» и бейджи непрочитанных
    // ---------------------------------------------------------------
    val conversations: List<Conversation> = MockChats.conversations()

    /** Бейдж на иконке: Сообщения — из непрочитанных чатов, Почта — mock. */
    fun badgeFor(id: AppId): Int? = when (id) {
        AppId.MESSAGES -> conversations.sumOf { it.unread }.takeIf { it > 0 }
        AppId.MAIL -> MAIL_MOCK_BADGE
        else -> null
    }

    /** Открытие чата сбрасывает его непрочитанные (гасит бейдж). */
    fun markConversationRead(conversationId: Int) {
        conversations.firstOrNull { it.id == conversationId }?.markRead()
    }

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
        LocalTime.now().format(DateTimeFormatter.ofPattern("H:mm"))

    // ---------------------------------------------------------------
    // Восстановление сохранённого состояния
    // ---------------------------------------------------------------
    init {
        viewModelScope.launch {
            val saved = settings.load()
            darkTheme = saved.darkTheme
            wallpaperIndex = saved.wallpaperIndex.coerceIn(0, Wallpapers.all.lastIndex)
            airplaneMode = saved.airplaneMode
            wifiEnabled = saved.wifiEnabled
            bluetoothEnabled = saved.bluetoothEnabled
            cellularData = saved.cellularData
            wifiNetwork = saved.wifiNetwork
            HomeLayoutCodec.decode(saved.homeLayout)?.let { homeLayout = HomeLayout(it) }
        }
    }

    private companion object {
        const val MAX_RECENTS = 6
        const val MAIL_MOCK_BADGE = 5
    }
}
