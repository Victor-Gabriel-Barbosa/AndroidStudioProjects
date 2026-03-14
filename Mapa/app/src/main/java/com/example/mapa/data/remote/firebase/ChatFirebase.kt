package com.example.mapa.data.remote.firebase

import android.util.Log
import androidx.core.net.toUri
import com.example.mapa.data.remote.dto.ChatDTO
import com.example.mapa.data.remote.dto.MsgDTO
import com.example.mapa.data.remote.datasource.ChatRemote
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Implementação do [com.example.mapa.data.remote.datasource.ChatRemote] que utiliza o Firebase Firestore para o banco de dados de mensagens
 * e o Firebase Storage para o armazenamento de imagens.
 *
 * @property db Instância do [com.google.firebase.firestore.FirebaseFirestore] para operações de banco de dados.
 * @property storage Instância do [com.google.firebase.storage.FirebaseStorage] para upload de arquivos.
 */
class ChatFirebase(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ChatRemote {
    /**
     * Referência à coleção de chats no Firestore
     */
    private val collection = db.collection("chats")

    /**
     * Salva uma nova mensagem em uma sala de chat e atualiza o resumo do chat.
     * As imagens da mensagem são primeiro enviadas para o Firebase Storage.
     *
     * @param id O ID do documento da sala de chat.
     * @param msg O objeto [com.example.mapa.data.remote.dto.MsgDTO] a ser salvo.
     * @param chat O objeto [com.example.mapa.data.remote.dto.ChatDTO] com o resumo atualizado a ser salvo (merge).
     * @return [Result.success] com `true` se a operação for bem-sucedida, [Result.failure] caso contrário.
     */
    override suspend fun save(id: String, msg: MsgDTO, chat: ChatDTO): Result<Boolean> =
        coroutineScope {
            return@coroutineScope try {
                val downloadUrls = msg.imgUrls.map { uri ->
                    async { uploadImg(msg.uid, uri) }
                }.awaitAll()

                val msgComImagens = msg.copy(imgUrls = downloadUrls)

                collection
                    .document(id)
                    .collection("msgs")
                    .document(msgComImagens.id)
                    .set(msgComImagens)
                    .await()

                collection
                    .document(id)
                    .set(chat, SetOptions.merge())
                    .await()

                Result.success(true)
            } catch (e: Exception) {
                Log.e("ChatFirebase", "save: ${e.message}")
                Result.failure(e)
            }
        }

    /**
     * Busca todos os chats em que um usuário participa, em tempo real.
     * Os chats são ordenados pela última mensagem enviada.
     *
     * @param uid O ID do usuário.
     * @return Um [kotlinx.coroutines.flow.Flow] que emite uma lista de [ChatDTO] sempre que houver atualizações.
     */
    override fun getByUid(uid: String): Flow<List<ChatDTO>> = callbackFlow {
        val listener = collection
            .whereArrayContains("visibleTo", uid)
            .orderBy("lastTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatFirebase", "getByUid: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val chats = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ChatDTO::class.java)?.copy(id = doc.id)
                    }
                    trySend(chats)
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Busca todas as mensagens de uma sala de chat específica, em tempo real.
     * As mensagens são ordenadas pela data de envio.
     *
     * @param id O ID da sala de chat.
     * @return Um [Flow] que emite uma lista de [MsgDTO] sempre que houver atualizações.
     */
    override fun getById(id: String): Flow<List<MsgDTO>> = callbackFlow {
        val listener = collection
            .document(id)
            .collection("msgs")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatFirebase", "getById: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val msgs = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(MsgDTO::class.java)?.copy(id = doc.id)
                    }
                    trySend(msgs)
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Marca todas as mensagens não lidas de um usuário em uma sala como lidas.
     *
     * @param id O ID da sala de chat.
     * @param uid O ID do usuário cujas mensagens não são o alvo (ou seja, o destinatário).
     * @return [Result.success] com `true` se a operação for bem-sucedida, [Result.failure] caso contrário.
     */
    override suspend fun updateMsgsReadById(id: String, uid: String): Result<Boolean> {
        return try {
            val msgsRef = collection.document(id).collection("msgs")

            val snapshot = msgsRef
                .whereEqualTo("uid", uid)
                .whereEqualTo("read", false)
                .get()
                .await()

            if (snapshot.isEmpty) return Result.success(true)

            val batch = db.batch()
            for (doc in snapshot.documents) batch.update(doc.reference, "read", true)
            batch.commit().await()

            Result.success(true)
        } catch (e: Exception) {
            Log.e("ChatFirebase", "updateMsgsReadById: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Remove uma mensagem específica de uma sala de chat.
     *
     * @param id O ID da sala de chat.
     * @param msgId O ID da mensagem a ser removida.
     * @return [Result.success] com `true` se a operação for bem-sucedida, [Result.failure] caso contrário.
     */
    override suspend fun deleteMsgById(id: String, msgId: String): Result<Boolean> {
        return try {
            collection
                .document(id)
                .collection("msgs")
                .document(msgId)
                .delete()
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Log.e("ChatFirebase", "deleteMsgById: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Atualiza o conteúdo de uma mensagem existente.
     *
     * @param id O ID da sala de chat.
     * @param msgId O ID da mensagem a ser atualizada.
     * @param msg O objeto [MsgDTO] com os novos dados.
     * @return [Result.success] com `true` se a operação for bem-sucedida, [Result.failure] caso contrário.
     */
    override suspend fun updateMsgById(id: String, msgId: String, msg: MsgDTO): Result<Boolean> {
        return try {
            collection
                .document(id)
                .collection("msgs")
                .document(msgId)
                .set(msg, SetOptions.merge())
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Log.e("ChatFirebase", "updateMsgById: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Remove o usuário da lista de usuarios que podem ver o chat.
     *
     * @param id O ID da sala.
     * @param uid O ID do usuário a ser removido.
     */
    override suspend fun hideChat(id: String, uid: String): Result<Boolean> {
        return try {
            collection
                .document(id)
                .update("visibleTo", FieldValue.arrayRemove(uid))
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Log.e("ChatFirebase", "hideChat: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Faz o upload de uma imagem para o Firebase Storage.
     *
     * @param uid O ID do usuário remetente, usado para organizar as imagens.
     * @param uri A URI da imagem a ser enviada.
     * @return A URL de download da imagem após o upload.
     */
    private suspend fun uploadImg(uid: String, uri: String): String {
        val filename = "${uid}/${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child("chats_imgs/$filename")
        ref.putFile(uri.toUri()).await()
        return ref.downloadUrl.await().toString()
    }
}