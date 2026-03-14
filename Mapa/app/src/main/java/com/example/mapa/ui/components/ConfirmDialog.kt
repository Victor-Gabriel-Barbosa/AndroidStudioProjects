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
 * @param visible Indica se o diálogo está visível.
 * @param title Título do diálogo.
 * @param text Texto do diálogo.
 * @param onDismiss Ação a ser executada ao fechar o diálogo.
 * @param onConfirm Ação a ser executada ao confirmar a ação.
 * @param textConfirm Texto do botão de confirmação.
 * @param textCancel Texto do botão de cancelamento.
 */
@Composable
fun ConfirmDialog(
    modifier: Modifier = Modifier,
    visible: Boolean = false,
    title: String = "",
    text: String = "",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    textConfirm: String = stringResource(R.string.sim),
    textCancel: String = stringResource(R.string.cancelar),
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = text) },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text(textConfirm)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(textCancel)
            }
        },
        modifier = modifier
    )
}

@Preview
@Composable
private fun ConfirmDialogPreview() {
    MapaTheme {
        ConfirmDialog(
            visible = true,
            title = stringResource(R.string.sair_da_conta) + "?",
            onDismiss = {},
            onConfirm = {}
        )
    }
}