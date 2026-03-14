package com.example.mapa.ui.components

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
 * Componente [CircularProgressIndicator] reutilizável que exibe uma animação de carregamento.
 *
 * @param modifier O [Modifier] a ser aplicado ao indicador de progresso.
 * @param size O diâmetro do indicador de progresso circular.
 * @param color A cor do indicador de progresso.
 * @param trackColor A cor do círculo de fundo (trilha) sobre o qual o indicador é desenhado.
 */
@Composable
fun LoadingAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    color: Color = MaterialTheme.colorScheme.tertiary,
    trackColor: Color = Color.Transparent
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = color,
        strokeWidth = (size * 0.12f).coerceIn(2.dp, 8.dp),
        trackColor = trackColor,
        strokeCap = StrokeCap.Round
    )
}

@Preview
@Composable
private fun LoadingAnimationPreview() {
    MapaTheme {
        LoadingAnimation()
    }
}