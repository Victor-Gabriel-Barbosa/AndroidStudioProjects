package com.example.refindu.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.refindu.models.Message
import com.example.refindu.viewmodels.ChatViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    targetUserUid: String,
    onBack: () -> Unit,
    viewModel: ChatViewModel = koinViewModel()
) {
    LaunchedEffect(targetUserUid) {
        viewModel.initializeChat(targetUserUid)
    }

    val messages by viewModel.messages.collectAsState()
    val currentUserId = viewModel.getCurrentUserUid()
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conversa") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(messages) { message ->
                    MessageBubble(message, isCurrentUser = message.senderUid == currentUserId)
                }
            }
            ChatInput(onSendMessage = { viewModel.sendMessage(it) })
        }
    }
}

@Composable
fun MessageBubble(message: Message, isCurrentUser: Boolean) {
    val bubbleColor = if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isCurrentUser) Color.White else Color.Black
    val alignment = if (isCurrentUser) Alignment.End else Alignment.Start

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(vertical = 2.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(text = message.text, color = textColor, fontSize = 16.sp)
                Row(
                    modifier = Modifier.padding(top = 4.dp).align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                    Text(text = sdf.format(Date(message.timestamp)), color = textColor.copy(0.7f), fontSize = 10.sp)
                    if (isCurrentUser) {
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Done,
                            contentDescription = null,
                            tint = if (message.isRead) Color(0xFF81D4FA) else textColor.copy(0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatInput(onSendMessage: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Row(
        modifier = Modifier.padding(8.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Mensagem...") },
            shape = RoundedCornerShape(24.dp)
        )
        IconButton(
            onClick = {
                if (text.isNotBlank()) {
                    onSendMessage(text)
                    text = ""
                }
            }
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
        }
    }
}