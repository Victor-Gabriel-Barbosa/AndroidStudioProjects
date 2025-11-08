package com.example.musica

import android.media.MediaPlayer
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.musica.api.*
import com.google.android.material.switchmaterial.SwitchMaterial
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var imgPlay: ImageView
    private lateinit var imgStop: ImageView
    private lateinit var textNomeMusica: TextView
    private lateinit var textNomeArtista: TextView
    private lateinit var switchRepeticao: SwitchMaterial
    private lateinit var service: DeezerService
    private lateinit var mediaSession: MediaSession

    private var ultimaUrl: String? = null
    private var ultimaMusica: String? = null
    private var ultimoArtista: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imgPlay = findViewById(R.id.imgPlay)
        imgStop = findViewById(R.id.imgStop)
        textNomeMusica = findViewById(R.id.textNomeMusica)
        textNomeArtista = findViewById(R.id.textNomeArtista)
        switchRepeticao = findViewById(R.id.switchRepeticao)

        // Inicializa Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.deezer.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        service = retrofit.create(DeezerService::class.java)

        // Inicializa MediaSession
        mediaSession = MediaSession(this, "MusicaSession")
        val stateBuilder = PlaybackState.Builder()
            .setActions(
                PlaybackState.ACTION_PLAY or
                        PlaybackState.ACTION_PAUSE or
                        PlaybackState.ACTION_SKIP_TO_NEXT
            )
        mediaSession.setPlaybackState(stateBuilder.build())

        mediaSession.setCallback(object : MediaSession.Callback() {
            override fun onPlay() {
                mediaPlayer?.start()
                imgPlay.setImageResource(R.drawable.ic_pause)
            }

            override fun onPause() {
                mediaPlayer?.pause()
                imgPlay.setImageResource(R.drawable.ic_play)
            }

            override fun onSkipToNext() {
                tocarMusica()
            }
        })
        mediaSession.isActive = true

        // Toca a primeira música
        tocarMusica()
    }

    private fun tocarMusica() {
        val letras = listOf("a", "e", "i", "o", "u", "m", "n", "r", "s")
        val letraAleatoria = letras.random()

        service.searchTracks(letraAleatoria).enqueue(object : Callback<DeezerResponse> {
            override fun onResponse(call: Call<DeezerResponse>, response: Response<DeezerResponse>) {
                val lista = response.body()?.data
                if (!lista.isNullOrEmpty()) {
                    val track = lista.random()
                    textNomeMusica.text = track.title
                    textNomeArtista.text = track.artist.name
                    ultimaUrl = track.preview
                    ultimaMusica = track.title
                    ultimoArtista = track.artist.name
                    inicializarPlayer(track.preview)
                } else {
                    Toast.makeText(this@MainActivity, "Nenhuma música encontrada", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DeezerResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Erro: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun inicializarPlayer(url: String) {
        mediaPlayer?.release()

        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            prepareAsync()

            setOnPreparedListener {
                start()
                imgPlay.setImageResource(R.drawable.ic_pause)

                imgPlay.setOnClickListener {
                    if (isPlaying) {
                        pause()
                        imgPlay.setImageResource(R.drawable.ic_play)
                    } else {
                        start()
                        imgPlay.setImageResource(R.drawable.ic_pause)
                    }
                }

                imgStop.setOnClickListener {
                    stop()
                    imgPlay.setImageResource(R.drawable.ic_play)
                }
            }

            setOnCompletionListener {
                if (switchRepeticao.isChecked) {
                    Toast.makeText(this@MainActivity, "Repetindo música...", Toast.LENGTH_SHORT).show()
                    ultimaUrl?.let { inicializarPlayer(it) }
                } else {
                    Toast.makeText(this@MainActivity, "Tocando próxima música...", Toast.LENGTH_SHORT).show()
                    tocarMusica()
                }
            }
        }
    }

    fun tocarProxMusica(view: View) {
        tocarMusica()
    }

    fun recarregar(view: View) {
        recreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaSession.release()
    }
}