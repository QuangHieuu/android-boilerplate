package boilerplate.di

import boilerplate.BuildConfig
import boilerplate.data.local.repository.user.TokenRepository
import boilerplate.data.remote.api.ApiRequest
import boilerplate.data.remote.api.middleware.InterceptorImpl
import boilerplate.data.remote.api.middleware.RxErrorHandlingCallAdapterFactory
import com.google.gson.Gson
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val remoteModule = module {
    single { provideInterceptor(get()) }
    single { provideOkHttpClient(get(), get()) }
    single { provideRetrofitBuilder(get(), get()) }
    single { provideApiRequest(get(), get()) }
}

fun provideApiRequest(builder: Retrofit.Builder, okHttpClient: OkHttpClient): ApiRequest {
    return ApiRequest(builder, okHttpClient)
}

fun provideRetrofitBuilder(gson: Gson, okHttpClient: OkHttpClient): Retrofit.Builder =
    Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())

fun provideInterceptor(tokenRepository: TokenRepository): Interceptor =
    InterceptorImpl(tokenRepository)

fun provideOkHttpClient(cache: Cache, interceptor: Interceptor): OkHttpClient =
    OkHttpClient.Builder().apply {
        cache(cache)
        addInterceptor(interceptor)
        readTimeout(
            NetworkConstants.READ_TIMEOUT, TimeUnit.SECONDS
        )
        writeTimeout(
            NetworkConstants.WRITE_TIMEOUT, TimeUnit.SECONDS
        )
        connectTimeout(
            NetworkConstants.CONNECTION_TIMEOUT, TimeUnit.SECONDS
        )
        if (BuildConfig.DEBUG) {
            addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }
    }.build()

object NetworkConstants {
    const val READ_TIMEOUT: Long = 30
    const val WRITE_TIMEOUT: Long = 30
    const val CONNECTION_TIMEOUT: Long = 30
}