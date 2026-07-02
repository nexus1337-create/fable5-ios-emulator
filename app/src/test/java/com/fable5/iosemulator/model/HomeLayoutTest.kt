package com.fable5.iosemulator.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeLayoutTest {

    private fun app(id: AppId) = HomeItem.App(AppCatalog[id])

    private fun layout() = HomeLayout(
        listOf(
            listOf(app(AppId.PHONE), app(AppId.SAFARI), app(AppId.MESSAGES)),
            listOf(app(AppId.PHOTOS))
        )
    )

    @Test
    fun `moveItem переставляет иконку внутри страницы`() {
        val l = layout()
        l.moveItem(page = 0, from = 0, to = 2)
        assertEquals(
            listOf(AppId.SAFARI, AppId.MESSAGES, AppId.PHONE),
            l.pages[0].map { (it as HomeItem.App).app.id }
        )
    }

    @Test
    fun `moveItem игнорирует некорректные индексы`() {
        val l = layout()
        l.moveItem(page = 0, from = 10, to = 0)
        l.moveItem(page = 5, from = 0, to = 1)
        assertEquals(3, l.pages[0].size)
    }

    @Test
    fun `mergeItems двух приложений создаёт папку`() {
        val l = layout()
        l.mergeItems(page = 0, from = 0, to = 1)
        val folder = l.pages[0][0] as HomeItem.Folder
        assertEquals(listOf(AppId.SAFARI, AppId.PHONE), folder.apps.map { it.id })
        assertEquals(2, l.pages[0].size)
    }

    @Test
    fun `mergeItems добавляет приложение в существующую папку`() {
        val l = layout()
        l.mergeItems(page = 0, from = 0, to = 1) // папка [SAFARI, PHONE]
        l.mergeItems(page = 0, from = 1, to = 0) // + MESSAGES
        val folder = l.pages[0][0] as HomeItem.Folder
        assertEquals(3, folder.apps.size)
        assertEquals(1, l.pages[0].size)
    }

    @Test
    fun `mergeItems не вкладывает папку в папку`() {
        val l = layout()
        l.mergeItems(page = 0, from = 0, to = 1) // папка на позиции 0
        l.mergeItems(page = 0, from = 0, to = 1) // папку на приложение — запрещено
        assertTrue(l.pages[0][0] is HomeItem.Folder)
        assertTrue(l.pages[0][1] is HomeItem.App)
    }

    @Test
    fun `новые папки получают уникальные id`() {
        val l = HomeLayout(
            listOf(
                listOf(
                    app(AppId.PHONE), app(AppId.SAFARI),
                    app(AppId.MESSAGES), app(AppId.PHOTOS)
                )
            )
        )
        l.mergeItems(page = 0, from = 0, to = 1)
        l.mergeItems(page = 0, from = 1, to = 2)
        val ids = l.pages[0].filterIsInstance<HomeItem.Folder>().map { it.id }
        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun `findFolder находит папку по ключу`() {
        val l = layout()
        l.mergeItems(page = 0, from = 0, to = 1)
        val folder = l.pages[0][0] as HomeItem.Folder
        assertNotNull(l.findFolder(folder.key))
        assertNull(l.findFolder("folder_999"))
    }
}
