package com.example.playerdemusica

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.text.format.DateUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.wallpaper),
                    contentDescription = "Wallpaper",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    MeuComp()
                }
            }
        }
    }
}

private fun fmtTempo(ms: Int): String = DateUtils.formatElapsedTime(ms / 1000L)

@Composable
fun MeuComp() {
    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var tocando by remember { mutableStateOf(false) }
    var progresso by remember { mutableFloatStateOf(0f) }
    var musicaSelecionada by remember { mutableStateOf(false) }

    // Launcher para selecionar arquivo de áudio
    val seletorMusica = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Libera o MediaPlayer anterior se existir
            mediaPlayer?.release()

            // Cria novo MediaPlayer com a música selecionada
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                prepare()
                isLooping = true
            }

            musicaSelecionada = true
            progresso = 0f
            tocando = false
        }
    }

    // Inicializa com a música padrão
    LaunchedEffect(Unit) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.musica).apply { isLooping = true }
            musicaSelecionada = true
        }
    }

    // Atualiza o progresso
    LaunchedEffect(tocando) {
        while (isActive && tocando) {
            mediaPlayer?.let { mp ->
                progresso =  mp.currentPosition.toFloat() / (mp.duration.takeIf { it > 0 } ?: 1)
            }
            delay(500L)
        }
    }

    // Libera recursos ao descartar
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
    }


    // Botão para selecionar música
    Button(
        onClick = {
            // Pausa a música atual antes de selecionar a próxima
            if (tocando) {
                mediaPlayer?.pause()
                tocando = false
            }
            seletorMusica.launch("audio/*")
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6200EE),
            contentColor = Color.White
        )
    ) {
        Icon(
            Icons.Outlined.MusicNote,
            contentDescription = "Selecionar Música",
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = "Selecionar Música",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )
    }

    // Slider de progresso da música
    Slider(
        value = progresso,
        onValueChange = { progresso = it },
        onValueChangeFinished = {
            mediaPlayer?.let {
                it.seekTo((progresso * it.duration).toInt())
            }
        },
        enabled = musicaSelecionada,
        modifier = Modifier.fillMaxWidth(0.5f),
        colors = SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor = Color(0xFF6200EE),
            inactiveTrackColor = Color.LightGray
        )
    )

    // Tempo atual da música / Tempo total da música
    Text(
        text = mediaPlayer?.let { "${fmtTempo(it.currentPosition)} / ${fmtTempo(it.duration)}" } ?: "00:00 / 00:00",
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    // Botão play/pause
    Button(
        onClick = {
            mediaPlayer?.let {
                if (tocando && it.isPlaying) {
                    it.pause()
                    tocando = false
                } else {
                    it.start()
                    tocando = true
                }
            }
        },
        enabled = musicaSelecionada,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6200EE),
            contentColor = Color.White
        )
    ) {
        Icon(
            if (tocando) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
            contentDescription = "PlayArrow",
            modifier = Modifier.size(24.dp)
        )
    }
}