package gg.it.base

import android.app.Application
import gg.it.di.appModule
import gg.it.di.contextRequireModule
import gg.it.di.remoteModule
import gg.it.di.repositoryModule
import gg.it.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class BaseApp : Application() {

    companion object {
        lateinit var sInstance: BaseApp
    }

    override fun onCreate() {
        super.onCreate()
        sInstance = this
        startKoin {
            androidLogger()
            androidContext(this@BaseApp)
            androidFileProperties()
            modules(
                appModule,
                viewModelModule,
                contextRequireModule,
                remoteModule,
                repositoryModule
            )
        }
    }
}