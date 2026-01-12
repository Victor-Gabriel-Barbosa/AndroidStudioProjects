package com.example.mapa.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapa.models.LoginState
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.mapa.repositories.AuthRepo
import com.example.mapa.models.Usuario
import com.example.mapa.repositories.UsuarioRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * ViewModel para login e logout
 */
class AuthViewModel(
    private val authRepo: AuthRepo,
    private val usuarioRepo: UsuarioRepo
) : ViewModel() {
    // Estado de login
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Parado)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()
    private val _carregandoFoto = MutableStateFlow(false)
    val carregandoFoto: StateFlow<Boolean> = _carregandoFoto.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val usuarioAtivoState: Flow<Usuario?> = authRepo.usuarioState
        .flatMapLatest { authUser ->
            if (authUser?.uid != null) usuarioRepo.findByUid(authUser.uid).map { it.firstOrNull() ?: authUser }
            else flowOf(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Verifica se já está logado ao abrir o app
    val usuarioAtivoLogado: StateFlow<Boolean?> = authRepo.usuarioLogado
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Tenta logar com e-mail/senha
    fun login(email: String, senha: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Carregando
            val res = authRepo.signInWithEmail(email, senha)
            res
                .onSuccess { _loginState.value = LoginState.Sucesso }
                .onFailure { _loginState.value = LoginState.Erro(it.message ?: "Erro ao fazer login") }
        }
    }

    // Tenta cadastrar com e-mail/senha
    fun cadastrar(email: String, senha: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Carregando
            val res = authRepo.signUpWithEmail(email, senha)
            res
                .onSuccess {
                    try {
                        val novoUsuario = authRepo.usuarioState.filterNotNull().first()
                        val saveRes = usuarioRepo.save(novoUsuario)
                        if (saveRes.isSuccess) _loginState.value = LoginState.Sucesso
                        else _loginState.value = LoginState.Erro("Conta criada, mas erro ao salvar dados")
                    } catch (e: Exception) {
                        _loginState.value = LoginState.Erro("Erro ao vincular dados do usuário: ${e.message}")
                    }
                }
                .onFailure { _loginState.value = LoginState.Erro(it.message ?: "Erro ao cadastrar") }
        }
    }

    // Tenta logar com Google
    fun loginWithGoogle(credential: AuthCredential) {
        viewModelScope.launch {
            _loginState.value = LoginState.Carregando
            val res = authRepo.signInWithGoogle(credential)
            res
                .onSuccess {
                    try {
                        val usuarioGoogle = authRepo.usuarioState.filterNotNull().first()
                        usuarioRepo.save(usuarioGoogle)
                        _loginState.value = LoginState.Sucesso
                    } catch (e: Exception) {
                        _loginState.value = LoginState.Erro("Erro ao processar dados do Google: ${e.message}")
                    }
                }
                .onFailure { _loginState.value = LoginState.Erro(it.message ?: "Erro Google Login") }
        }
    }

    // Desloga o usuário
    fun logout() {
        authRepo.signOut()
        _loginState.value = LoginState.Parado
    }

    // Atualiza a foto de perfil do usuário
    fun atualizarFoto(foto: String) {
        viewModelScope.launch {
            try {
                _carregandoFoto.value = true
                val usuario = authRepo.usuarioState.filterNotNull().first()
                usuarioRepo.updateByUid(usuario.uid, usuario.copy(foto = foto))
                _carregandoFoto.value = false
            } catch (e: Exception) {
                _loginState.value = LoginState.Erro("Erro ao atualizar foto: ${e.message}")
            } finally {
                _carregandoFoto.value = false
            }
        }
    }

    fun atualizarNome(nome: String) {
        viewModelScope.launch {
            try {
                _carregandoFoto.value = true
                val usuario = authRepo.usuarioState.filterNotNull().first()
                usuarioRepo.updateByUid(usuario.uid, usuario.copy(nome = nome))
                _carregandoFoto.value = false
            } catch (e: Exception) {
                _loginState.value = LoginState.Erro("Erro ao atualizar nome: ${e.message}")
            }
        }
    }

    // Reseta o estado (ex: após exibir erro)
    fun resetState() {
        _loginState.value = LoginState.Parado
    }
}

