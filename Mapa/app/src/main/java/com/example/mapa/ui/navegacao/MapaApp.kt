package com.example.mapa.ui.navegacao

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.mapa.models.Usuario
import com.example.mapa.ui.componentes.AvatarImg
import com.example.mapa.ui.telas.TelaChat
import com.example.mapa.ui.telas.TelaChatList
import com.example.mapa.ui.telas.TelaHome
import com.example.mapa.ui.telas.TelaPerfil
import com.example.mapa.ui.telas.TelaSalvos

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
fun MapaApp(
    carregandoFoto: Boolean,
    usuario: Usuario?,
    onLogout: () -> Unit,
    onEditarFoto: (String) -> Unit,
    onEditarNome: (String) -> Unit,
) {
    // Configura e observa a navegação para identificar o destino atual
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val destinoAtual = navBackStackEntry?.destination

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppNav.entries.forEach { item ->
                // Verifica se o item está selecionado
                val selecionado = destinoAtual?.hierarchy?.any { it.hasRoute(item.rota::class) } == true

                item(
                    selected = selecionado,
                    onClick = {
                        navController.navigate(item.rota) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        if (item == AppNav.PERFIL && usuario?.foto != null) AvatarImg(
                            foto = usuario.foto,
                            modifier = Modifier
                                .size(24.dp)
                                .border(
                                    width = if (selecionado) 1.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                        )
                        else Icon(
                            imageVector = if (selecionado) item.iconFill else item.icon,
                            contentDescription = stringResource(item.label)
                        )
                    },
                    label = { Text(stringResource(item.label)) }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Rota.Home,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable<Rota.Home> {
                    TelaHome(
                        usuario = usuario,
                        onChat = { navController.navigate(Rota.ChatDetalhe(it)) }
                    )
                }

                composable<Rota.Salvos> {
                    TelaSalvos()
                }

                navigation<Rota.ChatGraph>(startDestination = Rota.ChatList) {
                    composable<Rota.ChatList> {
                        TelaChatList(
                            onConversaClick = { navController.navigate(Rota.ChatDetalhe(it)) }
                        )
                    }

                    composable<Rota.ChatDetalhe> {
                        TelaChat(
                            onVoltar = { navController.popBackStack() }
                        )
                    }
                }

                composable<Rota.Perfil> {
                    TelaPerfil(
                        carregandoFoto = carregandoFoto,
                        usuario = usuario,
                        onLogout = onLogout,
                        onEditarFoto = onEditarFoto,
                        onEditarNome = onEditarNome
                    )
                }
            }
        }
    }
}