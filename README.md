# Fable 5 — iOS Emulator

Android-приложение на **Kotlin + Jetpack Compose**, которое визуально и функционально имитирует интерфейс iOS 18/19: домашний экран, Dynamic Island, App Switcher и набор mock-приложений. Реальной эмуляции iOS нет — только premium-имитация интерфейса.

- **Пакет:** `com.fable5.iosemulator`
- **Мин. версия Android:** 8.0 (API 26), target — API 35
- **Стек:** Kotlin 2.1, Jetpack Compose (BOM 2025.06), Material 3, MVVM, DataStore

## Возможности

| Функция | Реализация |
|---|---|
| Домашний экран | Страницы иконок (HorizontalPager), виджеты часов и погоды, док, точки страниц |
| Иконки | Все 20 приложений нарисованы вручную на Canvas в стиле оригинальных иконок iOS |
| Drag & Drop | Долгое нажатие поднимает иконку; перенос переставляет её, сброс на другую иконку создаёт папку |
| Персистентность | Тема, обои, тумблеры и раскладка домашнего экрана сохраняются в DataStore |
| Dynamic Island | Тап разворачивает в mock-плеер с анимированной волной и play/pause |
| Статус-бар | Живое время, сигнал, Wi-Fi; батарея показывает реальный заряд устройства |
| Бейджи | Счётчик на «Сообщениях» считается из непрочитанных чатов и гаснет при прочтении |
| App Switcher | Свайп вверх от Home Indicator; живые миниатюры экранов, свайп карточки вверх удаляет её |
| Safari (mock) | Стартовая страница с избранным, адресная строка, генерируемые страницы-заглушки |
| Messages (mock) | Список диалогов с индикатором непрочитанного, пузыри iMessage, автоответы |
| Photos (mock) | Сетка «фотографий», сегментированный переключатель, полноэкранный просмотр |
| Settings (mock) | Рабочие переключатели (авиарежим, Wi-Fi, Bluetooth), тёмная тема, выбор обоев |
| Темы | Светлая/тёмная (переключается в Настройках, как в iOS) |
| Обои | 7 mesh-градиентных пресетов с индивидуальной композицией «пятен» |

## Архитектура

**MVVM** с единым state-holder'ом и вынесенной в `model/` логикой:

```
app/src/main/java/com/fable5/iosemulator/
├── MainActivity.kt              # Единственная Activity: immersive-режим + setContent
├── data/
│   └── SettingsRepository.kt    # DataStore: тема, обои, тумблеры, раскладка
├── model/
│   ├── Models.kt                # AppId, IosApp, HomeItem (App/Folder), Wallpaper, AppCatalog
│   ├── HomeLayout.kt            # Логика раскладки (move/merge/папки) + кодек для DataStore
│   └── ChatModels.kt            # ChatMessage, Conversation (unread), mock-диалоги
├── viewmodel/
│   └── EmulatorViewModel.kt     # Навигация, тема, тумблеры, бейджи; восстановление из DataStore
└── ui/
    ├── EmulatorRoot.kt          # Корневой композабл: слои home → switcher → app →
    │                            # статус-бар/Island → жесты; AppScreenHost
    ├── theme/                   # Color.kt, Type.kt, Theme.kt — палитра и типографика iOS
    ├── components/              # Переиспользуемые iOS-компоненты
    │   ├── StatusBar.kt         # Статус-бар (время, сигнал, Wi-Fi, реальная батарея)
    │   ├── SystemState.kt       # Общий тикер времени (java.time) и уровень заряда
    │   ├── DynamicIsland.kt     # Интерактивный «остров» с пружинной анимацией
    │   ├── AppIcon.kt           # Иконка приложения / папки / элемент грида + бейджи
    │   ├── IosAppIcons.kt       # Все иконки iOS, нарисованные на Canvas
    │   ├── DraggableIconGrid.kt # Сетка с drag & drop и созданием папок
    │   ├── HomeIndicator.kt     # Индикатор + зона жестов (свайп домой/переключатель)
    │   ├── IosSwitch.kt         # Переключатель UISwitch
    │   └── WallpaperBackground.kt
    └── screens/                 # Экраны и mock-приложения
        ├── HomeScreen.kt        # Домашний экран: виджеты, страницы, док, папки
        ├── AppSwitcherScreen.kt # Переключатель приложений
        ├── LockScreen.kt        # Экран блокировки
        ├── ControlCenter.kt     # Пункт управления
        ├── SpotlightOverlay.kt  # Поиск
        ├── SafariScreen.kt
        ├── MessagesScreen.kt
        ├── PhotosScreen.kt
        └── SettingsScreen.kt
```

**Поток данных:** UI читает `mutableStateOf`/`SnapshotStateList` из `EmulatorViewModel` и вызывает его методы (`openApp`, `goHome`, `moveItem`, `mergeItems`, `sendMessage`, `setDarkThemeEnabled`…). Прямых мутаций состояния из UI нет — все изменения проходят через методы ViewModel, поэтому персистентность (DataStore) подключена в одном месте. Навигация — это просто состояние (`openedApp`, `switcherVisible`, `openedFolderKey`), поэтому переходы легко анимируются `AnimatedVisibility`/`AnimatedContent`.

## Тесты и CI

- Юнит-тесты: логика раскладки домашнего экрана (`HomeLayout`), кодек сериализации (`HomeLayoutCodec`), непрочитанные диалоги (`Conversation`) — `./gradlew test`.
- GitHub Actions: `lint → unit tests → assembleDebug`, готовый APK — в артефактах каждого запуска.

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
- **Свайп вниз от правого верхнего угла** — Пункт управления
- **Тап по Dynamic Island** — развернуть mock-плеер
- **Настройки → Оформление и обои** — тёмная тема и смена обоев
- **Кнопка «Назад» Android** — работает как жест «домой»
