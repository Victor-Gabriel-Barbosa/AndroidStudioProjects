package com.example.mapa.ui.screen

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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mapa.R
import com.example.mapa.data.remote.dto.MsgDTO
import com.example.mapa.data.remote.dto.UserDTO
import com.example.mapa.model.ChatUiState
import com.example.mapa.ui.components.LoadingAnimation
import com.example.mapa.ui.components.AvatarImg
import com.example.mapa.ui.components.BubbleMsg
import com.example.mapa.ui.components.CarouselImg
import com.example.mapa.ui.components.ReviewDialog
import com.example.mapa.ui.components.LoadingOverlay
import com.example.mapa.ui.theme.MapaTheme
import com.example.mapa.util.createPhotoUri
import com.example.mapa.viewmodels.ChatViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun ChatScreen(
    uid: String,
    locationId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel = koinViewModel()
) {
    // Inicializa o ViewModel com o UID do contato
    LaunchedEffect(uid, locationId) {
        chatViewModel.initialize(uid, locationId)
    }

    // Feedback visual (eventos) vindo do ViewModel
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        chatViewModel.channel.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    // Coleta o estado do ViewModel
    val chatUiState by chatViewModel.uiState.collectAsStateWithLifecycle()

    ChatScreenContent(
        chatUiState = chatUiState,
        onBack = onBack,
        onSendMsg = chatViewModel::sendMsg,
        onEditMsg = chatViewModel::updateMsg,
        onDeleteMsg = chatViewModel::deleteMsg,
        onReview = chatViewModel::rateUser,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenContent(
    chatUiState: ChatUiState,
    onBack: () -> Unit,
    onSendMsg: (MsgDTO) -> Unit,
    onEditMsg: (MsgDTO) -> Unit,
    onDeleteMsg: (String) -> Unit,
    onReview: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Gerencia o scroll da lista de mensagens
    val listState = rememberLazyListState()

    // Mostra o botão apenas se não estiver no final da lista
    val showScrollButton by remember {
        derivedStateOf { listState.canScrollForward }
    }

    // Mostra o diálogo de avaliação
    var showReviewDialog by rememberSaveable { mutableStateOf(false) }
    val rated = chatUiState.contact?.reviewerUids?.contains(chatUiState.uid) == true

    // Scroll automático para a última mensagem ao entrar ou receber nova msg
    LaunchedEffect(chatUiState.msgs.size) {
        if (chatUiState.msgs.isNotEmpty()) listState.animateScrollToItem(chatUiState.msgs.lastIndex)
    }

    chatUiState.contact?.let { contact ->
        ReviewDialog(
            visible = showReviewDialog,
            contact = contact,
            onDismiss = { showReviewDialog = false },
            onConfirm = { nota ->
                onReview(nota)
                showReviewDialog = false
            }
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (chatUiState.loadingPhoto) LoadingAnimation()
                        else AvatarImg(
                            photoUrl = chatUiState.contact?.photo,
                            modifier = Modifier.size(40.dp)
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = chatUiState.contact?.name ?: stringResource(R.string.carregando),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = chatUiState.contact?.averageRating.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )

                        Spacer(Modifier.weight(1f))

                        TextButton(
                            onClick = { showReviewDialog = true },
                            enabled = !rated
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
                    IconButton(onClick = onBack) {
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
        Box(modifier = Modifier.fillMaxSize()) {
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
                    .consumeWindowInsets(paddingValues)
                    .imePadding()
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
                            chatUiState.contact?.uid?.let { uid ->
                                BubbleMsg(
                                    msg = msg,
                                    author = msg.uid != uid,
                                    onEdit = onEditMsg,
                                    onDelete = onDeleteMsg
                                )
                            }
                        }
                    }

                    // Botão Flutuante para rolar para o fim
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showScrollButton,
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
                    loading = chatUiState.loading,
                    onSendMsg = onSendMsg
                )
            }
        }
        if (chatUiState.loading && chatUiState.msgs.isEmpty()) LoadingOverlay()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatEntrada(
    loading: Boolean,
    onSendMsg: (MsgDTO) -> Unit
) {
    val context = LocalContext.current

    // Estados para a entrada de mensagem (texto e imagens)
    var text by rememberSaveable { mutableStateOf("") }
    var imgUrls by rememberSaveable { mutableStateOf(listOf<String>()) }
    var uriTemp by rememberSaveable { mutableStateOf<Uri?>(null) }
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> imgUrls = imgUrls + uris.map { it.toString() } }
    )

    // Launcher para tirar foto
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { ok ->
        if (ok && uriTemp != null) imgUrls = imgUrls + uriTemp.toString()
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        CarouselImg(
            imgUrls = imgUrls,
            onRemoverImg = { imgUrls = imgUrls - it },
            modifier = Modifier.padding(8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = { text = it },
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
                                val uri = context.createPhotoUri()
                                uriTemp = uri
                                cameraLauncher.launch(uri)
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.CameraAlt,
                                    contentDescription = stringResource(R.string.tirar_foto)
                                )
                            }
                        }

                        val canSend = !loading && (text.isNotBlank() || imgUrls.isNotEmpty())

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
                                    if (canSend) {
                                        onSendMsg(
                                            MsgDTO(
                                                text = text,
                                                imgUrls = imgUrls
                                            )
                                        )
                                        text = ""
                                        imgUrls = listOf()
                                    }
                                },
                                enabled = canSend,
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                if (loading) LoadingAnimation()
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
fun ChatScreenContentPreview() {
    MapaTheme {
        val msg = MsgDTO(
            id = "1",
            text = "Olá, tudo bem?",
            uid = "123",
            timestamp = System.currentTimeMillis()
        )

        ChatScreenContent(
            modifier = Modifier.padding(top = 8.dp),
            chatUiState = ChatUiState(
                msgs = List(16) { msg.copy(id = "${it + 1}") },
                contact = UserDTO(
                    uid = "456",
                    name = "João da Silva Medeiros",
                    email = "james.francis.byrnes@example-pet-store.com",
                    photo = "https://img.freepik.com/vetores-gratis/ilustracao-do-jovem-sorridente_1308-174669.jpg",
                    averageRating = 4.5,
                    ratingCount = 10,
                    reviewerUids = listOf("456")
                ),
                loading = false
            ),
            onBack = {},
            onSendMsg = {},
            onEditMsg = {},
            onDeleteMsg = {},
            onReview = {}
        )
    }
}