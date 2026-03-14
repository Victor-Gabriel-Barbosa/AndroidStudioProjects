package com.example.mapa.data.repository

import android.util.Log
import com.example.mapa.data.local.dao.LocationDao
import com.example.mapa.data.mapper.toDTO
import com.example.mapa.data.mapper.toEntity
import com.example.mapa.data.remote.dto.LocationDTO
import com.example.mapa.data.remote.datasource.LocationRemote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Repositório para os dados de Locais, gerenciando as fontes de dados remota e local.
 *
 * @property locationRemote A fonte de dados remota para locais.
 * @property locationDao A fonte de dados local para locais.
 */
class LocationRepository(
    private val locationRemote: LocationRemote,
    private val locationDao: LocationDao
) {
    /**
     * Carrega todos os locais.
     *
     * Primeiro emite os dados do banco de dados local e, em seguida, busca os dados mais recentes
     * do servidor remoto e os insere no banco de dados local.
     *
     * @return Um Flow que emite uma lista de [LocationDTO].
     */
    fun getLocations(): Flow<List<LocationDTO>> = channelFlow {
        launch {
            locationDao.getAll()
                .map { lista -> lista.map { it.toDTO() } }
                .collectLatest { send(it) }
        }

        launch {
            locationRemote.getAll()
                .catch { e -> Log.e("LocalRepository", "Erro sync locais: $e") }
                .collect { lista -> locationDao.insertAll(lista.map { it.toEntity() }) }
        }
    }

    /**
     * Carrega todos os locais para um usuário específico.
     *
     * Primeiro emite os dados do banco de dados local para o UID fornecido e, em seguida, busca
     * os dados mais recentes do servidor remoto e os insere no banco de dados local.
     *
     * @param uid O ID do usuário para o qual carregar os locais.
     * @return Um Flow que emite uma lista de [LocationDTO].
     */
    fun getLocationsUser(uid: String): Flow<List<LocationDTO>> = channelFlow {
        launch {
            locationDao.getByUid(uid)
                .map { lista -> lista.map { it.toDTO() } }
                .collectLatest { send(it) }
        }

        launch {
            locationRemote.getByUid(uid)
                .catch { e -> Log.e("LocalRepository", "Erro sync locais: $e") }
                .collect { lista -> locationDao.insertAll(lista.map { it.toEntity() }) }
        }
    }

    /**
     * Salva um novo local na fonte de dados remota.
     *
     * @param local O objeto de transferência de dados local a ser salvo.
     * @return Um [Result] indicando sucesso ou falha.
     */
    suspend fun insertLocation(local: LocationDTO): Result<Boolean> {
        val previousLocation = locationDao.getById(local.id).firstOrNull()

        locationDao.insert(local.toEntity())
        val res = locationRemote.save(local)

        if (res.isFailure) {
            Log.e("LocalRepository", "Erro ao salvar remoto: ${res.exceptionOrNull()}")
            if (previousLocation != null) locationDao.insert(previousLocation)
            else locationDao.deleteById(local.id)
        }

        return res
    }

    /**
     * Deleta um local das fontes de dados local e remota.
     *
     * @param id O ID do local a ser deletado.
     * @return Um [Result] indicando sucesso ou falha da operação remota.
     */
    suspend fun deleteLocation(id: String): Result<Boolean> {
        val previousLocation = locationDao.getById(id).firstOrNull()

        locationDao.deleteById(id)
        val res = locationRemote.deleteById(id)

        if (res.isFailure) {
            Log.e("LocalRepository", "Erro ao deletar remoto. Restaurando local.")
            if (previousLocation != null) locationDao.insert(previousLocation)
            else locationDao.deleteById(id)
        }

        return res
    }

    /**
     * Atualiza um local nas fontes de dados local e remota.
     *
     * @param local O objeto de transferência de dados local a ser atualizado.
     * @return Um [Result] indicando sucesso ou falha da operação remota.
     */
    suspend fun updateLocation(local: LocationDTO): Result<Boolean> {
        val previousLocation = locationDao.getById(local.id).firstOrNull()

        locationDao.insert(local.toEntity())
        val res = locationRemote.updateById(local.id, local)

        if (res.isFailure) {
            Log.e("LocalRepository", "Erro ao atualizar remoto: ${res.exceptionOrNull()}")
            if (previousLocation != null) locationDao.insert(previousLocation)
            else locationDao.deleteById(local.id)
        }

        return res
    }
}