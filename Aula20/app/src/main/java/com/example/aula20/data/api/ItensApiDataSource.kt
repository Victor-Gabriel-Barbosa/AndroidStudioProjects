package com.example.aula20.data.api

import com.example.aula20.data.local.Item
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json

class ItensApiDataSource {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    val baseUrl = "http://bsimc.freeddns.org:2025/api/items"
    suspend fun getItems(): List<Item> {
        return client.get(baseUrl).body()
    }

    suspend fun postItem(item: Item) {
        client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(item)
        }
    }

    suspend fun putItem(item: Item) {
        client.put("$baseUrl/${item.id}") {
            contentType(ContentType.Application.Json)
            setBody(item)
        }
    }

    suspend fun deleteItem(id: Int) {
        client.delete("$baseUrl/$id")
    }
}