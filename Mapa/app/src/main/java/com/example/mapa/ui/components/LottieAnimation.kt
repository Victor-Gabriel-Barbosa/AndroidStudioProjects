package com.example.mapa.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.mapa.R
import com.example.mapa.ui.theme.MapaTheme

/**
 * Componente de animação que utiliza a biblioteca Lottie para exibir arquivos JSON de animação.
 *
 * @param animation Referência para o recurso raw da animação (ex: [R.raw.mapa_animado]).
 * @param modifier Modificador para ajustar o layout, tamanho ou comportamento do componente.
 * @param loop Se verdadeiro, a animação será repetida infinitamente.
 * @param speed A escala de velocidade da reprodução (1.0f é a velocidade normal).
 * @param onFinish Callback chamado quando a animação atinge o final do ciclo (100% de progresso).
 */
@Composable
fun LottieAnimation(
    animation: Int,
    modifier: Modifier = Modifier,
    loop: Boolean = true,
    speed: Float = 1f,
    onFinish: () -> Unit = {}
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animation))

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = if (loop) LottieConstants.IterateForever else 1,
        isPlaying = true,
        speed = speed
    )

    // Monitora o progresso e executa a função de retorno quando terminar
    LaunchedEffect(progress) {
        if (progress == 1.0f) { onFinish() }
    }

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier
    )
}

@Preview
@Composable
private fun LottieAnimationPreview() {
    MapaTheme {
        LottieAnimation(
            animation = R.raw.mapa_animado,
            modifier = Modifier.size(200.dp)
        )
    }
}