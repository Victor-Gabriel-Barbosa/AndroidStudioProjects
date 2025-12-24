package com.example.refindu.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.refindu.R
import com.example.refindu.models.Local

@Composable
fun DetailsLocal(
    local: Local?,
    userUid: String?,
    onClose: () -> Unit,
    onDelete: (String) -> Unit,
    onEdit: () -> Unit
) {
    // Prepara dados para exibição e verifica se o usuário atual é o criador do registro
    val name = local?.name ?: stringResource(R.string.item_desconhecido)
    val category = local?.category ?: stringResource(R.string.tipo_n_o_informado)
    val radius = local?.radius ?: 0.0
    val imageUrl = local?.imageUrl
    val isCreator = local?.uid != null && local.uid == userUid

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Barra visual superior (Drag Handle)
        Box(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .width(32.dp)
                .height(4.dp)
                .background(Color.LightGray.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
        )

        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabeçalho com Título e Fechar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.detalhes_da_perda),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.fechar))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Seção de Imagem (Opcional)
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray.copy(alpha = 0.3f))
                )
            }

            // Lista de Informações (Nome, Categoria, Raio)
            ListItem(
                headlineContent = { Text(name) },
                overlineContent = { Text(stringResource(R.string.o_que_foi_perdido)) },
                leadingContent = { Icon(Icons.Default.Info, contentDescription = null) }
            )

            ListItem(
                headlineContent = { Text(category) },
                overlineContent = { Text(stringResource(R.string.categoria)) },
                leadingContent = { Icon(Icons.Default.LocationOn, contentDescription = null) }
            )

            ListItem(
                headlineContent = {
                    Text(stringResource(R.string.metros, radius.toInt()))
                },
                overlineContent = { Text(stringResource(R.string.raio_de_busca_2)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botões de Ação: Condicionais baseados na propriedade do item
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isCreator) {
                    // Controles do Dono (Editar/Excluir)
                    Button(
                        onClick = { onDelete(local!!.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.excluir))
                    }

                    Button(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.editar))
                    }
                } else {
                    // Controles de Visitante (Apenas fechar)
                    Button(
                        onClick = onClose,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.entendido))
                    }
                }
            }
        }
    }
}