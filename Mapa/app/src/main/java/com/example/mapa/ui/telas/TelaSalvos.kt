package com.example.mapa.ui.telas

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.mapa.R
import com.example.mapa.models.Local
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
    val carregando by localViewModel.carregando.collectAsStateWithLifecycle()
    val locaisUsuarioAtivo by localViewModel.locaisUsuario.collectAsStateWithLifecycle()

    // Feedback visual (Toasts) vindo do ViewModel
    LaunchedEffect(Unit) {
        localViewModel.mensagens.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Salvos(
        modifier = modifier,
        carregando = carregando,
        locaisUsuarioAtivo = locaisUsuarioAtivo,
        onRemoverLocal = { localViewModel.removerLocal(it) },
        onEditarLocal = { localViewModel.editarLocal(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Salvos(
    modifier: Modifier = Modifier,
    carregando: Boolean,
    locaisUsuarioAtivo: List<Local>,
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

        if (locaisUsuarioAtivo.isEmpty()) {
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
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(
                    items = locaisUsuarioAtivo,
                    key = { it.id }
                ) { local ->
                    LocalItemCard(
                        local = local,
                        onEditClick = {
                            localSelecionado = local
                            editando = true
                        },
                        onExcluirClick = {
                            localSelecionado = local
                            mostrarDialogExcluir = true
                        }
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
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                tonalElevation = 2.dp,
            ) {
                FormLocal(
                    titulo = stringResource(R.string.editar_local),
                    localInicial = localSelecionado!!,
                    carregando = carregando,
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

@Composable
fun LocalItemCard(
    local: Local,
    onEditClick: () -> Unit,
    onExcluirClick: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
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
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = local.nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${local.descricao} • ${local.raio.toInt()}m",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Botões de edição e exclusão
            Row {
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
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SalvosPreview() {
    MapaTheme {
        Salvos(
            locaisUsuarioAtivo = listOf(
                Local(
                    id = "1",
                    latitude = -23.550520,
                    longitude = -46.633308,
                    nome = "Chave Perdida",
                    descricao = "Perdi perto da praça",
                    raio = 50.0
                )
            ),
            onEditarLocal = {},
            onRemoverLocal = {},
            carregando = false
        )
    }
}