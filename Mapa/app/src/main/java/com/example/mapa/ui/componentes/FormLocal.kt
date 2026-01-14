package com.example.mapa.ui.componentes

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mapa.R
import com.example.mapa.models.Local
import com.example.mapa.ui.theme.MapaTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Componente de formulário para adicionar ou editar um local.
 *
 * Este formulário contém campos para nome, tipo (perdido/encontrado), data, descrição,
 * um seletor de raio e um seletor de imagens. Gerencia seu próprio estado interno
 * para os campos de texto e seletores, e utiliza callbacks para notificar eventos
 * como salvar, fechar e alterar o raio.
 *
 * @param titulo O título a ser exibido no cabeçalho do formulário.
 * @param carregando Indica se o estado de carregamento deve ser exibido no botão de salvar.
 * @param onRaioChange Callback invocado quando o valor do slider de raio é alterado.
 * @param localInicial O objeto [Local] com os dados iniciais para popular o formulário.
 * @param onSalvar Callback invocado quando o usuário clica no botão "Salvar",
 * passando o objeto [Local] atualizado com os dados do formulário.
 * @param onFechar Callback invocado para fechar o formulário (botão de fechar ou cancelar).
 * @param modifier O [Modifier] a ser aplicado ao contêiner principal do formulário.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormLocal(
    titulo: String,
    carregando: Boolean,
    onRaioChange: (Double) -> Unit,
    localInicial: Local,
    onSalvar: (Local) -> Unit,
    onFechar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Variáveis de estado para os campos do formulário
    var nome by rememberSaveable { mutableStateOf(localInicial.nome) }
    var tipoSelecionado by rememberSaveable { mutableStateOf(localInicial.tipo) }
    var descricao by rememberSaveable { mutableStateOf(localInicial.descricao) }
    var imgs by rememberSaveable { mutableStateOf(localInicial.imgUrls) }

    // Estado do DatePicker
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = localInicial.data?.time ?: System.currentTimeMillis()
    )
    var datePicker by rememberSaveable { mutableStateOf(false) }

    val data = rememberSaveable(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { millis ->
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            formatter.format(Date(millis))
        } ?: ""
    }

    // Launcher de seleção de imagem
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> imgs = imgs + uris.map { it.toString() } }
    )

    // Estado da seletor de tipo
    var expandido by rememberSaveable { mutableStateOf(false) }

    // Diálogo de seleção de data
    if (datePicker) {
        DatePickerDialog(
            onDismissRequest = { datePicker = false },
            confirmButton = {
                TextButton(onClick = { datePicker = false }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { datePicker = false }) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = stringResource(R.string.selecione_a_data),
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp)
                    )
                }
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabeçalho com título e botão de fechar
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
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

            // Campo de nome
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text(stringResource(R.string.nome)) },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Campo de tipo
            ExposedDropdownMenuBox(
                expanded = expandido,
                onExpandedChange = { expandido = !expandido },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = tipoSelecionado,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.tipo)) },
                    placeholder = { Text(stringResource(R.string.selecione_uma_opcao)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido)
                   },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        )
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expandido,
                    onDismissRequest = { expandido = false }
                ) {
                    listOf(stringResource(R.string.perdido), stringResource(R.string.encontrado)).forEach { selecionado ->
                        DropdownMenuItem(
                            text = { Text(text = selecionado) },
                            onClick = {
                                tipoSelecionado = selecionado
                                expandido = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            // Campo de data
            OutlinedTextField(
                value = data,
                onValueChange = { },
                label = { Text(stringResource(R.string.data)) },
                shape = MaterialTheme.shapes.medium,
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { datePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Selecionar data")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                interactionSource = remember { MutableInteractionSource() }
                    .also { interactionSource ->
                        LaunchedEffect(interactionSource) {
                            interactionSource.interactions.collect {
                                if (it is PressInteraction.Release) datePicker = true
                            }
                        }
                    }
            )

            // Campo de descrição
            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = { Text(stringResource(R.string.descricao)) },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // Slider de raio
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.raio_de_busca),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = stringResource(R.string.metros, localInicial.raio.toInt()),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Slider(
                    value = localInicial.raio.toFloat(),
                    onValueChange = { onRaioChange(it.toDouble()) },
                    valueRange = 50f..500f
                )
            }

            // Seletor de imagem
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CarrosselImgs(
                    imgs = imgs,
                    onRemoverImg = { imgs = imgs - it }
                )

                OutlinedButton(
                    onClick = {
                        photoPicker.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = stringResource(R.string.adicionar_imagem)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(stringResource(R.string.adicionar_foto))
                }
            }

            // Botões de cancelar e salvar
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onFechar,
                    modifier = Modifier.weight(1f),
                    enabled = !carregando
                ) {
                    Text(stringResource(R.string.cancelar))
                }

                Button(
                    onClick = {
                        onSalvar(
                            localInicial.copy(
                                nome = nome,
                                tipo = tipoSelecionado,
                                data = datePickerState.selectedDateMillis?.let { Date(it) },
                                descricao = descricao,
                                imgUrls = imgs
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !carregando && nome.isNotBlank()
                ) {
                    if (carregando) AnimacaoCarregando(size = 24.dp)
                    else Text(stringResource(R.string.salvar))
                }
            }
        }
    }
}

/**
 * Preview para o componente [FormLocal].
 * Exibe o formulário no estado de carregamento.
 */
@Preview(showBackground = true)
@Composable
private fun FormLocalPreview() {
    MapaTheme {
        FormLocal(
            titulo = stringResource(R.string.adicionar_novo_local),
            localInicial = Local(
                id = "123",
                uid = "456",
                latitude = 0.0,
                longitude = 0.0,
                raio = 100.0,
                data = null,
                imgUrls = listOf()
            ),
            carregando = true,
            onRaioChange = { },
            onSalvar = { _ -> },
            onFechar = { }
        )
    }
}