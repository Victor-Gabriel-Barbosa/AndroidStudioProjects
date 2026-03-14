package com.example.mapa.ui.screen

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mapa.R
import com.example.mapa.data.remote.dto.UserDTO
import com.example.mapa.model.UserUiState
import com.example.mapa.ui.components.LoadingAnimation
import com.example.mapa.ui.components.AvatarImg
import com.example.mapa.ui.components.ConfirmDialog
import com.example.mapa.ui.components.EditDialog
import com.example.mapa.ui.components.Header
import com.example.mapa.ui.theme.MapaTheme
import com.example.mapa.viewmodels.AuthViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = koinViewModel()
) {
    val context = LocalContext.current

    // Observáveis do ViewModel
    val usuarioUiState by authViewModel.uiState.collectAsStateWithLifecycle()

    // Feedback visual (eventos) vindo do ViewModel
    LaunchedEffect(Unit) {
        authViewModel.channel.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    ProfileScreenContent(
        onLogout = authViewModel::logout,
        onEditarFoto = authViewModel::updatePhoto,
        onEditarNome = authViewModel::updateName,
        userUiState = usuarioUiState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenContent(
    onLogout: () -> Unit,
    onEditarFoto: (String) -> Unit,
    onEditarNome: (String) -> Unit,
    modifier: Modifier = Modifier,
    userUiState: UserUiState
) {
    // Estado dos diálogo de edição
    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }

    // Launcher de seleção de imagem
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) onEditarFoto(uri.toString())
        }
    )

    // Dialog de edição de nome
    EditDialog(
        visible = showEditDialog,
        initialText = userUiState.user?.name ?: "",
        title = stringResource(R.string.editar_nome),
        label = stringResource(R.string.nome),
        onDismiss = { showEditDialog = false },
        onConfirm = {
            onEditarNome(it)
            showEditDialog = false
        }
    )

    // Dialog de confirmação de logout
    ConfirmDialog(
        visible = showConfirmDialog,
        title = stringResource(R.string.sair_pergunta),
        text = stringResource(R.string.tem_certeza_que_deseja_sair),
        onDismiss = { showConfirmDialog = false },
        textConfirm = stringResource(R.string.sair),
        onConfirm = onLogout
    )

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Header(
            title = stringResource(R.string.perfil),
            icon = R.drawable.logo
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                // Avatar com feedback de carregamento
                if (userUiState.loadingPhoto) LoadingAnimation(size = 48.dp)
                else AvatarImg(
                    photoUrl = userUiState.user?.photo,
                    modifier = Modifier.size(120.dp)
                )

                Box(
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    // Botão de alterar foto com dica de uso
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                        tooltip = {
                            PlainTooltip {
                                Text(stringResource(R.string.alterar_foto))
                            }
                        },
                        state = rememberTooltipState()
                    ) {
                        IconButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface),
                            shape = CircleShape,
                            enabled = !userUiState.loadingPhoto,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = stringResource(R.string.alterar_foto),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            Text(
                text = "${userUiState.user?.averageRating}"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = userUiState.user?.name ?: stringResource(R.string.usu_rio_desconhecido),
                    style = MaterialTheme.typography.headlineMedium
                )

                // Botão de editar nome com dica de uso
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                    tooltip = {
                        PlainTooltip {
                            Text(stringResource(R.string.editar_nome))
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    IconButton(
                        onClick = { showEditDialog = !showEditDialog },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = stringResource(R.string.editar_nome),
                        )
                    }
                }
            }

            Text(
                text = userUiState.user?.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { showConfirmDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = stringResource(R.string.sair_da_conta))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenContentPreview() {
    MapaTheme {
        ProfileScreenContent(
            userUiState = UserUiState(
                UserDTO (
                    uid = "123",
                    name = "João da Silva",
                    email = "joaosilva@example.com",
                    photo = "https://cdn-icons-png.flaticon.com/512/12225/12225881.png"
                ),
                loggedIn = true,
                loadingPhoto = false,
                loadingName = false
            ),
            onLogout = {},
            onEditarFoto = {},
            onEditarNome = {}
        )
    }
}