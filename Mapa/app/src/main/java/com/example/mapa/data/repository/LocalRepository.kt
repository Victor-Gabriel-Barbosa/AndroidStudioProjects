package com.example.mapa.data.repository

import android.util.Log
import com.example.mapa.data.local.dao.LocalDao
import com.example.mapa.data.mapper.toDomain
import com.example.mapa.data.mapper.toEntity
import com.example.mapa.data.remote.dto.LocalDTO
import com.example.mapa.data.remote.source.LocalRemote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Repositório para os dados de Locais, gerenciando as fontes de dados remota e local.
 *
 * @property remote A fonte de dados remota para locais.
 * @property local A fonte de dados local para locais.
 */
class LocalRepository(
    private val remote: LocalRemote,
    private val local: LocalDao
) {
    /**
     * Carrega todos os locais.
     *
     * Primeiro emite os dados do banco de dados local e, em seguida, busca os dados mais recentes
     * do servidor remoto e os insere no banco de dados local.
     *
     * @return Um Flow que emite uma lista de [LocalDTO].
     */
    fun carregarLocais(): Flow<List<LocalDTO>> = channelFlow {
        launch {
            local.getAll()
                .map { lista -> lista.map { it.toDomain() } }
                .collectLatest { send(it) }
        }

        launch {
            remote.findAll()
                .catch { e -> Log.e("LocalRepository", "Erro sync locais: $e") }
                .collect { lista -> local.insertAll(lista.map { it.toEntity() }) }
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
    fun carregarLocaisUsuario(uid: String): Flow<List<LocalDTO>> = channelFlow {
        launch {
            local.getByUid(uid)
                .map { lista -> lista.map { it.toDomain() } }
                .collectLatest { send(it) }
        }

        launch {
            remote.findByUid(uid)
                .catch { e -> Log.e("LocalRepository", "Erro sync locais: $e") }
                .collect { lista -> local.insertAll(lista.map { it.toEntity() }) }
        }
    }

    /**
     * Salva um novo local na fonte de dados remota.
     *
     * @param local O objeto de transferência de dados local a ser salvo.
     * @return Um [Result] indicando sucesso ou falha.
     */
    suspend fun salvarLocal(local: LocalDTO): Result<Boolean> {
        val estadoAntigo = this@LocalRepository.local.getById(local.id).firstOrNull()

        this@LocalRepository.local.insert(local.toEntity())
        val res = remote.save(local)

        if (res.isFailure) {
            Log.e("LocalRepository", "Erro ao salvar remoto: ${res.exceptionOrNull()}")
            if (estadoAntigo != null) this@LocalRepository.local.insert(estadoAntigo)
            else this@LocalRepository.local.deleteById(local.id)
        }

        return res
    }

    /**
     * Deleta um local das fontes de dados local e remota.
     *
     * @param id O ID do local a ser deletado.
     * @return Um [Result] indicando sucesso ou falha da operação remota.
     */
    suspend fun deletarLocal(id: String): Result<Boolean> {
        val estadoAntigo = local.getById(id).firstOrNull()

        local.deleteById(id)
        val res = remote.deleteById(id)

        if (res.isFailure) {
            Log.e("LocalRepository", "Erro ao deletar remoto. Restaurando local.")
            if (estadoAntigo != null) local.insert(estadoAntigo)
            else local.deleteById(id)
        }

        return res
    }

    /**
     * Atualiza um local nas fontes de dados local e remota.
     *
     * @param local O objeto de transferência de dados local a ser atualizado.
     * @return Um [Result] indicando sucesso ou falha da operação remota.
     */
    suspend fun atualizarLocal(local: LocalDTO): Result<Boolean> {
        val estadoAntigo = this@LocalRepository.local.getById(local.id).firstOrNull()

        this@LocalRepository.local.insert(local.toEntity())
        val res = remote.updateById(local.id, local)

        if (res.isFailure) {
            Log.e("LocalRepository", "Erro ao atualizar remoto: ${res.exceptionOrNull()}")
            if (estadoAntigo != null) this@LocalRepository.local.insert(estadoAntigo)
            else this@LocalRepository.local.deleteById(local.id)
        }

        return res
    }

    /**
     * Marca um local como entregue nas fontes de dados local e remota.
     *
     * @param id O ID do local a ser marcado como entregue.
     * @return Um [Result] indicando sucesso ou falha da operação remota.
     */
    suspend fun marcarComoEntregue(id: String): Result<Boolean> {
        val estadoAntigo = local.getById(id).firstOrNull()

        local.updateEntregueById(id)
        val res = remote.updateEntregueById(id)

        if (res.isFailure) {
            Log.e("LocalRepository", "Erro ao atualizar remoto: ${res.exceptionOrNull()}")
            if (estadoAntigo != null) local.insert(estadoAntigo)
            else local.deleteById(id)
        }

        return res
    }
}