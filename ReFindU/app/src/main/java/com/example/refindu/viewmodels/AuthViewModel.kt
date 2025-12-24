package com.example.refindu.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.refindu.repos.AuthRepo
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModel para login e logout
class AuthViewModel(
    private val repository: AuthRepo
) : ViewModel() {
    // Estado de login
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    // Verifica se já está logado ao abrir o app
    val isUserSignedIn: Boolean get() = repository.isUserSignedIn

    val currenUserUid: String? get() = repository.userUid

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