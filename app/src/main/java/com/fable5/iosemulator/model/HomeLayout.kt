package com.fable5.iosemulator.model

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * Раскладка домашнего экрана: страницы с иконками/папками
 * и операции над ними (перестановка, объединение в папки).
 * Чистая логика без Android-зависимостей — покрыта юнит-тестами.
 */
class HomeLayout(initialPages: List<List<HomeItem>>) {

    val pages: List<SnapshotStateList<HomeItem>> =
        initialPages.map { page -> mutableStateListOf<HomeItem>().apply { addAll(page) } }

    private var folderIdCounter: Long =
        (pages.flatten().filterIsInstance<HomeItem.Folder>().maxOfOrNull { it.id } ?: -1L) + 1L

    /** Перемещение иконки внутри страницы (drag & drop). */
    fun moveItem(page: Int, from: Int, to: Int) {
        val list = pages.getOrNull(page) ?: return
        if (from !in list.indices || from == to) return
        val item = list.removeAt(from)
        list.add(to.coerceIn(0, list.size), item)
    }

    /**
     * Объединение элементов: перетаскивание приложения на другое приложение
     * создаёт папку; перетаскивание на папку — добавляет приложение в неё.
     */
    fun mergeItems(page: Int, from: Int, to: Int) {
        val list = pages.getOrNull(page) ?: return
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

    /** Поиск папки по ключу (для оверлея открытой папки). */
    fun findFolder(key: String): HomeItem.Folder? =
        pages.asSequence()
            .flatMap { it.asSequence() }
            .filterIsInstance<HomeItem.Folder>()
            .firstOrNull { it.key == key }

    companion object {
        /** Раскладка по умолчанию (первый запуск). */
        fun default(): HomeLayout = HomeLayout(
            listOf(
                listOf(
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
                listOf(
                    HomeItem.App(AppCatalog[AppId.APP_STORE]),
                    HomeItem.App(AppCatalog[AppId.PODCASTS]),
                    HomeItem.Folder(
                        id = 0L,
                        name = "Утилиты",
                        apps = listOf(AppCatalog[AppId.CALCULATOR], AppCatalog[AppId.STOCKS])
                    )
                )
            )
        )
    }
}

/**
 * Текстовый кодек раскладки для DataStore.
 * Формат: страницы через ';', элементы через ',',
 * приложение — имя [AppId], папка — `F:id:имя:APP1|APP2`.
 * Имя папки экранируется, т.к. пользовательских имён пока нет,
 * но ':' в имени сломал бы формат.
 */
object HomeLayoutCodec {

    fun encode(pages: List<List<HomeItem>>): String =
        pages.joinToString(";") { page ->
            page.joinToString(",") { item ->
                when (item) {
                    is HomeItem.App -> item.app.id.name
                    is HomeItem.Folder ->
                        "F:${item.id}:${escape(item.name)}:" +
                            item.apps.joinToString("|") { it.id.name }
                }
            }
        }

    /** Возвращает null, если строка повреждена — тогда берётся раскладка по умолчанию. */
    fun decode(encoded: String): List<List<HomeItem>>? {
        if (encoded.isBlank()) return null
        return try {
            encoded.split(";").map { page ->
                if (page.isEmpty()) emptyList()
                else page.split(",").map { token -> decodeItem(token) ?: return null }
            }
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun decodeItem(token: String): HomeItem? {
        if (!token.startsWith("F:")) {
            return HomeItem.App(AppCatalog[AppId.valueOf(token)])
        }
        val parts = token.split(":", limit = 4)
        if (parts.size != 4) return null
        val id = parts[1].toLongOrNull() ?: return null
        val apps = parts[3].split("|").filter { it.isNotEmpty() }
            .map { AppCatalog[AppId.valueOf(it)] }
        if (apps.isEmpty()) return null
        return HomeItem.Folder(id = id, name = unescape(parts[2]), apps = apps)
    }

    private fun escape(name: String) = name.replace("\\", "\\\\")
        .replace(":", "\\c").replace(",", "\\k").replace(";", "\\s").replace("|", "\\p")

    private fun unescape(name: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < name.length) {
            val ch = name[i]
            if (ch == '\\' && i + 1 < name.length) {
                sb.append(
                    when (name[i + 1]) {
                        'c' -> ':'; 'k' -> ','; 's' -> ';'; 'p' -> '|'; else -> name[i + 1]
                    }
                )
                i += 2
            } else {
                sb.append(ch)
                i++
            }
        }
        return sb.toString()
    }
}
