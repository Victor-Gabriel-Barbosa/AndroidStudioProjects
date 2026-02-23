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

@Composable
fun BarraAvalicao(
    nota: Int,
    onNotaChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxEstrelas: Int = 5
) {
    Row(modifier = modifier) {
        for (i in 1..maxEstrelas) {
            Icon(
                imageVector = if (i <= nota) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = stringResource(R.string.estrela, i),
                tint = if (i <= nota) Color(0xFFFFD700) else Color.Gray,
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onNotaChange(i) }
                    .padding(4.dp)
            )
        }
    }
}

@Preview
@Composable
private fun BarraAvalicaoPreview() {
    MapaTheme {
        BarraAvalicao(nota = 3, onNotaChange = {})
    }
}