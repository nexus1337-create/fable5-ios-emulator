# Fable 5 — iOS Emulator

Android-приложение на **Kotlin + Jetpack Compose**, которое визуально и функционально имитирует интерфейс iOS 18/19: домашний экран, Dynamic Island, App Switcher и набор mock-приложений. Реальной эмуляции iOS нет — только premium-имитация интерфейса.

- **Пакет:** `com.fable5.iosemulator`
- **Мин. версия Android:** 8.0 (API 26), target — API 34
- **Стек:** Kotlin 2.0, Jetpack Compose (BOM 2024.06), Material 3, MVVM

## Возможности

| Функция | Реализация |
|---|---|
| Домашний экран | Страницы иконок (HorizontalPager), виджеты часов и погоды, док, точки страниц |
| Drag & Drop | Долгое нажатие поднимает иконку; перенос переставляет её, сброс на другую иконку создаёт папку |
| Dynamic Island | Тап разворачивает в mock-плеер с анимированной волной и play/pause |
| Статус-бар | Живое время, сигнал, Wi-Fi, батарея; реагирует на авиарежим из настроек |
| App Switcher | Свайп вверх от Home Indicator; живые миниатюры экранов, свайп карточки вверх удаляет её |
| Safari (mock) | Стартовая страница с избранным, адресная строка, генерируемые страницы-заглушки |
| Messages (mock) | Список диалогов, пузыри iMessage, автоответы собеседников |
| Photos (mock) | Сетка «фотографий», сегментированный переключатель, полноэкранный просмотр |
| Settings (mock) | Рабочие переключатели (авиарежим, Wi-Fi, Bluetooth), тёмная тема, выбор обоев |
| Темы | Светлая/тёмная (переключается в Настройках, как в iOS) |
| Обои | 5 градиентных пресетов, меняются в Настройках → Оформление |

## Архитектура

Простой **MVVM** с единым источником состояния:

```
app/src/main/java/com/fable5/iosemulator/
├── MainActivity.kt              # Единственная Activity: immersive-режим + setContent
├── model/
│   └── Models.kt                # AppId, IosApp, HomeItem (App/Folder), Wallpaper, AppCatalog
├── viewmodel/
│   └── EmulatorViewModel.kt     # Всё состояние: навигация, тема, обои, раскладка
│                                # домашнего экрана, недавние приложения, чаты
└── ui/
    ├── EmulatorRoot.kt          # Корневой композабл: слои home → switcher → app →
    │                            # статус-бар/Island → жесты; AppScreenHost
    ├── theme/                   # Color.kt, Type.kt, Theme.kt — палитра и типографика iOS
    ├── components/              # Переиспользуемые iOS-компоненты
    │   ├── StatusBar.kt         # Статус-бар (время, сигнал, Wi-Fi, батарея)
    │   ├── DynamicIsland.kt     # Интерактивный «остров» с пружинной анимацией
    │   ├── AppIcon.kt           # Иконка приложения / папки / элемент грида
    │   ├── DraggableIconGrid.kt # Сетка с drag & drop и созданием папок
    │   ├── HomeIndicator.kt     # Индикатор + зона жестов (свайп домой/переключатель)
    │   ├── IosSwitch.kt         # Переключатель UISwitch
    │   └── WallpaperBackground.kt
    └── screens/                 # Экраны и mock-приложения
        ├── HomeScreen.kt        # Домашний экран: виджеты, страницы, док, папки
        ├── AppSwitcherScreen.kt # Переключатель приложений
        ├── SafariScreen.kt
        ├── MessagesScreen.kt
        ├── PhotosScreen.kt
        └── SettingsScreen.kt
```

**Поток данных:** UI читает `mutableStateOf`/`SnapshotStateList` из `EmulatorViewModel` и вызывает его методы (`openApp`, `goHome`, `showSwitcher`, `moveItem`, `mergeItems`, `sendMessage`…). Навигация — это просто состояние (`openedApp`, `switcherVisible`, `openedFolderKey`), поэтому переходы легко анимируются `AnimatedVisibility`/`AnimatedContent`.

**Слои `EmulatorRoot`** (снизу вверх): домашний экран всегда отрисован и блюрится под App Switcher (`Modifier.blur`, реальный blur на Android 12+); открытое приложение появляется с масштабированием как при запуске в iOS; статус-бар и Dynamic Island лежат поверх всего; нижняя зона жестов обрабатывает короткий свайп (домой) и длинный (переключатель).

## Сборка и запуск

```bash
# Android Studio: File → Open → корень проекта, затем Run ▶
# или из консоли:
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## Управление

- **Тап по иконке** — открыть приложение
- **Долгое нажатие + перенос** — переставить иконку; сброс на другую иконку — создать папку
- **Свайп вверх снизу** — домой (короткий) / App Switcher (длинный); на домашнем экране — сразу переключатель
- **Тап по Dynamic Island** — развернуть mock-плеер
- **Настройки → Оформление и обои** — тёмная тема и смена обоев
- **Кнопка «Назад» Android** — работает как жест «домой»
