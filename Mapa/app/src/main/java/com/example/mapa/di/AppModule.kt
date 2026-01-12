package com.example.mapa.di

import com.example.mapa.repositories.AuthRepo
import com.example.mapa.repositories.AuthFirebaseRepo
import com.example.mapa.repositories.ChatFirebaseRepo
import com.example.mapa.repositories.ChatRepo
import com.example.mapa.repositories.LocalRepo
import com.example.mapa.repositories.LocalFirebaseRepo
import com.example.mapa.repositories.UsuarioFirebaseRepo
import com.example.mapa.repositories.UsuarioRepo
import com.example.mapa.viewmodels.AuthViewModel
import com.example.mapa.viewmodels.ChatListViewModel
import com.example.mapa.viewmodels.LocalViewModel
import com.example.mapa.viewmodels.ChatViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// Configuração do módulo de dependências do Koin
val appModule = module {
    // Repositórios
    single<AuthRepo> { AuthFirebaseRepo() }
    single<LocalRepo> { LocalFirebaseRepo() }
    single<UsuarioRepo> { UsuarioFirebaseRepo() }
    single<ChatRepo> { ChatFirebaseRepo() }

    // ViewModels
    viewModel { AuthViewModel(get(), get()) }
    viewModel { LocalViewModel(get(), get()) }
    viewModel { ChatViewModel(get(), get(), get()) }
    viewModel { ChatListViewModel(get(), get(), get()) }
}