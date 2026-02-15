package com.example.musikt.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.musikt.data.remote.Track

@Composable
fun TrackListItem(track: Track, onPlay: (Track) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clickable { onPlay(track) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = track.album.cover,
            contentDescription = null,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = track.title)
            Text(text = track.artist.name)
        }
        IconButton(onClick = { onPlay(track) }) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Play")
        }
    }
}
