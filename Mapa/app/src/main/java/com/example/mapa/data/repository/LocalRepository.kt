package com.example.mapa.data.repository

import android.util.Log
import com.example.mapa.data.local.dao.LocalDao
import com.example.mapa.data.mapper.toDTO
import com.example.mapa.data.mapper.toEntity
import com.example.mapa.data.remote.dto.LocalDTO
import com.example.mapa.data.remote.datasource.LocalRemote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Repositório para os dados de Locais, gerenciando as fontes de dados remota e local.
 *
 * @property localRemote A fonte de dados remota para locais.
 * @property localDao A fonte de dados local para locais.
 */
class LocalRepository(
    private val localRemote: LocalRemote,
    private val localDao: LocalDao
) {
    /**
     * Carrega todos os locais.
     *
     * Primeiro emite os dados do banco de dados local e, em seguida, busca os dados mais recentes
     * do servidor remoto e os insere no banco de dados local.
     *
     * @return Um Flow que emite uma lista de [LocalDTO].
     */
    fun getLocais(): Flow<List<LocalDTO>> = channelFlow {
        launch {
            localDao.getAll()
                .map { lista -> lista.map { it.toDTO() } }
                .collectLatest { send(it) }
        }

        launch {
            localRemote.findAll()
                .catch { e -> Log.e("LocalRepository", "Erro sync locais: $e") }
                .collect { lista -> localDao.insertAll(lista.map { it.toEntity() }) }
        }
    }

    /**
     * Carrega todos os locais para um usuário específico.
     *
     * Primeiro emite os dados do banco de dados local para o UID fornecido e, em seguida, busca
     * os dados mais recentes do servidor remoto e os insere no banco de dados local.
     *
     * @param uid O ID do usuário para o qual carregar os locais.
     * @return Um Flow que emite uma lista de [LocalDTO].
     */
    fun getLocaisUsuario(uid: String): Flow<List<LocalDTO>> = channelFlow {
        launch {
            localDao.getByUid(uid)
                .map { lista -> lista.map { it.toDTO() } }
                .collectLatest { send(it) }
        }

        launch {
            localRemote.findByUid(uid)
                .catch { e -> Log.e("LocalRepository", "Erro sync locais: $e") }
                .collect { lista -> localDao.insertAll(lista.map { it.toEntity() }) }
        }
    }

    /**
     * Salva um novo local na fonte de dados remota.
     *
     * @param local O objeto de transferência de dados local a ser salvo.
     * @return Um [Result] indicando sucesso ou falha.
     */
    suspend fun insertLocal(local: LocalDTO): Result<Boolean> {
        val estadoAntigo = this@LocalRepository.localDao.getById(local.id).firstOrNull()

        this@LocalRepository.localDao.insert(local.toEntity())
        val res = localRemote.save(local)

        if (res.isFailure) {
            Log.e("LocalRepository", "Erro ao salvar remoto: ${res.exceptionOrNull()}")
            if (estadoAntigo != null) this@LocalRepository.localDao.insert(estadoAntigo)
            else this@LocalRepository.localDao.deleteById(local.id)
        }

        return res
    }

    /**
     * Deleta um local das fontes de dados local e remota.
     *
     * @param id O ID do local a ser deletado.
     * @return Um [Result] indicando sucesso ou falha da operação remota.
     */
    suspend fun deleteLocal(id: String): Result<Boolean> {
        val estadoAntigo = localDao.getById(id).firstOrNull()

        localDao.deleteById(id)
        val res = localRemote.deleteById(id)

        if (res.isFailure) {
            Log.e("LocalRepository", "Erro ao deletar remoto. Restaurando local.")
            if (estadoAntigo != null) localDao.insert(estadoAntigo)
            else localDao.deleteById(id)
        }

        return res
    }

    /**
     * Atualiza um local nas fontes de dados local e remota.
     *
     * @param local O objeto de transferência de dados local a ser atualizado.
     * @return Um [Result] indicando sucesso ou falha da operação remota.
     */
    suspend fun updateLocal(local: LocalDTO): Result<Boolean> {
        val estadoAntigo = this@LocalRepository.localDao.getById(local.id).firstOrNull()

        this@LocalRepository.localDao.insert(local.toEntity())
        val res = localRemote.updateById(local.id, local)

        if (res.isFailure) {
            Log.e("LocalRepository", "Erro ao atualizar remoto: ${res.exceptionOrNull()}")
            if (estadoAntigo != null) this@LocalRepository.localDao.insert(estadoAntigo)
            else this@LocalRepository.localDao.deleteById(local.id)
        }

        return res
    }
}