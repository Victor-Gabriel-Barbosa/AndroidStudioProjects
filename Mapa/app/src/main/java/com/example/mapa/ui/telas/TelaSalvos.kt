package com.example.mapa.ui.telas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.mapa.R
import com.example.mapa.data.remote.dto.LocalDTO
import com.example.mapa.model.SheetUiState
import com.example.mapa.model.LocalUiState
import com.example.mapa.ui.components.AnimacaoLottie
import com.example.mapa.ui.components.BarraPesquisa
import com.example.mapa.ui.components.DialogExcluir
import com.example.mapa.ui.components.FormLocal
import com.example.mapa.ui.components.Header
import com.example.mapa.ui.components.FundoCarregando
import com.example.mapa.ui.theme.MapaTheme
import com.example.mapa.viewmodels.LocalViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaSalvos(
    modifier: Modifier = Modifier,
    localViewModel: LocalViewModel = koinViewModel()
) {
    val context = LocalContext.current

    // Observáveis do ViewModel
    val uiState by localViewModel.uiState.collectAsStateWithLifecycle()

    // Feedback visual (eventos) vindo do ViewModel
    LaunchedEffect(Unit) {
        localViewModel.canal.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    TelaSalvosContent(
        modifier = modifier,
        localUiState = uiState,
        onRemoverLocal = { localViewModel.removerLocal(it) },
        onEditarLocal = { localViewModel.editarLocal(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaSalvosContent(
    modifier: Modifier = Modifier,
    localUiState: LocalUiState,
    onEditarLocal: (LocalDTO) -> Unit,
    onRemoverLocal: (String) -> Unit,
) {
    // Estados de UI locais (controles de diálogo, edição, etc)
    var mostrarDialogExcluir by rememberSaveable { mutableStateOf(false) }
    var localParaExcluir by rememberSaveable { mutableStateOf<LocalDTO?>(null) }

    // Estado do BottomSheet (oculto, editando)
    var sheetUiState by rememberSaveable { mutableStateOf<SheetUiState>(SheetUiState.Escondido) }

    // Estado visual do ModalBottomSheet
    val modalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Estado do texto barra de pesquisa
    var pesquisa by rememberSaveable { mutableStateOf("") }

    // Filtra os locais com base na pesquisa
    val locaisFiltrados = remember(localUiState.locaisUsuario, pesquisa) {
        if (pesquisa.isBlank()) localUiState.locaisUsuario
        else {
            localUiState.locaisUsuario.filter { item ->
                item.nome.contains(pesquisa, ignoreCase = true) ||
                        item.descricao.contains(pesquisa, ignoreCase = true)
            }
        }
    }

    // Diálogo de confirmação de exclusão
    DialogExcluir(
        visivel = mostrarDialogExcluir && localParaExcluir != null,
        titulo = stringResource(R.string.excluir_local),
        mensagem = stringResource(
            R.string.tem_certeza_que_deseja_excluir_essa_acao_nao_pode_ser_desfeita,
            localParaExcluir?.nome ?: ""
        ),
        onConfirmar = {
            localParaExcluir?.let { onRemoverLocal(it.id) }
            mostrarDialogExcluir = false
            localParaExcluir = null
        },
        onCancelar = {
            mostrarDialogExcluir = false
            localParaExcluir = null
        }
    )

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Header(
            titulo = stringResource(R.string.salvos),
            icone = R.drawable.logo,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        BarraPesquisa(
            onPesquisa = { pesquisa = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        )

        // Gerenciamento dos estados de UI
        when {
            // Estado de carregamento
            localUiState.carregando -> FundoCarregando()

            // Estado de erro
            localUiState.erro != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = stringResource(R.string.erro),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )

                    Text(
                        text = stringResource(
                            R.string.ocorreu_um_erro_ao_carregar_as_conversas,
                            localUiState.erro
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Estado de lista vazia (Sem nenhum local salvo)
            locaisFiltrados.isEmpty() && pesquisa.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimacaoLottie(
                        animacao = R.raw.mapa_animado,
                        modifier = Modifier.size(200.dp)
                    )

                    Text(text = stringResource(R.string.nenhum_local_salvo))
                }
            }

            // Se a busca não retornou nada (mas existem locais salvos)
            locaisFiltrados.isEmpty() && pesquisa.isNotEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimacaoLottie(
                        animacao = R.raw.mapa_animado,
                        modifier = Modifier.size(200.dp)
                    )

                    Text(text = stringResource(R.string.nenhum_local_encontrado))
                }
            }

            // Estado de sucesso com dados
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = locaisFiltrados,
                        key = { local -> local.id }
                    ) { item ->
                        LocalItem(
                            local = item,
                            onEditar = {
                                sheetUiState = SheetUiState.Editando(item)
                            },
                            onExcluir = {
                                localParaExcluir = item
                                mostrarDialogExcluir = true
                            }
                        )
                    }
                }
            }
        }

        // BottomSheet de edição
        val estado = sheetUiState as? SheetUiState.Editando
        if (estado != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    sheetUiState = SheetUiState.Escondido
                },
                sheetState = modalSheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                tonalElevation = 2.dp,
                dragHandle = {
                    Box(
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .background(MaterialTheme.colorScheme.onSurface, CircleShape)
                        )
                    }
                },
            ) {
                FormLocal(
                    titulo = stringResource(R.string.editar_local),
                    localInicial = estado.local,
                    carregando = localUiState.carregando,
                    onRaioChange = {},
                    onSalvar = { local ->
                        onEditarLocal(local)
                        sheetUiState = SheetUiState.Escondido
                    },
                    onFechar = {
                        sheetUiState = SheetUiState.Escondido
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalItem(
    local: LocalDTO,
    onEditar: () -> Unit,
    onExcluir: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandido by rememberSaveable { mutableStateOf(false) }

    ListItem(
        modifier = modifier,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        tonalElevation = 4.dp,
        leadingContent = {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (local.imgUrls.isNotEmpty()) {
                        AsyncImage(
                            model = local.imgUrls[0],
                            contentDescription = local.nome,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        },
        headlineContent = {
            Text(
                text = local.nome,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },

        supportingContent = {
            Text(
                text = "${local.descricao} • ${local.raio.toInt()}m",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            // Botão de edição/exclusão com dica de uso
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    TooltipAnchorPosition.Above
                ),
                tooltip = {
                    PlainTooltip {
                        Text(stringResource(R.string.editar_ou_excluir))
                    }
                },
                state = rememberTooltipState()
            ) {
                IconButton(
                    onClick = { expandido = true },
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.opcoes),
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
                            onEditar()
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
                            onExcluir()
                        }
                    )
                }
            }
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TelaSalvosContentPreview() {
    MapaTheme {
        TelaSalvosContent(
            localUiState = LocalUiState(
                locaisUsuario = listOf(
                    LocalDTO(
                        id = "1",
                        latitude = -23.550520,
                        longitude = -46.633308,
                        nome = "Chave Perdida",
                        descricao = "Perdi perto da praça",
                        raio = 50.0
                    ),
                    LocalDTO(
                        id = "2",
                        latitude = -23.550520,
                        longitude = -46.633308,
                        nome = "Chave Perdida",
                        descricao = "Perdi perto da praça",
                        raio = 100.0
                    )
                ),
                carregando = false
            ),
            onEditarLocal = {},
            onRemoverLocal = {},
        )
    }
}