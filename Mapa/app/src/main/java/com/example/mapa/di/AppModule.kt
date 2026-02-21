package com.example.mapa.di

import androidx.room.Room
import com.example.mapa.data.local.AppDatabase
import com.example.mapa.data.remote.source.AuthRemote
import com.example.mapa.data.remote.source.AuthRemoteImpl
import com.example.mapa.data.remote.source.ChatRemote
import com.example.mapa.data.remote.source.ChatRemoteImpl
import com.example.mapa.data.remote.source.LocalRemote
import com.example.mapa.data.remote.source.LocalRemoteImpl
import com.example.mapa.data.remote.source.UsuarioRemote
import com.example.mapa.data.remote.source.UsuarioRemoteImpl
import com.example.mapa.data.repository.ChatRepository
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
    // Firebase
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { FirebaseStorage.getInstance() }

    // Room
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "mapa_database.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    // DAOs
    single { get<AppDatabase>().usuarioDao() }
    single { get<AppDatabase>().localDao() }
    single { get<AppDatabase>().chatDao() }

    // Remotes
    single<AuthRemote> { AuthRemoteImpl(get()) }
    single<UsuarioRemote> { UsuarioRemoteImpl(get(), get()) }
    single<LocalRemote> { LocalRemoteImpl(get(), get()) }
    single<ChatRemote> { ChatRemoteImpl(get(), get()) }

    // Repositories
    single { UsuarioRepository(get(), get(),  get()) }
    single { LocalRepository(get(), get()) }
    single { ChatRepository(get(), get()) }

    // ViewModels
    viewModel { AuthViewModel(get(), get()) }
    viewModel { LocalViewModel(get(), get()) }
    viewModel { ChatViewModel(get(), get(), get(), get()) }
    viewModel { ChatListViewModel(get(), get(), get()) }
}