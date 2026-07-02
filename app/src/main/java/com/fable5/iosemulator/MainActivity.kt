package com.fable5.iosemulator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fable5.iosemulator.ui.EmulatorRoot
import com.fable5.iosemulator.viewmodel.EmulatorViewModel

/**
 * Единственная Activity приложения.
 * Прячет системные бары Android, чтобы эмулятор занимал весь экран
 * и выглядел как настоящий iPhone (статус-бар и Home Indicator
 * рисуются самим эмулятором).
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Полноэкранный immersive-режим: системные бары возвращаются свайпом
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            val emulatorViewModel: EmulatorViewModel = viewModel()
            EmulatorRoot(emulatorViewModel)
        }
    }
}
