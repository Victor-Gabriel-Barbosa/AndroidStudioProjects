package com.example.aula18.database
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.aula18.daos.ItemDao
import com.example.aula18.entities.Item

@Database(entities = [Item::class], version = 1)
abstract class MeuAppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
}
