package boilerplate.di

import android.app.Application
import android.content.res.Resources
import boilerplate.data.local.sharedPrefs.SharedPrefsApi
import boilerplate.data.local.sharedPrefs.SharedPrefsImpl
import boilerplate.data.local.sharedPrefs.SharedPrefsKey
import boilerplate.data.remote.service.ApiUrl
import boilerplate.service.signalr.SignalRReceiver
import com.google.gson.Gson
import okhttp3.Cache
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val contextRequireModule = module {
    single { provideResources(androidApplication()) }
    single { provideSharedPrefsApi(get(), get()) }
    single { provideOkHttpCache(androidApplication()) }
    single { provideSignalRReceiver(androidApplication()) }
}

fun provideSharedPrefsApi(app: Application, gson: Gson): SharedPrefsApi {
    return SharedPrefsImpl(app, gson).also { server ->
        server.get(SharedPrefsKey.SERVER, String::class.java)
            .ifEmpty { ApiUrl.DEFAULT }
            .also { ApiUrl.setHost(it) }
    }
}

fun provideOkHttpCache(app: Application): Cache {
    val cacheSize: Long = 10 * 1024 * 1024 // 10 MiB
    return Cache(app.cacheDir, cacheSize)
}

fun provideResources(app: Application): Resources {
    return app.resources
}

fun provideSignalRReceiver(app: Application): SignalRReceiver {
    return SignalRReceiver(app)
}