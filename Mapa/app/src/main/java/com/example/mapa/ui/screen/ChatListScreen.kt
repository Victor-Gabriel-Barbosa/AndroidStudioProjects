package com.example.mapa.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mapa.R
import com.example.mapa.data.remote.dto.ChatDTO
import com.example.mapa.data.remote.dto.MsgDTO
import com.example.mapa.data.remote.dto.UserDTO
import com.example.mapa.model.ChatItem
import com.example.mapa.model.ChatListUiState
import com.example.mapa.ui.components.LottieAnimation
import com.example.mapa.ui.components.AvatarImg
import com.example.mapa.ui.components.SearchBar
import com.example.mapa.ui.components.LoadingOverlay
import com.example.mapa.ui.components.Header
import com.example.mapa.ui.theme.MapaTheme
import com.example.mapa.viewmodels.ChatListViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatListScreen(
    onChat: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    chatListViewModel: ChatListViewModel = koinViewModel(),
) {
    // Coleta o estado do ViewModel
    val chatListUiState by chatListViewModel.uiState.collectAsStateWithLifecycle()

    ChatListScreenContent(
        chatListUiState = chatListUiState,
        onChat = onChat,
        onDelete = chatListViewModel::deleteMsg,
        modifier = modifier
    )
}

@Composable
fun ChatListScreenContent(
    chatListUiState: ChatListUiState,
    onChat: (String, String) -> Unit,
    onDelete: (Set<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    // Estado do texto barra de pesquisa
    var search by rememberSaveable { mutableStateOf("") }

    // Estado para armazenar os IDs das salas selecionadas
    var selected by rememberSaveable { mutableStateOf(emptySet<String>()) }

    // Filtra os chats com base na pesquisa
    val chats = rememberSaveable(chatListUiState.chats, search) {
        if (search.isBlank()) chatListUiState.chats
        else {
            chatListUiState.chats.filter { item ->
                item.contact?.name?.contains(search, ignoreCase = true) == true ||
                        item.chat.lastMsg?.text?.contains(search, ignoreCase = true) == true
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        if (selected.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { selected = emptySet() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cancelar_selecao),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${selected.size}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = {
                    onDelete(selected)
                    selected = emptySet()
                }) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.excluir_selecionados),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        } else {
            Header(
                title = stringResource(R.string.mensagens),
                icon = R.drawable.logo,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        SearchBar(
            onSearch = { search = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        )

        // Gerenciamento dos estados de UI
        when {
            // Estado de carregamento
            chatListUiState.loading -> LoadingOverlay()

            // Estado de erro
            chatListUiState.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = stringResource(R.string.erro),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = stringResource(
                            R.string.ocorreu_um_erro_ao_carregar_as_conversas,
                            chatListUiState.error
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Estado de lista vazia (Sem nenhuma conversa na conta)
            chats.isEmpty() && search.isBlank() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LottieAnimation(
                        animation = R.raw.globo_animacao,
                        modifier = Modifier.size(200.dp)
                    )

                    Text(
                        text = stringResource(R.string.voce_ainda_nao_tem_conversas),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Se a busca não retornou nada (mas existem conversas na conta)
            chats.isEmpty() && search.isNotEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LottieAnimation(
                        animation = R.raw.globo_animacao,
                        modifier = Modifier.size(200.dp)
                    )

                    Text(
                        text = stringResource(R.string.nenhuma_conversa_encontrada),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Estado de sucesso com dados
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    items(
                        items = chats,
                        key = { item -> item.chat.id }
                    ) { item ->
                        val selecionado = selected.contains(item.chat.id)

                        ConversaItem(
                            chatItem = item,
                            selected = selecionado,
                            onClick = {
                                if (selected.isNotEmpty()) selected = if (selecionado) selected - item.chat.id else selected + item.chat.id
                                else if (item.contact?.uid?.isNotEmpty() == true) onChat(item.contact.uid, item.chat.id)
                            },
                            onLongClick = {
                                if (selected.isEmpty()) selected = selected + item.chat.id
                                else selected = if (selecionado) selected - item.chat.id else selected + item.chat.id
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConversaItem(
    chatItem: ChatItem,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    else Color.Transparent

    ListItem(
        modifier = modifier
            .background(background)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        tonalElevation = 4.dp,
        leadingContent = {
            Box {
                BadgedBox(
                    badge = {
                        if (chatItem.chat.lastMsg?.uid == chatItem.contact?.uid && chatItem.chat.lastMsg?.read == false) {
                            Badge(containerColor = MaterialTheme.colorScheme.error)
                        }
                    }
                ) {
                    AvatarImg(
                        photoUrl = chatItem.contact?.photo,
                        modifier = Modifier.size(48.dp)
                    )
                }

                if (selected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.selecionado),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .size(20.dp)
                    )
                }
            }
        },
        headlineContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chatItem.contact?.name ?: stringResource(R.string.usuario_desconhecido),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = chatItem.contact?.averageRating.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }
        },
        supportingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (chatItem.chat.lastMsg?.uid == chatItem.contact?.uid) {
                    Icon(
                        imageVector = if (chatItem.chat.lastMsg?.read == true) Icons.Default.DoneAll else Icons.Default.Done,
                        contentDescription = if (chatItem.chat.lastMsg?.read == true) stringResource(R.string.lido) else stringResource(R.string.enviado),
                        tint = if (chatItem.chat.lastMsg?.read == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                if (chatItem.chat.lastMsg?.imgUrls?.isNotEmpty() == true) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = stringResource(R.string.imagem),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                Text(
                    text = when {
                        chatItem.chat.lastMsg?.text?.isNotBlank() == true -> chatItem.chat.lastMsg.text
                        chatItem.chat.lastMsg?.imgUrls?.isNotEmpty() == true -> stringResource(R.string.foto)
                        else -> ""
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        trailingContent = {
            val date = Date(chatItem.chat.lastMsg?.timestamp ?: 0)
            val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())

            Text(
                text = fmt.format(date),
                style = MaterialTheme.typography.labelSmall
            )
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ChatListScreenContentPreview() {
    MapaTheme {
        ChatListScreenContent(
            chatListUiState = ChatListUiState(
                loading = false,
                error = null,
                chats = listOf(
                    ChatItem(
                        chat = ChatDTO(
                            id = "1",
                            lastMsg = MsgDTO(text = "Olá, tudo bem?")
                        ),
                        contact = UserDTO(name = "João", averageRating = 4.4)
                    ),
                    ChatItem(
                        chat = ChatDTO(
                            id = "2",
                            lastMsg = MsgDTO(text = "Como vai?")
                        ),
                        contact = UserDTO(name = "Maria", averageRating = 5.0)
                    )
                )
            ),
            onChat = { _, _ -> },
            onDelete = {}
        )
    }
}