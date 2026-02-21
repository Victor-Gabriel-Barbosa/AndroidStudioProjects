package com.example.mapa.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mapa.data.local.dao.ChatDao
import com.example.mapa.data.local.dao.LocalDao
import com.example.mapa.data.local.dao.UsuarioDao
import com.example.mapa.data.local.entity.ChatEntity
import com.example.mapa.data.local.entity.LocalEntity
import com.example.mapa.data.local.entity.MensagemEntity
import com.example.mapa.data.local.entity.UsuarioEntity

@Database(
    entities = [
        UsuarioEntity::class,
        LocalEntity::class,
        ChatEntity::class,
        MensagemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun localDao(): LocalDao
    abstract fun chatDao(): ChatDao
}