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
import com.example.mapa.data.remote.dto.Local
import com.example.mapa.ui.theme.MapaTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Componente que exibe os detalhes e informações de um [Local] específico.
 *
 * Mostra informações como nome, tipo, data, descrição e raio.
 * Apresenta um carrossel de imagens, se houver.
 * Oferece botões de ação diferentes dependendo se o usuário atual é o criador do registro:
 * - Se for o criador: botões para "Excluir" e "Editar".
 * - Se não for o criador: botões para "Cancelar" e "Conversar" (iniciar chat).
 *
 * @param local O objeto [Local] cujos detalhes serão exibidos.
 * @param usuarioUid O UID do usuário logado, para verificar se ele é o criador do local.
 * @param onFechar Callback para fechar a visualização dos detalhes.
 * @param onExcluir Callback para solicitar a exclusão do local, passando o ID do local.
 * @param onEditar Callback para iniciar a edição do local.
 * @param onChat Callback para iniciar um chat com o criador do local, passando o UID do criador.
 */
@Composable
fun InfoLocal(
    local: Local,
    usuarioUid: String?,
    onFechar: () -> Unit,
    onExcluir: (String) -> Unit,
    onEditar: () -> Unit,
    onChat: (String) -> Unit
) {
    // Formata a data para exibição
    val data = remember(local.data) {
        local.data?.let {
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
                onClick = onFechar,
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
            CarrosselImgs(
                imgs = local.imgUrls,
                modifier = Modifier.fillMaxWidth()
            )

            // Nome
            ListItem(
                headlineContent = {
                    Text(local.nome, fontWeight = FontWeight.SemiBold)
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
                        text = local.tipo,
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
                        text = local.descricao,
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
                        text = stringResource(R.string.metros, local.raio.toInt()),
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
                if (local.uid == usuarioUid) {
                    // (Editar/Excluir)
                    Button(
                        onClick = { local.id.let(onExcluir) },
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
                    TextButton(
                        onClick = onFechar,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.cancelar))
                    }

                    Button(
                        onClick = {
                            onChat(local.uid)
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

/**
 * Preview do componente [InfoLocal].
 *
 * Simula a visualização dos detalhes de um local onde o usuário logado é o criador,
 * mostrando os botões de "Excluir" e "Editar".
 */
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