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
 * Componente de diálogo de confirmação.
 *
 * @param modifier Modificador para personalização.
 * @param visivel Indica se o diálogo está visível.
 * @param titulo Título do diálogo.
 * @param texto Texto do diálogo.
 * @param onFechar Ação a ser executada ao fechar o diálogo.
 * @param onConfirmar Ação a ser executada ao confirmar a ação.
 * @param textoConfirmar Texto do botão de confirmação.
 * @param textoCancelar Texto do botão de cancelamento.
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