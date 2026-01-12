package com.example.mapa.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mapa.ui.theme.MapaTheme

/**
 * Sobreposição de circulo de pregresso de carregamento
 */
@Composable
fun OverlayCarregando() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        AnimacaoCarregando(
            size = 100.dp,
            strokeWidth = 8.dp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OverlayCarregandoPreview() {
    MapaTheme {
        OverlayCarregando()
    }
}