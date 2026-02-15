package com.example.aula16

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.example.aula16.ui.theme.Aula16Theme

private data class RotaHome(val usuario: String)
private data object RotaLogin
private data object RotaCadastro

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TelaNavegacao()
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TelaNavegacao() {
    val backStack = remember { mutableStateListOf<Any>(RotaLogin) }
    val onVoltar = { if (backStack.size > 1) backStack.removeLastOrNull() }

    Aula16Theme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            NavDisplay(backStack = backStack, onBack = { backStack.removeLastOrNull() }) { rota ->
                when (rota) {
                    is RotaHome -> NavEntry(rota) {
                        TelaHome(
                            usuario = rota.usuario,
                            onLogout = {
                                backStack.clear()
                                backStack.add(RotaLogin)
                            },
                            modifier = Modifier.padding(innerPadding),
                            onVoltar = onVoltar
                        )
                    }

                    is RotaLogin -> NavEntry(rota) {
                        TelaLogin(
                            onLogin = { usuario ->
                                backStack.clear()
                                backStack.add(RotaHome(usuario))
                            },
                            onNavCadastro = {
                                backStack.add(RotaCadastro)
                            },
                            onVoltar = onVoltar,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }

                    is RotaCadastro -> NavEntry(rota) {
                        TelaCadastro(
                            onCadastro = { usuario ->
                                backStack.clear()
                                backStack.add(RotaHome(usuario))
                            },
                            onNavLogin = {
                                backStack.add(RotaLogin)
                            },
                            onVoltar = onVoltar,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }

                    else -> {
                        error("Rota desconhecida: $rota")
                    }
                }
            }
        }
    }
}

@Composable
fun TelaHome(
    modifier: Modifier = Modifier,
    usuario: String = "",
    onLogout: () -> Unit = { },
    onVoltar: () -> Unit = { }
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        IconButton(
            onClick = { onVoltar() }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar"
            )
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Bem-vindo $usuario!",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.size(16.dp))

            Button(
                onClick = { onLogout() },
            ) {
                Text("Sair")
            }
        }
    }
}

@Composable
fun TelaLogin(
    modifier: Modifier = Modifier,
    onLogin: (String) -> Unit = { },
    onNavCadastro: () -> Unit = { },
    onVoltar: () -> Unit = { }
) {
    var usuario by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var erroLogin by rememberSaveable { mutableStateOf(false) }

    fun verificaLogin() {
        val usuariosValidos = mapOf(
            "admin" to "admin",
            "victor" to "123",
            "joao" to "321"
        )

        usuario = usuario.trim()
        senha = senha.trim()

        if (usuario.isEmpty() || senha.isEmpty()) {
            erroLogin = true
            return
        }

        if (usuariosValidos[usuario] != senha) {
            erroLogin = true
            return
        }

        erroLogin = false
        onLogin(usuario)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        IconButton(
            onClick = { onVoltar() }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar"
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Login",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.size(16.dp))

            TextField(
                value = usuario,
                onValueChange = {
                    usuario = it
                    erroLogin = false
                },
                label = { Text("Usuário") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.size(16.dp))

            TextField(
                value = senha,
                onValueChange = {
                    senha = it
                    erroLogin = false
                },
                label = { Text("Senha") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            if (erroLogin) {
                Spacer(modifier = Modifier.size(16.dp))

                Row {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Erro",
                        tint = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.size(8.dp))

                    Text(
                        text = "Usuário e/ou senha inválidos. Tente novamente.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.size(16.dp))

            Button(
                onClick = {
                    verificaLogin()
                },
            ) {
                Text("Entrar")
            }

            Spacer(modifier = Modifier.size(16.dp))

            TextButton(
                onClick = {
                    onNavCadastro()
                }
            ) {
                Text("Não possui cadastro? Clique aqui!")
            }
        }
    }
}

@Composable
fun TelaCadastro(
    modifier: Modifier = Modifier,
    onCadastro: (String) -> Unit = { },
    onNavLogin: () -> Unit = { },
    onVoltar: () -> Unit = { }
) {
    var nome by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var usuario by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var erroCadastro by rememberSaveable { mutableStateOf(false) }

    fun verificaLogin() {
        val usuariosValidos = mapOf(
            "admin" to "admin",
            "victor" to "123",
            "joao" to "321"
        )

        nome = nome.trim()
        email = email.trim()
        usuario = usuario.trim()
        senha = senha.trim()

        if (nome.isEmpty() || email.isEmpty() || usuario.isEmpty() || senha.isEmpty()) {
            erroCadastro = true
            return
        }

        if (usuariosValidos[usuario] != null) {
            erroCadastro = true
            return
        }

        onCadastro(nome)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        IconButton(
            onClick = { onVoltar() }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar"
            )
        }
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Cadastre-se aqui!",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.size(16.dp))

            TextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.size(16.dp))

            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.size(16.dp))

            TextField(
                value = usuario,
                onValueChange = { usuario = it },
                label = { Text("Usuário") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.size(16.dp))

            TextField(
                value = senha,
                onValueChange = { senha = it },
                label = { Text("Senha") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.size(16.dp))

            if (erroCadastro) {
                Spacer(modifier = Modifier.size(16.dp))

                Text(
                    text = "Usuário já cadastrado. Tente novamente.",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    verificaLogin()
                }
            ) {
                Text("Cadastrar")
            }

            Spacer(modifier = Modifier.size(16.dp))

            TextButton(
                onClick = {
                    onNavLogin()
                }
            ) {
                Text("Já possui cadastro? Clique aqui!")
            }
        }
    }
}