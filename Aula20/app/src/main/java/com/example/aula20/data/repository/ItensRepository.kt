package com.example.aula20.data.repository

import com.example.aula20.data.api.ItensApiDataSource
import com.example.aula20.data.local.ItemDao
import com.example.aula20.data.local.Item

class ItensRepository(
    private val itemDao: ItemDao,
    private val itensApiDataSource: ItensApiDataSource
) {
    val allItems = itemDao.getAllItems()
    suspend fun insert(item: Item) {
        itemDao.insert(item)
        itensApiDataSource.postItem(item)
    }

    suspend fun update(item: Item) {
        itemDao.update(item)
        itensApiDataSource.putItem(item)
    }
    suspend fun deleteById(id: Int) {
        itemDao.deleteById(id)
        itensApiDataSource.deleteItem(id)
    }
    suspend fun clearAllLocal() {
        itemDao.clearAll()
    }

    suspend fun refreshItems() {
        val itensApi = itensApiDataSource.getItems()
        clearAllLocal()
        itemDao.insertAll(itensApi)
    }
}