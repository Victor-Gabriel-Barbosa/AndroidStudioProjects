package com.example.mapa.data.repository

import android.util.Log
import com.example.mapa.data.local.dao.LocalDao
import com.example.mapa.data.local.mapper.toDomain
import com.example.mapa.data.local.mapper.toEntity
import com.example.mapa.data.remote.LocalRepository
import com.example.mapa.data.remote.dto.Local
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class LocalRepository(
    private val remoteRepo: LocalRepository,
    private val localDao: LocalDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        iniciarSyncGlobal()
    }

    fun getLocais(): Flow<List<Local>> {
        return localDao.getAll().map { lista ->
            lista.map { it.toDomain() }
        }
    }

    fun getLocaisUsuario(uid: String): Flow<List<Local>> {
        return localDao.getByUid(uid).map { lista ->
            lista.map { it.toDomain() }
        }
    }

    private fun iniciarSyncGlobal() {
        scope.launch {
            remoteRepo.findAll().collect { locaisRemotos ->
                if (locaisRemotos.isNotEmpty()) {
                    Log.d("LocalRepository", "Sync: ${locaisRemotos.size} locais baixados.")
                    val entidades = locaisRemotos.map { it.toEntity() }
                    localDao.insertAll(entidades)
                }
            }
        }
    }

    suspend fun salvarLocal(local: Local): Result<Boolean> {
        return try {
            val res = remoteRepo.save(local)
            res
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletarLocal(id: String): Result<Boolean> {
        localDao.deleteById(id)
        return remoteRepo.deleteById(id)
    }

    suspend fun atualizarLocal(local: Local): Result<Boolean> {
        localDao.insert(local.toEntity())

        val res = remoteRepo.updateById(local.id, local)
        if (res.isFailure) Log.e("LocalRepository", "Erro ao atualizar remoto: ${res.exceptionOrNull()}")

        return res
    }
}