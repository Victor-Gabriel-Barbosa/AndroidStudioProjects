package com.example.mapa.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mapa.data.local.dao.ChatDao
import com.example.mapa.data.local.dao.LocationDao
import com.example.mapa.data.local.dao.UserDao
import com.example.mapa.data.local.entity.ChatEntity
import com.example.mapa.data.local.entity.LocationEntity
import com.example.mapa.data.local.entity.MsgEntity
import com.example.mapa.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        LocationEntity::class,
        ChatEntity::class,
        MsgEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun locationDao(): LocationDao
    abstract fun chatDao(): ChatDao
}