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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.mapa.R
import com.example.mapa.models.Usuario
import com.example.mapa.ui.componentes.AnimacaoCarregando
import com.example.mapa.ui.componentes.AvatarImg
import com.example.mapa.ui.componentes.DialogEditar
import com.example.mapa.ui.componentes.Header
import com.example.mapa.ui.theme.MapaTheme

@Composable
fun TelaPerfil(
    carregandoFoto: Boolean,
    onLogout: () -> Unit,
    onEditarFoto: (String) -> Unit,
    onEditarNome: (String) -> Unit,
    modifier: Modifier = Modifier,
    usuario: Usuario? = null
) {
    // Estados de edição de nome
    var editarDialog by rememberSaveable { mutableStateOf(false) }
    var nome by rememberSaveable { mutableStateOf(usuario?.nome ?: "") }

    // Launcher de seleção de imagem
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) onEditarFoto(uri.toString())
        }
    )

    // Dialog de edição de nome
    DialogEditar(
        visivel = editarDialog,
        textoInicial = nome,
        titulo = stringResource(R.string.editar_nome),
        labelCampo = stringResource(R.string.nome),
        onFechar = { editarDialog = false },
        onConfirmar = {
            onEditarNome(it)
            editarDialog = false
        }
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
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                if (carregandoFoto) AnimacaoCarregando()
                else AvatarImg(
                    foto = usuario?.foto,
                    modifier = Modifier.size(120.dp)
                )

                IconButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    shape = CircleShape,
                    enabled = !carregandoFoto,
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = usuario?.nome ?: stringResource(R.string.usu_rio_desconhecido),
                    style = MaterialTheme.typography.headlineMedium
                )

                IconButton(
                    onClick = { editarDialog = !editarDialog },
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.editar),
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = stringResource(R.string.sair_da_conta))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TelaPerfilPreview() {
    MapaTheme {
        TelaPerfil(
            usuario = Usuario(
                uid = "123",
                nome = "João da Silva",
                email = "joaosilva@example.com",
                foto = "https://cdn-icons-png.flaticon.com/512/12225/12225881.png"
            ),
            carregandoFoto = false,
            onLogout = {},
            onEditarFoto = {},
            onEditarNome = {}
        )
    }
}