package com.example.mapa.ui.screens

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
import com.example.mapa.data.remote.dto.MensagemDTO
import com.example.mapa.data.remote.dto.UsuarioDTO
import com.example.mapa.model.ChatItem
import com.example.mapa.model.ChatListUiState
import com.example.mapa.ui.components.AnimacaoLottie
import com.example.mapa.ui.components.AvatarImg
import com.example.mapa.ui.components.BarraPesquisa
import com.example.mapa.ui.components.Header
import com.example.mapa.ui.components.OverlayCarregando
import com.example.mapa.ui.theme.MapaTheme
import com.example.mapa.viewmodels.ChatListViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TelaChatList(
    onChat: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    chatListViewModel: ChatListViewModel = koinViewModel(),
) {
    // Coleta o estado do ViewModel
    val chatListUiState by chatListViewModel.uiState.collectAsStateWithLifecycle()

    TelaChatListContent(
        chatListUiState = chatListUiState,
        onChat = onChat,
        onExcluir = chatListViewModel::excluirConversas,
        modifier = modifier
    )
}

@Composable
fun TelaChatListContent(
    chatListUiState: ChatListUiState,
    onChat: (String, String) -> Unit,
    onExcluir: (Set<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    // Estado do texto barra de pesquisa
    var pesquisa by rememberSaveable { mutableStateOf("") }

    // Estado para armazenar os IDs das salas selecionadas
    var selecionados by rememberSaveable { mutableStateOf(emptySet<String>()) }
    val modoSelecao = selecionados.isNotEmpty()

    // Filtra os chats com base na pesquisa
    val chatsFiltrados = rememberSaveable(chatListUiState.chats, pesquisa) {
        if (pesquisa.isBlank()) chatListUiState.chats
        else {
            chatListUiState.chats.filter { item ->
                item.contato?.nome?.contains(pesquisa, ignoreCase = true) == true ||
                        item.chat.ultimaMsg?.texto?.contains(pesquisa, ignoreCase = true) == true
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        if (modoSelecao) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { selecionados = emptySet() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cancelar_selecao),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${selecionados.size}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = {
                    onExcluir(selecionados)
                    selecionados = emptySet()
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
                titulo = stringResource(R.string.mensagens),
                icone = R.drawable.logo,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        BarraPesquisa(
            onPesquisa = { pesquisa = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        )

        // Gerenciamento dos estados de UI
        when {
            // Estado de carregamento
            chatListUiState.carregando -> OverlayCarregando()

            // Estado de erro
            chatListUiState.erro != null -> {
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
                            chatListUiState.erro
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Estado de lista vazia (Sem nenhuma conversa na conta)
            chatsFiltrados.isEmpty() && pesquisa.isBlank() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimacaoLottie(
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
            chatsFiltrados.isEmpty() && pesquisa.isNotEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimacaoLottie(
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
                ) {
                    items(
                        items = chatsFiltrados,
                        key = { item -> item.chat.salaId }
                    ) { item ->
                        val selecionado = selecionados.contains(item.chat.salaId)

                        ConversaItem(
                            chatItem = item,
                            selecionado = selecionado,
                            onClick = {
                                if (modoSelecao) selecionados = if (selecionado) selecionados - item.chat.salaId else selecionados + item.chat.salaId
                                else if (item.contato?.uid?.isNotEmpty() == true) onChat(item.contato.uid, item.chat.salaId)
                            },
                            onLongClick = {
                                if (!modoSelecao) selecionados = selecionados + item.chat.salaId
                                else selecionados = if (selecionado) selecionados - item.chat.salaId else selecionados + item.chat.salaId
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
    selecionado: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = if (selecionado) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
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
                AvatarImg(
                    foto = chatItem.contato?.foto,
                    modifier = Modifier.size(48.dp)
                )

                if (selecionado) {
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
                    text = chatItem.contato?.nome ?: stringResource(R.string.usuario_desconhecido),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = chatItem.contato?.notaMedia.toString(),
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
                if (chatItem.chat.ultimaMsg?.autorUid == chatItem.contato?.uid) {
                    Icon(
                        imageVector = if (chatItem.chat.ultimaMsg?.lido == true) Icons.Default.DoneAll else Icons.Default.Done,
                        contentDescription = if (chatItem.chat.ultimaMsg?.lido == true) stringResource(
                            R.string.lido
                        ) else stringResource(R.string.enviado),
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
                        chatItem.chat.ultimaMsg?.imgUrls?.isNotEmpty() == true -> stringResource(
                            R.string.foto
                        )

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
            chatListUiState = ChatListUiState(
                carregando = false,
                erro = null,
                chats = listOf(
                    ChatItem(
                        chat = ChatDTO(
                            salaId = "1",
                            ultimaMsg = MensagemDTO(texto = "Olá, tudo bem?")
                        ),
                        contato = UsuarioDTO(nome = "João", notaMedia = 4.4)
                    ),
                    ChatItem(
                        chat = ChatDTO(
                            salaId = "2",
                            ultimaMsg = MensagemDTO(texto = "Como vai?")
                        ),
                        contato = UsuarioDTO(nome = "Maria", notaMedia = 5.0)
                    )
                )
            ),
            onChat = { _, _ -> },
            onExcluir = {}
        )
    }
}