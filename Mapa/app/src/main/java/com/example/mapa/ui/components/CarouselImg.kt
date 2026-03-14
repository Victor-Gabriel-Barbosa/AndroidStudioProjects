package com.example.mapa.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mapa.R
import com.example.mapa.ui.theme.MapaTheme

/**
 * Componente de carrossel que exibe uma lista de imagens.
 *
 * @param imgUrls Lista de strings contendo as URLs ou caminhos das imagens.
 * @param modifier [Modifier] para customizar o layout do container.
 * @param onRemoverImg Callback opcional para tratar a remoção de uma imagem.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarouselImg(
    imgUrls: List<String>,
    modifier: Modifier = Modifier,
    onRemoverImg: ((String) -> Unit)? = null
) {
    if (imgUrls.isEmpty()) return

    // Diálogo de imagem com zoom
    var showDialogImg by rememberSaveable { mutableStateOf<String?>(null) }
    ImgDialog(
        imgUrl = showDialogImg,
        onDismiss = { showDialogImg = null }
    )

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        if (imgUrls.isNotEmpty()) {
            HorizontalMultiBrowseCarousel(
                state = rememberCarouselState { imgUrls.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                preferredItemWidth = 160.dp,
                itemSpacing = 8.dp
            ) { index ->
                Box(
                    modifier = Modifier
                        .height(140.dp)
                        .fillMaxWidth()
                ) {
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { showDialogImg = imgUrls[index] }
                    ) {
                        AsyncImage(
                            model = imgUrls[index],
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            placeholder = rememberVectorPainter(Icons.Default.Image),
                            error = rememberVectorPainter(Icons.Default.BrokenImage)
                        )
                    }
                    if (onRemoverImg != null) {
                        Surface(
                            onClick = { onRemoverImg(imgUrls[index]) },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.remover_imagem),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun CarouselImgPreview() {
    MapaTheme {
        CarouselImg(
            imgUrls = listOf(
                "https://4kwallpapers.com/images/walls/thumbs_3t/24938.jpg",
                "https://4kwallpapers.com/images/walls/thumbs_3t/24938.jpg",
                "https://4kwallpapers.com/images/walls/thumbs_3t/24938.jpg",
                "https://4kwallpapers.com/images/walls/thumbs_3t/24938.jpg",
                "https://4kwallpapers.com/images/walls/thumbs_3t/24938.jpg",
                "https://4kwallpapers.com/images/walls/thumbs_3t/24938.jpg"
            )
        )
    }
}