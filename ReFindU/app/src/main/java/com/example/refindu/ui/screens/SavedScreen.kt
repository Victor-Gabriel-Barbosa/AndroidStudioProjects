package com.example.refindu.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.refindu.R
import com.example.refindu.models.Local
import com.example.refindu.ui.components.LocalForm
import com.example.refindu.viewmodels.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = koinViewModel()
) {
    val context = LocalContext.current

    // Observáveis do ViewModel
    val isLoading by homeViewModel.isLoading.collectAsState()
    val userLocals by homeViewModel.userLocals.collectAsState()

    // Feedback visual (Toasts) vindo do ViewModel
    LaunchedEffect(Unit) {
        homeViewModel.userMessage.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    SavedScreenContent(
        modifier = modifier,
        isLoading = isLoading,
        userLocals = userLocals,
        onRemoveLocal = { id -> homeViewModel.removeLocal(id) },
        onEditLocal = { local -> homeViewModel.editLocal(local) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreenContent(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    userLocals: List<Local>,
    onRemoveLocal: (String) -> Unit,
    onEditLocal: (Local) -> Unit
) {
    // Estados de UI locais (controles de diálogo, edição, etc)
    var selectedLocal by rememberSaveable { mutableStateOf<Local?>(null) }
    var isEditing by rememberSaveable { mutableStateOf(false) }
    var tempRadius by rememberSaveable { mutableDoubleStateOf(200.0) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = modifier
            .padding(start = 8.dp, end = 8.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(32.dp).clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.o_que_perdi),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        // Dialog de exclusão
        if (showDeleteDialog && selectedLocal != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(text = stringResource(R.string.excluir_local)) },
                text = {
                    Text(stringResource(R.string.tem_certeza_que_deseja_excluir_essa_acao_nao_pode_ser_desfeita, selectedLocal?.name ?: ""))
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedLocal?.let { loc -> onRemoveLocal(loc.id) }
                            showDeleteDialog = false
                            selectedLocal = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text(stringResource(R.string.sim_excluir)) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.cancelar)) }
                }
            )
        }

        // Lista ou Vazio
        if (userLocals.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = stringResource(R.string.voce_nao_tem_nada_perdido))
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(userLocals) { local ->
                    LocalItemCard(
                        local = local,
                        onEditClick = {
                            tempRadius = local.radius
                            selectedLocal = local
                            isEditing = true
                        },
                        onDeleteClick = {
                            selectedLocal = local
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }

    // BottomSheet de Edição
    if (isEditing && selectedLocal != null) {
        ModalBottomSheet(
            onDismissRequest = {
                selectedLocal = null
                isEditing = false
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            LocalForm(
                title = stringResource(R.string.editar_local),
                initialRadius = tempRadius,
                isLoading = isLoading,
                onRadiusChange = { tempRadius = it },
                initialName = selectedLocal!!.name,
                initialCategory = selectedLocal!!.category,
                initialImgUrl = selectedLocal!!.imgUrl,
                onCancel = {
                    selectedLocal = null
                    isEditing = false
                },
                onSave = { name, category, img ->
                    val updatedLocal = selectedLocal!!.copy(
                        name = name,
                        category = category,
                        radius = tempRadius,
                        imgUri = img
                    )
                    onEditLocal(updatedLocal)
                    selectedLocal = null
                    isEditing = false
                },
                onClose = {
                    selectedLocal = null
                    isEditing = false
                }
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun LocalItemCard(
    local: Local,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
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
                    if (local.imgUrl != null) {
                        AsyncImage(
                            model = local.imgUrl,
                            contentDescription = local.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = local.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${local.category} • ${local.radius.toInt()}m",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Deletar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SavedScreenPreview() {
    val fakeLocals = listOf(
        Local(id = "1", name = "Chaves de Casa", category = "Pessoal", radius = 100.0, latitude = 0.0, longitude = 0.0),
        Local(id = "2", name = "Carteira", category = "Documentos", radius = 50.0, latitude = 0.0, longitude = 0.0)
    )

    MaterialTheme {
        SavedScreenContent(
            isLoading = false,
            userLocals = fakeLocals,
            onRemoveLocal = {},
            onEditLocal = {}
        )
    }
}