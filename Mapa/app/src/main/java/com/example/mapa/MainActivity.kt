package com.example.mapa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mapa.models.LoginState
import com.example.mapa.ui.componentes.OverlayCarregando
import com.example.mapa.ui.navegacao.AuthFlow
import com.example.mapa.ui.navegacao.MapaApp
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
                    val usuarioLogado by authViewModel.usuarioAtivoLogado.collectAsStateWithLifecycle()
                    val usuario by authViewModel.usuarioAtivoState.collectAsStateWithLifecycle(null)
                    val carregandoFoto by authViewModel.carregandoFoto.collectAsStateWithLifecycle()

                    when {
                        // Exibe a tela de carregamento enquanto verifica o estado de autenticação
                        usuarioLogado == null -> OverlayCarregando()

                        // Exibe a tela principal se o usuário estiver logado
                        usuarioLogado == true && loginState is LoginState.Parado -> {
                            MapaApp(
                                carregandoFoto = carregandoFoto,
                                usuario = usuario,
                                onLogout = { authViewModel.logout() },
                                onEditarFoto = { authViewModel.atualizarFoto(it) },
                                onEditarNome = { authViewModel.atualizarNome(it) }
                            )
                        }

                        // Caso contrário, exibe a tela de autenticação
                        else -> {
                            AuthFlow(
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