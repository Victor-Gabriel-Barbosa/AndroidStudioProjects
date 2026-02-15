package com.example.mapa.data.repository

import android.util.Log
import com.example.mapa.data.local.dao.UsuarioDao
import com.example.mapa.data.local.entity.UsuarioEntity
import com.example.mapa.data.local.mapper.toDomain
import com.example.mapa.data.local.mapper.toEntity
import com.example.mapa.data.remote.AuthRepository
import com.example.mapa.data.remote.UsuarioRepository
import com.example.mapa.data.remote.dto.Usuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class UsuarioRepository(
    authRepository: AuthRepository,
    private val usuarioRepository: UsuarioRepository,
    private val usuarioDao: UsuarioDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    @OptIn(ExperimentalCoroutinesApi::class)
    val usuario: Flow<UsuarioEntity?> = authRepository.usuarioState
        .flatMapLatest { usuario ->
            if (usuario != null) {
                syncUsuario(usuario.uid)
                usuarioDao.getById(usuario.uid)
            } else flowOf(null)
        }

    suspend fun syncUsuario(uid: String) {
        val usuarios = usuarioRepository.findByUid(uid).firstOrNull()
        val usuario = usuarios?.firstOrNull()
        if (usuario != null) usuarioDao.insert(usuario.toEntity())
    }

    suspend fun syncContato(uid: String) {
        val listaUsuarios = usuarioRepository.findByUid(uid).firstOrNull()

        val usuarioEncontrado = listaUsuarios?.firstOrNull()

        if (usuarioEncontrado != null) {
            usuarioDao.insert(usuarioEncontrado.toEntity())
            Log.d("UserRepository", "Contato sincronizado: ${usuarioEncontrado.nome}")
        } else Log.e("UserRepository", "Contato não encontrado no Firebase: $uid")
    }

    fun getUsuario(uid: String): Flow<Usuario?> {
        return usuarioDao.getById(uid).map { entity ->
            entity?.toDomain()
        }
    }

    suspend fun updateUsuario(usuario: Usuario) {
        usuarioDao.insert(usuario.toEntity())
        val res = usuarioRepository.updateByUid(usuario.uid, usuario)
        if (res.isFailure) Log.e("UserRepository", "Falha ao salvar remoto", res.exceptionOrNull())
    }
}