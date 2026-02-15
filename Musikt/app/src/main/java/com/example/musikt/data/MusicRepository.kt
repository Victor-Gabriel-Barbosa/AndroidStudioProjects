package com.example.musikt.data

import com.example.musikt.data.remote.DeezerApi
import com.example.musikt.data.remote.Track
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MusicRepository {

    private val deezerApi: DeezerApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.deezer.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        deezerApi = retrofit.create(DeezerApi::class.java)
    }

    suspend fun getTopTracks(): List<Track> {
        return deezerApi.getTopTracks().data
    }
}