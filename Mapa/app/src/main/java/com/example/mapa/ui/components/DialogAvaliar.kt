package com.example.mapa.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mapa.R
import com.example.mapa.data.remote.dto.UsuarioDTO
import com.example.mapa.ui.theme.MapaTheme

/**
 * Componente de diálogo de avaliação de usuário.
 *
 * @param visivel Indica se o diálogo está visível.
 * @param contato O usuário a ser avaliado.
 * @param onFechar A ação a ser executada ao fechar o diálogo.
 * @param onConfirmar A ação a ser executada ao confirmar a avaliação.
 * @param modifier Modificador para personalização.
 */
@Composable
fun DialogAvaliar(
    visivel: Boolean,
    contato: UsuarioDTO,
    onFechar: () -> Unit,
    onConfirmar: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visivel) return

    var nota by rememberSaveable { mutableIntStateOf(0) }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onFechar,
        title = {
            Text(text = stringResource(R.string.avaliar_usuario, contato.nome ?: ""))
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.qual_nota_voce_da_para_a_ajuda),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                BarraAvalicao(
                    nota = nota,
                    onNotaChange = { nota = it }
                )

                Text(
                    text = stringResource(R.string.nota, nota),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmar(nota.toDouble()) },
                enabled = nota > 0
            ) {
                Text(stringResource(R.string.confirmar))
            }
        },
        dismissButton = {
            TextButton(onClick = onFechar) {
                Text(stringResource(R.string.cancelar))
            }
        }
    )
}

@Preview
@Composable
private fun DialogAvaliarPreview() {
    MapaTheme {
        DialogAvaliar(
            visivel = true,
            contato = UsuarioDTO(
                uid = "123",
                nome = "João",
                email = "john.tyler@examplepetstore.com",
                foto = "https://img.freepik.com/vetores-gratis/ilustracao-do-jovem-sorridente_1308-174669.jpg"
            ),
            onFechar = {},
            onConfirmar = {}
        )
    }
}