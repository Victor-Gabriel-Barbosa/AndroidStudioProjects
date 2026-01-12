package com.example.mapa.ui.componentes

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.mapa.ui.theme.MapaTheme

/**
 * Animação de circulo de pregresso de carregamento
 */
@Composable
fun AnimacaoCarregando(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    color: Color = MaterialTheme.colorScheme.tertiary,
    strokeWidth: Dp = 4.dp,
    trackColor: Color = Color.Transparent
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = color,
        strokeWidth = strokeWidth,
        trackColor = trackColor,
        strokeCap = StrokeCap.Round
    )
}

@Preview
@Composable
private fun AnimacaoCarregandoPreview() {
    MapaTheme {
        AnimacaoCarregando()
    }
}