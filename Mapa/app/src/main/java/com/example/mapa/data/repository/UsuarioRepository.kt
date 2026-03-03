package com.example.mapa.data.repository

import android.util.Log
import com.example.mapa.data.local.dao.UsuarioDao
import com.example.mapa.data.local.entity.UsuarioEntity
import com.example.mapa.data.mapper.toDTO
import com.example.mapa.data.mapper.toEntity
import com.example.mapa.data.remote.dto.UsuarioDTO
import com.example.mapa.data.remote.datasource.AuthRemote
import com.example.mapa.data.remote.datasource.UsuarioRemote
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
 * @property usuarioRemote A fonte de dados remota para usuários.
 * @property usuarioDao A fonte de dados local para usuários.
 */
class UsuarioRepository(
    auth: AuthRemote,
    private val usuarioRemote: UsuarioRemote,
    private val usuarioDao: UsuarioDao
) {
    /**
     * Um fluxo que emite o estado do usuário atualmente autenticado.
     * Retorna nulo se nenhum usuário estiver logado.
     * Sincroniza automaticamente os dados do usuário do Firebase para o banco de dados local.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val usuario: Flow<UsuarioEntity?> = auth.usuario.flatMapLatest { usuario ->
        if (usuario == null) return@flatMapLatest flowOf(null)

        flow {
            syncUsuario(usuario.uid)
            emitAll(usuarioDao.getById(usuario.uid))
        }
    }

    /**
     * Sincroniza os dados de um usuário específico do Firebase para o banco de dados local.
     *
     * @param uid O ID do usuário a ser sincronizado.
     */
    suspend fun syncUsuario(uid: String) {
        val usuario = usuarioRemote.findByUid(uid).firstOrNull()
        if (usuario != null) usuarioDao.insert(usuario.toEntity())
    }

    /**
     * Sincroniza os dados de um contato específico do Firebase para o banco de dados local.
     * Registra se o contato foi sincronizado com sucesso ou não encontrado.
     *
     * @param uid O ID do contato a ser sincronizado.
     */
    suspend fun syncContato(uid: String) {
        val contato = usuarioRemote.findByUid(uid).firstOrNull()

        if (contato != null) {
            usuarioDao.insert(contato.toEntity())
            Log.d("UserRepository", "syncContato: ${contato.nome}")
        } else Log.e("UserRepository", "Contato não encontrado no Firebase: $uid")
    }

    /**
     * Carrega os dados de um usuário do banco de dados local.
     *
     * @param uid O ID do usuário a ser carregado.
     * @return Um Flow que emite o [UsuarioDTO] correspondente, ou nulo se não for encontrado.
     */
    fun getUsuario(uid: String): Flow<UsuarioDTO?> = channelFlow {
        launch {
            usuarioDao.getById(uid)
                .map { usuario -> usuario?.toDTO() }
                .collectLatest { send(it) }
        }

        launch {
            usuarioRemote.findByUid(uid)
                .catch { e -> Log.e("UserRepository", "Erro sync usuário: $e") }
                .collect { usuario -> if (usuario != null) usuarioDao.insert(usuario.toEntity()) }
        }
    }

    /**
     * Atualiza os dados de um usuário no banco de dados local e no Firebase.
     * Registra um erro se a atualização remota falhar.
     *
     * @param usuario O objeto de transferência de dados do usuário com os dados atualizados.
     * @return Um [Result] indicando sucesso ou falha da operação.
     */
    suspend fun updateUsuario(usuario: UsuarioDTO): Result<Boolean> {
        val estadoAntigo = usuarioDao.getById(usuario.uid).firstOrNull()

        usuarioDao.insert(usuario.toEntity())
        val res = usuarioRemote.updateByUid(usuario.uid, usuario)

        if (res.isFailure) {
            Log.e("UserRepository", "Falha ao salvar remoto", res.exceptionOrNull())
            if (estadoAntigo != null) usuarioDao.insert(estadoAntigo)
            else usuarioDao.deleteById(usuario.uid)
        }

        return res
    }

    /**
     * Atuaiza a nota do usuário e a quantidade de avaliações.
     *
     * @param contato O usuário a ser avaliado.
     * @param notaNova A nova nota a ser atribuída ao usuário.
     * @return Um [Result] indicando sucesso ou falha na avaliação do usuário.
     */
    suspend fun updateUsuarioNota(contato: UsuarioDTO, meuUid: String, nota: Double): Result<Boolean> {
        return try {
            if (contato.avaliadores.contains(meuUid)) return Result.failure(Exception("Você já avaliou este usuário."))

            val novaQtd = contato.notaQtd + 1
            val novaMedia = ((contato.notaMedia * contato.notaQtd) + nota) / novaQtd
            val novosAvaliadores = contato.avaliadores + meuUid

            val usuarioAtualizado = contato.copy(
                notaMedia = novaMedia,
                notaQtd = novaQtd,
                avaliadores = novosAvaliadores
            )

            val estadoAntigo = usuarioDao.getById(contato.uid).firstOrNull()

            usuarioDao.insert(usuarioAtualizado.toEntity())
            val res = usuarioRemote.updateByUid(contato.uid, usuarioAtualizado)

            if (res.isFailure) {
                Log.e("UserRepository", "Erro ao avaliar remoto: ${res.exceptionOrNull()}")
                if (estadoAntigo != null) usuarioDao.insert(estadoAntigo)
                else usuarioDao.deleteById(contato.uid)
            }

            res
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}