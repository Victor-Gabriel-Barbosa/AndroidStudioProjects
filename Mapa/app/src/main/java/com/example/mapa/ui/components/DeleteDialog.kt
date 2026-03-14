package com.example.mapa.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mapa.R
import com.example.mapa.ui.theme.MapaTheme

/**
 * Componente de diálogo de exclusão.
 *
 * @param visible Controla se o diálogo está visível ou não.
 * @param title O texto a ser exibido como título do diálogo.
 * @param msg O corpo do texto do diálogo, descrevendo a ação.
 * @param onConfirm Callback invocado quando o usuário clica no botão de confirmação.
 * @param onCancel Callback invocado quando o usuário clica no botão de cancelar ou fecha o diálogo.
 * @param textConfirm O texto para o botão de confirmação. O padrão é "Sim, excluir".
 * @param textCancel O texto para o botão de cancelar. O padrão é "Cancelar".
 */
@Composable
fun DeleteDialog(
    visible: Boolean,
    title: String,
    msg: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    textConfirm: String = stringResource(R.string.sim_excluir),
    textCancel: String = stringResource(R.string.cancelar),
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onCancel,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = msg,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = textConfirm,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel
            ) {
                Text(
                    text = textCancel,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

@Preview
@Composable
private fun DeleteDialogPreview() {
    MapaTheme {
        DeleteDialog(
            visible = true,
            title = stringResource(R.string.excluir_local),
            msg = stringResource(
                R.string.tem_certeza_que_deseja_excluir_essa_acao_nao_pode_ser_desfeita,
                "Chave Perdida"
            ),
            onConfirm = {},
            onCancel = {}
        )
    }
}