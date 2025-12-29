package com.example.refindu.viewmodels

import androidx.lifecycle.ViewModel
import com.example.refindu.models.ChatSummary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatListViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _chats = MutableStateFlow<List<ChatSummary>>(emptyList())
    val chats: StateFlow<List<ChatSummary>> = _chats.asStateFlow()

    init {
        loadChats()
    }

    private fun loadChats() {
        val currentUserUid = auth.currentUser?.uid ?: return

        // Busca chats onde "participants" contém meu ID
        firestore.collection("chats")
            .whereArrayContains("participants", currentUserUid)
            .orderBy("lastTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ChatSummary::class.java)?.copy(roomId = doc.id)
                    }
                    _chats.value = list
                }
            }
    }

    fun getPartnerUid(participants: List<String>): String {
        val currentUserUid = auth.currentUser?.uid
        return participants.find { it != currentUserUid } ?: ""
    }
}