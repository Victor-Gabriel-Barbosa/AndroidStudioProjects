package com.example.aula18.repository

import com.example.aula18.daos.ItemDao
import com.example.aula18.entities.Item

class ItensRepository(private val itemDao: ItemDao) {
    val allItems = itemDao.getAllItems()
    suspend fun insert(item: Item) {
        itemDao.insert(item)
    }
    suspend fun update(item: Item) {
        itemDao.update(item)
    }
    suspend fun deleteById(id: Int) {
        itemDao.deleteById(id)
    }
    suspend fun deleteAll() {
        itemDao.deleteAll()
    }
}