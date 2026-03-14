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
 * @param visible Indica se o diálogo está visível.
 * @param initialText Texto inicial para edição.
 * @param title Título do diálogo.
 * @param label Rótulo do campo de texto.
 * @param onDismiss Ação a ser executada ao fechar o diálogo.
 * @param onConfirm Ação a ser executada ao confirmar a edição.
 * @param textConfirm Texto do botão de confirmação.
 * @param textCancel Texto do botão de cancelamento.
 */
@Composable
fun EditDialog(
    modifier: Modifier = Modifier,
    visible: Boolean = false,
    initialText: String = "",
    title: String = "",
    label: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    textConfirm: String = stringResource(R.string.salvar),
    textCancel: String = stringResource(R.string.cancelar),
) {
    if (!visible) return

    var text by rememberSaveable(initialText) { mutableStateOf(initialText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (text.isNotBlank()) onConfirm(text.trim())
                    onDismiss()
                },
                enabled = text.isNotBlank()
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
private fun EditDialogPreview() {
    MapaTheme {
        EditDialog(
            visible = true,
            initialText = "Victor",
            title = stringResource(R.string.editar_nome),
            label = stringResource(R.string.nome),
            onDismiss = { },
            onConfirm = { }
        )
    }
}