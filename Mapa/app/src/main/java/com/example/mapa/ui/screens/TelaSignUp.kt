package com.example.mapa.ui.screens

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mapa.R
import com.example.mapa.model.LoginUiState
import com.example.mapa.ui.components.AnimacaoCarregando
import com.example.mapa.utils.labelObrigatorio
import com.example.mapa.ui.theme.MapaTheme

@Composable
fun TelaSignUp(
    loginUiState: LoginUiState,
    onSignup: (String, String) -> Unit,
    onNavegarParaLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estados dos campos
    var email by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var confirmacaoSenha by rememberSaveable { mutableStateOf("") }
    var senhaVisivel by rememberSaveable { mutableStateOf(false) }
    var confirmacaoSenhaVisivel by rememberSaveable { mutableStateOf(false) }

    // Estados de erro para cada campo
    var erroEmail by rememberSaveable { mutableStateOf<Int?>(null) }
    var erroSenha by rememberSaveable { mutableStateOf<Int?>(null) }
    var erroConfirmacao by rememberSaveable { mutableStateOf<Int?>(null) }

    // Gerenciador de teclado
    val focusManager = LocalFocusManager.current

    // Valida os campos antes de enviar
    fun validarEEnviar() {
        // Reseta erros
        erroEmail = null
        erroSenha = null
        erroConfirmacao = null

        var erro = false

        // Valida email (Vazio ou Formato inválido)
        if (email.isBlank()) {
            erroEmail = R.string.o_e_mail_obrigatorio
            erro = true
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            erroEmail = R.string.formato_de_e_mail_invalido
            erro = true
        }

        // Valida senha (Tamanho)
        if (senha.length < 6) {
            erroSenha = R.string.a_senha_deve_ter_no_minimo_6_caracteres
            erro = true
        }

        // Valida confirmação de senha (Igualdade)
        if (confirmacaoSenha != senha) {
            erroConfirmacao = R.string.as_senhas_nao_coincidem
            erro = true
        }

        if (!erro) {
            focusManager.clearFocus() // Esconde o teclado
            onSignup(email, senha)
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.login_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = stringResource(R.string.criar_conta),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo de email
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (erroEmail != null) erroEmail = null // Limpa erro ao digitar
                        },
                        label = { Text(stringResource(R.string.e_mail).labelObrigatorio()) },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = loginUiState !is LoginUiState.Carregando,
                        isError = erroEmail != null,
                        supportingText = { if (erroEmail != null) Text(stringResource(erroEmail!!)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Mail,
                                contentDescription = null
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Campo de senha
                    OutlinedTextField(
                        value = senha,
                        onValueChange = {
                            senha = it
                            if (erroSenha != null) erroSenha = null
                        },
                        label = { Text(stringResource(R.string.senha).labelObrigatorio()) },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = loginUiState !is LoginUiState.Carregando,
                        isError = erroSenha != null,
                        supportingText = { if (erroSenha != null) Text(stringResource(erroSenha!!)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                                Icon(
                                    imageVector = if (senhaVisivel) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Campo de confirmar senha
                    OutlinedTextField(
                        value = confirmacaoSenha,
                        onValueChange = {
                            confirmacaoSenha = it
                            if (erroConfirmacao != null) erroConfirmacao = null
                        },
                        label = { Text(stringResource(R.string.confirmar_senha).labelObrigatorio()) },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = loginUiState !is LoginUiState.Carregando,
                        isError = erroConfirmacao != null,
                        supportingText = {
                            if (erroConfirmacao != null) Text(stringResource(erroConfirmacao!!))
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        visualTransformation = if (confirmacaoSenhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                confirmacaoSenhaVisivel = !confirmacaoSenhaVisivel
                            }) {
                                Icon(
                                    imageVector = if (confirmacaoSenhaVisivel) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (loginUiState is LoginUiState.Carregando) AnimacaoCarregando()
                    else {
                        Button(
                            onClick = { validarEEnviar() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.cadastrar),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onNavegarParaLogin,
                        enabled = loginUiState !is LoginUiState.Carregando,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                    ) {
                        Text(stringResource(R.string.ja_tem_conta_faca_login))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TelaSignUpPreview() {
    MapaTheme {
        TelaSignUp(
            loginUiState = LoginUiState.Parado,
            onSignup = { _, _ -> },
            onNavegarParaLogin = {}
        )
    }
}