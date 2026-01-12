package com.example.mapa.ui.telas

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
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
import androidx.compose.runtime.remember
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
import com.example.mapa.models.Mensagem
import com.example.mapa.models.Usuario
import com.example.mapa.ui.componentes.BolhaMsg
import com.example.mapa.ui.componentes.CarrosselImgs
import com.example.mapa.ui.componentes.AvatarImg
import com.example.mapa.ui.componentes.OverlayCarregando
import com.example.mapa.ui.theme.MapaTheme
import com.example.mapa.viewmodels.ChatViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaChat(
    destinatarioUid: String,
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel = koinViewModel()
) {
    val context = LocalContext.current

    // Inicia a conversa com o parceiro
    LaunchedEffect(destinatarioUid) {
        chatViewModel.iniciarChat(destinatarioUid)
    }

    // Feedback visual (Toasts) vindo do ViewModel
    LaunchedEffect(Unit) {
        chatViewModel.mensagens.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    // Coleta o estado do ViewModel de chat
    val uiState by chatViewModel.uiState.collectAsStateWithLifecycle()
    val remetenteUid by chatViewModel.remetenteUid.collectAsStateWithLifecycle()

    Chat(
        msgs = uiState.msgs,
        remetenteUid = remetenteUid,
        destinatario = uiState.destinatario,
        carregando = uiState.carregando,
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
    msgs: List<Mensagem>,
    remetenteUid: String?,
    destinatario: Usuario?,
    carregando: Boolean,
    onVoltar: () -> Unit,
    onEnviarMsg: (Mensagem) -> Unit,
    onEditarMsg: (String, Mensagem) -> Unit,
    onExcluirMsg: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Scroll da lista de mensagens
    val listState = rememberLazyListState()
    LaunchedEffect(msgs.size) {
        if (msgs.isNotEmpty()) listState.animateScrollToItem(msgs.size - 1)
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
                            foto = destinatario?.foto,
                            modifier = Modifier.size(40.dp)
                        )

                        Spacer(Modifier.width(12.dp))

                        Text(
                            text = destinatario?.nome ?: stringResource(R.string.carregando),
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
                .imePadding()
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
                    items = msgs,
                    key = { it.id }
                ) { msg ->
                    remetenteUid?.let { uid ->
                        BolhaMsg(
                            msg = msg,
                            remetente = msg.remetenteUid == uid,
                            onEditar = onEditarMsg,
                            onExcluir = onExcluirMsg
                        )
                    }
                }
            }
            ChatEntrada(onEnviarMensagem = onEnviarMsg)
        }

        // Feedback de carregamento
        if (carregando && msgs.isEmpty()) OverlayCarregando()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatEntrada(
    onEnviarMensagem: (Mensagem) -> Unit
) {
    // Campos de texto e imagem
    var texto by remember { mutableStateOf("") }
    var imgs by rememberSaveable { mutableStateOf(listOf<String>()) }

    // Launcher de seleção de imagem
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> imgs = imgs + uris.map { it.toString() } }
    )

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
                                contentDescription = stringResource(R.string.adicionar_imagem),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Botão com feedback visual (enable/disable)
                val podeEnviar = texto.isNotBlank() || imgs.isNotEmpty()
                IconButton(
                    onClick = {
                        if (podeEnviar) {
                            onEnviarMensagem(
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ChatPreview() {
    MapaTheme {
        Chat(
            msgs = listOf(
                Mensagem(
                    id = "1",
                    texto = "Olá, tudo bem?",
                    remetenteUid = "123",
                    destinatarioUid = "456",
                    timestamp = System.currentTimeMillis()
                )
            ),
            remetenteUid = "123",
            destinatario = Usuario(
                uid = "456",
                nome = "João",
                email = "james.francis.byrnes@example-pet-store.com",
                foto = "https://img.freepik.com/vetores-gratis/ilustracao-do-jovem-sorridente_1308-174669.jpg"
            ),
            carregando = false,
            onVoltar = {},
            onEnviarMsg = {},
            onEditarMsg = { _, _ -> },
            onExcluirMsg = {}
        )
    }
}