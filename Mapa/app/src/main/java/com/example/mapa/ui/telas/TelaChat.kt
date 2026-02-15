package com.example.mapa.ui.telas

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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import com.example.mapa.models.ChatState
import com.example.mapa.models.Mensagem
import com.example.mapa.data.remote.dto.Usuario
import com.example.mapa.ui.componentes.AnimacaoCarregando
import com.example.mapa.ui.componentes.AvatarImg
import com.example.mapa.ui.componentes.BolhaMsg
import com.example.mapa.ui.componentes.CarrosselImgs
import com.example.mapa.ui.componentes.OverlayCarregando
import com.example.mapa.ui.theme.MapaTheme
import com.example.mapa.utils.criarUriParaFoto
import com.example.mapa.viewmodels.ChatViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaChat(
    uid: String,
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel = koinViewModel()
) {
    // Inicializa o ViewModel com o UID do contato
    LaunchedEffect(uid) {
        chatViewModel.inicializar(uid)
    }

    // Feedback visual (Toasts) vindo do ViewModel
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        chatViewModel.mensagens.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    // Coleta o estado do ViewModel
    val uiState by chatViewModel.uiState.collectAsStateWithLifecycle()

    TelaChatContent(
        chatState = uiState,
        onVoltar = onVoltar,
        onEnviarMsg = chatViewModel::enviarMsg,
        onEditarMsg = chatViewModel::editarMsg,
        onExcluirMsg = chatViewModel::excluirMsg,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaChatContent(
    chatState: ChatState,
    onVoltar: () -> Unit,
    onEnviarMsg: (Mensagem) -> Unit,
    onEditarMsg: (String, Mensagem) -> Unit,
    onExcluirMsg: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    // Gerencia o scroll da lista de mensagens
    val listState = rememberLazyListState()

    // Mostra o botão apenas se não estiver no final da lista
    val mostrarScrollButton by remember {
        derivedStateOf { listState.canScrollForward }
    }

    // Scroll automático para a última mensagem ao entrar ou receber nova msg
    LaunchedEffect(chatState.msgs.size) {
        if (chatState.msgs.isNotEmpty()) listState.animateScrollToItem(chatState.msgs.lastIndex)
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
                        if (chatState.carregandoFoto) AnimacaoCarregando()
                        else AvatarImg(
                            foto = chatState.contato?.foto,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = chatState.contato?.nome ?: stringResource(R.string.carregando),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
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
                        items(items = chatState.msgs, key = { it.id }) { msg ->
                            chatState.contato?.uid?.let { uid ->
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
                                        if (chatState.msgs.isNotEmpty()) listState.animateScrollToItem(
                                            chatState.msgs.lastIndex
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
                    carregando = chatState.carregando,
                    onEnviarMsg = onEnviarMsg
                )
            }
        }
        if (chatState.carregando && chatState.msgs.isEmpty()) OverlayCarregando()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatEntrada(
    carregando: Boolean,
    onEnviarMsg: (Mensagem) -> Unit
) {
    val context = LocalContext.current
    var texto by rememberSaveable { mutableStateOf("") }
    var imgs by rememberSaveable { mutableStateOf(listOf<String>()) }
    var uriTemp by rememberSaveable { mutableStateOf<Uri?>(null) }
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> imgs = imgs + uris.map { it.toString() } }
    )
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
                                    imageVector = Icons.Default.AttachFile,
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
                                    imageVector = Icons.Default.CameraAlt,
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
                                            Mensagem(
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TelaChatContentPreview() {
    MapaTheme {
        val mensagem = Mensagem(
            id = "1",
            texto = "Olá, tudo bem?",
            autorUid = "123",
            timestamp = System.currentTimeMillis()
        )
        TelaChatContent(
            chatState = ChatState(
                msgs = List(16) { mensagem.copy(id = "${it + 1}") },
                contato = Usuario(
                    uid = "456",
                    nome = "João",
                    email = "james.francis.byrnes@example-pet-store.com",
                    foto = "https://img.freepik.com/vetores-gratis/ilustracao-do-jovem-sorridente_1308-174669.jpg"
                ),
                carregando = false
            ),
            onVoltar = {},
            onEnviarMsg = {},
            onEditarMsg = { _, _ -> },
            onExcluirMsg = {}
        )
    }
}