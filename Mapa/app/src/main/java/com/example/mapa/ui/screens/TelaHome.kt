package com.example.mapa.ui.screens

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import com.example.mapa.data.remote.dto.LocalDTO
import com.example.mapa.data.remote.dto.UsuarioDTO
import com.example.mapa.model.SheetState
import com.example.mapa.model.LocalUiState
import com.example.mapa.service.GeofenceHelper
import com.example.mapa.ui.components.DialogExcluir
import com.example.mapa.ui.components.FormLocal
import com.example.mapa.ui.components.Header
import com.example.mapa.ui.components.InfoLocal
import com.example.mapa.ui.components.OverlayCarregando
import com.example.mapa.ui.theme.MapaTheme
import com.example.mapa.utils.SolicitarPermissoes
import com.example.mapa.viewmodels.LocalViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
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

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TelaHome(
    onChat: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    usuario: UsuarioDTO? = null,
    localViewModel: LocalViewModel = koinViewModel()
) {
    val context = LocalContext.current

    // Variáveis de estado para o mapa
    val localUiState by localViewModel.uiState.collectAsStateWithLifecycle()
    val cameraPositionState = rememberCameraPositionState()
    val geofenceHelper = remember { GeofenceHelper(context) }
    var perms1PlanoOk by rememberSaveable { mutableStateOf(false) }
    var perms2PlanoOk by rememberSaveable { mutableStateOf(false) }

    // Solicita permissões quando o usuário entra na tela
    SolicitarPermissoes(
        onPermsChange = { permsLoc1Plano, permsLoc2Plano ->
            perms1PlanoOk = permsLoc1Plano
            perms2PlanoOk = permsLoc2Plano
        }
    )

    // Atualiza a posição do mapa quando as permissões são concedidas
    LaunchedEffect(perms1PlanoOk) {
        if (perms1PlanoOk) {
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

    // Registra geofences quando as permissões são concedidas
    LaunchedEffect(
        perms1PlanoOk,
        perms2PlanoOk,
        localUiState.locais
    ) {
        val podeRegistrarGeofence = perms1PlanoOk && perms2PlanoOk

        if (!podeRegistrarGeofence) {
            Log.d("GeofenceHelper", "Aguardando permissões para registrar geofences")
            return@LaunchedEffect
        }

        if (localUiState.locais.isEmpty()) {
            Log.d("GeofenceHelper", "Lista vazia, removendo geofences")
            geofenceHelper.removerTodas()
            return@LaunchedEffect
        }

        geofenceHelper.registrarLocaisPerdidos(localUiState.locais)
    }

    // Feedback visual (Toasts) vindo do ViewModel
    LaunchedEffect(Unit) {
        localViewModel.canal.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    TelaHomeContent(
        modifier = modifier,
        usuario = usuario,
        localUiState = localUiState,
        cameraPositionState = cameraPositionState,
        permsLocalizacao = perms1PlanoOk,
        onAdicionarLocal = { localViewModel.adicionarLocal(it) },
        onEditarLocal = { localViewModel.editarLocal(it) },
        onRemoverLocal = { localViewModel.removerLocal(it) },
        onChat = onChat
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaHomeContent(
    modifier: Modifier = Modifier,
    usuario: UsuarioDTO?,
    localUiState: LocalUiState,
    cameraPositionState: CameraPositionState,
    permsLocalizacao: Boolean,
    onAdicionarLocal: (LocalDTO) -> Unit,
    onEditarLocal: (LocalDTO) -> Unit,
    onRemoverLocal: (String) -> Unit,
    onChat: (String, String) -> Unit
) {
    // Estados de UI (formulários, seleção, sheet)
    var mapaCarregando by remember { mutableStateOf(true) }
    var mostrarDialogExcluir by rememberSaveable { mutableStateOf(false) }

    // Estado do BottomSheet (oculto, adicionando, visualizando, editando)
    var sheetState by rememberSaveable { mutableStateOf<SheetState>(SheetState.Hidden) }

    // Estado de visibilidade do BottomSheet
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    // Atualiza a visibilidade do BottomSheet quando o estado da sheet muda
    val sheetVisivel = sheetState !is SheetState.Hidden
    LaunchedEffect(sheetVisivel) {
        if (sheetVisivel) scaffoldState.bottomSheetState.partialExpand()
        else scaffoldState.bottomSheetState.hide()
    }

    // Atualiza as variáveis de estado para o BottomSheet quando ele é fechado
    LaunchedEffect(scaffoldState.bottomSheetState.currentValue) {
        if (scaffoldState.bottomSheetState.currentValue == SheetValue.Hidden) sheetState = SheetState.Hidden
    }

    // Local alvo para exclusão (visualização ou edição)
    val localParaExcluir = when (val estado = sheetState) {
        is SheetState.Visualizando -> estado.local
        is SheetState.Editando -> estado.local
        else -> null
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
            sheetState = SheetState.Hidden
        },
        onCancelar = { mostrarDialogExcluir = false }
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
            when (val estado = sheetState) {
                SheetState.Hidden -> {
                    Box(modifier = Modifier.height(1.dp))
                }

                is SheetState.Adicionando -> {
                    FormLocal(
                        titulo = stringResource(R.string.adicionar_novo_local),
                        carregando = localUiState.carregando,
                        localInicial = LocalDTO(
                            uid = usuario?.uid ?: "",
                            latitude = estado.posicao.latitude,
                            longitude = estado.posicao.longitude,
                            raio = estado.raio
                        ),
                        onRaioChange = { novoRaio ->
                            sheetState = estado.copy(raio = novoRaio)
                        },
                        onSalvar = { local ->
                            onAdicionarLocal(local)
                            sheetState = SheetState.Hidden
                        },
                        onFechar = {
                            sheetState = SheetState.Hidden
                        }
                    )
                }

                is SheetState.Visualizando -> {
                    InfoLocal(
                        local = estado.local,
                        usuarioUid = usuario?.uid,
                        onFechar = { sheetState = SheetState.Hidden },
                        onExcluir = { mostrarDialogExcluir = true },
                        onEditar = {
                            sheetState = SheetState.Editando(
                                local = estado.local,
                                raio = estado.local.raio
                            )
                        },
                        onChat = onChat
                    )
                }

                is SheetState.Editando -> {
                    FormLocal(
                        titulo = stringResource(R.string.editar_local),
                        localInicial = estado.local.copy(raio = estado.raio),
                        carregando = localUiState.carregando,
                        onRaioChange = { novoRaio ->
                            sheetState = estado.copy(raio = novoRaio)
                        },
                        onSalvar = { local ->
                            onEditarLocal(local)
                            sheetState = SheetState.Hidden
                        },
                        onFechar = {
                            sheetState = SheetState.Hidden
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
                onMapLoaded = { mapaCarregando = false },
                properties = MapProperties(isMyLocationEnabled = permsLocalizacao),
                onMapLongClick = {
                    if (!localUiState.carregando && !mapaCarregando) sheetState = SheetState.Adicionando(posicao = it)
                },
                contentPadding = if (sheetVisivel) innerPadding else PaddingValues()
            ) {
                // Renderiza locais existentes
                localUiState.locais.forEach { local ->
                    val posi = LatLng(local.latitude, local.longitude)
                    val corIcone = if (local.tipo == stringResource(R.string.perdido)) BitmapDescriptorFactory.HUE_RED else BitmapDescriptorFactory.HUE_GREEN
                    val corCirculo = if (local.tipo == stringResource(R.string.perdido)) MaterialTheme.colorScheme.error.copy(0.5f) else MaterialTheme.colorScheme.primary.copy(0.5f)
                    val corBorda = if (local.tipo == stringResource(R.string.perdido)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

                    Marker(
                        state = MarkerState(posi),
                        title = local.nome,
                        snippet = local.descricao,
                        icon = BitmapDescriptorFactory.defaultMarker(corIcone),
                        onClick = {
                            sheetState = SheetState.Visualizando(local)
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
                val adicionando = sheetState as? SheetState.Adicionando
                adicionando?.let { estado ->
                    Marker(
                        state = MarkerState(estado.posicao),
                        title = stringResource(R.string.local_marcado),
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    )
                    Circle(
                        center = estado.posicao,
                        radius = estado.raio,
                        fillColor = Color(0x330066FF),
                        strokeColor = MaterialTheme.colorScheme.primary,
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
fun TelaHomeContentPreview() {
    MapaTheme {
        TelaHomeContent(
            usuario = UsuarioDTO(uid = "123", nome = "Teste", email = "teste@email.com"),
            localUiState = LocalUiState(
                locais = listOf(
                    LocalDTO(
                        id = "1",
                        latitude = -23.550520,
                        longitude = -46.633308,
                        nome = "Chave Perdida",
                        descricao = "Perdi perto da praça",
                        raio = 50.0
                    )
                ),
                carregando = false
            ),
            cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(LatLng(-23.55, -46.63), 15f)
            },
            permsLocalizacao = true,
            onAdicionarLocal = {},
            onEditarLocal = {},
            onRemoverLocal = {},
            onChat = { _, _ -> }
        )
    }
}