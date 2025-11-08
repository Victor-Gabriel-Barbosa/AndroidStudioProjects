package com.example.musica.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

data class DeezerTrack(
    val id: Long,
    val title: String,
    val preview: String, // link MP3 30s
    val artist: Artist
)

data class Artist(val name: String)

interface DeezerService {
    @GET("search")
    fun searchTracks(@Query("q") query: String): Call<DeezerResponse>
}

data class DeezerResponse(val data: List<DeezerTrack>)
