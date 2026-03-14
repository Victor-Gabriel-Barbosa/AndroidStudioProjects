package com.example.mapa.ui.screen

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
import com.example.mapa.data.remote.dto.LocationDTO
import com.example.mapa.model.SheetUiState
import com.example.mapa.model.LocationUiState
import com.example.mapa.ui.components.LottieAnimation
import com.example.mapa.ui.components.SearchBar
import com.example.mapa.ui.components.DeleteDialog
import com.example.mapa.ui.components.LocationForm
import com.example.mapa.ui.components.LoadingOverlay
import com.example.mapa.ui.components.Header
import com.example.mapa.ui.theme.MapaTheme
import com.example.mapa.viewmodels.LocationViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    modifier: Modifier = Modifier,
    locationViewModel: LocationViewModel = koinViewModel()
) {
    val context = LocalContext.current

    // Observáveis do ViewModel
    val uiState by locationViewModel.uiState.collectAsStateWithLifecycle()

    // Feedback visual (eventos) vindo do ViewModel
    LaunchedEffect(Unit) {
        locationViewModel.channel.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    SavedScreenContent(
        modifier = modifier,
        locationUiState = uiState,
        onDeleteLocation = { locationViewModel.deleteLocation(it) },
        onEditLocation = { locationViewModel.updateLocation(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreenContent(
    modifier: Modifier = Modifier,
    locationUiState: LocationUiState,
    onEditLocation: (LocationDTO) -> Unit,
    onDeleteLocation: (String) -> Unit,
) {
    // Estados de UI locais (controles de diálogo, edição, etc)
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var locationToDelete by rememberSaveable { mutableStateOf<LocationDTO?>(null) }

    // Estado do BottomSheet (oculto, editando)
    var sheetUiState by rememberSaveable { mutableStateOf<SheetUiState>(SheetUiState.Hidden) }

    // Estado visual do ModalBottomSheet
    val modalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Estado do texto barra de pesquisa
    var search by rememberSaveable { mutableStateOf("") }

    // Filtra os locais com base na pesquisa
    val locaisFiltrados = remember(locationUiState.locationsUser, search) {
        if (search.isBlank()) locationUiState.locationsUser
        else {
            locationUiState.locationsUser.filter { item ->
                item.name.contains(search, ignoreCase = true) ||
                        item.description.contains(search, ignoreCase = true)
            }
        }
    }

    // Diálogo de confirmação de exclusão
    DeleteDialog(
        visible = showDeleteDialog && locationToDelete != null,
        title = stringResource(R.string.excluir_local),
        msg = stringResource(
            R.string.tem_certeza_que_deseja_excluir_essa_acao_nao_pode_ser_desfeita,
            locationToDelete?.name ?: ""
        ),
        onConfirm = {
            locationToDelete?.let { onDeleteLocation(it.id) }
            showDeleteDialog = false
            locationToDelete = null
        },
        onCancel = {
            showDeleteDialog = false
            locationToDelete = null
        }
    )

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Header(
            title = stringResource(R.string.salvos),
            icon = R.drawable.logo,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        SearchBar(
            onSearch = { search = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        )

        // Gerenciamento dos estados de UI
        when {
            // Estado de carregamento
            locationUiState.loading -> LoadingOverlay()

            // Estado de erro
            locationUiState.error != null -> {
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
                            locationUiState.error
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Estado de lista vazia (Sem nenhum local salvo)
            locaisFiltrados.isEmpty() && search.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LottieAnimation(
                        animation = R.raw.mapa_animado,
                        modifier = Modifier.size(200.dp)
                    )

                    Text(text = stringResource(R.string.nenhum_local_salvo))
                }
            }

            // Se a busca não retornou nada (mas existem locais salvos)
            locaisFiltrados.isEmpty() && search.isNotEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LottieAnimation(
                        animation = R.raw.mapa_animado,
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
                                sheetUiState = SheetUiState.Editing(item)
                            },
                            onExcluir = {
                                locationToDelete = item
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }

        // BottomSheet de edição
        val estado = sheetUiState as? SheetUiState.Editing
        if (estado != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    sheetUiState = SheetUiState.Hidden
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
                LocationForm(
                    title = stringResource(R.string.editar_local),
                    initialLocation = estado.location,
                    loading = locationUiState.loading,
                    onRadiusChange = {},
                    onConfirm = { local ->
                        onEditLocation(local)
                        sheetUiState = SheetUiState.Hidden
                    },
                    onDismiss = {
                        sheetUiState = SheetUiState.Hidden
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalItem(
    local: LocationDTO,
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
                            contentDescription = local.name,
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
                text = local.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },

        supportingContent = {
            Text(
                text = "${local.description} • ${local.radius.toInt()}m",
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
fun SavedScreenContentPreview() {
    MapaTheme {
        SavedScreenContent(
            locationUiState = LocationUiState(
                locationsUser = listOf(
                    LocationDTO(
                        id = "1",
                        latitude = -23.550520,
                        longitude = -46.633308,
                        name = "Chave Perdida",
                        description = "Perdi perto da praça",
                        radius = 50.0
                    ),
                    LocationDTO(
                        id = "2",
                        latitude = -23.550520,
                        longitude = -46.633308,
                        name = "Chave Perdida",
                        description = "Perdi perto da praça",
                        radius = 100.0
                    )
                ),
                loading = false
            ),
            onEditLocation = {},
            onDeleteLocation = {},
        )
    }
}