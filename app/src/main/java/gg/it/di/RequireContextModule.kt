package gg.it.di

import android.app.Application
import android.content.res.Resources
import gg.it.data.local.sharedPrefs.SharedPrefsApi
import gg.it.data.local.sharedPrefs.SharedPrefsImpl
import okhttp3.Cache
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val contextRequireModule = module {
    single { provideResources(androidApplication()) }
    single { provideSharedPrefsApi(get()) }
    single { provideOkHttpCache(androidApplication()) }
}

fun provideSharedPrefsApi(app: Application): SharedPrefsApi {
    return SharedPrefsImpl(app)
}

fun provideOkHttpCache(app: Application): Cache {
    val cacheSize: Long = 10 * 1024 * 1024 // 10 MiB
    return Cache(app.cacheDir, cacheSize)
}

fun provideResources(app: Application): Resources {
    return app.resources
}