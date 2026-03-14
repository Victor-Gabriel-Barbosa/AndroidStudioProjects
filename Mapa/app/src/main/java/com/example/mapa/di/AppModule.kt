package com.example.mapa.di

import androidx.room.Room
import com.example.mapa.data.local.AppDatabase
import com.example.mapa.data.remote.datasource.AuthRemote
import com.example.mapa.data.remote.firebase.AuthFirebase
import com.example.mapa.data.remote.datasource.ChatRemote
import com.example.mapa.data.remote.firebase.ChatFirebase
import com.example.mapa.data.remote.datasource.LocationRemote
import com.example.mapa.data.remote.firebase.LocationFirebase
import com.example.mapa.data.remote.datasource.UserRemote
import com.example.mapa.data.remote.firebase.UserFirebase
import com.example.mapa.data.repository.ChatRepository
import com.example.mapa.data.repository.LocationRepository
import com.example.mapa.data.repository.UserRepository
import com.example.mapa.viewmodels.AuthViewModel
import com.example.mapa.viewmodels.ChatListViewModel
import com.example.mapa.viewmodels.ChatViewModel
import com.example.mapa.viewmodels.LocationViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Configuração do módulo de dependências do Koin.
 */
val appModule = module {
    // Firebase
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { FirebaseStorage.getInstance() }
    single { FirebaseMessaging.getInstance() }

    // Room
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "mapa_database.db"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    // DAOs
    single { get<AppDatabase>().userDao() }
    single { get<AppDatabase>().locationDao() }
    single { get<AppDatabase>().chatDao() }

    // Remotes
    single<AuthRemote> { AuthFirebase(get()) }
    single<UserRemote> { UserFirebase(get(), get()) }
    single<LocationRemote> { LocationFirebase(get(), get()) }
    single<ChatRemote> { ChatFirebase(get(), get()) }

    // Repositories
    single { UserRepository(get(), get(),  get()) }
    single { LocationRepository(get(), get()) }
    single { ChatRepository(get(), get()) }

    // ViewModels
    viewModel { AuthViewModel(get(), get()) }
    viewModel { LocationViewModel(get(), get()) }
    viewModel { ChatViewModel(get(), get(), get()) }
    viewModel { ChatListViewModel(get(), get(), get()) }
}