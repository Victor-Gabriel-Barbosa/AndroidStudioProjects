package com.example.mapa.ui.telas

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.example.mapa.models.Local
import com.example.mapa.models.Usuario
import com.example.mapa.ui.componentes.OverlayCarregando
import com.example.mapa.ui.componentes.DialogExcluir
import com.example.mapa.ui.componentes.FormLocal
import com.example.mapa.ui.componentes.Header
import com.example.mapa.ui.componentes.InfoLocal
import com.example.mapa.ui.theme.MapaTheme
import com.example.mapa.viewmodels.LocalViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TelaHome(
    onChat: (String) -> Unit,
    modifier: Modifier = Modifier,
    usuario: Usuario? = null,
    localViewModel: LocalViewModel = koinViewModel()
) {
    val context = LocalContext.current

    // Variáveis de estado para o mapa
    val locaisMarcados by localViewModel.locais.collectAsStateWithLifecycle()
    val carregando by localViewModel.carregando.collectAsStateWithLifecycle()
    val cameraPositionState = rememberCameraPositionState()

    // Lista de estados das permissões de localização
    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Solicita permissões de localização ao iniciar a tela
    LaunchedEffect(Unit) {
        locationPermissionState.launchMultiplePermissionRequest()
    }

    // Atualiza a posição da câmera com a última localização conhecida
    LaunchedEffect(locationPermissionState.allPermissionsGranted) {
        if (locationPermissionState.allPermissionsGranted) {
            @SuppressLint("MissingPermission")
            LocationServices.getFusedLocationProviderClient(context).lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(
                        LatLng(location.latitude, location.longitude), 15f
                    )
                }
            }
        }
    }

    // Feedback visual (Toasts) vindo do ViewModel
    LaunchedEffect(Unit) {
        localViewModel.mensagens.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    Home(
        modifier = modifier,
        usuario = usuario,
        locaisMarcados = locaisMarcados,
        carregando = carregando,
        cameraPositionState = cameraPositionState,
        permissaoLocalizacao = locationPermissionState.allPermissionsGranted,
        onAdicionarLocal = { localViewModel.adicionarLocal(it) },
        onEditarLocal = { localViewModel.editarLocal(it) },
        onRemoverLocal = { localViewModel.removerLocal(it) },
        onChat = onChat
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    modifier: Modifier = Modifier,
    usuario: Usuario?,
    locaisMarcados: List<Local>,
    carregando: Boolean,
    cameraPositionState: CameraPositionState,
    permissaoLocalizacao: Boolean,
    onAdicionarLocal: (Local) -> Unit,
    onEditarLocal: (Local) -> Unit,
    onRemoverLocal: (String) -> Unit,
    onChat: (String) -> Unit
) {
    // Estados de UI (formulários, seleção, sheet)
    var mapaCarregando by remember { mutableStateOf(true) }
    var localMarcado by rememberSaveable { mutableStateOf<LatLng?>(null) }
    var localSelecionado by rememberSaveable { mutableStateOf<Local?>(null) }
    var raio by rememberSaveable { mutableDoubleStateOf(50.0) }
    var mostrarDialogExcluir by rememberSaveable { mutableStateOf(false) }
    var editando by rememberSaveable { mutableStateOf(false) }

    // Estado de visibilidade do BottomSheet
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    // Atualiza a visibilidade do BottomSheet quando um novo local é adicionado ou selecionado
    val sheetVisivel = localMarcado != null || localSelecionado != null
    LaunchedEffect(sheetVisivel) {
        if (sheetVisivel) scaffoldState.bottomSheetState.partialExpand()
        else scaffoldState.bottomSheetState.hide()
    }

    // Atualiza as variáveis de estado para o BottomSheet quando ele é fechado
    LaunchedEffect(scaffoldState.bottomSheetState.targetValue) {
        if (scaffoldState.bottomSheetState.targetValue == SheetValue.Hidden) {
            localMarcado = null
            localSelecionado = null
            editando = false
        }
    }

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
        onCancelar = { mostrarDialogExcluir = false }
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 100.dp,
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetContentColor = MaterialTheme.colorScheme.onSurface,
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetShadowElevation = 8.dp,
        sheetTonalElevation = 2.dp,
        sheetContent = {
            when {
                localMarcado != null -> {
                    FormLocal(
                        titulo = stringResource(R.string.adicionar_novo_local),
                        carregando = carregando,
                        localInicial = Local(
                            uid = usuario?.uid ?: "",
                            latitude = localMarcado!!.latitude,
                            longitude = localMarcado!!.longitude,
                            raio = raio
                        ),
                        onRaioChange = { raio = it },
                        onSalvar = { local ->
                            onAdicionarLocal(local)
                            localMarcado = null
                        },
                        onFechar = { localMarcado = null }
                    )
                }

                localSelecionado != null -> {
                    if (editando) {
                        FormLocal(
                            titulo = stringResource(R.string.editar_local),
                            localInicial = localSelecionado!!,
                            carregando = carregando,
                            onRaioChange = { raio = it },
                            onSalvar = { local ->
                                onEditarLocal(local)
                                editando = false
                                localSelecionado = null
                            },
                            onFechar = {
                                localSelecionado = null
                                editando = false
                            }
                        )
                    } else {
                        InfoLocal(
                            local = localSelecionado!!,
                            usuarioUid = usuario?.uid,
                            onFechar = { localSelecionado = null },
                            onExcluir = { mostrarDialogExcluir = true },
                            onEditar = {
                                raio = localSelecionado!!.raio
                                editando = true
                            },
                            onChat = onChat
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapLoaded = { mapaCarregando = false },
                properties = MapProperties(isMyLocationEnabled = permissaoLocalizacao),
                onMapLongClick = {
                    if (!carregando && !mapaCarregando) {
                        localMarcado = it
                        localSelecionado = null
                    }
                },
                contentPadding = if (sheetVisivel) innerPadding else PaddingValues()
            ) {
                // Renderiza locais existentes
                locaisMarcados.forEach { local ->
                    val posi = LatLng(local.latitude, local.longitude)
                    val corIcone = if (local.tipo == stringResource(R.string.perdido)) BitmapDescriptorFactory.HUE_RED else BitmapDescriptorFactory.HUE_GREEN
                    val corCirculo = if (local.tipo == stringResource(R.string.perdido)) Color(0x22FF0000) else Color(0x220066FF)
                    val corBorda = if (local.tipo == stringResource(R.string.perdido)) Color.Red else Color.Blue

                    Marker(
                        state = MarkerState(posi),
                        title = local.nome,
                        snippet = local.descricao,
                        icon = BitmapDescriptorFactory.defaultMarker(corIcone),
                        onClick = {
                            localSelecionado = local
                            localMarcado = null
                            false
                        }
                    )
                    Circle(
                        center = posi,
                        radius = local.raio,
                        fillColor = corCirculo,
                        strokeColor = corBorda,
                        strokeWidth = 2f
                    )
                }

                // Renderiza novo local (Rascunho)
                localMarcado?.let {
                    Marker(
                        state = MarkerState(it),
                        title = stringResource(R.string.local_marcado),
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    )
                    Circle(
                        center = it,
                        radius = raio,
                        fillColor = Color(0x330066FF),
                        strokeColor = Color.Blue,
                        strokeWidth = 4f
                    )
                }
            }

            if (mapaCarregando) OverlayCarregando()

            // Renderiza o cabeçalho
            Header(
                titulo = stringResource(R.string.inicio),
                icone = R.drawable.logo,
                modifier = modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomePreview() {
    MapaTheme {
        Home(
            usuario = Usuario(uid = "123", nome = "Teste", email = "teste@email.com"),
            locaisMarcados = listOf(
                Local(
                    id = "1",
                    latitude = -23.550520,
                    longitude = -46.633308,
                    nome = "Chave Perdida",
                    descricao = "Perdi perto da praça",
                    raio = 50.0
                )
            ),
            carregando = false,
            cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(LatLng(-23.55, -46.63), 15f)
            },
            permissaoLocalizacao = true,
            onAdicionarLocal = {},
            onEditarLocal = {},
            onRemoverLocal = {},
            onChat = {}
        )
    }
}