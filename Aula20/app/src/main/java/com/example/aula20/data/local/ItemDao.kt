package com.example.aula20.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM item")
    fun getAllItems(): Flow<List<Item>>
    @Insert
    suspend fun insert(item: Item)
    @Insert
    suspend fun insertAll(itens: List<Item>)
    @Update
    suspend fun update(item: Item)
    @Query("DELETE FROM item WHERE id = :id")
    suspend fun deleteById(id: Int)
    @Query("DELETE FROM item")
    suspend fun clearAll()
}