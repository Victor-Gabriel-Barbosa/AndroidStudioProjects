package com.example.musikt.data.remote

import retrofit2.http.GET

interface DeezerApi {
    @GET("chart/0/tracks")
    suspend fun getTopTracks(): TopTracksResponse
}

data class TopTracksResponse(
    val data: List<Track>
)
