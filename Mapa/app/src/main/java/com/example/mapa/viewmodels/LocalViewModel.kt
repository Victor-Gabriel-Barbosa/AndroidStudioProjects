package com.example.mapa.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapa.repositories.AuthRepo
import com.example.mapa.repositories.LocalRepo
import com.example.mapa.models.Local
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel para locais salvos
 */
class LocalViewModel(
    private val localRepo: LocalRepo,
    authRepo: AuthRepo
) : ViewModel() {
    // Estado de carregamento
    private val _carregando = MutableStateFlow(false)
    val carregando = _carregando.asStateFlow()

    // Canal de mensagens
    private val _mensagens = Channel<String>(Channel.BUFFERED)
    val mensagens = _mensagens.receiveAsFlow()

    // Lista de locais salvos
    val locais: StateFlow<List<Local>> =
        localRepo.findAll()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val locaisUsuario: StateFlow<List<Local>> = authRepo.usuarioState
        .flatMapLatest { user ->
            if (user?.uid.isNullOrBlank()) flowOf(emptyList())
            else localRepo.findByUid(user.uid)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    // Adiciona um novo local
    fun adicionarLocal(local: Local) {
        viewModelScope.launch {
            _carregando.value = true

            localRepo.save(local)
                .onSuccess { _mensagens.send("Local salvo com sucesso!") }
                .onFailure { e ->
                    Log.e("LocalViewModel", "adicionarLocal: ${e.message}")
                    _mensagens.send("Erro ao salvar: ${e.message}")
                }

            _carregando.value = false
        }
    }

    // Edita um local existente
    fun editarLocal(local: Local) {
        viewModelScope.launch {
            _carregando.value = true

            localRepo.updateById(local.id, local)
                .onSuccess { _mensagens.send("Local atualizado!") }
                .onFailure { e ->
                    Log.e("LocalViewModel", "editarLocal: ${e.message}")
                    _mensagens.send("Falha ao atualizar: ${e.message}")
                }

            _carregando.value = false
        }
    }

    // Remove um local existente
    fun removerLocal(id: String) {
        viewModelScope.launch {
            _carregando.value = true

            localRepo.deleteById(id)
                .onSuccess { _mensagens.send("Local removido com sucesso!") }
                .onFailure { e ->
                    Log.e("LocalViewModel", "removerLocal: ${e.message}")
                    _mensagens.send("Erro ao remover: ${e.message}")
                }

            _carregando.value = false
        }
    }
}