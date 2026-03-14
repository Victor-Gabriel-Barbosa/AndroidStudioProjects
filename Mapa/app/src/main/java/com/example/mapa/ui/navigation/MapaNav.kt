package com.example.mapa.ui.navigation

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
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
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
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.example.mapa.data.remote.dto.UserDTO
import com.example.mapa.model.UserUiState
import com.example.mapa.ui.components.AvatarImg
import com.example.mapa.ui.screen.ChatScreen
import com.example.mapa.ui.screen.ChatListScreen
import com.example.mapa.ui.screen.HomeScreen
import com.example.mapa.ui.screen.ProfileScreen
import com.example.mapa.ui.screen.SavedScreen
import com.example.mapa.viewmodels.ChatListViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Composable principal que configura a estrutura de navegação do aplicativo.
 *
 * @param userUiState O objeto [UserDTO] do usuário atualmente logado. Nulo se não houver usuário logado.
 */
@Composable
fun MapaNav(
    userUiState: UserUiState,
    chatListViewModel: ChatListViewModel = koinViewModel(),
) {
    val context = LocalContext.current

    // Estado da navegação principal
    val backStack = rememberSaveable { mutableStateListOf<Routes>(Routes.Home) }
    val destination = backStack.lastOrNull()

    // Obtém dados enviados para a Activity através do Intent
    val activity = context as? ComponentActivity
    val intent = activity?.intent
    val contactUidIntent = intent?.getStringExtra("contactUid")
    val locationIdIntent = intent?.getStringExtra("locationId")

    LaunchedEffect(contactUidIntent, locationIdIntent) {
        if (contactUidIntent != null && locationIdIntent != null) {
            backStack.add(Routes.Chat(uid = contactUidIntent, localId = locationIdIntent))
            activity.intent = Intent()
        }
    }

    // Conta a quantidade de mensagens não lidas
    val unreadCount by chatListViewModel.unreadCount.collectAsStateWithLifecycle()

    NavigationSuiteScaffold(
        layoutType = if (destination is Routes.Chat) NavigationSuiteType.None else NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo()),
        navigationSuiteItems = {
            AppRotas.entries.forEach { item ->
                // Verifica se o item está selecionado
                val selected = when (item.route) {
                    Routes.ChatRoutes -> destination == Routes.ChatRoutes || destination == Routes.ChatList || destination is Routes.Chat
                    else -> destination == item.route
                }

                item(
                    selected = selected,
                    onClick = {
                        if (destination != item.route) {
                            if (item.route == Routes.Home) {
                                backStack.clear()
                                backStack.add(Routes.Home)
                            } else {
                                if (backStack.last() != item.route) {
                                    backStack.removeAll { it == item.route }
                                    backStack.add(item.route)
                                }
                            }
                        }
                    },
                    icon = {
                        when (item) {
                            AppRotas.PROFILE if userUiState.user?.photo != null -> {
                                AvatarImg(
                                    photoUrl = userUiState.user.photo,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .border(
                                            width = if (selected) 2.dp else 0.dp,
                                            color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = CircleShape
                                        )
                                )
                            }

                            AppRotas.CHAT if unreadCount > 0 -> {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                                            Text("$unreadCount")
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (selected) item.iconFill else item.icon,
                                        contentDescription = stringResource(item.label)
                                    )
                                }
                            }

                            else -> {
                                Icon(
                                    imageVector = if (selected) item.iconFill else item.icon,
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
                onBack = { backStack.removeLastOrNull() },
                modifier = Modifier.padding(innerPadding)
            ) { rota ->
                when (rota) {
                    Routes.Home -> NavEntry(rota) {
                        HomeScreen(
                            user = userUiState.user,
                            onChat = { uid, localId -> backStack.add(Routes.Chat(uid, localId)) }
                        )
                    }

                    Routes.Saved -> NavEntry(rota) {
                        SavedScreen()
                    }

                    Routes.ChatRoutes, Routes.ChatList -> NavEntry(rota) {
                        ChatListScreen(
                            onChat = { uid, localId -> backStack.add(Routes.Chat(uid, localId)) },
                        )
                    }

                    is Routes.Chat -> NavEntry(rota) {
                        ChatScreen(
                            uid = rota.uid,
                            locationId = rota.localId,
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }

                    Routes.Profile -> NavEntry(rota) {
                        ProfileScreen()
                    }

                    else -> error("Rota não encontrada: $rota")
                }
            }
        }
    }
}