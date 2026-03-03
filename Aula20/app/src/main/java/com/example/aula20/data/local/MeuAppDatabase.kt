package com.example.aula20.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Item::class], version = 1)
abstract class MeuAppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
}