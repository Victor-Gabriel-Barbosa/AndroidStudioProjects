package com.example.mapa.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.mapa.R
import com.example.mapa.ui.theme.MapaTheme

/**
 * Componente que exibe a imagem de perfil do usuário em formato circular.
 * Utiliza a biblioteca Coil para carregamento assíncrono e exibe um feedback
 * de carregamento ou um ícone de erro caso a imagem não possa ser carregada.
 *
 * @param foto URL ou caminho da imagem de perfil. Se nulo, exibirá o estado de erro.
 * @param modifier [Modifier] para customizar o layout, tamanho e comportamento do componente.
 */
@Composable
fun AvatarImg(
    foto: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = CircleShape
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest
                .Builder(LocalContext.current)
                .data(foto)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.foto_de_perfil),
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    AnimacaoCarregando()
                }
            },
            error = {
                Icon(
                    imageVector = Icons.Default.PersonOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        )
    }
}

/**
 * Preview do componente [AvatarImg].
 */
@Preview
@Composable
private fun AvatarImgPreview() {
    MapaTheme {
        AvatarImg(
            foto = "https://placekitten.com/200/200",
            modifier = Modifier.size(48.dp)
        )
    }
}