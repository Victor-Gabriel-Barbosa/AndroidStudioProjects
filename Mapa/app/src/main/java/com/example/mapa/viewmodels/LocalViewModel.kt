package com.example.mapa.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapa.data.remote.datasource.AuthRemote
import com.example.mapa.data.remote.dto.LocalDTO
import com.example.mapa.data.repository.LocalRepository
import com.example.mapa.model.LocalUiState
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
import java.util.UUID

/**
 * ViewModel para gerenciar locais salvos.
 *
 * @property localRepo O repositório para operações de locais.
 * @property authRemote O repositório para operações de autenticação.
 */
class LocalViewModel(
    private val localRepo: LocalRepository,
    authRemote: AuthRemote
) : ViewModel() {
    /**
     * Estado de carregamento.
     */
    private val _carregando = MutableStateFlow(false)

    /**
     * Canal para enviar mensagens de eventos para a UI.
     */
    private val _canal = Channel<String>(Channel.BUFFERED)
    val canal = _canal.receiveAsFlow()

    /**
     * Fluxo de todos os locais.
     */
    private val locaisFlow = localRepo.getLocais()

    /**
     * Fluxo de locais do usuário.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val locaisUsuarioFlow = authRemote.usuario
        .flatMapLatest { usuario ->
            if (usuario?.uid.isNullOrBlank()) flowOf(emptyList())
            else localRepo.getLocaisUsuario(usuario.uid)
        }

    /**
     * O estado da UI para a tela de locais.
     */
    val uiState: StateFlow<LocalUiState> = combine(
        locaisFlow,
        locaisUsuarioFlow,
        _carregando
    ) { locais, locaisUsuario, carregando ->
        LocalUiState(
            locais = locais,
            locaisUsuario = locaisUsuario,
            carregando = carregando
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LocalUiState(carregando = true)
    )

    /**
     * Adiciona um novo local ao repositório.
     *
     * @param local O local a ser adicionado.
     */
    fun adicionarLocal(local: LocalDTO) {
        viewModelScope.launch {
            _carregando.value = true

            localRepo.insertLocal(local.copy(id = UUID.randomUUID().toString()))
                .onSuccess { _canal.send("Local salvo com sucesso!") }
                .onFailure { _canal.send("Erro ao salvar: ${it.message}") }

            _carregando.value = false
        }
    }

    /**
     * Edita um local existente no repositório.
     *
     * @param local O local a ser atualizado.
     */
    fun editarLocal(local: LocalDTO) {
        viewModelScope.launch {
            _carregando.value = true

            localRepo.updateLocal(local)
                .onSuccess {
                    _canal.send("Local atualizado com sucesso!")
                }
                .onFailure { e ->
                    _canal.send("Salvo no dispositivo. Sincronização pendente: ${e.message}")
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

            localRepo.deleteLocal(id)
                .onSuccess { _canal.send("Local removido!") }
                .onFailure { _canal.send("Erro ao remover.") }

            _carregando.value = false
        }
    }
}