package com.example.mapa.ui.componentes

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
 * Diálogo de edição de texto
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

    var texto by rememberSaveable(textoInicial) { mutableStateOf(textoInicial) }

    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text(text = titulo) },
        text = {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (texto.isNotBlank()) onConfirmar(texto.trim())
                    onFechar()
                },
                enabled = texto.isNotBlank()
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