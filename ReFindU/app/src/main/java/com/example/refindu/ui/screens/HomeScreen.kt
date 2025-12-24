package com.example.refindu.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.refindu.R
import com.example.refindu.models.Local
import com.example.refindu.viewmodels.AuthViewModel
import com.example.refindu.viewmodels.HomeViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    val context = LocalContext.current

    // Observáveis do ViewModel
    val isLoading by homeViewModel.isLoading.collectAsState()
    val locals by homeViewModel.locals.collectAsState()
    val userUid = remember { authViewModel.currenUserUid }

    // Estados de UI e Mapa
    val cameraPositionState = rememberCameraPositionState()
    var lossLocal by rememberSaveable { mutableStateOf<LatLng?>(null) } // Ponto temporário (novo cadastro)
    var radius by rememberSaveable { mutableDoubleStateOf(200.0) }

    // Estados de Seleção e Edição
    var selectedLocal by rememberSaveable { mutableStateOf<Local?>(null) }
    var isEditing by rememberSaveable { mutableStateOf(false) }

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

    // Reseta modo de edição ao limpar seleção
    LaunchedEffect(lossLocal, selectedLocal) {
        if (selectedLocal == null) isEditing = false
    }

    // Feedback visual (Toasts) vindo do ViewModel
    LaunchedEffect(Unit) {
        homeViewModel.userMessage.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetContentColor = MaterialTheme.colorScheme.onSurface,
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetShadowElevation = 8.dp,
        sheetPeekHeight = if (isSheetVisible) 120.dp else 0.dp,
        sheetDragHandle = null,
        sheetContent = {
            // Lógica de conteúdo da Sheet: Cadastro vs Edição vs Detalhes
            if (lossLocal != null) {
                LocalForm(
                    title = stringResource(R.string.adicionar_novo_local),
                    initialRadius = radius,
                    isSaving = isLoading,
                    onRadiusChange = { radius = it },
                    onCancel = { lossLocal = null },
                    onSave = { nome, tipo, img ->
                        homeViewModel.addLocal(
                            Local(
                                latitude = lossLocal!!.latitude,
                                longitude = lossLocal!!.longitude,
                                radius = radius,
                                name = nome,
                                category = tipo,
                                imageUri = img
                            )
                        )
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
                        isSaving = isLoading,
                        onRadiusChange = { radius = it },
                        onCancel = { isEditing = false },
                        onSave = { nome, tipo, img ->
                            val updatedLocal = selectedLocal!!.copy(
                                name = nome,
                                category = tipo,
                                radius = radius,
                                imageUri = img
                            )
                            homeViewModel.editLocal(updatedLocal)
                            isEditing = false
                            selectedLocal = null
                        },
                        onClose = {
                            selectedLocal = null
                            isEditing = false
                        },
                        initialName = selectedLocal!!.name,
                        initialCategory = selectedLocal!!.category,
                        initialImageUrl = selectedLocal!!.imageUrl
                    )
                } else {
                    DetailsLocal(
                        local = selectedLocal!!,
                        userUid,
                        onClose = { selectedLocal = null },
                        onDelete = {
                            homeViewModel.removeLocal(it)
                            selectedLocal = null
                        },
                        onEdit = {
                            radius = selectedLocal!!.radius
                            isEditing = true
                        }
                    )
                }
            } else Spacer(modifier = Modifier.height(1.dp))
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = permissionState.allPermissionsGranted),
                uiSettings = MapUiSettings(myLocationButtonEnabled = true, zoomControlsEnabled = false),
                onMapClick = {
                    // Clique no mapa inicia fluxo de novo cadastro
                    if (!isLoading) {
                        lossLocal = it
                        selectedLocal = null
                    }
                },
                contentPadding = PaddingValues(top = 24.dp)
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
                            true
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

            // Header fixo superior
            Surface(
                modifier = modifier.align(Alignment.TopCenter),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Text(
                    text = stringResource(R.string.tela_inicial),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
            }
        }
    }
}