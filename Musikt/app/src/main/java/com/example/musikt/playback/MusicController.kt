package com.example.musikt.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.musikt.data.remote.Track
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

class MusicController(context: Context) {
    private var mediaController: MediaController? = null
    private val controllerFuture: ListenableFuture<MediaController>

    init {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                mediaController = controllerFuture.get()
            },
            MoreExecutors.directExecutor()
        )
    }

    fun play(track: Track) {
        mediaController?.setMediaItem(MediaItem.fromUri(track.preview))
        mediaController?.prepare()
        mediaController?.play()
    }

    fun release() {
        MediaController.releaseFuture(controllerFuture)
    }
}