package com.example.mapa.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapa.data.remote.AuthRepository
import com.example.mapa.data.remote.dto.Local
import com.example.mapa.data.repository.LocalRepository
import com.example.mapa.models.LocalState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel para gerenciar locais salvos.
 *
 * @property localRepo O repositório para operações de locais.
 * @property authRepository O repositório para operações de autenticação.
 */
class LocalViewModel(
    private val localRepo: LocalRepository,
    authRepository: AuthRepository
) : ViewModel() {
    /**
     * Estado de carregamento.
     */
    private val _carregando = MutableStateFlow(false)

    /**
     * Canal para enviar mensagens de Snackbar para a UI.
     */
    private val _mensagens = Channel<String>(Channel.BUFFERED)
    val mensagens = _mensagens.receiveAsFlow()

    /**
     * Fluxo de todos os locais.
     */
    private val locaisFlow = localRepo.getLocais()

    /**
     * Fluxo de locais do usuário.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val locaisUsuarioFlow = authRepository.usuarioState
        .flatMapLatest { usuario ->
            if (usuario?.uid.isNullOrBlank()) flowOf(emptyList())
            else localRepo.getLocaisUsuario(usuario.uid)
        }

    /**
     * O estado da UI para a tela de locais.
     */
    val uiState: StateFlow<LocalState> = combine(
        locaisFlow,
        locaisUsuarioFlow,
        _carregando
    ) { locais, locaisUsuario, carregando ->
        LocalState(
            locais = locais,
            locaisUsuario = locaisUsuario,
            carregando = carregando
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LocalState(carregando = true)
    )

    /**
     * Adiciona um novo local ao repositório.
     *
     * @param local O local a ser adicionado.
     */
    fun adicionarLocal(local: Local) {
        viewModelScope.launch {
            _carregando.value = true

            localRepo.salvarLocal(local)
                .onSuccess { _mensagens.send("Local salvo com sucesso!") }
                .onFailure { _mensagens.send("Erro ao salvar: ${it.message}") }

            _carregando.value = false
        }
    }

    /**
     * Edita um local existente no repositório.
     *
     * @param local O local a ser atualizado.
     */
    fun editarLocal(local: Local) {
        viewModelScope.launch {
            _carregando.value = true

            localRepo.atualizarLocal(local)
                .onSuccess {
                    _mensagens.send("Local atualizado com sucesso!")
                }
                .onFailure { e ->
                    _mensagens.send("Salvo no dispositivo. Sincronização pendente: ${e.message}")
                }

            _carregando.value = false
        }
    }

    /**
     * Remove um local do repositório.
     *
     * @param id O ID do local a ser removido.
     */
    fun removerLocal(id: String) {
        viewModelScope.launch {
            _carregando.value = true

            localRepo.deletarLocal(id)
                .onSuccess { _mensagens.send("Local removido!") }
                .onFailure { _mensagens.send("Erro ao remover.") }

            _carregando.value = false
        }
    }
}