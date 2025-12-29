package com.example.refindu.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.refindu.R

@Composable
fun LocalForm(
    title: String,
    initialRadius: Double,
    isLoading: Boolean,
    onRadiusChange: (Double) -> Unit,
    onCancel: () -> Unit,
    onSave: (String, String, Uri?) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    initialName: String = "",
    initialCategory: String = "",
    initialImgUrl: String? = null
) {
    // Gerenciamento de estado dos campos (suporta edição e criação)
    var name by remember { mutableStateOf(initialName) }
    var category by remember { mutableStateOf(initialCategory) }
    var imageUrl by remember { mutableStateOf<Any?>(initialImgUrl) }

    // Launcher para seleção de mídia da galeria (Photo Picker)
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        if (it != null) imageUrl = it
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabeçalho com título e ação de fechar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.fechar))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Campos de entrada de texto
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.nome_do_local)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text(stringResource(R.string.tipo_ex_objeto_perdido)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Lógica de exibição de imagem: Preview com opção de remoção ou Botão de adição
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (imageUrl != null) {
                    Box(modifier = Modifier.size(120.dp)) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(12.dp))
                        )
                        IconButton(
                            onClick = { imageUrl = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(28.dp)
                                .background(Color.DarkGray.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remover", tint = Color.Red, modifier = Modifier.size(16.dp))
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.adicionar_foto_opcional))
                    }
                }
            }

            // Controle deslizante para raio de busca
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Raio de busca", style = MaterialTheme.typography.bodyMedium)
                    Text("${initialRadius.toInt()}m", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = initialRadius.toFloat(),
                    onValueChange = { onRadiusChange(it.toDouble()) },
                    valueRange = 50f..1000f
                )
            }

            // Botões de ação (Cancelar/Salvar) com feedback de carregamento
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Text(stringResource(R.string.cancelar))
                }

                Button(
                    onClick = { onSave(name, category, imageUrl as Uri?) },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && name.isNotEmpty()
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    else Text(stringResource(R.string.salvar))
                }
            }
        }
    }
}