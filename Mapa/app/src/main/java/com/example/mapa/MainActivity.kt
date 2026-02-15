package com.example.mapa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mapa.models.LoginState
import com.example.mapa.ui.componentes.OverlayCarregando
import com.example.mapa.ui.navegacao.AuthNav
import com.example.mapa.ui.navegacao.MapaNav
import com.example.mapa.ui.theme.MapaTheme
import com.example.mapa.viewmodels.AuthViewModel
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.KoinContext

class MainActivity : ComponentActivity() {
    /**
     * Analytics para rastrear eventos no Firebase
     */
    private lateinit var analytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        analytics = Firebase.analytics

        setContent {
            KoinContext {
                MapaTheme {
                    // Variáveis e estados de autenticação
                    val authViewModel: AuthViewModel = koinViewModel()
                    val loginState by authViewModel.loginState.collectAsStateWithLifecycle()
                    val usuarioState by authViewModel.uiState.collectAsStateWithLifecycle()

                    when {
                        // Exibe a tela de carregamento enquanto verifica o estado de autenticação
                        usuarioState.logado == null -> OverlayCarregando()

                        // Exibe a tela principal se o usuário estiver logado
                        usuarioState.logado == true && loginState is LoginState.Parado -> {
                            MapaNav(
                                usuarioState = usuarioState,
                                onLogout = { authViewModel.logout() },
                                onEditarFoto = { authViewModel.atualizarFoto(it) },
                                onEditarNome = { authViewModel.atualizarNome(it) }
                            )
                        }

                        // Caso contrário, exibe a tela de autenticação
                        else -> {
                            AuthNav(
                                authViewModel = authViewModel,
                                loginState = loginState,
                                onLoginConcluido = { authViewModel.resetState() }
                            )
                        }
                    }
                }
            }
        }
    }
}