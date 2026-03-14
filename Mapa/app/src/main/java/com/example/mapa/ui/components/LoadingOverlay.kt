package com.example.mapa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.mapa.ui.theme.MapaTheme

/**
 * Componente de sobreposição para exibir uma animação de carregamento.
 *
 * @param size O tamanho da animação de carregamento.
 */
@Composable
fun LoadingOverlay(
    size: Dp = 60.dp,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        LoadingAnimation(size = size)
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingOverlayPreview() {
    MapaTheme {
        LoadingOverlay()
    }
}
