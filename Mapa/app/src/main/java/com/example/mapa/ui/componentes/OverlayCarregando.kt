package com.example.mapa.ui.componentes

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
 * Exibe uma sobreposição de carregamento em tela cheia.
 *
 * Este composable é usado para indicar que uma operação está em andamento,
 * bloqueando a interação do usuário com a interface subjacente. A sobreposição
 * preenche toda a tela e exibe uma animação de carregamento no centro.
 */
@Composable
fun OverlayCarregando(
    size: Dp = 60.dp,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        AnimacaoCarregando(size = size)
    }
}

/**
 * Preview para o [OverlayCarregando].
 */
@Preview(showBackground = true)
@Composable
private fun OverlayCarregandoPreview() {
    MapaTheme {
        OverlayCarregando()
    }
}
