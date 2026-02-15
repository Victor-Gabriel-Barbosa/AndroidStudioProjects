package com.example.mapa.di

import androidx.room.Room
import com.example.mapa.data.local.AppDatabase
import com.example.mapa.data.remote.AuthRepositoryImpl
import com.example.mapa.data.remote.AuthRepository
import com.example.mapa.data.remote.ChatRepositoryImpl
import com.example.mapa.data.remote.ChatRepository
import com.example.mapa.data.remote.LocalRepositoryImpl
import com.example.mapa.data.remote.LocalRepository
import com.example.mapa.data.remote.UsuarioRepositoryImpl
import com.example.mapa.data.remote.UsuarioRepository
import com.example.mapa.data.repository.LocalRepository
import com.example.mapa.data.repository.UsuarioRepository
import com.example.mapa.viewmodels.AuthViewModel
import com.example.mapa.viewmodels.ChatListViewModel
import com.example.mapa.viewmodels.ChatViewModel
import com.example.mapa.viewmodels.LocalViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// Configuração do módulo de dependências do Koin
val appModule = module {
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { FirebaseStorage.getInstance() }

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "mapa_database.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    single { get<AppDatabase>().usuarioDao() }
    single { get<AppDatabase>().localDao() }

    // Repositórios
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<LocalRepository> { LocalRepositoryImpl(get(), get()) }
    single<UsuarioRepository> { UsuarioRepositoryImpl(get(), get()) }
    single<LocalRepository> { LocalRepositoryImpl() }
    single<ChatRepository> { ChatRepositoryImpl(get(), get()) }

    single {
        UsuarioRepository(
            authRepository = get(),
            usuarioRepository = get(),
            usuarioDao = get()
        )
    }

    single {
        LocalRepository(
            remoteRepo = get(),
            localDao = get()
        )
    }

    // ViewModels
    viewModel { AuthViewModel(get(), get()) }
    viewModel { LocalViewModel(get(), get()) }
    viewModel { ChatViewModel(get(), get(), get()) }
    viewModel { ChatListViewModel(get(), get(), get()) }
}