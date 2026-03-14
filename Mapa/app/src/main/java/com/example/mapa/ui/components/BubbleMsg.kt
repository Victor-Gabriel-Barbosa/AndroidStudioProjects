package com.example.mapa.ui.components

import android.content.ClipData
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mapa.R
import com.example.mapa.data.remote.dto.MsgDTO
import com.example.mapa.ui.theme.MapaTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Componente que exibe uma bolha de mensagem no chat.
 *
 * @param msg O objeto [MsgDTO] contendo os dados da mensagem a ser exibida.
 * @param author Indica se a mensagem foi enviada pelo usuário atual (alinha à direita e mostra opções).
 * @param onEdit Callback invocado quando o usuário confirma a edição da mensagem.
 * @param onDelete Callback invocado quando o usuário confirma a exclusão da mensagem.
 */
@Composable
fun BubbleMsg(
    msg: MsgDTO,
    author: Boolean,
    onEdit: (MsgDTO) -> Unit,
    onDelete: (String) -> Unit
) {
    // Gerenciador de clipboard para copiar texto
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Cores e alinhamento da mensagem
    val textColor =
        if (author) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val selectionColors = if (author) {
        TextSelectionColors(
            handleColor = MaterialTheme.colorScheme.onPrimary,
            backgroundColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
        )
    } else {
        TextSelectionColors(
            handleColor = MaterialTheme.colorScheme.primary,
            backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        )
    }

    // Estados para controle de exibição de diálogos
    var showMenu by rememberSaveable { mutableStateOf(false) }
    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    var showImgDialog by rememberSaveable { mutableStateOf<String?>(null) }

    ImgDialog(
        imgUrl = showImgDialog,
        onDismiss = { showImgDialog = null }
    )

    // Forma arredondada da mensagem
    val shape = if (author) RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
    else RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)

    // Calcula o tamanho máximo da mensagem (80% da largura da tela)
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val maxWidth = with(density) {
        (windowInfo.containerSize.width * 0.8f).toDp()
    }

    // Diálogo de edição de mensagem
    EditDialog(
        visible = showEditDialog,
        initialText = msg.text,
        title = stringResource(R.string.editar_mensagem),
        label = stringResource(R.string.mensagem),
        onDismiss = { showEditDialog = false },
        onConfirm = {
            showEditDialog = false
            onEdit(
                msg.copy(text = it, edited = true, timestamp = System.currentTimeMillis())
            )
        }
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (author) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (author) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            shape = shape,
            modifier = Modifier
                .widthIn(max = maxWidth)
                .clip(shape)
                .clickable {
                    focusManager.clearFocus()
                    showMenu = true
                }
        ) {
            Box(
                modifier = Modifier.padding(10.dp)
            ) {
                Column {
                    msg.imgUrls.forEach {
                        AsyncImage(
                            model = it,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(width = 220.dp, height = 180.dp)
                                .clip(MaterialTheme.shapes.extraLarge)
                                .clickable { showImgDialog = it }
                        )
                    }

                    CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
                        SelectionContainer {
                            Text(
                                text = msg.text,
                                color = textColor
                            )
                        }
                    }

                    // Rodapé da mensagem (Hora + Check)
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

                        Text(
                            text = if (msg.edited) stringResource(R.string.editado) else "",
                            color = textColor.copy(0.7f),
                            style = MaterialTheme.typography.labelSmall
                        )

                        Spacer(Modifier.width(4.dp))

                        Text(
                            text = sdf.format(Date(msg.timestamp)),
                            color = textColor.copy(0.7f),
                            style = MaterialTheme.typography.labelSmall
                        )

                        if (author) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = if (msg.read) Icons.Default.DoneAll else Icons.Default.Done,
                                contentDescription = if (msg.read) stringResource(R.string.lido) else stringResource(
                                    R.string.enviado
                                ),
                                tint = if (msg.read) MaterialTheme.colorScheme.onPrimary else textColor.copy(
                                    0.7f
                                ),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    offset = DpOffset(x = 0.dp, y = 0.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.copiar)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = stringResource(R.string.copiar_texto)
                            )
                        },
                        onClick = {
                            showMenu = false
                            scope.launch {
                                val clipData = ClipData.newPlainText("mensagem", msg.text)
                                clipboard.setClipEntry(ClipEntry(clipData))
                            }
                        }
                    )

                    // Opções de edição e exclusão (apenas se for o autor da mensagem)
                    if (author) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.editar)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = stringResource(R.string.editar_mensagem)
                                )
                            },
                            onClick = {
                                showMenu = false
                                showEditDialog = true
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.excluir)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = stringResource(R.string.excluir_mensagem)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDelete(msg.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun BubbleMsgPreview() {
    MapaTheme {
        Column {
            BubbleMsg(
                msg = MsgDTO(
                    id = "1",
                    text = "Olá, tudo bem? Encontrei o seu celular perdido",
                    uid = "123",
                    read = true,
                    timestamp = System.currentTimeMillis()
                ),
                author = true,
                onEdit = {},
                onDelete = {}
            )

            Spacer(modifier = Modifier.height(8.dp))

            BubbleMsg(
                msg = MsgDTO(
                    id = "2",
                    text = "Ola",
                    uid = "456",
                    read = true,
                    timestamp = System.currentTimeMillis()
                ),
                author = false,
                onEdit = {},
                onDelete = {}
            )
        }
    }
}