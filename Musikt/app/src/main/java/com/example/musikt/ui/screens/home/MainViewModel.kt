package com.example.musikt.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.musikt.data.MusicRepository
import com.example.musikt.data.remote.Track
import com.example.musikt.playback.MusicController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MusicRepository()
    private val musicController = MusicController(application)

    private val _topTracks = MutableStateFlow<List<Track>>(emptyList())
    val topTracks: StateFlow<List<Track>> = _topTracks

    init {
        viewModelScope.launch {
            _topTracks.value = repository.getTopTracks()
        }
    }

    fun play(track: Track) {
        musicController.play(track)
    }

    override fun onCleared() {
        super.onCleared()
        musicController.release()
    }
}