package com.example.mapa.ui.componentes

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
 * Um componente de diálogo de alerta reutilizável para confirmar uma ação de exclusão.
 * Exibe um ícone de aviso, título, mensagem e botões de confirmação e cancelamento.
 *
 * @param visivel Controla se o diálogo está visível ou não.
 * @param titulo O texto a ser exibido como título do diálogo.
 * @param mensagem O corpo do texto do diálogo, descrevendo a ação.
 * @param onConfirmar Callback invocado quando o usuário clica no botão de confirmação.
 * @param onCancelar Callback invocado quando o usuário clica no botão de cancelar ou fecha o diálogo.
 * @param textoConfirmar O texto para o botão de confirmação. O padrão é "Sim, excluir".
 * @param textoCancelar O texto para o botão de cancelar. O padrão é "Cancelar".
 */
@Composable
fun DialogExcluir(
    visivel: Boolean,
    titulo: String,
    mensagem: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    textoConfirmar: String = stringResource(R.string.sim_excluir),
    textoCancelar: String = stringResource(R.string.cancelar),
) {
    if (!visivel) return

    AlertDialog(
        onDismissRequest = onCancelar,
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
                text = titulo,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = mensagem,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmar,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = textoConfirmar,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancelar
            ) {
                Text(
                    text = textoCancelar,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

/**
 * Preview para o componente [DialogExcluir].
 */
@Preview
@Composable
private fun DialogExcluirPreview() {
    MapaTheme {
        DialogExcluir(
            visivel = true,
            titulo = stringResource(R.string.excluir_local),
            mensagem = stringResource(
                R.string.tem_certeza_que_deseja_excluir_essa_acao_nao_pode_ser_desfeita,
                "Chave Perdida"
            ),
            onConfirmar = {},
            onCancelar = {}
        )
    }
}