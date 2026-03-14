package com.example.mapa.ui.screen

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mapa.R
import com.example.mapa.data.remote.dto.LocationDTO
import com.example.mapa.data.remote.dto.UserDTO
import com.example.mapa.model.LocationUiState
import com.example.mapa.model.SheetUiState
import com.example.mapa.ui.components.DeleteDialog
import com.example.mapa.ui.components.LocationForm
import com.example.mapa.ui.components.LoadingOverlay
import com.example.mapa.ui.components.Header
import com.example.mapa.ui.components.LocationDetails
import com.example.mapa.ui.theme.MapaTheme
import com.example.mapa.util.ReqPerms
import com.example.mapa.viewmodels.LocationViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    onChat: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    user: UserDTO? = null,
    locationViewModel: LocationViewModel = koinViewModel()
) {
    val context = LocalContext.current

    // Variáveis de estado para o mapa
    val locationUiState by locationViewModel.uiState.collectAsStateWithLifecycle()
    val cameraPositionState = rememberCameraPositionState()
    var permsLocationOk by rememberSaveable { mutableStateOf(false) }

    // Solicita permissões quando o usuário entra na tela
    ReqPerms(
        onPermsChange = { permsLocationOk = it }
    )

    // Atualiza a posição do mapa quando as permissões são concedidas
    LaunchedEffect(permsLocationOk) {
        if (permsLocationOk) {
            @SuppressLint("MissingPermission")
            LocationServices.getFusedLocationProviderClient(context).lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(
                            LatLng(location.latitude, location.longitude),
                            15f
                        )
                    }
                }
        }
    }

    // Feedback visual (Toasts) vindo do ViewModel
    LaunchedEffect(Unit) {
        locationViewModel.channel.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    HomeScreenContent(
        modifier = modifier,
        user = user,
        locationUiState = locationUiState,
        cameraPositionState = cameraPositionState,
        permsLocation = permsLocationOk,
        onAddLocation = { locationViewModel.insertLocation(it) },
        onEditLocation = { locationViewModel.updateLocation(it) },
        onDeleteLocation = { locationViewModel.deleteLocation(it) },
        onChat = onChat
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    user: UserDTO?,
    locationUiState: LocationUiState,
    cameraPositionState: CameraPositionState,
    permsLocation: Boolean,
    onAddLocation: (LocationDTO) -> Unit,
    onEditLocation: (LocationDTO) -> Unit,
    onDeleteLocation: (String) -> Unit,
    onChat: (String, String) -> Unit
) {
    // Estados de UI (carregando, dialogs, etc)
    var loadingMap by remember { mutableStateOf(true) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    // Estado do BottomSheet (oculto, adicionando, visualizando, editando)
    var sheetUiState by rememberSaveable { mutableStateOf<SheetUiState>(SheetUiState.Hidden) }

    // Estado de visibilidade do BottomSheet
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    // Atualiza a visibilidade do BottomSheet quando o estado da sheet muda
    val sheetVisivel = sheetUiState !is SheetUiState.Hidden
    LaunchedEffect(sheetVisivel) {
        if (sheetVisivel) scaffoldState.bottomSheetState.partialExpand()
        else scaffoldState.bottomSheetState.hide()
    }

    // Atualiza as variáveis de estado para o BottomSheet quando ele é fechado
    LaunchedEffect(scaffoldState.bottomSheetState.currentValue) {
        if (scaffoldState.bottomSheetState.currentValue == SheetValue.Hidden) sheetUiState = SheetUiState.Hidden
    }

    // Local alvo para exclusão (visualização ou edição)
    val locationToDelete = when (val state = sheetUiState) {
        is SheetUiState.Viewing -> state.location
        is SheetUiState.Editing -> state.location
        else -> null
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
            sheetUiState = SheetUiState.Hidden
        },
        onCancel = { showDeleteDialog = false }
    )

    // Sheet para manipulação de locais
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 100.dp,
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetContentColor = MaterialTheme.colorScheme.onSurface,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetShadowElevation = 8.dp,
        sheetTonalElevation = 2.dp,
        modifier = modifier,
        sheetDragHandle = {
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
        sheetContent = {
            when (val state = sheetUiState) {
                SheetUiState.Hidden -> {
                    Box(modifier = Modifier.height(1.dp))
                }

                is SheetUiState.Adding -> {
                    LocationForm(
                        title = stringResource(R.string.adicionar_novo_local),
                        loading = locationUiState.loading,
                        initialLocation = LocationDTO(
                            uid = user?.uid ?: "",
                            latitude = state.latLng.latitude,
                            longitude = state.latLng.longitude,
                            radius = state.radius
                        ),
                        onRadiusChange = { novoRaio ->
                            sheetUiState = state.copy(radius = novoRaio)
                        },
                        onConfirm = { local ->
                            onAddLocation(local)
                            sheetUiState = SheetUiState.Hidden
                        },
                        onDismiss = {
                            sheetUiState = SheetUiState.Hidden
                        }
                    )
                }

                is SheetUiState.Viewing -> {
                    LocationDetails(
                        location = state.location,
                        userUid = user?.uid,
                        onDismiss = { sheetUiState = SheetUiState.Hidden },
                        onDelete = { showDeleteDialog = true },
                        onEdit = {
                            sheetUiState = SheetUiState.Editing(
                                location = state.location,
                                radius = state.location.radius
                            )
                        },
                        onChat = onChat
                    )
                }

                is SheetUiState.Editing -> {
                    LocationForm(
                        title = stringResource(R.string.editar_local),
                        initialLocation = state.location.copy(radius = state.radius),
                        loading = locationUiState.loading,
                        onRadiusChange = { novoRaio ->
                            sheetUiState = state.copy(radius = novoRaio)
                        },
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
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapLoaded = { loadingMap = false },
                properties = MapProperties(isMyLocationEnabled = permsLocation),
                onMapLongClick = {
                    if (!locationUiState.loading && !loadingMap) sheetUiState = SheetUiState.Adding(latLng = it)
                },
                contentPadding = if (sheetVisivel) innerPadding else PaddingValues()
            ) {
                // Renderiza locais existentes
                locationUiState.locations.forEach { local ->
                    val latLng = LatLng(local.latitude, local.longitude)
                    val iconColor = if (local.type == stringResource(R.string.perdido)) BitmapDescriptorFactory.HUE_RED else BitmapDescriptorFactory.HUE_GREEN
                    val circleColor = if (local.type == stringResource(R.string.perdido)) MaterialTheme.colorScheme.error.copy(0.5f) else MaterialTheme.colorScheme.primary.copy(0.5f)
                    val borderColor = if (local.type == stringResource(R.string.perdido)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

                    Marker(
                        state = MarkerState(latLng),
                        title = local.name,
                        snippet = local.description,
                        icon = BitmapDescriptorFactory.defaultMarker(iconColor),
                        onClick = {
                            sheetUiState = SheetUiState.Viewing(local)
                            false
                        }
                    )
                    Circle(
                        center = latLng,
                        radius = local.radius,
                        fillColor = circleColor,
                        strokeColor = borderColor,
                        strokeWidth = 2f
                    )
                }

                // Renderiza novo local (Rascunho)
                (sheetUiState as? SheetUiState.Adding)?.let { estado ->
                    Marker(
                        state = MarkerState(estado.latLng),
                        title = stringResource(R.string.local_marcado),
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    )
                    Circle(
                        center = estado.latLng,
                        radius = estado.radius,
                        fillColor = Color(0x330066FF),
                        strokeColor = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4f
                    )
                }
            }

            if (loadingMap) LoadingOverlay()

            // Renderiza o cabeçalho
            Header(
                title = stringResource(R.string.mapa),
                icon = R.drawable.logo,
                modifier = modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenContentPreview() {
    MapaTheme {
        HomeScreenContent(
            user = UserDTO(uid = "123", name = "Teste", email = "teste@email.com"),
            locationUiState = LocationUiState(
                locations = listOf(
                    LocationDTO(
                        id = "1",
                        latitude = -23.550520,
                        longitude = -46.633308,
                        name = "Chave Perdida",
                        description = "Perdi perto da praça",
                        radius = 50.0
                    )
                ),
                loading = false
            ),
            cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(LatLng(-23.55, -46.63), 15f)
            },
            permsLocation = true,
            onAddLocation = {},
            onEditLocation = {},
            onDeleteLocation = {},
            onChat = { _, _ -> }
        )
    }
}