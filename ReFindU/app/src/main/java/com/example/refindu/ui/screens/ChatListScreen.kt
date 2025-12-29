package com.example.refindu.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.refindu.R
import com.example.refindu.models.ChatSummary
import com.example.refindu.viewmodels.ChatListViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.koin.androidx.compose.koinViewModel

@Composable
fun ChatListScreen(
    onChatClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatListViewModel = koinViewModel(),
) {
    val chats by viewModel.chats.collectAsState()

    if (chats.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.voce_ainda_nao_tem_conversas))
        }
    } else {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(chats) { chat ->
                val partnerId = viewModel.getPartnerUid(chat.participants)
                ChatListItem(
                    chat = chat,
                    partnerUid = partnerId,
                    onClick = { onChatClick(partnerId) }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun ChatListItem(chat: ChatSummary, partnerUid: String, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        leadingContent = {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(40.dp))
        },
        headlineContent = {
            // Futuramente você pode buscar o nome do usuário no Firestore usando o partnerId
            Text(text = "Usuário (ID: ${partnerUid.take(4)}...)", fontWeight = FontWeight.Bold)
        },
        supportingContent = {
            Text(text = chat.lastMessage, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        trailingContent = {
            val date = Date(chat.lastTimestamp)
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            Text(format.format(date), style = MaterialTheme.typography.bodySmall)
        }
    )
}