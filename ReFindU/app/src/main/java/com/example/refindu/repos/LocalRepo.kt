package com.example.refindu.repos

import com.example.refindu.models.Local
import kotlinx.coroutines.flow.Flow

// Interface para repositório de locais salvos
interface LocalRepo {
    suspend fun save(local: Local): Result<Boolean>
    fun findAll(): Flow<List<Local>>
    fun findByUid(uid: String): Flow<List<Local>>
    suspend fun updateById(localId: String, local: Local): Result<Boolean>
    suspend fun deleteById(localId: String): Result<Boolean>
}