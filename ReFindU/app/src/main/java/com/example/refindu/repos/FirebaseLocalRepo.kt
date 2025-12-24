package com.example.refindu.repos

import android.net.Uri
import com.example.refindu.models.Local
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
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
            var downloadUrl: String? = null

            // Upload da imagem para o Storage
            if (local.imageUri != null) {
                val filename = "${uid}/${UUID.randomUUID()}.jpg"
                val ref = storage.reference.child("local_images/$filename")

                ref.putFile(local.imageUri).await()
                downloadUrl = ref.downloadUrl.await().toString()
            }

            // Mapeamento e persistência no Firestore
            val localData = hashMapOf(
                "uid" to uid,
                "latitude" to local.latitude,
                "longitude" to local.longitude,
                "radius" to local.radius,
                "name" to local.name,
                "category" to local.category,
                "imageUrl" to downloadUrl,
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
                if (snapshot != null) {
                    val lista = snapshot.documents.mapNotNull { doc ->
                        Local(
                            id = doc.id,
                            uid = doc.getString("uid") ?: "",
                            latitude = doc.getDouble("latitude") ?: 0.0,
                            longitude = doc.getDouble("longitude") ?: 0.0,
                            radius = doc.getDouble("radius") ?: 0.0,
                            name = doc.getString("name") ?: "",
                            category = doc.getString("category") ?: "",
                            imageUrl = doc.getString("imageUrl")
                        )
                    }
                    trySend(lista)
                }
            }
        awaitClose { listener.remove() }
    }

    // Atualiza dados de um local existente e gerencia a substituição da imagem no Storage
    override suspend fun updateById(localId: String, local: Local): Result<Boolean> {
        return try {
            // Remove imagem antiga antes da atualização
            deleteImage(localId)

            // Define nova URL: Upload de nova imagem ou mantém a URL existente (se não houver nova Uri)
            val newImageUrl = if (local.imageUri != null) uploadImage(local.uid, local.imageUri) else local.imageUrl

            val updatedLocal = local.copy(imageUrl = newImageUrl)

            val updateMap = mapOf(
                "name" to updatedLocal.name,
                "category" to updatedLocal.category,
                "radius" to updatedLocal.radius,
                "imageUrl" to updatedLocal.imageUrl
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
            deleteImage(localId)
            db.collection("saved_locals").document(localId).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Helper: Realiza upload para 'local_images/{uid}/{uuid}' e retorna URL de download
    private suspend fun uploadImage(uid: String, uri: Uri): String {
        val filename = "${uid}/${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child("local_images/$filename")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    // Helper: Recupera a URL da imagem no Firestore e remove o arquivo do Storage
    private suspend fun deleteImage(localId: String) {
        val snapshot = db.collection("saved_locals").document(localId).get().await()
        val imageUrl = snapshot.getString("imageUrl")

        if (!imageUrl.isNullOrEmpty()) {
            try {
                storage.getReferenceFromUrl(imageUrl).delete().await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}