package com.example.mapa.data.remote.firebase

import android.util.Log
import androidx.core.net.toUri
import com.example.mapa.data.remote.dto.LocalDTO
import com.example.mapa.data.remote.datasource.LocalRemote
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
 * Implementação de [com.example.mapa.data.remote.datasource.LocalRemote] que utiliza o Firebase Firestore para persistência de dados
 * e o Firebase Storage para o armazenamento de imagens.
 *
 * @property db Instância do [com.google.firebase.firestore.FirebaseFirestore] para operações de banco de dados.
 * @property storage Instância do [com.google.firebase.storage.FirebaseStorage] para upload de arquivos.
 */
class LocalFirebase(
    db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : LocalRemote {
    /**
     * Referência à coleção de locais no Firestore
     */
    private val collection = db.collection("locais")

    /**
     * Salva um novo local no Firestore. As imagens associadas são primeiramente enviadas
     * para o Firebase Storage e as URLs de download são salvas no documento do local.
     *
     * @param local O objeto [com.example.mapa.data.remote.dto.LocalDTO] a ser salvo.
     * @return [Result.success] com `true` se a operação for bem-sucedida, [Result.failure] caso contrário.
     */
    override suspend fun save(local: LocalDTO): Result<Boolean> = coroutineScope {
        return@coroutineScope try {
            val downloadUrls = local.imgUrls.map { uri ->
                async { uploadImg(local.uid, uri) }
            }.awaitAll()

            collection
                .document(local.id)
                .set(local.copy(imgUrls = downloadUrls))
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Log.e("LocalFirebaseRepo", "save: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Carrega todos os locais do Firestore em tempo real.
     *
     * @return Um [kotlinx.coroutines.flow.Flow] que emite uma lista de [LocalDTO] sempre que houver atualizações.
     */
    override fun findAll(): Flow<List<LocalDTO>> = callbackFlow {
        val listener = collection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("LocalFirebaseRepo", "findAll: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val locais = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(LocalDTO::class.java)?.copy(id = doc.id)
                    }
                    trySend(locais)
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Carrega todos os locais pertencentes a um usuário específico em tempo real.
     *
     * @param uid O ID do usuário.
     * @return Um [Flow] que emite uma lista de [LocalDTO] do usuário sempre que houver atualizações.
     */
    override fun findByUid(uid: String): Flow<List<LocalDTO>> = callbackFlow {
        val listener = collection
            .whereEqualTo("uid", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("LocalFirebaseRepo", "findByUid: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val locais = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(LocalDTO::class.java)?.copy(id = doc.id)
                    }
                    trySend(locais)
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Atualiza um local existente no Firestore. Novas imagens (que não começam com "http")
     * são enviadas para o Storage.
     *
     * @param localId O ID do documento do local a ser atualizado.
     * @param local O objeto [LocalDTO] com os dados atualizados.
     * @return [Result.success] com `true` se a operação for bem-sucedida, [Result.failure] caso contrário.
     */
    override suspend fun updateById(localId: String, local: LocalDTO): Result<Boolean> =
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
                Log.e("LocalFirebaseRepo", "updateById: ${e.message}")
                Result.failure(e)
            }
        }

    /**
     * Remove um local do Firestore pelo seu ID.
     * Nota: Esta implementação não remove as imagens associadas do Firebase Storage.
     *
     * @param localId O ID do documento do local a ser removido.
     * @return [Result.success] com `true` se a operação for bem-sucedida, [Result.failure] caso contrário.
     */
    override suspend fun deleteById(localId: String): Result<Boolean> {
        return try {
            collection
                .document(localId)
                .delete()
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Log.e("LocalFirebaseRepo", "deleteById: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Faz o upload de uma imagem para o Firebase Storage.
     *
     * @param uid O ID do usuário, usado para organizar as imagens no Storage.
     * @param uri A URI da imagem a ser enviada.
     * @return A URL de download da imagem após o upload.
     */
    private suspend fun uploadImg(uid: String, uri: String): String {
        val ref = storage.reference.child("fotos_locais/${uid}/${UUID.randomUUID()}.jpg")
        ref.putFile(uri.toUri()).await()
        return ref.downloadUrl.await().toString()
    }
}