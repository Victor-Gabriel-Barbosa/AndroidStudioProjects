package com.example.mapa.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mapa.R
import com.example.mapa.data.remote.dto.Chat
import com.example.mapa.models.ChatItem
import com.example.mapa.models.ChatListState
import com.example.mapa.models.Mensagem
import com.example.mapa.data.remote.dto.Usuario
import com.example.mapa.ui.componentes.Animacao
import com.example.mapa.ui.componentes.AvatarImg
import com.example.mapa.ui.componentes.Header
import com.example.mapa.ui.componentes.OverlayCarregando
import com.example.mapa.ui.theme.MapaTheme
import com.example.mapa.viewmodels.ChatListViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TelaChatList(
    onConversa: (String) -> Unit,
    modifier: Modifier = Modifier,
    chatListViewModel: ChatListViewModel = koinViewModel(),
) {
    // Coleta o estado do ViewModel
    val uiState by chatListViewModel.uiState.collectAsStateWithLifecycle()

    TelaChatListContent(
        chatState = uiState,
        onConversa = onConversa,
        onExcluir = chatListViewModel::excluirConversa,
        modifier = modifier
    )
}

@Composable
fun TelaChatListContent(
    chatState: ChatListState,
    onConversa: (String) -> Unit,
    onExcluir: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Estado do texto barra de pesquisa
    var textoPesquisa by rememberSaveable { mutableStateOf("") }

    // Filtra os chats com base na pesquisa
    val chatsFiltrados = rememberSaveable(chatState.chats, textoPesquisa) {
        if (textoPesquisa.isBlank()) chatState.chats
        else {
            chatState.chats.filter { item ->
                item.contato?.nome?.contains(textoPesquisa, ignoreCase = true) == true ||
                        item.chat.ultimaMsg?.texto?.contains(textoPesquisa, ignoreCase = true) == true
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Header(
            titulo = stringResource(R.string.mensagens),
            icone = R.drawable.logo,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Barra de pesquisa
        if (!chatState.carregando && chatState.erro == null) {
            OutlinedTextField(
                value = textoPesquisa,
                onValueChange = { textoPesquisa = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.pesquisar_conversa)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.pesquisar)
                    )
                },
                trailingIcon = {
                    if (textoPesquisa.isNotEmpty()) {
                        IconButton(onClick = { textoPesquisa = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.limpar_pesquisa)
                            )
                        }
                    }
                },
                shape = MaterialTheme.shapes.extraLarge,
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        // Gerenciamento dos estados de UI
        when {
            // Estado de carregamento
            chatState.carregando -> OverlayCarregando()

            // Estado de erro
            chatState.erro != null -> {
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
                            chatState.erro
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Estado de lista vazia (Sem nenhuma conversa na conta)
            chatsFiltrados.isEmpty() && textoPesquisa.isBlank() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Animacao(
                        animacao = R.raw.globo_animacao,
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
            chatsFiltrados.isEmpty() && textoPesquisa.isNotEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Animacao(
                        animacao = R.raw.globo_animacao,
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
                        .padding(horizontal = 16.dp)
                ) {
                    itemsIndexed(
                        items = chatsFiltrados,
                        key = { _, item -> item.chat.salaId }
                    ) { index, item ->
                        val dismissState = rememberSwipeToDismissBoxState()

                        // Reage à mudança de estado
                        LaunchedEffect(dismissState.currentValue) {
                            if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) onExcluir(item.chat.salaId)
                        }

                        val itemShape = when {
                            chatsFiltrados.size == 1 -> RoundedCornerShape(24.dp)
                            index == 0 -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                            index == chatsFiltrados.lastIndex -> RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                            else -> RectangleShape
                        }

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                val color = MaterialTheme.colorScheme.errorContainer
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(itemShape)
                                        .background(color)
                                        .padding(horizontal = 24.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Excluir",
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            },
                            content = {
                                ConversaItem(
                                    chatItem = item,
                                    onClick = {
                                        if (item.contato?.uid?.isNotEmpty() == true) onConversa(item.contato.uid)
                                    },
                                    modifier = Modifier.clip(itemShape)
                                )
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier.clickable { onClick() },
        tonalElevation = 4.dp,
        leadingContent = {
            AvatarImg(
                foto = chatItem.contato?.foto,
                modifier = Modifier.size(48.dp)
            )
        },
        headlineContent = {
            Text(
                text = chatItem.contato?.nome ?: stringResource(R.string.usuario_desconhecido),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (chatItem.chat.ultimaMsg?.autorUid == chatItem.contato?.uid) {
                    Icon(
                        imageVector = if (chatItem.chat.ultimaMsg?.lido == true) Icons.Default.DoneAll else Icons.Default.Done,
                        contentDescription = if (chatItem.chat.ultimaMsg?.lido == true) stringResource(R.string.lido) else stringResource(R.string.enviado),
                        tint = if (chatItem.chat.ultimaMsg?.lido == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                if (chatItem.chat.ultimaMsg?.imgUrls?.isNotEmpty() == true) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = stringResource(R.string.imagem),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                Text(
                    text = when {
                        chatItem.chat.ultimaMsg?.texto?.isNotBlank() == true -> chatItem.chat.ultimaMsg.texto
                        chatItem.chat.ultimaMsg?.imgUrls?.isNotEmpty() == true -> stringResource(R.string.foto)
                        else -> ""
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        trailingContent = {
            val data = Date(chatItem.chat.ultimaMsg?.timestamp ?: 0)
            val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())

            Text(
                text = fmt.format(data),
                style = MaterialTheme.typography.labelSmall
            )
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TelaChatListContentPreview() {
    MapaTheme {
        TelaChatListContent(
            chatState = ChatListState(
                carregando = false,
                erro = null,
                chats = listOf(
                    ChatItem(
                        chat = Chat(salaId = "1", ultimaMsg = Mensagem(texto = "Olá, tudo bem?")),
                        contato = Usuario(nome = "João")
                    ),
                    ChatItem(
                        chat = Chat(salaId = "2", ultimaMsg = Mensagem(texto = "Como vai?")),
                        contato = Usuario(nome = "Maria")
                    )
                )
            ),
            onConversa = {},
            onExcluir = {}
        )
    }
}