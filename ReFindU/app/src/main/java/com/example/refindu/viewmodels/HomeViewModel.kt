package com.example.refindu.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.refindu.models.Local
import com.example.refindu.repos.AuthRepo
import com.example.refindu.repos.LocalRepo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ViewModel para Home
class HomeViewModel(
    private val repository: LocalRepo
) : ViewModel() {

    // Estado de carregamento
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _userMessage = Channel<String>(Channel.BUFFERED)
    val userMessage = _userMessage.receiveAsFlow()

    // Lista de locais salvos
    val locals: StateFlow<List<Local>> =
        repository.findAll()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // Adiciona um novo local
    fun addLocal(local: Local) {
        viewModelScope.launch {
            _isLoading.value = true

            repository.save(local)
                .onSuccess { _userMessage.send("Local salvo com sucesso!") }
                .onFailure { e -> _userMessage.send("Erro ao salvar: ${e.message}") }

            _isLoading.value = false
        }
    }

    // Edita um local existente
    fun editLocal(local: Local) {
        viewModelScope.launch {
            _isLoading.value = true

            repository.updateById(local.id, local)
                .onSuccess { _userMessage.send("Local atualizado!") }
                .onFailure { _userMessage.send("Falha ao atualizar.") }

            _isLoading.value = false
        }
    }

    // Remove um local existente
    fun removeLocal(id: String) {
        viewModelScope.launch {
            _isLoading.value = true

            repository.deleteById(id)
                .onSuccess { _userMessage.send("Local removido com sucesso!") }
                .onFailure { e -> _userMessage.send("Erro ao remover: ${e.message}") }

            _isLoading.value = false
        }
    }
}