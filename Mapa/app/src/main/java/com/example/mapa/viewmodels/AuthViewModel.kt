package com.example.mapa.viewmodels

import android.util.Log
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
 * ViewModel para gerenciar a autenticação do usuário, incluindo login, cadastro e logout.
 *
 * @property authRepo Repositório para operações de autenticação com Firebase.
 * @property usuarioRepo Repositório para operações de dados do usuário no Firestore.
 */
class AuthViewModel(
    private val authRepo: AuthRepo,
    private val usuarioRepo: UsuarioRepo
) : ViewModel() {
    /**
     * Estado que representa o processo de login/cadastro.
     */
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Parado)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    /**
     * Estado que indica se a foto de perfil está sendo carregada/atualizada.
     */
    private val _carregandoFoto = MutableStateFlow(false)
    val carregandoFoto: StateFlow<Boolean> = _carregandoFoto.asStateFlow()

    /**
     * `Flow` que emite o estado do usuário ativo, combinando dados do Firebase Auth e Firestore.
     * Retorna `null` se não houver usuário logado.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val usuarioAtivoState: Flow<Usuario?> = authRepo.usuarioState
        .flatMapLatest { usuario ->
            if (usuario?.uid != null) usuarioRepo.findByUid(usuario.uid).map { it.firstOrNull() ?: usuario }
            else flowOf(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /**
     * `StateFlow` que indica se há um usuário logado no Firebase Auth.
     * `true` se logado, `false` se deslogado, `null` durante a inicialização.
     */
    val usuarioAtivoLogado: StateFlow<Boolean?> = authRepo.usuarioLogado
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
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

            authRepo.signInWithEmail(email, senha)
                .onSuccess { _loginState.value = LoginState.Sucesso }
                .onFailure {
                    Log.e("AuthViewModel", "login: ${it.message}")
                    _loginState.value = LoginState.Erro(it.message ?: "Erro ao fazer login")
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

            authRepo.signUpWithEmail(email, senha)
                .onSuccess {
                    try {
                        val novoUsuario = authRepo.usuarioState.filterNotNull().first()
                        val res = usuarioRepo.save(novoUsuario)
                        if (res.isSuccess) _loginState.value = LoginState.Sucesso
                        else _loginState.value = LoginState.Erro("Conta criada, mas erro ao salvar dados")
                    } catch (e: Exception) {
                        Log.e("AuthViewModel", "cadastrar: ${e.message}")
                        _loginState.value = LoginState.Erro("Erro ao vincular dados do usuário: ${e.message}")
                    }
                }
                .onFailure {
                    Log.e("AuthViewModel", "cadastrar: ${it.message}")
                    _loginState.value = LoginState.Erro(it.message ?: "Erro ao cadastrar")
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

            authRepo.signInWithGoogle(credential)
                .onSuccess {
                    try {
                        val usuarioGoogle = authRepo.usuarioState.filterNotNull().first()
                        usuarioRepo.save(usuarioGoogle)
                        _loginState.value = LoginState.Sucesso
                    } catch (e: Exception) {
                        Log.e("AuthViewModel", "loginWithGoogle: ${e.message}")
                        _loginState.value = LoginState.Erro("Erro ao processar dados do Google: ${e.message}")
                    }
                }
                .onFailure {
                    Log.e("AuthViewModel", "loginWithGoogle: ${it.message}")
                    _loginState.value = LoginState.Erro(it.message ?: "Erro Google Login")
                }
        }
    }

    /**
     * Realiza o logout do usuário atual e reseta o estado de login.
     */
    fun logout() {
        authRepo.signOut()
        _loginState.value = LoginState.Parado
    }

    /**
     * Atualiza a URL da foto de perfil do usuário logado.
     *
     * @param foto A nova URL da foto de perfil.
     */
    fun atualizarFoto(foto: String) {
        viewModelScope.launch {
            try {
                _carregandoFoto.value = true
                val usuario = authRepo.usuarioState.filterNotNull().first()
                usuarioRepo.updateByUid(usuario.uid, usuario.copy(foto = foto))
                _carregandoFoto.value = false
            } catch (e: Exception) {
                Log.e("AuthViewModel", "atualizarFoto: ${e.message}")
                _loginState.value = LoginState.Erro("Erro ao atualizar foto: ${e.message}")
            } finally {
                _carregandoFoto.value = false
            }
        }
    }

    /**
     * Atualiza o nome do usuário logado.
     *
     * @param nome O novo nome do usuário.
     */
    fun atualizarNome(nome: String) {
        viewModelScope.launch {
            try {
                _carregandoFoto.value = true
                val usuario = authRepo.usuarioState.filterNotNull().first()
                usuarioRepo.updateByUid(usuario.uid, usuario.copy(nome = nome))
                _carregandoFoto.value = false
            } catch (e: Exception) {
                Log.e("AuthViewModel", "atualizarNome: ${e.message}")
                _loginState.value = LoginState.Erro("Erro ao atualizar nome: ${e.message}")
            }
        }
    }

    /**
     * Reseta o estado de login para `Parado`. Útil para limpar o estado após a exibição de um erro.
     */
    fun resetState() {
        _loginState.value = LoginState.Parado
    }
}
