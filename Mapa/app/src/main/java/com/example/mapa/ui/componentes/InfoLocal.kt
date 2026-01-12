package com.example.mapa.ui.componentes

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mapa.R
import com.example.mapa.models.Local
import com.example.mapa.ui.theme.MapaTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Detalhes e informações de um local
 */
@Composable
fun InfoLocal(
    local: Local?,
    usuarioUid: String?,
    onFechar: () -> Unit,
    onExcluir: (String) -> Unit,
    onEditar: () -> Unit,
    onChat: (String) -> Unit
) {
    // Prepara dados para exibição e verifica se o usuário atual é o criador do registro
    val nome = local?.nome ?: stringResource(R.string.sem_nome)
    val tipo = local?.tipo ?: stringResource(R.string.sem_tipo)
    val data = remember(local?.data) {
        local?.data?.let {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
        }
    }
    val descricao = local?.descricao ?: stringResource(R.string.sem_descricao)
    val raio = local?.raio ?: 0.0
    val imgs = local?.imgUrls
    val criador = local?.uid != null && local.uid == usuarioUid

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabeçalho com título e botão de fechar
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.detalhes_do_local),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onFechar,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.fechar)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Exibe as imagens se disponíveis
            if (!imgs.isNullOrEmpty()) {
                CarrosselImgs(
                    imgs = imgs,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Lista de Informações (Nome, Tipo, Descrição, Raio)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Nome
                ListItem(
                    headlineContent = {
                        Text(nome, fontWeight = FontWeight.SemiBold)
                    },
                    overlineContent = { Text(stringResource(R.string.o_que_foi_perdido)) },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                // Tipo
                ListItem(
                    headlineContent = {
                        Text(
                            text = tipo,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    overlineContent = { Text(stringResource(R.string.tipo)) },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                // Data
                ListItem(
                    headlineContent = {
                        Text(
                            text = data ?: stringResource(R.string.sem_data),
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    overlineContent = { Text(stringResource(R.string.data)) },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                // Descrição
                ListItem(
                    headlineContent = {
                        Text(
                            text = descricao,
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    overlineContent = { Text(stringResource(R.string.descricao)) },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                // Raio
                ListItem(
                    headlineContent = {
                        Text(
                            text = stringResource(R.string.metros, raio.toInt()),
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    overlineContent = { Text(stringResource(R.string.raio_da_busca)) },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botões de ação (Editar/Excluir ou Cancelar/Chat)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (criador) {
                    // (Editar/Excluir)
                    Button(
                        onClick = { onExcluir(local.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.excluir))
                    }

                    Button(
                        onClick = onEditar,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.editar))
                    }
                } else {
                    // (Cancelar/Chat)
                    Button(
                        onClick = onFechar,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.cancelar))
                    }

                    Button(
                        onClick = {
                            if (local?.uid != null) onChat(local.uid)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.conversar))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InfoLocalPreview() {
    MapaTheme {
        InfoLocal(
            local = Local(
                id = "123",
                uid = "456",
                latitude = 0.0,
                longitude = 0.0,
                raio = 100.0,
                nome = "Smartphone",
                data = Date(System.currentTimeMillis()),
                tipo = "Perdido",
                descricao = "Descrição do local perdido",
                imgUrls = listOf()
            ),
            usuarioUid = "456",
            onFechar = { },
            onExcluir = { },
            onEditar = { },
            onChat = { }
        )
    }
}