package com.example.aula18.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.aula18.entities.Item
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM item")
    fun getAllItems(): Flow<List<Item>>
    @Insert
    suspend fun insert(item: Item)
    @Update
    suspend fun update(item: Item)
    @Query("DELETE FROM item WHERE id = :id")
    suspend fun deleteById(id: Int)
    @Query("DELETE FROM item")
    suspend fun deleteAll()
}