package com.example.mapa.repositories

import androidx.core.net.toUri
import com.example.mapa.models.Local
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
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
 * Implementação do repositório utilizando Firebase Firestore para dados e Storage para mídia
 */
class LocalFirebaseRepo(
    db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : LocalRepo {

    private val collection = db.collection("locais")

    // Salva um novo local no Firestore e armazena a imagem no Storage
    override suspend fun save(local: Local): Result<Boolean> = coroutineScope {
        return@coroutineScope try {
            val downloadUrls = local.imgUrls.map { uri ->
                async { uploadImg(local.uid, uri) }
            }.awaitAll()

            collection
                .add(local.copy(imgUrls = downloadUrls))
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Carrega todos os locais
    override fun findAll(): Flow<List<Local>> = callbackFlow {
        val listener = collection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val locais = snapshot.documents.mapNotNull { doc ->
                        doc.toLocal()
                    }
                    trySend(locais)
                }
            }
        awaitClose { listener.remove() }
    }

    // Carrega todos os locais do usuário
    override fun findByUid(uid: String): Flow<List<Local>> = callbackFlow {
        val listener = collection
            .whereEqualTo("uid", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val locais = snapshot.documents.mapNotNull { doc ->
                        doc.toLocal()
                    }
                    trySend(locais)
                }
            }
        awaitClose { listener.remove() }
    }

    // Atualiza um local pelo ID
    override suspend fun updateById(localId: String, local: Local): Result<Boolean> =
        coroutineScope {
            return@coroutineScope try {
                val downloadUrls = local.imgUrls.map { url ->
                    async {
                        if (url.startsWith("http")) url else uploadImg(local.uid, url)
                    }
                }.awaitAll()

                collection
                    .document(localId)
                    .set(local.copy(imgUrls = downloadUrls), SetOptions.merge())
                    .await()

                Result.success(true)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // Remove um local pelo ID e as imagens correspondentes
    override suspend fun deleteById(localId: String): Result<Boolean> {
        return try {
            collection.document(localId).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Faz upload da imagem do local para o Storage
    private suspend fun uploadImg(uid: String, uri: String): String {
        val filename = "${uid}/${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child("imgs_locais/$filename")
        ref.putFile(uri.toUri()).await()
        return ref.downloadUrl.await().toString()
    }

    // Converte um DocumentSnapshot para um objeto Local
    private fun DocumentSnapshot.toLocal(): Local {
        return Local(
            id = id,
            uid = getString("uid") ?: "",
            tipo = getString("tipo") ?: "",
            latitude = getDouble("latitude") ?: 0.0,
            longitude = getDouble("longitude") ?: 0.0,
            raio = getDouble("raio") ?: 0.0,
            nome = getString("nome") ?: "",
            descricao = getString("descricao") ?: "",
            data = getDate("data"),
            imgUrls = (get("imgUrls") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        )
    }
}