package com.example.mapa.ui.components

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
import androidx.compose.material3.TextButton
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
import com.example.mapa.data.remote.dto.LocationDTO
import com.example.mapa.ui.theme.MapaTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Componente que exibe os detalhes de um local.
 *
 * @param location O objeto [LocationDTO] cujos detalhes serão exibidos.
 * @param userUid O UID do usuário logado, para verificar se ele é o criador do local.
 * @param onDismiss Callback para fechar a visualização dos detalhes.
 * @param onDelete Callback para solicitar a exclusão do local, passando o ID do local.
 * @param onEdit Callback para iniciar a edição do local.
 * @param onChat Callback para iniciar um chat com o criador do local, passando o UID do criador.
 */
@Composable
fun LocationDetails(
    location: LocationDTO,
    userUid: String?,
    onDismiss: () -> Unit,
    onDelete: (String) -> Unit,
    onEdit: () -> Unit,
    onChat: (String, String) -> Unit
) {
    // Formata a data para exibição
    val date = remember(location.date) {
        location.date?.let {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cabeçalho com título e botão de fechar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.detalhes_do_local),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.fechar)
                )
            }
        }

        HorizontalDivider()


        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(top = 4.dp, bottom = 24.dp)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagens/fotos
            CarouselImg(
                imgUrls = location.imgUrls,
                modifier = Modifier.fillMaxWidth()
            )

            // Nome
            ListItem(
                headlineContent = {
                    Text(location.name, fontWeight = FontWeight.SemiBold)
                },
                overlineContent = {
                    Text(stringResource(R.string.o_que_foi_perdido))
                },
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
                        text = location.type,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                overlineContent = {
                    Text(stringResource(R.string.tipo))
                },
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
                        text = date ?: stringResource(R.string.sem_data),
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
                        text = location.description.ifBlank { stringResource(R.string.sem_descricao) },
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                overlineContent = {
                    Text(stringResource(R.string.descricao))
                },
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
                        text = stringResource(R.string.metros, location.radius.toInt()),
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

            Spacer(modifier = Modifier.height(8.dp))

            // Botões de ação (Editar/Excluir ou Cancelar/Chat)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Verifica se o usuário é o criador do local
                if (location.uid == userUid) {
                    // (Editar/Excluir)
                    Button(
                        onClick = { location.id.let(onDelete) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
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
                        onClick = onEdit,
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
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.cancelar))
                    }

                    Button(
                        onClick = {
                            onChat(location.uid, location.id)
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
private fun LocationDetailsPreview() {
    MapaTheme {
        LocationDetails(
            location = LocationDTO(
                id = "123",
                uid = "456",
                latitude = 0.0,
                longitude = 0.0,
                radius = 100.0,
                name = "Smartphone",
                date = Date(System.currentTimeMillis()),
                type = "Perdido",
                description = "Descrição do local perdido",
                imgUrls = listOf()
            ),
            userUid = "456",
            onDismiss = {},
            onDelete = {},
            onEdit = {},
            onChat = { _, _ -> }
        )
    }
}