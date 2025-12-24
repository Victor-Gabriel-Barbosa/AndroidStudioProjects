package com.example.refindu

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.refindu.ui.screens.FavoritesScreen
import com.example.refindu.ui.screens.HomeScreen
import com.example.refindu.ui.screens.ProfileScreen
import com.example.refindu.ui.screens.SavedScreen
import com.example.refindu.ui.screens.login.SigninScreen
import com.example.refindu.ui.screens.login.SignupScreen
import com.example.refindu.ui.theme.ReFindUTheme
import com.example.refindu.viewmodels.AuthViewModel
import com.example.refindu.viewmodels.LoginState
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ReFindUTheme {
                // Injeta o ViewModel com Koin
                val authViewModel: AuthViewModel = koinViewModel()

                // Observa o estado do login
                val loginState by authViewModel.loginState.collectAsState()

                // Estado local simples para controlar a navegação inicial
                var isUserLoggedIn by remember { mutableStateOf(authViewModel.isUserSignedIn) }

                // Se o estado mudar para Success, atualiza a UI
                LaunchedEffect(loginState) {
                    if (loginState is LoginState.Success) isUserLoggedIn = true
                }

                if (isUserLoggedIn) {
                    ReFindUApp(
                        onLogout = {
                            authViewModel.logout()
                            isUserLoggedIn = false
                        }
                    )
                } else {
                    AuthFlow(
                        authViewModel = authViewModel, // Passa o ViewModel
                        state = loginState
                    )
                }
            }
        }
    }
}

@Composable
fun AuthFlow(
    authViewModel: AuthViewModel,
    state: LoginState
) {
    var showLoginScreen by remember { mutableStateOf(true) }
    val context = LocalContext.current

    // Tratamento de erros (Toast)
    LaunchedEffect(state) {
        if (state is LoginState.Error) {
            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            authViewModel.resetState()
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        if (showLoginScreen) {
            SigninScreen(
                modifier = Modifier.padding(innerPadding),
                onLoginClick = { email, password ->
                    authViewModel.login(email, password)
                },
                onGoogleLoginClick = { credential ->
                    authViewModel.loginWithGoogle(credential)
                },
                onNavigateToSignup = { showLoginScreen = false },
                state = state
            )
        } else {
            SignupScreen(
                modifier = Modifier.padding(innerPadding),
                onSignupClick = { email, password ->
                    authViewModel.register(email, password)
                },
                onNavigateToLogin = { showLoginScreen = true },
                state = state
            )
        }
    }
}

@PreviewScreenSizes
@Composable
fun ReFindUApp(onLogout: () -> Unit = {}) {
    var currentPage by rememberSaveable { mutableStateOf(MainNavItem.HOME) }

    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    val (userName, userPhoto) = if (isPreview) Pair("Usuário Visitante", null)
    else {
        val user = Firebase.auth.currentUser
        val name = user?.displayName ?: user?.email ?: stringResource(R.string.usuario)
        val photo = user?.photoUrl?.toString()
        Pair(name, photo)
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            MainNavItem.entries.forEach {
                item(
                    icon = {
                        if (it == MainNavItem.PROFILE && userPhoto != null) {
                            AsyncImage(
                                model = userPhoto,
                                contentDescription = stringResource(it.label),
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = it.icon,
                                contentDescription = stringResource(it.label)
                            )
                        }
                    },
                    label = { Text(stringResource(it.label)) },
                    selected = it == currentPage,
                    onClick = { currentPage = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            when (currentPage) {
                MainNavItem.HOME -> HomeScreen(modifier = Modifier.padding(innerPadding))
                MainNavItem.SAVES -> SavedScreen(modifier = Modifier.padding(innerPadding))
                MainNavItem.FAVORITES -> FavoritesScreen(modifier = Modifier.padding(innerPadding))
                MainNavItem.PROFILE -> {
                    ProfileScreen(
                        modifier = Modifier.padding(innerPadding),
                        userName = userName,
                        photoUrl = userPhoto,
                        onLogoutClick = onLogout
                    )
                }
            }
        }
    }
}

enum class MainNavItem(
    @field:StringRes val label: Int,
    val icon: ImageVector,
) {
    HOME(R.string.inicio, Icons.Default.Home),
    SAVES(R.string.salvos, Icons.Default.LocationOn),
    FAVORITES(R.string.favoritos, Icons.Default.Favorite),
    PROFILE(R.string.perfil, Icons.Default.AccountCircle),
}