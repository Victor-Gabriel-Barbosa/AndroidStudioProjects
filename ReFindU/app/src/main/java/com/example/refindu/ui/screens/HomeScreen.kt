package com.example.refindu.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.refindu.R
import com.example.refindu.models.Local
import com.example.refindu.ui.components.InfoLocal
import com.example.refindu.ui.components.LocalForm
import com.example.refindu.viewmodels.AuthViewModel
import com.example.refindu.viewmodels.HomeViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(
    onChat: (String) -> Unit,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    val context = LocalContext.current

    // Observáveis do ViewModel
    val isLoading by homeViewModel.isLoading.collectAsStateWithLifecycle()
    val locals by homeViewModel.locals.collectAsStateWithLifecycle()
    val userUid by authViewModel.currentUserUid.collectAsStateWithLifecycle()

    // Feedback visual (Toasts) vindo do ViewModel
    LaunchedEffect(Unit) {
        homeViewModel.userMessage.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // Chama o conteúdo da tela passando os estados e callbacks
    HomeScreenContent(
        modifier = modifier,
        isLoading = isLoading,
        locals = locals,
        userUid = userUid,
        onAddLocal = { local -> homeViewModel.addLocal(local) },
        onEditLocal = { local -> homeViewModel.editLocal(local) },
        onRemoveLocal = { id -> homeViewModel.removeLocal(id) },
        onChat = onChat
    )
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    locals: List<Local>,
    userUid: String?,
    onAddLocal: (Local) -> Unit,
    onEditLocal: (Local) -> Unit,
    onRemoveLocal: (String) -> Unit,
    onChat: (String) -> Unit
) {
    val context = LocalContext.current

    // Estados de UI e Mapa
    val cameraPositionState = rememberCameraPositionState()
    var lossLocal by rememberSaveable { mutableStateOf<LatLng?>(null) } // Ponto temporário (novo cadastro)
    var radius by rememberSaveable { mutableDoubleStateOf(200.0) }

    // Estados de Seleção e Edição
    var selectedLocal by rememberSaveable { mutableStateOf<Local?>(null) }
    var isEditing by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    // Configuração do BottomSheet
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true
        )
    )

    // Gerenciamento de Permissões
    val permissionState = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    )

    LaunchedEffect(Unit) { permissionState.launchMultiplePermissionRequest() }

    // Move a câmera para a posição do usuário ao conceder permissão
    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            LocationServices.getFusedLocationProviderClient(context).lastLocation.addOnSuccessListener {
                if (it != null) cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(it.latitude, it.longitude), 15f)
            }
        }
    }

    // Controle de visibilidade da BottomSheet
    val isSheetVisible = lossLocal != null || selectedLocal != null

    LaunchedEffect(isSheetVisible) {
        if (isSheetVisible) scaffoldState.bottomSheetState.expand()
        else scaffoldState.bottomSheetState.partialExpand()
    }

    // Reseta modo de edição ao limpar seleção
    LaunchedEffect(lossLocal, selectedLocal) {
        if (selectedLocal == null) isEditing = false
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetContentColor = MaterialTheme.colorScheme.onSurface,
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetShadowElevation = 8.dp,
        sheetPeekHeight = if (isSheetVisible) 100.dp else 0.dp,
        sheetContent = {
            // Lógica de conteúdo da Sheet: Cadastro vs Edição vs Detalhes
            if (lossLocal != null) {
                LocalForm(
                    title = stringResource(R.string.adicionar_novo_local),
                    initialRadius = radius,
                    isLoading = isLoading,
                    onRadiusChange = { radius = it },
                    onCancel = { lossLocal = null },
                    onSave = { name, category, img ->
                        onAddLocal(
                            Local(
                                latitude = lossLocal!!.latitude,
                                longitude = lossLocal!!.longitude,
                                radius = radius,
                                name = name,
                                category = category,
                                imgUri = img
                            )
                        )
                        lossLocal = null
                        selectedLocal = null
                    },
                    onClose = {
                        lossLocal = null
                        selectedLocal = null
                    }
                )
            } else if (selectedLocal != null) {
                if (isEditing) {
                    LocalForm(
                        title = stringResource(R.string.editar_novo_local),
                        initialRadius = radius,
                        isLoading = isLoading,
                        onRadiusChange = { radius = it },
                        onCancel = { isEditing = false },
                        onSave = { name, category, img ->
                            val updatedLocal = selectedLocal!!.copy(
                                name = name,
                                category = category,
                                radius = radius,
                                imgUri = img
                            )
                            onEditLocal(updatedLocal)
                            isEditing = false
                            selectedLocal = null
                        },
                        onClose = {
                            selectedLocal = null
                            isEditing = false
                        },
                        initialName = selectedLocal!!.name,
                        initialCategory = selectedLocal!!.category,
                        initialImgUrl = selectedLocal!!.imgUrl
                    )
                } else {
                    InfoLocal(
                        local = selectedLocal!!,
                        userUid = userUid,
                        onClose = {
                            selectedLocal = null
                        },
                        onDelete = {
                            showDeleteDialog = true
                        },
                        onEdit = {
                            radius = selectedLocal!!.radius
                            isEditing = true
                        },
                        onChat = onChat
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(1.dp))
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = permissionState.allPermissionsGranted),
                onMapClick = {
                    // Clique no mapa inicia fluxo de novo cadastro
                    if (!isLoading) {
                        lossLocal = it
                        selectedLocal = null
                    }
                },
                contentPadding = PaddingValues(
                    top = 28.dp,
                    bottom = if (isSheetVisible) 100.dp else 0.dp
                )
            ) {
                // Renderização de locais existentes (Firestore)
                locals.forEach { local ->
                    val pos = LatLng(local.latitude, local.longitude)
                    Marker(
                        state = MarkerState(pos),
                        title = local.name,
                        snippet = "${local.category} - ${local.radius.toInt()}m",
                        onClick = {
                            selectedLocal = local
                            lossLocal = null
                            false
                        }
                    )
                    Circle(
                        center = pos,
                        radius = local.radius,
                        fillColor = Color(0x22FF0000),
                        strokeColor = Color.Red,
                        strokeWidth = 2f
                    )
                }
                // Renderização do marcador temporário (Novo Local)
                lossLocal?.let { local ->
                    Marker(
                        state = MarkerState(local),
                        title = stringResource(R.string.novo_local),
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    )
                    Circle(
                        center = local,
                        radius = radius,
                        fillColor = Color(0x330066FF),
                        strokeColor = Color.Blue,
                        strokeWidth = 4f
                    )
                }
            }

            // Dialog de confirmação de exclusão
            if (showDeleteDialog && selectedLocal != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = {
                        Text(text = stringResource(R.string.excluir_local))
                    },
                    text = {
                        Text(
                            stringResource(
                                R.string.tem_certeza_que_deseja_excluir_essa_acao_nao_pode_ser_desfeita,
                                selectedLocal?.name ?: ""
                            ))
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                // Ação real de deletar via callback
                                selectedLocal?.let { loc -> onRemoveLocal(loc.id) }
                                showDeleteDialog = false
                                selectedLocal = null
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(stringResource(R.string.sim_excluir))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDeleteDialog = false }
                        ) {
                            Text(stringResource(R.string.cancelar))
                        }
                    }
                )
            }

            // Header fixo superior
            Surface(
                modifier = modifier.align(Alignment.TopCenter),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = null,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}