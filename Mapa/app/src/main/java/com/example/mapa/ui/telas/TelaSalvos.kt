package com.example.mapa.ui.telas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.mapa.R
import com.example.mapa.data.remote.dto.Local
import com.example.mapa.models.LocalState
import com.example.mapa.ui.componentes.Animacao
import com.example.mapa.ui.componentes.DialogExcluir
import com.example.mapa.ui.componentes.FormLocal
import com.example.mapa.ui.componentes.Header
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

    // Feedback visual (Toasts) vindo do ViewModel
    LaunchedEffect(Unit) {
        localViewModel.mensagens.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    TelaSalvosContent(
        modifier = modifier,
        localState = uiState,
        onRemoverLocal = { localViewModel.removerLocal(it) },
        onEditarLocal = { localViewModel.editarLocal(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaSalvosContent(
    modifier: Modifier = Modifier,
    localState: LocalState,
    onEditarLocal: (Local) -> Unit,
    onRemoverLocal: (String) -> Unit,
) {
    // Estados de UI locais (controles de diálogo, edição, etc)
    var localSelecionado by rememberSaveable { mutableStateOf<Local?>(null) }
    var editando by rememberSaveable { mutableStateOf(false) }
    var mostrarDialogExcluir by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Diálogo de confirmação de exclusão
    DialogExcluir(
        visivel = mostrarDialogExcluir && localSelecionado != null,
        titulo = stringResource(R.string.excluir_local),
        mensagem = stringResource(
            R.string.tem_certeza_que_deseja_excluir_essa_acao_nao_pode_ser_desfeita,
            localSelecionado?.nome ?: ""
        ),
        onConfirmar = {
            localSelecionado?.let { onRemoverLocal(it.id) }
            mostrarDialogExcluir = false
            localSelecionado = null
        },
        onCancelar = {
            mostrarDialogExcluir = false
            localSelecionado = null
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

        if (localState.locaisUsuario.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Animacao(
                    animacao = R.raw.mapa_animado,
                    modifier = Modifier.size(200.dp)
                )

                Text(text = stringResource(R.string.nenhum_local_salvo))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                ) {
                itemsIndexed(
                    items = localState.locaisUsuario,
                    key = { _, local -> local.id }
                ) { index, item ->
                    val itemShape = when {
                        localState.locaisUsuario.size == 1 -> RoundedCornerShape(24.dp)
                        index == 0 -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        index == localState.locaisUsuario.lastIndex -> RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        else -> RectangleShape
                    }

                    LocalItem(
                        local = item,
                        onEditClick = {
                            localSelecionado = item
                            editando = true
                        },
                        onExcluirClick = {
                            localSelecionado = item
                            mostrarDialogExcluir = true
                        },
                        modifier = Modifier.clip(itemShape)
                    )
                }
            }
        }

        // BottomSheet de edição
        if (editando && localSelecionado != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    localSelecionado = null
                    editando = false
                },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                tonalElevation = 2.dp,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp, bottom = 8.dp),
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
                    localInicial = localSelecionado!!,
                    carregando = localState.carregando,
                    onRaioChange = {},
                    onSalvar = { local ->
                        onEditarLocal(local)
                        localSelecionado = null
                        editando = false
                    },
                    onFechar = {
                        localSelecionado = null
                        editando = false
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalItem(
    local: Local,
    onEditClick: () -> Unit,
    onExcluirClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier,
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
            Row {
                // Botão de edição com dica de uso
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                    tooltip = {
                        PlainTooltip {
                            Text(stringResource(R.string.editar))
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    IconButton(
                        onClick = onEditClick,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.editar)
                        )
                    }
                }

                // Botão de exclusão com dica de uso
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                    tooltip = {
                        PlainTooltip {
                            Text(stringResource(R.string.excluir))
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    IconButton(
                        onClick = onExcluirClick,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.excluir)
                        )
                    }
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
            localState = LocalState(
                locaisUsuario = listOf(
                    Local(
                        id = "1",
                        latitude = -23.550520,
                        longitude = -46.633308,
                        nome = "Chave Perdida",
                        descricao = "Perdi perto da praça",
                        raio = 50.0
                    ),
                    Local(
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