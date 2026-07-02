package com.fable5.iosemulator.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeLayoutCodecTest {

    @Test
    fun `раскладка по умолчанию переживает encode-decode без потерь`() {
        val original = HomeLayout.default().pages.map { it.toList() }
        val decoded = HomeLayoutCodec.decode(HomeLayoutCodec.encode(original))
        assertEquals(original, decoded)
    }

    @Test
    fun `папка с приложениями кодируется и раскодируется`() {
        val pages = listOf(
            listOf(
                HomeItem.App(AppCatalog[AppId.PHONE]),
                HomeItem.Folder(
                    id = 7L,
                    name = "Разное: и,всё;такое|прочее",
                    apps = listOf(AppCatalog[AppId.NOTES], AppCatalog[AppId.STOCKS])
                )
            ),
            listOf(HomeItem.App(AppCatalog[AppId.SAFARI]))
        )
        val decoded = HomeLayoutCodec.decode(HomeLayoutCodec.encode(pages))
        assertEquals(pages, decoded)
    }

    @Test
    fun `пустая строка означает отсутствие сохранённой раскладки`() {
        assertNull(HomeLayoutCodec.decode(""))
    }

    @Test
    fun `повреждённые данные не роняют приложение`() {
        assertNull(HomeLayoutCodec.decode("NOT_AN_APP,PHONE"))
        assertNull(HomeLayoutCodec.decode("F:xx:имя:PHONE"))
        assertNull(HomeLayoutCodec.decode("F:1:имя:"))
    }
}
