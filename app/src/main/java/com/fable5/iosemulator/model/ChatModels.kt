package com.fable5.iosemulator.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue

/** Одно сообщение в чате. */
data class ChatMessage(val text: String, val fromMe: Boolean, val time: String)

/**
 * Диалог «Сообщений»: имя собеседника, история и заготовленные
 * автоответы. Счётчик непрочитанных питает бейдж на иконке.
 */
class Conversation(
    val id: Int,
    val name: String,
    val colorIndex: Int,
    initial: List<ChatMessage>,
    val cannedReplies: List<String>,
    initialUnread: Int = 0
) {
    val messages = mutableStateListOf<ChatMessage>().apply { addAll(initial) }
    var replyCursor = 0
    var unread by mutableIntStateOf(initialUnread)
        private set

    fun markRead() {
        unread = 0
    }

    fun markIncoming() {
        unread++
    }
}

/** Стартовые mock-данные приложения «Сообщения». */
object MockChats {
    fun conversations(): List<Conversation> = listOf(
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
            ),
            initialUnread = 2
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
}
