package com.example.mapa.ui.componentes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mapa.R
import com.example.mapa.models.Mensagem
import com.example.mapa.ui.theme.MapaTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Componente que exibe uma bolha de mensagem no chat.
 * Suporta mensagens de texto, imagens, indicadores de leitura e menu de opções (editar/excluir).
 *
 * @param msg O objeto [Mensagem] contendo os dados da mensagem a ser exibida.
 * @param remetente Indica se a mensagem foi enviada pelo usuário atual (alinha à direita e mostra opções).
 * @param onEditar Callback invocado quando o usuário confirma a edição da mensagem.
 * @param onExcluir Callback invocado quando o usuário confirma a exclusão da mensagem.
 */
@Composable
fun BolhaMsg(
    msg: Mensagem,
    remetente: Boolean,
    onEditar: (String, Mensagem) -> Unit,
    onExcluir: (String) -> Unit
) {
    // Cores e alinhamento da mensagem
    val corCaixa = if (remetente) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val corTexto = if (remetente) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val alignment = if (remetente) Alignment.End else Alignment.Start

    // Estado para controlar a visibilidade do menu
    var expandido by rememberSaveable { mutableStateOf(false) }
    var editarDialog by rememberSaveable { mutableStateOf(false) }

    // Diálogo de imagem com zoom
    var dialogImg by rememberSaveable { mutableStateOf<String?>(null) }
    DialogImg(
        img = dialogImg,
        onFechar = { dialogImg = null }
    )

    // Forma arredondada da mensagem
    val shape = if (remetente) RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
    else RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = corCaixa,
            shape = shape,
            modifier = Modifier.padding(vertical = 2.dp)
        ) {
            Box(modifier = Modifier.padding(10.dp)) {
                Column(
                    modifier = Modifier.padding(end = if (remetente) 20.dp else 0.dp)
                ) {
                    msg.imgUrls.forEach {
                        AsyncImage(
                            model = it,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(width = 220.dp, height = 180.dp)
                                .clip(MaterialTheme.shapes.extraLarge)
                                .clickable { dialogImg = it }
                        )
                    }

                    Text(
                        text = msg.texto,
                        color = corTexto,
                        fontSize = 16.sp
                    )

                    // Rodapé da mensagem (Hora + Check)
                    Row(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

                        Text(
                            text = if (msg.editado) stringResource(R.string.editado) else "",
                            color = corTexto.copy(0.7f),
                            style = MaterialTheme.typography.labelSmall
                        )

                        Spacer(Modifier.width(4.dp))

                        Text(
                            text = sdf.format(Date(msg.timestamp)),
                            color = corTexto.copy(0.7f),
                            style = MaterialTheme.typography.labelSmall
                        )

                        if (remetente) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = if (msg.lido) Icons.Default.DoneAll else Icons.Default.Done,
                                contentDescription = if (msg.lido) stringResource(R.string.lido) else stringResource(R.string.enviado),
                                tint = if (msg.lido) MaterialTheme.colorScheme.onPrimary else corTexto.copy(0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Lógica do Menu (Apenas se for o usuário ativo)
                if (remetente) {
                    Box(modifier = Modifier.align(Alignment.TopEnd)) {
                        IconButton(
                            onClick = { expandido = true },
                            modifier = Modifier
                                .size(20.dp)
                                .padding(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.opcoes),
                                tint = corTexto.copy(alpha = 0.7f)
                            )
                        }

                        DropdownMenu(
                            expanded = expandido,
                            onDismissRequest = { expandido = false },
                            offset = DpOffset(x = 0.dp, y = 0.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.editar)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    expandido = false
                                    editarDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.excluir)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    expandido = false
                                    onExcluir(msg.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo de edição de mensagem
    DialogEditar(
        visivel = editarDialog,
        textoInicial = msg.texto,
        titulo = stringResource(R.string.editar_mensagem),
        label = stringResource(R.string.mensagem),
        onFechar = { editarDialog = false },
        onConfirmar = {
            editarDialog = false
            onEditar(msg.id, msg.copy(texto = it, editado = true, timestamp = System.currentTimeMillis()))
        }
    )
}

/**
 * Preview das bolhas de mensagem em um contexto de chat.
 */
@Preview
@Composable
fun BolhaMsgPreview() {
    MapaTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            BolhaMsg(
                msg = Mensagem(
                    id = "1",
                    texto = "Olá, tudo bem?",
                    remetenteUid = "123",
                    destinatarioUid = "456",
                    lido = true,
                    timestamp = System.currentTimeMillis()
                ),
                remetente = true,
                onEditar = { _, _ -> },
                onExcluir = { }
            )

            Spacer(modifier = Modifier.size(8.dp))

            BolhaMsg(
                msg = Mensagem(
                    id = "2",
                    texto = "Perfeito e vc?",
                    remetenteUid = "456",
                    destinatarioUid = "123",
                    lido = true,
                    timestamp = System.currentTimeMillis()
                ),
                remetente = false,
                onEditar = { _, _ -> },
                onExcluir = { }
            )
        }
    }
}