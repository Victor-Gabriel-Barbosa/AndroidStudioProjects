package com.example.refindu.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.refindu.models.User
import com.example.refindu.repos.AuthRepo
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ViewModel para login e logout
class AuthViewModel(
    private val repository: AuthRepo
) : ViewModel() {
    // Estado de login
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    val currentUserState: Flow<User?> = repository.userState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Verifica se já está logado ao abrir o app
    val isUserSignedIn: StateFlow<Boolean?> = currentUserState.map { it != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val currentUserUid: StateFlow<String?> = currentUserState.map { it?.uid }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Tenta logar com e-mail/senha
    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = repository.signInWithEmail(email, pass)
            result
                .onSuccess { _loginState.value = LoginState.Success }
                .onFailure { _loginState.value = LoginState.Error(it.message ?: "Erro ao fazer login")
            }
        }
    }

    // Tenta cadastrar com e-mail/senha
    fun register(email: String, pass: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = repository.signUpWithEmail(email, pass)
            result
                .onSuccess { _loginState.value = LoginState.Success }
                .onFailure { _loginState.value = LoginState.Error(it.message ?: "Erro ao cadastrar") }
        }
    }

    // Tenta logar com Google
    fun loginWithGoogle(credential: AuthCredential) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = repository.signInWithGoogle(credential)
            result
                .onSuccess { _loginState.value = LoginState.Success }
                .onFailure { _loginState.value = LoginState.Error(it.message ?: "Erro Google Login") }
        }
    }

    // Desloga o usuário
    fun logout() {
        repository.signOut()
        _loginState.value = LoginState.Idle
    }

    // Reseta o estado (ex: após exibir erro)
    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}

// Estados possíveis da tela de login
sealed class LoginState {
    data object Idle : LoginState()
    data object Loading : LoginState()
    data object Success : LoginState()
    data class Error(val message: String) : LoginState()
}