package com.example.mapa.ui.telas

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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mapa.R
import com.example.mapa.data.remote.dto.Usuario
import com.example.mapa.models.UsuarioState
import com.example.mapa.ui.componentes.AnimacaoCarregando
import com.example.mapa.ui.componentes.AvatarImg
import com.example.mapa.ui.componentes.DialogConfirmar
import com.example.mapa.ui.componentes.DialogEditar
import com.example.mapa.ui.componentes.Header
import com.example.mapa.ui.theme.MapaTheme
import com.example.mapa.viewmodels.AuthViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun TelaPerfil(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = koinViewModel()
) {
    val usuarioState by authViewModel.uiState.collectAsStateWithLifecycle()

    TelaPerfilContent(
        onLogout = authViewModel::logout,
        onEditarFoto = authViewModel::atualizarFoto,
        onEditarNome = authViewModel::atualizarNome,
        usuarioState = usuarioState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPerfilContent(
    onLogout: () -> Unit,
    onEditarFoto: (String) -> Unit,
    onEditarNome: (String) -> Unit,
    modifier: Modifier = Modifier,
    usuarioState: UsuarioState
) {
    // Estado dos diálogo de edição
    var mostrarDialogEditar by rememberSaveable { mutableStateOf(false) }
    var mostrarDialogConfirmar by rememberSaveable { mutableStateOf(false) }

    // Launcher de seleção de imagem
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) onEditarFoto(uri.toString())
        }
    )

    // Dialog de edição de nome
    DialogEditar(
        visivel = mostrarDialogEditar,
        textoInicial = usuarioState.usuario?.nome ?: "",
        titulo = stringResource(R.string.editar_nome),
        label = stringResource(R.string.nome),
        onFechar = { mostrarDialogEditar = false },
        onConfirmar = {
            onEditarNome(it)
            mostrarDialogEditar = false
        }
    )

    // Dialog de confirmação de logout
    DialogConfirmar(
        visivel = mostrarDialogConfirmar,
        titulo = stringResource(R.string.sair_pergunta),
        texto = stringResource(R.string.tem_certeza_que_deseja_sair),
        onFechar = { mostrarDialogConfirmar = false },
        textoConfirmar = stringResource(R.string.sair),
        onConfirmar = onLogout
    )

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Header(
            titulo = stringResource(R.string.perfil),
            icone = R.drawable.logo,
            modifier = Modifier.align(Alignment.CenterHorizontally)
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
                if (usuarioState.carregandoFoto) AnimacaoCarregando(size = 48.dp)
                else AvatarImg(
                    foto = usuarioState.usuario?.foto,
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
                            enabled = !usuarioState.carregandoFoto,
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = stringResource(R.string.alterar_foto),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = usuarioState.usuario?.nome ?: stringResource(R.string.usu_rio_desconhecido),
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
                        onClick = { mostrarDialogEditar = !mostrarDialogEditar },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.editar_nome),
                        )
                    }
                }
            }

            Text(
                text = usuarioState.usuario?.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { mostrarDialogConfirmar = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = stringResource(R.string.sair_da_conta))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TelaPerfilContentPreview() {
    MapaTheme {
        TelaPerfilContent(
            usuarioState = UsuarioState(
                Usuario (
                    uid = "123",
                    nome = "João da Silva",
                    email = "joaosilva@example.com",
                    foto = "https://cdn-icons-png.flaticon.com/512/12225/12225881.png"
                ),
                logado = true,
                carregandoFoto = false,
                carregandoNome = false
            ),
            onLogout = {},
            onEditarFoto = {},
            onEditarNome = {}
        )
    }
}