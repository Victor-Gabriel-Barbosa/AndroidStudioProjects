package com.example.mapa.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapa.data.mapper.toDTO
import com.example.mapa.model.LoginUiState
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.mapa.data.remote.datasource.AuthRemote
import com.example.mapa.model.UserUiState
import com.example.mapa.data.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * ViewModel para gerenciar a autenticação do usuário, incluindo login, cadastro e logout.
 *
 * @property authRemote Repositório para operações de autenticação com Firebase.
 * @property userRepo Repositório para operações de dados do usuário.
 */
class AuthViewModel(
    private val authRemote: AuthRemote,
    private val userRepo: UserRepository
) : ViewModel() {
    /**
     * Estado que representa o processo de login/cadastro.
     */
    private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.Stopped)
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    /**
     * Estados que indicam se a foto de perfil ou o nome do usuário estão sendo carregados/atualizados.
     */
    private val _loadingPhoto = MutableStateFlow(false)
    private val _loadingName = MutableStateFlow(false)

    /**
     * Canal para enviar mensagens de eventos para a UI.
     */
    private val _channel = Channel<String>(Channel.BUFFERED)
    val channel = _channel.receiveAsFlow()

    /**
     * `Flow` que emite o estado do usuário ativo, combinando dados do Firebase Auth e Firestore.
     * Retorna `null` se não houver usuário logado.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UserUiState> = combine(
        userRepo.user,
        authRemote.user,
        _loadingPhoto,
        _loadingName
    ) { userRepo, userAuth, loadingPhoto, loadingName ->
        UserUiState(
            user = userRepo?.toDTO() ?: userAuth,
            loggedIn = userAuth != null,
            loadingPhoto = loadingPhoto,
            loadingName = loadingName
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserUiState(loggedIn = null)
    )

    /**
     * Tenta realizar o login do usuário com e-mail e senha.
     * Atualiza o `loginState` para refletir o resultado da operação.
     *
     * @param email O e-mail do usuário.
     * @param password A senha do usuário.
     */
    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _loginUiState.value = LoginUiState.Loading

            authRemote.signInWithEmail(email, password)
                .onSuccess {
                    val user = authRemote.user.first()
                    if (user != null) userRepo.syncUserAuth(user.copy(email = email))
                    _loginUiState.value = LoginUiState.Success
                }
                .onFailure { e -> _loginUiState.value = LoginUiState.Error(e.message ?: "Erro no login") }
        }
    }

    /**
     * Tenta cadastrar um novo usuário com e-mail e senha.
     * Após o cadastro, salva os dados do novo usuário no Firestore.
     * Atualiza o `loginState` para refletir o resultado da operação.
     *
     * @param email O e-mail do novo usuário.
     * @param password A senha do novo usuário.
     */
    fun registerWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _loginUiState.value = LoginUiState.Loading

            authRemote.signUpWithEmail(email, password)
                .onSuccess {
                    val user = authRemote.user.first()
                    if (user != null) userRepo.syncUserAuth(user.copy(email = email))

                    _loginUiState.value = LoginUiState.Success
                }
                .onFailure { error ->
                    _loginUiState.value = LoginUiState.Error(error.message ?: "Erro no cadastro")
                }
        }
    }

    /**
     * Tenta realizar o login do usuário utilizando uma credencial do Google.
     * Após o login, salva ou atualiza os dados do usuário no Firestore.
     * Atualiza o `loginState` para refletir o resultado da operação.
     *
     * @param credential A credencial de autenticação do Google.
     */
    fun loginWithGoogle(credential: AuthCredential) {
        viewModelScope.launch {
            _loginUiState.value = LoginUiState.Loading

            authRemote.signInWithGoogle(credential)
                .onSuccess {
                    val userAuth = authRemote.user.first()
                    if (userAuth != null) userRepo.syncUserAuth(userAuth)

                    _loginUiState.value = LoginUiState.Success
                }
                .onFailure { error ->
                    _loginUiState.value = LoginUiState.Error(error.message ?: "Erro no login com Google")
                }
        }
    }

    /**
     * Realiza o logout do usuário atual e reseta o estado de login.
     */
    fun logout() {
        authRemote.signOut()
        _loginUiState.value = LoginUiState.Stopped
    }

    /**
     * Atualiza a URL da foto de perfil do usuário logado.
     *
     * @param photoUri A nova URL da foto de perfil.
     */
    fun updatePhoto(photoUri: String) {
        val user = uiState.value.user ?: return

        viewModelScope.launch {
            _loadingPhoto.value = true

            userRepo.updateUser(user.copy(photo = photoUri))
                .onFailure { _channel.send("Erro ao atualizar foto: ${it.message}") }

            _loadingPhoto.value = false
        }
    }

    /**
     * Atualiza o nome do usuário logado.
     *
     * @param nome O novo nome do usuário.
     */
    fun updateName(nome: String) {
        val user = uiState.value.user ?: return

        viewModelScope.launch {
            _loadingName.value = true

            userRepo.updateUser(user.copy(name = nome))
                .onFailure { _channel.send("Erro ao atualizar nome: ${it.message}") }

            _loadingName.value = false
        }
    }

    /**
     * Reseta o estado de login para `Parado`. Útil para limpar o estado após a exibição de um erro.
     */
    fun resetState() {
        _loginUiState.value = LoginUiState.Stopped
    }
}
