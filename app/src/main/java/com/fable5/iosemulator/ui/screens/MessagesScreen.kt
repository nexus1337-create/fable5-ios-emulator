package com.fable5.iosemulator.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fable5.iosemulator.ui.components.StatusBarHeight
import com.fable5.iosemulator.ui.theme.IosBlue
import com.fable5.iosemulator.ui.theme.IosBubbleGrayDark
import com.fable5.iosemulator.ui.theme.IosBubbleGrayLight
import com.fable5.iosemulator.viewmodel.EmulatorViewModel

/** Градиенты аватаров собеседников. */
private val avatarGradients = listOf(
    listOf(Color(0xFFFF9A9E), Color(0xFFF6416C)),
    listOf(Color(0xFF667EEA), Color(0xFF764BA2)),
    listOf(Color(0xFF43E97B), Color(0xFF38A169)),
    listOf(Color(0xFF4FACFE), Color(0xFF00C6FB))
)

/**
 * Mock Messages: список диалогов и чат с пузырями iMessage.
 * Отправленное сообщение получает автоматический ответ (имитация).
 */
@Composable
fun MessagesScreen(vm: EmulatorViewModel) {
    var selectedId by remember { mutableStateOf<Int?>(null) }

    AnimatedContent(
        targetState = selectedId,
        transitionSpec = {
            if (targetState != null) {
                // Вглубь: чат въезжает справа
                (slideInHorizontally { it } + fadeIn()) togetherWith
                    (slideOutHorizontally { -it / 3 } + fadeOut())
            } else {
                // Назад: список возвращается слева
                (slideInHorizontally { -it / 3 } + fadeIn()) togetherWith
                    (slideOutHorizontally { it } + fadeOut())
            }
        },
        label = "messagesNav"
    ) { conversationId ->
        if (conversationId == null) {
            ConversationList(vm) { selectedId = it }
        } else {
            val conversation = vm.conversations.first { it.id == conversationId }
            ChatScreen(vm, conversation) { selectedId = null }
        }
    }
}

/** Список диалогов с крупным заголовком и поиском. */
@Composable
private fun ConversationList(vm: EmulatorViewModel, onOpen: (Int) -> Unit) {
    val dark = vm.darkTheme
    val background = if (dark) Color.Black else Color.White
    val textColor = if (dark) Color.White else Color.Black

    Column(
        Modifier
            .fillMaxSize()
            .background(background)
    ) {
        Spacer(Modifier.height(StatusBarHeight))
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Сообщения", color = textColor, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Filled.Edit,
                contentDescription = "Новое сообщение",
                tint = IosBlue,
                modifier = Modifier.size(24.dp)
            )
        }
        // Поле поиска
        Row(
            Modifier
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (dark) Color(0xFF1C1C1E) else Color(0xFFE9E9EB))
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Search,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.45f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text("Поиск", color = textColor.copy(alpha = 0.45f), fontSize = 16.sp)
        }

        LazyColumn(Modifier.fillMaxSize()) {
            items(vm.conversations, key = { it.id }) { conversation ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onOpen(conversation.id) }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Аватар с первой буквой имени
                    Box(
                        Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    avatarGradients[conversation.colorIndex % avatarGradients.size]
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            conversation.name.take(1),
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            conversation.name,
                            color = textColor,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        val last = conversation.messages.lastOrNull()
                        Text(
                            last?.text ?: "",
                            color = textColor.copy(alpha = 0.55f),
                            fontSize = 15.sp,
                            maxLines = 1
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            conversation.messages.lastOrNull()?.time ?: "",
                            color = textColor.copy(alpha = 0.45f),
                            fontSize = 14.sp
                        )
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = textColor.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                // Разделитель с отступом, как в iOS
                Box(
                    Modifier
                        .padding(start = 84.dp)
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(textColor.copy(alpha = 0.15f))
                )
            }
        }
    }
}

/** Экран одного чата с пузырями и полем ввода iMessage. */
@Composable
private fun ChatScreen(
    vm: EmulatorViewModel,
    conversation: EmulatorViewModel.Conversation,
    onBack: () -> Unit
) {
    val dark = vm.darkTheme
    val background = if (dark) Color.Black else Color.White
    val textColor = if (dark) Color.White else Color.Black
    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Автопрокрутка к последнему сообщению
    LaunchedEffect(conversation.messages.size) {
        if (conversation.messages.isNotEmpty()) {
            listState.animateScrollToItem(conversation.messages.size - 1)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(background)
    ) {
        Spacer(Modifier.height(StatusBarHeight))

        // ---------- Шапка чата ----------
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.ArrowBackIosNew,
                contentDescription = "Назад",
                tint = IosBlue,
                modifier = Modifier
                    .size(22.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onBack() }
            )
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                avatarGradients[conversation.colorIndex % avatarGradients.size]
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        conversation.name.take(1),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(conversation.name, color = textColor, fontSize = 12.sp)
            }
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.width(22.dp))
        }

        // ---------- Сообщения ----------
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
        ) {
            items(conversation.messages) { message ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = if (message.fromMe) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        Modifier
                            .widthIn(max = 280.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 18.dp,
                                    topEnd = 18.dp,
                                    bottomStart = if (message.fromMe) 18.dp else 5.dp,
                                    bottomEnd = if (message.fromMe) 5.dp else 18.dp
                                )
                            )
                            .background(
                                if (message.fromMe) IosBlue
                                else if (dark) IosBubbleGrayDark else IosBubbleGrayLight
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            message.text,
                            color = if (message.fromMe) Color.White else textColor,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // ---------- Поле ввода ----------
        Row(
            Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .padding(bottom = 26.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (dark) Color(0xFF2C2C2E) else Color(0xFFE9E9EB)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = null,
                    tint = textColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Row(
                Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (dark) Color(0xFF1C1C1E) else Color.White)
                    .background(textColor.copy(alpha = 0.06f))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    singleLine = true,
                    textStyle = TextStyle(color = textColor, fontSize = 16.sp),
                    cursorBrush = SolidColor(IosBlue),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        Box {
                            if (draft.isEmpty()) {
                                Text(
                                    "iMessage",
                                    color = textColor.copy(alpha = 0.4f),
                                    fontSize = 16.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                if (draft.isEmpty()) {
                    Icon(
                        Icons.Filled.Mic,
                        contentDescription = null,
                        tint = textColor.copy(alpha = 0.45f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            // Кнопка отправки появляется, когда есть текст
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (draft.isBlank()) IosBlue.copy(alpha = 0.35f) else IosBlue)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = draft.isNotBlank()
                    ) {
                        vm.sendMessage(conversation.id, draft.trim())
                        draft = ""
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.ArrowUpward,
                    contentDescription = "Отправить",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
