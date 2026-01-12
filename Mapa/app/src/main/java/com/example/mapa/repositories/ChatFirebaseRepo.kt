package com.example.mapa.repositories

import androidx.core.net.toUri
import com.example.mapa.models.Chat
import com.example.mapa.models.Mensagem
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
 * Implementação do repositório de chats utilizando Firebase Firestore
 */
class ChatFirebaseRepo(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : ChatRepo {

    // Referência à coleção de chats no Firestore
    private val collection = db.collection("chats")

    // Salva uma mensagem e atualiza o resumo do chat no Firestore
    override suspend fun save(salaId: String, mensagem: Mensagem, chatResumo: Chat): Result<Boolean> = coroutineScope {
        return@coroutineScope try {
            val downloadUrls = mensagem.imgUrls.map { uri ->
                async { uploadImg(mensagem.remetenteUid, uri) }
            }.awaitAll()

            collection
                .document(salaId)
                .collection("mensagens")
                .add(mensagem.copy(imgUrls = downloadUrls))
                .await()

            collection
                .document(salaId)
                .set(chatResumo, SetOptions.merge())
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Busca todos os chats do usuário pelo UID
    override fun findByUid(uid: String): Flow<List<Chat>> = callbackFlow {
        val listener = collection
            .whereArrayContains("participantes", uid)
            .orderBy("ultimoTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val chats = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Chat::class.java)?.copy(salaId = doc.id)
                    }
                    trySend(chats)
                }
            }
        awaitClose { listener.remove() }
    }

    // Busca todas as mensagens de um chat pelo ID
    override fun findById(salaId: String): Flow<List<Mensagem>> = callbackFlow {
        val listener = collection
            .document(salaId)
            .collection("mensagens")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val msgs = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Mensagem::class.java)?.copy(id = doc.id)
                    }
                    trySend(msgs)
                }
            }
        awaitClose { listener.remove() }
    }

    // Marca todas as mensagens do usuário como lidas
    override suspend fun updateMsgsLidasById(salaId: String, uid: String): Result<Boolean> {
        return try {
            val msgsRef = collection.document(salaId).collection("mensagens")

            val snapshot = msgsRef
                .whereEqualTo("remetenteUid", uid)
                .whereEqualTo("lido", false)
                .get()
                .await()

            if (snapshot.isEmpty) return Result.success(true)

            val batch = db.batch()
            for (doc in snapshot.documents) batch.update(doc.reference, "lido", true)
            batch.commit().await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Remove uma mensagem pelo ID
    override suspend fun deleteMsgById(salaId: String, mensagemId: String): Result<Boolean> {
        return try {
            collection
                .document(salaId)
                .collection("mensagens")
                .document(mensagemId)
                .delete()
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Atualiza o texto de uma mensagem pelo ID
    override suspend fun updateMsgById(salaId: String, mensagemId: String, novaMensagem: Mensagem): Result<Boolean> {
        return try {
            collection
                .document(salaId)
                .collection("mensagens")
                .document(mensagemId)
                .set(novaMensagem, SetOptions.merge())
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Faz upload da imagem do local para o Storage
    private suspend fun uploadImg(uid: String, uri: String): String {
        val filename = "${uid}/${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child("imgs_msgs/$filename")
        ref.putFile(uri.toUri()).await()
        return ref.downloadUrl.await().toString()
    }
}