package com.example.mapa.ui.navegacao

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.example.mapa.data.remote.dto.UsuarioDTO
import com.example.mapa.model.UsuarioUiState
import com.example.mapa.ui.components.AvatarImg
import com.example.mapa.ui.telas.TelaChat
import com.example.mapa.ui.telas.TelaChatList
import com.example.mapa.ui.telas.TelaHome
import com.example.mapa.ui.telas.TelaPerfil
import com.example.mapa.ui.telas.TelaSalvos
import com.example.mapa.viewmodels.ChatListViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Composable principal que configura a estrutura de navegação do aplicativo.
 *
 * Utiliza um [NavigationSuiteScaffold] para fornecer a barra de navegação principal
 * e um [NavHost] para gerenciar as transições entre as diferentes telas (`TelaHome`, `TelaChat`, `TelaPerfil`, etc.).
 * Ele observa o back stack de navegação para destacar o item de navegação correto.
 *
 * @param usuarioUiState O objeto [UsuarioDTO] do usuário atualmente logado. Nulo se não houver usuário logado.
 */
@Composable
fun MapaNav(
    usuarioUiState: UsuarioUiState,
    chatListViewModel: ChatListViewModel = koinViewModel(),
) {
    // Estado da navegação principal
    val backStack = rememberSaveable { mutableStateListOf<Rotas>(Rotas.Home) }
    val destinoAtual = backStack.lastOrNull()

    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val intent = activity?.intent

    // Tenta extrair os dados vindos do FirebaseMessagingService
    val contatoUidIntent = intent?.getStringExtra("contatoUid")
    val localIdIntent = intent?.getStringExtra("localId")

    LaunchedEffect(contatoUidIntent, localIdIntent) {
        if (contatoUidIntent != null && localIdIntent != null) {
            backStack.add(Rotas.ChatDetalhe(uid = contatoUidIntent, localId = localIdIntent))
            activity.intent = Intent()
        }
    }

    // Back Handler global para o App
    BackHandler(enabled = backStack.size > 1) {
        backStack.removeLastOrNull()
    }

    // Conta a quantidade de mensagens não lidas
    val qtdNaoLidas by chatListViewModel.qtdNaoLidas.collectAsStateWithLifecycle()

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppRotas.entries.forEach { item ->
                // Verifica se o item está selecionado
                val selecionado = when (item.rota) {
                    Rotas.ChatRotas -> destinoAtual == Rotas.ChatRotas || destinoAtual == Rotas.ChatList || destinoAtual is Rotas.ChatDetalhe
                    else -> destinoAtual == item.rota
                }

                item(
                    selected = selecionado,
                    onClick = {
                        if (destinoAtual != item.rota) {
                            if (item.rota == Rotas.Home) {
                                backStack.clear()
                                backStack.add(Rotas.Home)
                            } else {
                                if (backStack.last() != item.rota) {
                                    backStack.removeAll { it == item.rota }
                                    backStack.add(item.rota)
                                }
                            }
                        }
                    },
                    icon = {
                        when (item) {
                            AppRotas.PERFIL if usuarioUiState.usuario?.foto != null -> {
                                AvatarImg(
                                    foto = usuarioUiState.usuario.foto,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .border(
                                            width = if (selecionado) 2.dp else 0.dp,
                                            color = if (selecionado) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = CircleShape
                                        )
                                )
                            }

                            AppRotas.CHAT if qtdNaoLidas > 0 -> {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                                            Text("$qtdNaoLidas")
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (selecionado) item.iconFill else item.icon,
                                        contentDescription = stringResource(item.label)
                                    )
                                }
                            }

                            else -> {
                                Icon(
                                    imageVector = if (selecionado) item.iconFill else item.icon,
                                    contentDescription = stringResource(item.label)
                                )
                            }
                        }
                    },
                    label = {
                        Text(
                            text = stringResource(item.label),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            NavDisplay(
                backStack = backStack,
                modifier = Modifier.padding(innerPadding)
            ) { rota ->
                when (rota) {
                    Rotas.Home -> NavEntry(rota) {
                        TelaHome(
                            usuario = usuarioUiState.usuario,
                            onChat = { uid, localId -> backStack.add(Rotas.ChatDetalhe(uid, localId)) }
                        )
                    }

                    Rotas.Salvos -> NavEntry(rota) {
                        TelaSalvos()
                    }

                    Rotas.ChatRotas, Rotas.ChatList -> NavEntry(rota) {
                        TelaChatList(
                            onChat = { uid, localId -> backStack.add(Rotas.ChatDetalhe(uid, localId)) },
                        )
                    }

                    is Rotas.ChatDetalhe -> NavEntry(rota) {
                        TelaChat(
                            uid = rota.uid,
                            localId = rota.localId,
                            onVoltar = { backStack.removeLastOrNull() }
                        )
                    }

                    Rotas.Perfil -> NavEntry(rota) {
                        TelaPerfil()
                    }

                    else -> error("Rota não encontrada: $rota")
                }
            }
        }
    }
}