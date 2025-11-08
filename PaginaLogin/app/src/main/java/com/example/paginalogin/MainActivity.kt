package com.example.paginalogin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.paginalogin.ui.screens.LoginScreen
import com.example.paginalogin.ui.theme.PaginaLoginTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        enableEdgeToEdge()
        setContent {
            PaginaLoginTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LoginScreen(
                        auth = auth,
                        onLoginSuccess = { user ->
                            Toast.makeText(
                                this,
                                "Login realizado: ${user.email}",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        onForgotPassword = {
                            // Implementar recuperação de senha
                        },
                        onCreateAccount = {
                            // Navegar para tela de criação de conta
                        }
                    )
                }
            }
        }
    }
}