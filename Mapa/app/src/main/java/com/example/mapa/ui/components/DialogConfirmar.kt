package com.example.mapa.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.mapa.R
import com.example.mapa.ui.theme.MapaTheme

/**
 * Diálogo de confirmação de ação.
 */
@Composable
fun DialogConfirmar(
    modifier: Modifier = Modifier,
    visivel: Boolean = false,
    titulo: String = "",
    texto: String = "",
    onFechar: () -> Unit,
    onConfirmar: () -> Unit,
    textoConfirmar: String = stringResource(R.string.sim),
    textoCancelar: String = stringResource(R.string.cancelar),
) {
    if (!visivel) return

    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text(text = titulo) },
        text = { Text(text = texto) },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmar()
                    onFechar()
                }
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
private fun DialogConfirmarPreview() {
    MapaTheme {
        DialogConfirmar(
            visivel = true,
            titulo = stringResource(R.string.sair_da_conta) + "?",
            onFechar = {},
            onConfirmar = {}
        )
    }
}