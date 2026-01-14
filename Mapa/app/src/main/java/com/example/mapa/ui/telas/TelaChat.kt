package com.example.mapa.ui.telas

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mapa.R
import com.example.mapa.models.ChatUiState
import com.example.mapa.models.Mensagem
import com.example.mapa.models.Usuario
import com.example.mapa.ui.componentes.AnimacaoCarregando
import com.example.mapa.ui.componentes.AvatarImg
import com.example.mapa.ui.componentes.BolhaMsg
import com.example.mapa.ui.componentes.CarrosselImgs
import com.example.mapa.ui.componentes.OverlayCarregando
import com.example.mapa.ui.theme.MapaTheme
import com.example.mapa.utils.criarUriParaFoto
import com.example.mapa.viewmodels.ChatViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaChat(
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel = koinViewModel()
) {
    val context = LocalContext.current

    // Feedback visual (Toasts) vindo do ViewModel
    LaunchedEffect(Unit) {
        chatViewModel.mensagens.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    // Coleta o estado do ViewModel de chat
    val uiState by chatViewModel.uiState.collectAsStateWithLifecycle()
    val carregandoMsg by chatViewModel.carregandoMsg.collectAsStateWithLifecycle()

    Chat(
        chatState = uiState,
        carregandoMsg = carregandoMsg,
        onVoltar = onVoltar,
        onEnviarMsg = chatViewModel::enviarMsg,
        onEditarMsg = chatViewModel::editarMsg,
        onExcluirMsg = chatViewModel::excluirMsg,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Chat(
    chatState: ChatUiState,
    carregandoMsg: Boolean,
    onVoltar: () -> Unit,
    onEnviarMsg: (Mensagem) -> Unit,
    onEditarMsg: (String, Mensagem) -> Unit,
    onExcluirMsg: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Scroll da lista de mensagens
    val listState = rememberLazyListState()
    LaunchedEffect(chatState.msgs.size) {
        if (chatState.msgs.isNotEmpty()) listState.animateScrollToItem(chatState.msgs.size - 1)
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
                        AvatarImg(
                            foto = chatState.destinatario?.foto,
                            modifier = Modifier.size(40.dp)
                        )

                        Spacer(Modifier.width(12.dp))

                        Text(
                            text = chatState.destinatario?.nome ?: stringResource(R.string.carregando),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .windowInsetsPadding(
                    WindowInsets.ime.exclude(WindowInsets.navigationBars)
                        .exclude(WindowInsets.navigationBars).exclude(WindowInsets.navigationBars)
                        .exclude(WindowInsets.navigationBars).exclude(WindowInsets.navigationBars)
                )
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(
                    items = chatState.msgs,
                    key = { it.id }
                ) { msg ->
                    chatState.destinatario?.uid?.let { uid ->
                        BolhaMsg(
                            msg = msg,
                            remetente = msg.remetenteUid != uid,
                            onEditar = onEditarMsg,
                            onExcluir = onExcluirMsg
                        )
                    }
                }
            }
            ChatEntrada(
                carregandoMsg = carregandoMsg,
                onEnviarMsg = onEnviarMsg
            )
        }

        // Feedback de carregamento
        if (chatState.carregando && chatState.msgs.isEmpty()) OverlayCarregando()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatEntrada(
    carregandoMsg: Boolean,
    onEnviarMsg: (Mensagem) -> Unit
) {
    val context = LocalContext.current

    // Campos de texto e imagem
    var texto by rememberSaveable { mutableStateOf("") }
    var imgs by rememberSaveable { mutableStateOf(listOf<String>()) }
    var uriTemp by rememberSaveable { mutableStateOf<Uri?>(null) }

    // Launcher de seleção de imagem
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> imgs = imgs + uris.map { it.toString() } }
    )

    // Launcher de captura de imagem
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { sucesso ->
        if (sucesso && uriTemp != null) imgs = imgs + uriTemp.toString()
    }

    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()

    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            CarrosselImgs(
                imgs = imgs,
                onRemoverImg = { imgs = imgs - it },
                modifier = Modifier.padding(8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.mensagem)) },
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    trailingIcon = {
                        Row {
                            // Botão de seleção de imagens da galeria
                            IconButton(
                                onClick = {
                                    photoPicker.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = stringResource(R.string.adicionar_imagem)
                                )
                            }

                            // Botão de captura de imagem
                            IconButton(
                                onClick = {
                                    val uri = context.criarUriParaFoto()
                                    uriTemp = uri
                                    cameraLauncher.launch(uri)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = stringResource(R.string.tirar_foto)
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Botão com feedback visual (enable/disable)
                val podeEnviar = texto.isNotBlank() || imgs.isNotEmpty() && !carregandoMsg
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
                    modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    if (carregandoMsg) AnimacaoCarregando()
                    else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(R.string.enviar),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ChatPreview() {
    MapaTheme {
        Chat(
            chatState = ChatUiState(
                msgs = listOf(
                    Mensagem(
                        id = "1",
                        texto = "Olá, tudo bem?",
                        remetenteUid = "123",
                        destinatarioUid = "456",
                        timestamp = System.currentTimeMillis()
                    )
                ),
                destinatario = Usuario(
                    uid = "456",
                    nome = "João",
                    email = "james.francis.byrnes@example-pet-store.com",
                    foto = "https://img.freepik.com/vetores-gratis/ilustracao-do-jovem-sorridente_1308-174669.jpg"
                ),
                carregando = false,
            ),
            carregandoMsg = false,
            onVoltar = {},
            onEnviarMsg = {},
            onEditarMsg = { _, _ -> },
            onExcluirMsg = {}
        )
    }
}