package com.example.mapa.repositories

import android.util.Log
import androidx.core.net.toUri
import com.example.mapa.models.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Implementação do repositório de usuários utilizando Firebase Firestore e Storage
 */
class UsuarioFirebaseRepo(
    db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
) : UsuarioRepo {

    // Referência à coleção de usuários no Firestore
    private val collection = db.collection("usuarios")

    // Salva um usuário no Firestore
    override suspend fun save(usuario: Usuario): Result<Boolean> {
        return try {
            collection.document(usuario.uid)
                .set(usuario)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Log.e("UsuarioRepo", "Erro ao salvar: ", e)
            Result.failure(e)
        }
    }

    // Carrega todos os usuários do Firestore
    override fun findAll(): Flow<List<Usuario>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val usuarios = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Usuario::class.java)
                }
                trySend(usuarios)
            }
        }

        awaitClose { listener.remove() }
    }

    // Carrega um usuário pelo ID do usuário
    override fun findByUid(uid: String): Flow<List<Usuario>> = callbackFlow {
        val listener = collection.document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val usuario = snapshot.toObject(Usuario::class.java)
                if (usuario != null) trySend(listOf(usuario))
                else trySend(emptyList())
            } else trySend(emptyList())
        }

        awaitClose { listener.remove() }
    }

    // Atualiza um usuário pelo ID do usuário
    override suspend fun updateByUid(usuarioUid: String, usuario: Usuario): Result<Boolean> {
        return try {
            val downloadUrl = if (usuario.foto != null && !usuario.foto.startsWith("http")) uploadImg(usuarioUid, usuario.foto)
            else usuario.foto

            collection.document(usuarioUid)
                .set(usuario.copy(foto = downloadUrl), SetOptions.merge())
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Remove um usuário pelo ID do usuário
    override suspend fun deleteByUid(usuarioUid: String): Result<Boolean> {
        return try {
            collection.document(usuarioUid)
                .delete()
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Faz upload da foto do usuário para o Storage
    private suspend fun uploadImg(uid: String, uri: String): String {
        val filename = "${uid}.jpg"
        val ref = storage.reference.child("fotos_usuarios/$filename")
        ref.putFile(uri.toUri()).await()
        return ref.downloadUrl.await().toString()
    }
}