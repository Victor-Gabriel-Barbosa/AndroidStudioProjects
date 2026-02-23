package com.example.mapa.data.remote.source

import android.util.Log
import androidx.core.net.toUri
import com.example.mapa.data.remote.dto.UsuarioDTO
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Implementação do [UsuarioRemote] que utiliza o Firebase Firestore para o banco de dados
 * e o Firebase Storage para o armazenamento da foto de perfil do usuário.
 *
 * @param db Instância do [FirebaseFirestore] para operações de banco de dados.
 * @param storage Instância do [FirebaseStorage] para upload de arquivos.
 */
class UsuarioRemoteImpl(
    db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
) : UsuarioRemote {
    /**
     * Referência à coleção de usuários no Firestore
     */
    private val collection = db.collection("usuarios")

    /**
     * Salva um novo usuário no Firestore. O ID do documento será o UID do usuário.
     *
     * @param usuario O objeto [UsuarioDTO] a ser salvo.
     * @return [Result.success] com `true` se a operação for bem-sucedida, [Result.failure] caso contrário.
     */
    override suspend fun save(usuario: UsuarioDTO): Result<Boolean> {
        return try {
            collection.document(usuario.uid)
                .set(usuario)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Log.e("UsuarioFirebaseRepo", "save: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Carrega todos os usuários do Firestore em tempo real.
     *
     * @return Um [Flow] que emite uma lista de [UsuarioDTO] sempre que houver atualizações.
     */
    override fun findAll(): Flow<List<UsuarioDTO>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("UsuarioFirebaseRepo", "findAll: ${error.message}")
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val usuarios = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(UsuarioDTO::class.java)
                }
                trySend(usuarios)
            }
        }

        awaitClose { listener.remove() }
    }

    /**
     * Carrega um usuário específico pelo seu UID em tempo real.
     *
     * @param uid O ID do usuário a ser buscado.
     * @return Um [Flow] que emite uma lista contendo o [UsuarioDTO] (ou vazia se não encontrado)
     * sempre que houver atualizações.
     */
    override fun findByUid(uid: String): Flow<List<UsuarioDTO>> = callbackFlow {
        val listener = collection.document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("UsuarioFirebaseRepo", "findByUid: ${error.message}")
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val usuario = snapshot.toObject(UsuarioDTO::class.java)
                if (usuario != null) trySend(listOf(usuario))
                else trySend(emptyList())
            } else trySend(emptyList())
        }

        awaitClose { listener.remove() }
    }

    /**
     * Atualiza os dados de um usuário no Firestore. Se uma nova foto for fornecida (URI local),
     * ela será enviada para o Firebase Storage e a URL será atualizada.
     *
     * @param uid O UID do usuário a ser atualizado.
     * @param usuario O objeto [UsuarioDTO] com os dados atualizados.
     * @return [Result.success] com `true` se a operação for bem-sucedida, [Result.failure] caso contrário.
     */
    override suspend fun updateByUid(uid: String, usuario: UsuarioDTO): Result<Boolean> {
        return try {
            val downloadUrl = if (usuario.foto != null && !usuario.foto.startsWith("http")) uploadImg(uid, usuario.foto)
            else usuario.foto

            collection.document(uid)
                .set(usuario.copy(foto = downloadUrl), SetOptions.merge())
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Log.e("UsuarioFirebaseRepo", "updateByUid: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Remove um usuário do Firestore pelo seu UID.
     * Nota: Esta implementação não remove a foto do usuário do Firebase Storage.
     *
     * @param uid O UID do usuário a ser removido.
     * @return [Result.success] com `true` se a operação for bem-sucedida, [Result.failure] caso contrário.
     */
    override suspend fun deleteByUid(uid: String): Result<Boolean> {
        return try {
            collection.document(uid)
                .delete()
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Log.e("UsuarioFirebaseRepo", "deleteByUid: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Faz o upload da foto do usuário para o Firebase Storage.
     *
     * @param uid O UID do usuário, usado como nome do arquivo.
     * @param uri A URI da imagem a ser enviada.
     * @return A URL de download da imagem após o upload.
     */
    private suspend fun uploadImg(uid: String, uri: String): String {
        val ref = storage.reference.child("fotos_usuarios/${uid}.jpg")
        ref.putFile(uri.toUri()).await()
        return ref.downloadUrl.await().toString()
    }
}