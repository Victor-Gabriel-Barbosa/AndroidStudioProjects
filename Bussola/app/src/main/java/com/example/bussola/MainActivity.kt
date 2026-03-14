package com.example.bussola

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bussola.ui.theme.BussolaTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BussolaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Bussola(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// Função para ler sensores do dispositivo
@Composable
fun rememberSensorState(type: Int): FloatArray {
    val context = LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    var values by remember { mutableStateOf(FloatArray(3)) }

    DisposableEffect(type) {
        val sensor = sensorManager.getDefaultSensor(type)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent?) {
                values = e?.values?.copyOf() ?: return
            }

            override fun onAccuracyChanged(s: Sensor?, a: Int) {}
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)

        onDispose { sensorManager.unregisterListener(listener) }
    }
    return values
}

@Composable
fun Bussola(modifier: Modifier = Modifier) {
    // Estado para guardar o ângulo (azimute)
    var azimuth by remember { mutableFloatStateOf(0f) }

    // Estado para guardar a rotação contínua
    var rotacaoAtual by remember { mutableFloatStateOf(0f) }

    // Lendo sensores de gravidade e geomagnetismo
    val gravidade = rememberSensorState(type = Sensor.TYPE_ACCELEROMETER)
    val geomagnetico = rememberSensorState(type = Sensor.TYPE_MAGNETIC_FIELD)

    // Calcula o ângulo (azimute) com base nos sensores
    LaunchedEffect(gravidade, geomagnetico) {
        val r = FloatArray(9)
        val i = FloatArray(9)

        if (SensorManager.getRotationMatrix(r, i, gravidade, geomagnetico)) {
            val orientacao = FloatArray(3)
            SensorManager.getOrientation(r, orientacao)

            var graus = Math.toDegrees(orientacao[0].toDouble()).toFloat()
            graus = (graus + 360) % 360

            // Compara o grau atual com o destino e ajusta para não dar a volta completa
            var diff = graus - (rotacaoAtual % 360f)
            if (diff < -180f) diff += 360f
            if (diff > 180f) diff -= 360f

            rotacaoAtual += diff

            // Atualiza o azimute real para mostrar no texto (0 a 360)
            azimuth = graus
        }
    }

    // Animação suavizada usando a rotação contínua
    val azimuthAnimacao by animateFloatAsState(
        targetValue = -rotacaoAtual,
        animationSpec = tween(durationMillis = 150)
    )

    // UI da bússola
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${azimuth.roundToInt()}°",
            color = Color.White,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = getDirecaoLabel(azimuth),
            color = Color.LightGray,
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Desenha a bússola
        Box(modifier = Modifier.size(300.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centro = Offset(size.width / 2, size.height / 2)
                val raio = size.minDimension / 2

                // Rotaciona o canvas baseado no azimute animado
                withTransform({
                    rotate(azimuthAnimacao, centro)
                }) {
                    // Círculo externo
                    drawCircle(
                        color = Color.Gray,
                        radius = raio,
                        center = centro,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
                    )

                    // Marcadores (N, S, L, O)
                    drawContext.canvas.nativeCanvas.apply {
                        // Norte (Vermelho)
                        drawLine(
                            color = Color.Red,
                            start = centro,
                            end = Offset(centro.x, centro.y - raio + 20),
                            strokeWidth = 8.dp.toPx()
                        )
                        // Sul (Branco)
                        drawLine(
                            color = Color.White,
                            start = centro,
                            end = Offset(centro.x, centro.y + raio - 20),
                            strokeWidth = 8.dp.toPx()
                        )
                        // Leste/Oeste (Marcas menores)
                        drawLine(
                            Color.DarkGray,
                            Offset(centro.x - raio + 20, centro.y),
                            Offset(centro.x + raio - 20, centro.y),
                            strokeWidth = 4.dp.toPx()
                        )
                    }
                }
            }
        }
    }
}

// Função auxiliar para dizer se é Norte, Sul, etc.
fun getDirecaoLabel(azimuth: Float): String {
    return when {
        azimuth !in 22.5..<337.5 -> "Norte"
        azimuth in 22.5..<67.5 -> "Nordeste"
        azimuth in 67.5..<112.5 -> "Leste"
        azimuth in 112.5..<157.5 -> "Sudeste"
        azimuth in 157.5..<202.5 -> "Sul"
        azimuth in 202.5..<247.5 -> "Sudoeste"
        azimuth in 247.5..<292.5 -> "Oeste"
        azimuth in 292.5..<337.5 -> "Noroeste"
        else -> ""
    }
}