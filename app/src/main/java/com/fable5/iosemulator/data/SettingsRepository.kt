package com.fable5.iosemulator.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.settingsStore: DataStore<Preferences> by preferencesDataStore(name = "emulator_settings")

/** Снимок сохранённого состояния эмулятора. */
data class PersistedSettings(
    val darkTheme: Boolean = false,
    val wallpaperIndex: Int = 0,
    val airplaneMode: Boolean = false,
    val wifiEnabled: Boolean = true,
    val bluetoothEnabled: Boolean = true,
    val cellularData: Boolean = true,
    val wifiNetwork: String = "FableNet",
    val homeLayout: String = ""
)

/**
 * Персистентность через DataStore Preferences: тема, обои,
 * системные тумблеры и раскладка домашнего экрана переживают
 * перезапуск приложения и смерть процесса.
 */
class SettingsRepository(context: Context) {

    private val store = context.applicationContext.settingsStore

    suspend fun load(): PersistedSettings {
        val prefs = store.data.first()
        return PersistedSettings(
            darkTheme = prefs[KEY_DARK_THEME] ?: false,
            wallpaperIndex = prefs[KEY_WALLPAPER] ?: 0,
            airplaneMode = prefs[KEY_AIRPLANE] ?: false,
            wifiEnabled = prefs[KEY_WIFI] ?: true,
            bluetoothEnabled = prefs[KEY_BLUETOOTH] ?: true,
            cellularData = prefs[KEY_CELLULAR] ?: true,
            wifiNetwork = prefs[KEY_WIFI_NETWORK] ?: "FableNet",
            homeLayout = prefs[KEY_HOME_LAYOUT] ?: ""
        )
    }

    suspend fun saveDarkTheme(value: Boolean) = store.edit { it[KEY_DARK_THEME] = value }
    suspend fun saveWallpaperIndex(value: Int) = store.edit { it[KEY_WALLPAPER] = value }
    suspend fun saveAirplaneMode(value: Boolean) = store.edit { it[KEY_AIRPLANE] = value }
    suspend fun saveWifiEnabled(value: Boolean) = store.edit { it[KEY_WIFI] = value }
    suspend fun saveBluetoothEnabled(value: Boolean) = store.edit { it[KEY_BLUETOOTH] = value }
    suspend fun saveCellularData(value: Boolean) = store.edit { it[KEY_CELLULAR] = value }
    suspend fun saveWifiNetwork(value: String) = store.edit { it[KEY_WIFI_NETWORK] = value }
    suspend fun saveHomeLayout(encoded: String) = store.edit { it[KEY_HOME_LAYOUT] = encoded }

    private companion object {
        val KEY_DARK_THEME = booleanPreferencesKey("dark_theme")
        val KEY_WALLPAPER = intPreferencesKey("wallpaper_index")
        val KEY_AIRPLANE = booleanPreferencesKey("airplane_mode")
        val KEY_WIFI = booleanPreferencesKey("wifi_enabled")
        val KEY_BLUETOOTH = booleanPreferencesKey("bluetooth_enabled")
        val KEY_CELLULAR = booleanPreferencesKey("cellular_data")
        val KEY_WIFI_NETWORK = stringPreferencesKey("wifi_network")
        val KEY_HOME_LAYOUT = stringPreferencesKey("home_layout")
    }
}
