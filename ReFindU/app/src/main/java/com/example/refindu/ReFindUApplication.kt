package com.example.refindu

import android.app.Application
import com.example.refindu.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class ReFindUApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@ReFindUApplication)
            modules(appModule)
        }
    }
}