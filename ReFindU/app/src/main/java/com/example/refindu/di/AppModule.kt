package com.example.refindu.di

import com.example.refindu.repos.AuthRepo
import com.example.refindu.repos.FirebaseAuthRepo
import com.example.refindu.repos.FirebaseLocalRepo
import com.example.refindu.repos.LocalRepo
import com.example.refindu.viewmodels.AuthViewModel
import com.example.refindu.viewmodels.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// Configuração do módulo de dependências do Koin
val appModule = module {
    // Instâncias Singleton (Repositórios) - Única instância para toda a aplicação
    single<AuthRepo> { FirebaseAuthRepo() }
    single<LocalRepo> { FirebaseLocalRepo() }

    // Factories de ViewModels com resolução automática de dependências via get()
    viewModel { AuthViewModel(get()) }
    viewModel { HomeViewModel(get()) }
}