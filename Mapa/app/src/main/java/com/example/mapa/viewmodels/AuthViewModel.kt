package com.example.mapa.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapa.data.local.mapper.toDomain
import com.example.mapa.models.LoginState
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.mapa.data.remote.AuthRepository
import com.example.mapa.models.UsuarioState
import com.example.mapa.data.repository.UsuarioRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

/**
 * ViewModel para gerenciar a autenticação do usuário, incluindo login, cadastro e logout.
 *
 * @property authRepository Repositório para operações de autenticação com Firebase.
 * @property usuarioRepo Repositório para operações de dados do usuário no Firestore.
 */
class AuthViewModel(
    private val authRepository: AuthRepository,
    private val usuarioRepo: UsuarioRepository
) : ViewModel() {
    /**
     * Estado que representa o processo de login/cadastro.
     */
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Parado)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    /**
     * Estados que indicam se a foto de perfil ou o nome do usuário estão sendo carregados/atualizados.
     */
    private val _carregandoFoto = MutableStateFlow(false)
    private val _carregandoNome = MutableStateFlow(false)

    /**
     * `Flow` que emite o estado do usuário ativo, combinando dados do Firebase Auth e Firestore.
     * Retorna `null` se não houver usuário logado.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UsuarioState> = combine(
        usuarioRepo.usuario,
        authRepository.usuarioState,
        _carregandoFoto,
        _carregandoNome
    ) { usuarioLocalEntity, usuarioAuth, carregandoFoto, carregandoNome ->
        val usuarioParaExibir = usuarioLocalEntity?.toDomain() ?: usuarioAuth

        UsuarioState(
            usuario = usuarioParaExibir,
            logado = usuarioAuth != null,
            carregandoFoto = carregandoFoto,
            carregandoNome = carregandoNome
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UsuarioState(logado = null)
    )

    /**
     * Tenta realizar o login do usuário com e-mail e senha.
     * Atualiza o `loginState` para refletir o resultado da operação.
     *
     * @param email O e-mail do usuário.
     * @param senha A senha do usuário.
     */
    fun login(email: String, senha: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Carregando

            authRepository.signInWithEmail(email, senha)
                .onSuccess {
                    _loginState.value = LoginState.Sucesso
                }
                .onFailure { error ->
                    _loginState.value = LoginState.Erro(error.message ?: "Erro no login")
                }
        }
    }

    /**
     * Tenta cadastrar um novo usuário com e-mail e senha.
     * Após o cadastro, salva os dados do novo usuário no Firestore.
     * Atualiza o `loginState` para refletir o resultado da operação.
     *
     * @param email O e-mail do novo usuário.
     * @param senha A senha do novo usuário.
     */
    fun cadastrar(email: String, senha: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Carregando

            authRepository.signUpWithEmail(email, senha)
                .onSuccess {
                    val usuarioAtual = authRepository.usuarioState.first()
                    if (usuarioAtual != null) usuarioRepo.updateUsuario(usuarioAtual.copy(email = email))
                    _loginState.value = LoginState.Sucesso
                }
                .onFailure { error ->
                    _loginState.value = LoginState.Erro(error.message ?: "Erro no cadastro")
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
            _loginState.value = LoginState.Carregando
            authRepository.signInWithGoogle(credential)
                .onSuccess {
                    val usuarioAuth = authRepository.usuarioState.first()
                    if (usuarioAuth != null) usuarioRepo.updateUsuario(usuarioAuth)
                    _loginState.value = LoginState.Sucesso
                }
                .onFailure { error ->
                    _loginState.value = LoginState.Erro(error.message ?: "Erro Google")
                }
        }
    }

    /**
     * Realiza o logout do usuário atual e reseta o estado de login.
     */
    fun logout() {
        authRepository.signOut()
        _loginState.value = LoginState.Parado
    }

    /**
     * Atualiza a URL da foto de perfil do usuário logado.
     *
     * @param foto A nova URL da foto de perfil.
     */
    fun atualizarFoto(foto: String) {
        val usuarioAtual = uiState.value.usuario ?: return

        viewModelScope.launch {
            _carregandoFoto.value = true
            usuarioRepo.updateUsuario(usuarioAtual.copy(foto = foto))
            _carregandoFoto.value = false
        }
    }

    /**
     * Atualiza o nome do usuário logado.
     *
     * @param nome O novo nome do usuário.
     */
    fun atualizarNome(nome: String) {
        val usuarioAtual = uiState.value.usuario ?: return

        viewModelScope.launch {
            _carregandoNome.value = true
            usuarioRepo.updateUsuario(usuarioAtual.copy(nome = nome))
            _carregandoNome.value = false
        }
    }

    /**
     * Reseta o estado de login para `Parado`. Útil para limpar o estado após a exibição de um erro.
     */
    fun resetState() {
        _loginState.value = LoginState.Parado
    }
}
