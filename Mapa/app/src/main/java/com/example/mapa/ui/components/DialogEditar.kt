package com.example.mapa.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.mapa.R
import com.example.mapa.ui.theme.MapaTheme

/**
 * Componente de diálogo de edição de texto.
 *
 * @param modifier Modificador para personalização.
 * @param visivel Indica se o diálogo está visível.
 * @param textoInicial Texto inicial para edição.
 * @param titulo Título do diálogo.
 * @param label Rótulo do campo de texto.
 * @param onFechar Ação a ser executada ao fechar o diálogo.
 * @param onConfirmar Ação a ser executada ao confirmar a edição.
 * @param textoConfirmar Texto do botão de confirmação.
 * @param textoCancelar Texto do botão de cancelamento.
 */
@Composable
fun DialogEditar(
    modifier: Modifier = Modifier,
    visivel: Boolean = false,
    textoInicial: String = "",
    titulo: String = "",
    label: String = "",
    onFechar: () -> Unit,
    onConfirmar: (String) -> Unit,
    textoConfirmar: String = stringResource(R.string.salvar),
    textoCancelar: String = stringResource(R.string.cancelar),
) {
    if (!visivel) return

    var nome by rememberSaveable(textoInicial) { mutableStateOf(textoInicial) }

    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text(text = titulo) },
        text = {
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nome.isNotBlank()) onConfirmar(nome.trim())
                    onFechar()
                },
                enabled = nome.isNotBlank()
            ) {
                Text(textoConfirmar)
            }
        },
        dismissButton = {
            TextButton(onClick = onFechar) {
                Text(textoCancelar)
            }
        },
        modifier = modifier
    )
}

@Preview
@Composable
private fun DialogEditarPreview() {
    MapaTheme {
        DialogEditar(
            visivel = true,
            textoInicial = "Victor",
            titulo = stringResource(R.string.editar_nome),
            label = stringResource(R.string.nome),
            onFechar = { },
            onConfirmar = { }
        )
    }
}