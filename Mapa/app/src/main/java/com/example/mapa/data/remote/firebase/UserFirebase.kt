package com.example.mapa.data.remote.firebase

import android.util.Log
import androidx.core.net.toUri
import com.example.mapa.data.remote.dto.UserDTO
import com.example.mapa.data.remote.datasource.UserRemote
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Implementação do [UserRemote] que utiliza o Firebase Firestore para o banco de dados
 * e o Firebase Storage para o armazenamento da foto de perfil do usuário.
 *
 * @param db Instância do [FirebaseFirestore] para operações de banco de dados.
 * @param storage Instância do [FirebaseStorage] para upload de arquivos.
 */
class UserFirebase(
    db: FirebaseFirestore,
    private val storage: FirebaseStorage,
) : UserRemote {
    /**
     * Referência à coleção de usuários no Firestore
     */
    private val collection = db.collection("users")

    /**
     * Salva um novo usuário no Firestore. O ID do documento será o UID do usuário.
     *
     * @param user O objeto [com.example.mapa.data.remote.dto.UserDTO] a ser salvo.
     * @return [Result.success] com `true` se a operação for bem-sucedida, [Result.failure] caso contrário.
     */
    override suspend fun save(user: UserDTO): Result<Boolean> {
        return try {
            collection.document(user.uid)
                .set(user)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Log.e("UserFirebase", "save: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Carrega todos os usuários do Firestore em tempo real.
     *
     * @return Um [kotlinx.coroutines.flow.Flow] que emite uma lista de [UserDTO] sempre que houver atualizações.
     */
    override fun getAll(): Flow<List<UserDTO>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("UserFirebase", "getAll: ${error.message}")
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val usuarios = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(UserDTO::class.java)
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
     * @return Um [Flow] que emite um objeto [UserDTO] sempre que houver uma atualização.
     */
    override fun getByUid(uid: String): Flow<UserDTO?> = callbackFlow {
        val listener = collection.document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) trySend(snapshot.toObject(UserDTO::class.java))
            else trySend(null)
        }

        awaitClose { listener.remove() }
    }

    /**
     * Atualiza os dados de um usuário no Firestore. Se uma nova foto for fornecida (URI local),
     * ela será enviada para o Firebase Storage e a URL será atualizada.
     *
     * @param uid O UID do usuário a ser atualizado.
     * @param user O objeto [UserDTO] com os dados atualizados.
     * @return [Result.success] com `true` se a operação for bem-sucedida, [Result.failure] caso contrário.
     */
    override suspend fun updateByUid(uid: String, user: UserDTO): Result<Boolean> {
        return try {
            val downloadUrl = if (user.photo != null && !user.photo.startsWith("http")) uploadImg(uid, user.photo)
            else user.photo

            collection.document(uid)
                .set(user.copy(photo = downloadUrl), SetOptions.merge())
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Log.e("UserFirebase", "updateByUid: ${e.message}")
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
            Log.e("UserFirebase", "deleteByUid: ${e.message}")
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
        val ref = storage.reference.child("users_avatar/${uid}.jpg")
        ref.putFile(uri.toUri()).await()
        return ref.downloadUrl.await().toString()
    }
}