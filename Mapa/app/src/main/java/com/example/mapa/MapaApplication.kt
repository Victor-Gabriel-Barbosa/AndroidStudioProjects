package com.example.mapa

import android.app.Application
import com.example.mapa.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

/**
 * Classe de aplicativo personalizada que inicializa o Koin para a injeção de dependência.
 */
class MapaApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MapaApplication)
            modules(appModule)
        }
    }
}