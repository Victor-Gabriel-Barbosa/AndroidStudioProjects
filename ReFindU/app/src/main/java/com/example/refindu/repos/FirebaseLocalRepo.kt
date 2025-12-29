package com.example.refindu.repos

import android.net.Uri
import com.example.refindu.models.Local
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

// Implementação do repositório utilizando Firebase Firestore para dados e Storage para mídia
class FirebaseLocalRepo(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : LocalRepo {

    // Realiza upload da imagem no Storage (se existir) antes de salvar os metadados no Firestore
    override suspend fun save(local: Local): Result<Boolean> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("Usuário não logado")
            val downloadUrl = if (local.imgUri != null) uploadImg(uid, local.imgUri) else null

            // Mapeamento e persistência no Firestore
            val localData = hashMapOf(
                "uid" to uid,
                "latitude" to local.latitude,
                "longitude" to local.longitude,
                "radius" to local.radius,
                "name" to local.name,
                "category" to local.category,
                "imgUrl" to downloadUrl,
                "createAt" to FieldValue.serverTimestamp()
            )

            db.collection("saved_locals").add(localData).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Retorna um Flow observável que emite atualizações em tempo real da coleção 'saved_locals'
    override fun findAll(): Flow<List<Local>> = callbackFlow {
        val listener = db.collection("saved_locals")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                snapshot?.let { trySend(it.documents.map { doc -> doc.toLocal() }) }
            }
        awaitClose { listener.remove() }
    }

    // Retorna um Flow observável que emite atualizações em tempo real da coleção 'saved_locals' filtrada por 'uid'
    override fun findByUid(uid: String): Flow<List<Local>> = callbackFlow {
        val listener = db.collection("saved_locals")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                snapshot?.let { trySend(it.documents.map { doc -> doc.toLocal() }) }
            }
        awaitClose { listener.remove() }
    }

    // Atualiza dados de um local existente e gerencia a substituição da imagem no Storage
    override suspend fun updateById(localId: String, local: Local): Result<Boolean> {
        return try {
            // Remove imagem antiga e faz upload da nova se existir
            val newImageUrl = if (local.imgUri != null) {
                deleteImg(localId)
                uploadImg(local.uid, local.imgUri)
            } else local.imgUrl

            val updatedLocal = local.copy(imgUrl = newImageUrl)

            val updateMap = mapOf(
                "name" to updatedLocal.name,
                "category" to updatedLocal.category,
                "radius" to updatedLocal.radius,
                "imgUrl" to updatedLocal.imgUrl
            )

            db.collection("saved_locals").document(localId).update(updateMap).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Remove o documento do Firestore e a imagem associada no Storage
    override suspend fun deleteById(localId: String): Result<Boolean> {
        return try {
            db.collection("saved_locals").document(localId).delete().await()
            deleteImg(localId)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Função de extensão para centralizar o mapeamento
    private fun DocumentSnapshot.toLocal(): Local {
        return Local(
            id = this.id,
            uid = this.getString("uid") ?: "",
            latitude = this.getDouble("latitude") ?: 0.0,
            longitude = this.getDouble("longitude") ?: 0.0,
            radius = this.getDouble("radius") ?: 0.0,
            name = this.getString("name") ?: "",
            category = this.getString("category") ?: "",
            imgUrl = this.getString("imgUrl")
        )
    }

    // Realiza upload para 'local_imgs/{uid}/{uuid}' e retorna URL de download
    private suspend fun uploadImg(uid: String, uri: Uri): String {
        val filename = "${uid}/${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child("local_imgs/$filename")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    // Recupera a URL da imagem no Firestore e remove o arquivo do Storage
    private suspend fun deleteImg(localId: String) {
        val snapshot = db.collection("saved_locals").document(localId).get().await()
        val imgUrl = snapshot.getString("imgUrl")

        if (!imgUrl.isNullOrEmpty()) {
            try {
                storage.getReferenceFromUrl(imgUrl).delete().await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}