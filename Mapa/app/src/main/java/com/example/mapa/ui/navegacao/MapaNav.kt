package com.example.mapa.ui.navegacao

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.example.mapa.data.remote.dto.Usuario
import com.example.mapa.models.UsuarioState
import com.example.mapa.ui.componentes.AvatarImg
import com.example.mapa.ui.telas.TelaChat
import com.example.mapa.ui.telas.TelaChatList
import com.example.mapa.ui.telas.TelaHome
import com.example.mapa.ui.telas.TelaPerfil
import com.example.mapa.ui.telas.TelaSalvos
import com.example.mapa.ui.theme.MapaTheme
import com.example.mapa.viewmodels.ChatListViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Composable principal que configura a estrutura de navegação do aplicativo.
 *
 * Utiliza um [NavigationSuiteScaffold] para fornecer a barra de navegação principal
 * e um [NavHost] para gerenciar as transições entre as diferentes telas (`TelaHome`, `TelaChat`, `TelaPerfil`, etc.).
 * Ele observa o back stack de navegação para destacar o item de navegação correto.
 *
 * @param carregandoFoto Indica se a foto do perfil está sendo carregada/atualizada,
 * para exibir feedback visual na [TelaPerfil].
 * @param usuario O objeto [Usuario] do usuário atualmente logado. Nulo se não houver usuário logado.
 * @param onLogout Callback acionado quando o usuário solicita o logout.
 * @param onEditarFoto Callback acionado quando o usuário edita a foto do perfil.
 * @param onEditarNome Callback acionado quando o usuário edita o nome do perfil.
 */
@Composable
fun MapaNav(
    usuarioState: UsuarioState,
    onLogout: () -> Unit,
    onEditarFoto: (String) -> Unit,
    onEditarNome: (String) -> Unit,
    chatListViewModel: ChatListViewModel = koinViewModel(),
) {
    // Estado da navegação principal
    val backStack = rememberSaveable { mutableStateListOf<Rotas>(Rotas.Home) }
    val destinoAtual = backStack.lastOrNull()

    // Back Handler global para o App
    BackHandler(enabled = backStack.size > 1) {
        backStack.removeLastOrNull()
    }

    val qtdNaoLidas by chatListViewModel.qtdNaoLidas.collectAsStateWithLifecycle()

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppNav.entries.forEach { item ->
                // Verifica se o item está selecionado
                val selecionado = when (item.rota) {
                    Rotas.ChatGraph -> destinoAtual == Rotas.ChatGraph || destinoAtual == Rotas.ChatList || destinoAtual is Rotas.ChatDetalhe
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
                        val iconContent = @Composable {
                            Icon(
                                imageVector = if (selecionado) item.iconFill else item.icon,
                                contentDescription = stringResource(item.label),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        when {
                            item == AppNav.PERFIL && usuarioState.usuario?.foto != null -> {
                                AvatarImg(
                                    foto = usuarioState.usuario.foto,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .border(
                                            width = if (selecionado) 2.dp else 0.dp,
                                            color = if (selecionado) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = CircleShape
                                        )
                                )
                            }

                            item == AppNav.CHAT && qtdNaoLidas > 0 -> {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                                            Text("$qtdNaoLidas")
                                        }
                                    }
                                ) {
                                    iconContent()
                                }
                            }

                            else -> iconContent()
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
                            usuario = usuarioState.usuario,
                            onChat = { uid -> backStack.add(Rotas.ChatDetalhe(uid)) }
                        )
                    }

                    Rotas.Salvos -> NavEntry(rota) {
                        TelaSalvos()
                    }

                    Rotas.ChatGraph, Rotas.ChatList -> NavEntry(rota) {
                        TelaChatList(
                            onConversa = { uid -> backStack.add(Rotas.ChatDetalhe(uid)) },
                        )
                    }

                    is Rotas.ChatDetalhe -> NavEntry(rota) {
                        TelaChat(
                            uid = rota.uid,
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MapaPreview() {
    MapaTheme {
        MapaNav(
            usuarioState = UsuarioState(
                Usuario(
                    uid = "123",
                    nome = "João",
                    email = "william.henry.harrison@example-pet-store.com",
                    foto = "https://img.freepik.com/vetores-gratis/ilustracao-do-jovem-sorridente_1308-174669.jpg"
                ),
                logado = true,
                carregandoFoto = false,
                carregandoNome = false
            ),
            onLogout = {},
            onEditarFoto = {},
            onEditarNome = {}
        )
    }
}