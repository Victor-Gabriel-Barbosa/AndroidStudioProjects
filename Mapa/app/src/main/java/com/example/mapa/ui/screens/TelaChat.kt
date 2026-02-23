package com.example.mapa.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mapa.R
import com.example.mapa.data.remote.dto.MensagemDTO
import com.example.mapa.data.remote.dto.UsuarioDTO
import com.example.mapa.model.ChatUiState
import com.example.mapa.ui.components.AnimacaoCarregando
import com.example.mapa.ui.components.AvatarImg
import com.example.mapa.ui.components.BolhaMsg
import com.example.mapa.ui.components.CarrosselImgs
import com.example.mapa.ui.components.DialogAvaliar
import com.example.mapa.ui.components.OverlayCarregando
import com.example.mapa.ui.theme.MapaTheme
import com.example.mapa.utils.criarUriParaFoto
import com.example.mapa.viewmodels.ChatViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaChat(
    uid: String,
    localId: String,
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel = koinViewModel()
) {
    // Inicializa o ViewModel com o UID do contato
    LaunchedEffect(uid, localId) {
        chatViewModel.inicializar(uid, localId)
    }

    // Feedback visual (eventos) vindo do ViewModel
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        chatViewModel.canal.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    // Coleta o estado do ViewModel
    val chatUiState by chatViewModel.uiState.collectAsStateWithLifecycle()

    TelaChatContent(
        chatUiState = chatUiState,
        onVoltar = onVoltar,
        onEnviarMsg = chatViewModel::enviarMsg,
        onEditarMsg = chatViewModel::editarMsg,
        onExcluirMsg = chatViewModel::excluirMsg,
        onConfirmarEntrega = chatViewModel::avaliarUsuario,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaChatContent(
    chatUiState: ChatUiState,
    onVoltar: () -> Unit,
    onEnviarMsg: (MensagemDTO) -> Unit,
    onEditarMsg: (String, MensagemDTO) -> Unit,
    onExcluirMsg: (String) -> Unit,
    onConfirmarEntrega: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    // Gerencia o scroll da lista de mensagens
    val listState = rememberLazyListState()

    // Mostra o botão apenas se não estiver no final da lista
    val mostrarScrollButton by remember {
        derivedStateOf { listState.canScrollForward }
    }

    // Mostra o diálogo de avaliação
    var mostrarDialogAvaliar by rememberSaveable { mutableStateOf(false) }
    val avaliou = chatUiState.contato?.avaliadores?.contains(chatUiState.contato.uid) == true

    // Scroll automático para a última mensagem ao entrar ou receber nova msg
    LaunchedEffect(chatUiState.msgs.size) {
        if (chatUiState.msgs.isNotEmpty()) listState.animateScrollToItem(chatUiState.msgs.lastIndex)
    }

    chatUiState.contato?.let { contato ->
        DialogAvaliar(
            visivel = mostrarDialogAvaliar,
            contato = contato,
            onFechar = { mostrarDialogAvaliar = false },
            onConfirmar = { nota ->
                onConfirmarEntrega(nota)
                mostrarDialogAvaliar = false
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (chatUiState.carregandoFoto) AnimacaoCarregando()
                        else AvatarImg(
                            foto = chatUiState.contato?.foto,
                            modifier = Modifier.size(40.dp)
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = chatUiState.contato?.nome ?: stringResource(R.string.carregando),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = chatUiState.contato?.notaMedia.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )

                        Spacer(Modifier.weight(1f))

                        TextButton(
                            onClick = { mostrarDialogAvaliar = true },
                            enabled = !avaliou
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.StarRate,
                                contentDescription = stringResource(R.string.entregue)
                            )

                            Spacer(Modifier.width(8.dp))

                            Text(
                                text = stringResource(R.string.avaliar)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.voltar)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.chat_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            Surface(
                modifier = Modifier.matchParentSize(),
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
            ) {}

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .windowInsetsPadding(
                        WindowInsets.ime
                            .exclude(WindowInsets.navigationBars)
                            .exclude(WindowInsets.navigationBars)
                            .exclude(WindowInsets.navigationBars)
                            .exclude(WindowInsets.navigationBars)
                            .exclude(WindowInsets.navigationBars)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(items = chatUiState.msgs, key = { it.id }) { msg ->
                            chatUiState.contato?.uid?.let { uid ->
                                BolhaMsg(
                                    msg = msg,
                                    autor = msg.autorUid != uid,
                                    onEditar = onEditarMsg,
                                    onExcluir = onExcluirMsg
                                )
                            }
                        }
                    }

                    // Botão Flutuante para rolar para o fim
                    androidx.compose.animation.AnimatedVisibility(
                        visible = mostrarScrollButton,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut(),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    ) {
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                TooltipAnchorPosition.Above
                            ),
                            tooltip = {
                                PlainTooltip {
                                    Text(stringResource(R.string.ir_para_o_fim))
                                }
                            },
                            state = rememberTooltipState()
                        ) {
                            SmallFloatingActionButton(
                                onClick = {
                                    scope.launch {
                                        if (chatUiState.msgs.isNotEmpty()) listState.animateScrollToItem(
                                            chatUiState.msgs.lastIndex
                                        )
                                    }
                                },
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = stringResource(R.string.ir_para_o_fim)
                                )
                            }
                        }
                    }
                }

                ChatEntrada(
                    carregando = chatUiState.carregando,
                    onEnviarMsg = onEnviarMsg
                )
            }
        }
        if (chatUiState.carregando && chatUiState.msgs.isEmpty()) OverlayCarregando()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatEntrada(
    carregando: Boolean,
    onEnviarMsg: (MensagemDTO) -> Unit
) {
    val context = LocalContext.current

    // Estados para a entrada de mensagem (texto e imagens)
    var texto by rememberSaveable { mutableStateOf("") }
    var imgs by rememberSaveable { mutableStateOf(listOf<String>()) }
    var uriTemp by rememberSaveable { mutableStateOf<Uri?>(null) }
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> imgs = imgs + uris.map { it.toString() } }
    )

    // Launcher para tirar foto
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { sucesso ->
        if (sucesso && uriTemp != null) imgs = imgs + uriTemp.toString()
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        CarrosselImgs(
            imgs = imgs,
            onRemoverImg = { imgs = imgs - it },
            modifier = Modifier.padding(8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = texto,
                onValueChange = { texto = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.mensagem)) },
                shape = MaterialTheme.shapes.extraLarge,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                trailingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botão de adicionar imagens da galeria com dica de uso
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                            tooltip = {
                                PlainTooltip {
                                    Text(stringResource(R.string.adicionar_imagens))
                                }
                            },
                            state = rememberTooltipState()
                        ) {
                            IconButton(onClick = {
                                photoPicker.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.AttachFile,
                                    contentDescription = stringResource(R.string.adicionar_imagem)
                                )
                            }
                        }

                        // Botão de tirar foto com dica de uso
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                            tooltip = {
                                PlainTooltip {
                                    Text(stringResource(R.string.tirar_foto))
                                }
                            },
                            state = rememberTooltipState()
                        ) {
                            IconButton(onClick = {
                                val uri = context.criarUriParaFoto()
                                uriTemp = uri
                                cameraLauncher.launch(uri)
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.CameraAlt,
                                    contentDescription = stringResource(R.string.tirar_foto)
                                )
                            }
                        }

                        val podeEnviar = !carregando && (texto.isNotBlank() || imgs.isNotEmpty())

                        // Botão de enviar mensagem com dica de uso
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                            tooltip = {
                                PlainTooltip {
                                    Text(stringResource(R.string.enviar))
                                }
                            },
                            state = rememberTooltipState()
                        ) {
                            IconButton(
                                onClick = {
                                    if (podeEnviar) {
                                        onEnviarMsg(
                                            MensagemDTO(
                                                texto = texto,
                                                imgUrls = imgs
                                            )
                                        )
                                        texto = ""
                                        imgs = listOf()
                                    }
                                },
                                enabled = podeEnviar,
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                if (carregando) AnimacaoCarregando()
                                else {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = stringResource(R.string.enviar)
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TelaChatContentPreview() {
    MapaTheme {
        val mensagem = MensagemDTO(
            id = "1",
            texto = "Olá, tudo bem?",
            autorUid = "123",
            timestamp = System.currentTimeMillis()
        )

        TelaChatContent(
            chatUiState = ChatUiState(
                msgs = List(16) { mensagem.copy(id = "${it + 1}") },
                contato = UsuarioDTO(
                    uid = "456",
                    nome = "João da Silva Medeiros",
                    email = "james.francis.byrnes@example-pet-store.com",
                    foto = "https://img.freepik.com/vetores-gratis/ilustracao-do-jovem-sorridente_1308-174669.jpg",
                    notaMedia = 4.5,
                    notaQtd = 10,
                    avaliadores = listOf("456")
                ),
                carregando = false
            ),
            onVoltar = {},
            onEnviarMsg = {},
            onEditarMsg = { _, _ -> },
            onExcluirMsg = {},
            onConfirmarEntrega = {}
        )
    }
}