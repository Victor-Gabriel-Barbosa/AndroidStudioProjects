package com.example.mapa.data.repository

import android.util.Log
import com.example.mapa.data.local.dao.UserDao
import com.example.mapa.data.local.entity.UserEntity
import com.example.mapa.data.mapper.toDTO
import com.example.mapa.data.mapper.toEntity
import com.example.mapa.data.remote.dto.UserDTO
import com.example.mapa.data.remote.datasource.AuthRemote
import com.example.mapa.data.remote.datasource.UserRemote
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Repositório para os dados do usuário, gerenciando as fontes de dados remota e local.
 *
 * @property auth A fonte de dados remota para autenticação.
 * @property userRemote A fonte de dados remota para usuários.
 * @property userDao A fonte de dados local para usuários.
 */
class UserRepository(
    private val auth: AuthRemote,
    private val userRemote: UserRemote,
    private val userDao: UserDao
) {
    /**
     * Um fluxo que emite o estado do usuário atualmente autenticado.
     * Retorna nulo se nenhum usuário estiver logado.
     * Sincroniza automaticamente os dados do usuário do Firebase para o banco de dados local.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val user: Flow<UserEntity?> = auth.user.flatMapLatest { user ->
        if (user == null) return@flatMapLatest flowOf(null)

        flow {
            syncUser(user.uid)
            emitAll(userDao.getById(user.uid))
        }
    }

    /**
     * Sincroniza os dados de um usuário específico do Firebase para o banco de dados local.
     *
     * @param uid O ID do usuário a ser sincronizado.
     */
    suspend fun syncUser(uid: String) {
        val user = userRemote.getByUid(uid).firstOrNull()
        if (user != null) userDao.insert(user.toEntity())
    }

    /**
     * Carrega os dados de um usuário do banco de dados local.
     *
     * @param uid O ID do usuário a ser carregado.
     * @return Um Flow que emite o [UserDTO] correspondente, ou nulo se não for encontrado.
     */
    fun getUser(uid: String): Flow<UserDTO?> = channelFlow {
        launch {
            userDao.getById(uid)
                .map { usuario -> usuario?.toDTO() }
                .collectLatest { send(it) }
        }

        launch {
            userRemote.getByUid(uid)
                .catch { e -> Log.e("UserRepository", "Erro sync usuário: $e") }
                .collect { usuario -> if (usuario != null) userDao.insert(usuario.toEntity()) }
        }
    }

    /**
     * Sincroniza os dados do usuário autenticado com o Firebase.
     *
     * @param userAuth O objeto de transferência de dados do usuário autenticado.
     */
    suspend fun syncUserAuth(userAuth: UserDTO): Result<Boolean> {
        return try {
            // Tenta pegar o token diretamente do AuthRemote
            val token = auth.getFcmToken() ?: ""

            // Busca o usuário existente no banco de dados Remoto
            val user = userRemote.getByUid(userAuth.uid).firstOrNull()

            // Atualiza o token do usuário existente ou cria um novo se não existir
            updateUser(user?.copy(fcmToken = token) ?: userAuth.copy(fcmToken = token))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Atualiza os dados de um usuário no banco de dados local e no Firebase.
     * Registra um erro se a atualização remota falhar.
     *
     * @param user O objeto de transferência de dados do usuário com os dados atualizados.
     * @return Um [Result] indicando sucesso ou falha da operação.
     */
    suspend fun updateUser(user: UserDTO): Result<Boolean> {
        val previousUser = userDao.getById(user.uid).firstOrNull()

        userDao.insert(user.toEntity())
        val res = userRemote.updateByUid(user.uid, user)

        if (res.isFailure) {
            Log.e("UserRepository", "Falha ao salvar remoto", res.exceptionOrNull())
            if (previousUser != null) userDao.insert(previousUser)
            else userDao.deleteByUid(user.uid)
        }

        return res
    }

    /**
     * Atuaiza a nota do usuário e a quantidade de avaliações.
     *
     * @param contact O usuário a ser avaliado.
     * @param rating A nova nota a ser atribuída ao usuário.
     * @return Um [Result] indicando sucesso ou falha na avaliação do usuário.
     */
    suspend fun updateUserRating(contact: UserDTO, myUid: String, rating: Double): Result<Boolean> {
        return try {
            if (contact.reviewerUids.contains(myUid)) return Result.failure(Exception("Você já avaliou este usuário."))

            val ratingCont = contact.ratingCount + 1
            val averageRating = ((contact.averageRating * contact.ratingCount) + rating) / ratingCont
            val reviewers = contact.reviewerUids + myUid

            val user = contact.copy(
                averageRating = averageRating,
                ratingCount = ratingCont,
                reviewerUids = reviewers
            )

            val previousUser = userDao.getById(contact.uid).firstOrNull()

            userDao.insert(user.toEntity())
            val res = userRemote.updateByUid(contact.uid, user)

            if (res.isFailure) {
                Log.e("UserRepository", "Erro ao avaliar remoto: ${res.exceptionOrNull()}")
                if (previousUser != null) userDao.insert(previousUser)
                else userDao.deleteByUid(contact.uid)
            }

            res
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}