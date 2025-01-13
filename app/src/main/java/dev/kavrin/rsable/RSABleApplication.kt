package dev.kavrin.rsable

import android.app.Application
import dev.kavrin.rsable.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class RSABleApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@RSABleApplication)
            modules(viewModelModule)
        }
    }
}