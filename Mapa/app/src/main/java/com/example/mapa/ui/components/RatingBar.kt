package com.example.mapa.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mapa.R
import com.example.mapa.ui.theme.MapaTheme

/**
 * Componente que exibe uma barra de avaliação de estrelas.
 *
 * @param rating A nota atual da avaliação (de 1 a 5).
 * @param onRatingChange Callback que é chamado quando a nota é alterada.
 * @param modifier [Modifier] para customizar o layout, tamanho e comportamento do componente.
 * @param maxStars O número máximo de estrelas
 */
@Composable
fun RatingBar(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxStars: Int = 5
) {
    Row(modifier = modifier) {
        for (i in 1..maxStars) {
            Icon(
                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = stringResource(R.string.estrela, i),
                tint = if (i <= rating) Color(0xFFFFD700) else Color.Gray,
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onRatingChange(i) }
                    .padding(4.dp)
            )
        }
    }
}

@Preview
@Composable
private fun RatingBarPreview() {
    MapaTheme {
        RatingBar(rating = 3, onRatingChange = {})
    }
}