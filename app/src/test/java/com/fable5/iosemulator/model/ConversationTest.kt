package com.fable5.iosemulator.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ConversationTest {

    @Test
    fun `markRead сбрасывает непрочитанные`() {
        val convo = Conversation(
            id = 0, name = "Тест", colorIndex = 0,
            initial = emptyList(), cannedReplies = listOf("ок"),
            initialUnread = 3
        )
        convo.markRead()
        assertEquals(0, convo.unread)
    }

    @Test
    fun `markIncoming увеличивает счётчик`() {
        val convo = Conversation(
            id = 0, name = "Тест", colorIndex = 0,
            initial = emptyList(), cannedReplies = listOf("ок")
        )
        convo.markIncoming()
        convo.markIncoming()
        assertEquals(2, convo.unread)
    }

    @Test
    fun `суммарный бейдж Сообщений считается по всем диалогам`() {
        val conversations = MockChats.conversations()
        assertEquals(2, conversations.sumOf { it.unread })
    }
}
