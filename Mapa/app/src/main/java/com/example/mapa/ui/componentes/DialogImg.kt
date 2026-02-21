package com.example.mapa.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.mapa.R
import com.example.mapa.ui.theme.MapaTheme

/**
 * Composable que exibe um diálogo de imagem em tela cheia com suporte a gestos de zoom e pan.
 *
 * @param img URL ou caminho da imagem a ser exibida. Se for nulo ou estiver em branco, o diálogo não é exibido.
 * @param onFechar Callback invocado quando o diálogo deve ser fechado.
 * @param modifier [Modifier] a ser aplicado ao Surface do diálogo.
 */
@Composable
fun DialogImg(
    img: String?,
    onFechar: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (img.isNullOrBlank()) return

    Dialog(
        onDismissRequest = onFechar,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale *= zoom
                            scale = scale.coerceIn(1f, 3f)
                            offset = if (scale > 1f) offset + pan else Offset.Zero
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                scale = if (scale > 1f) 1f else 2f
                                offset = Offset.Zero
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = img,
                    contentDescription = stringResource(R.string.imagem_com_zoom),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                )

                IconButton(
                    onClick = onFechar,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .padding(top = 24.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.fechar),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Preview do componente [DialogImg].
 */
@Preview
@Composable
private fun DialogImgPreview() {
    MapaTheme {
        DialogImg(
            img = "https://4kwallpapers.com/images/walls/thumbs_3t/24938.jpg",
            onFechar = {}
        )
    }
}