package gg.it.di

import com.google.gson.Gson
import gg.it.BuildConfig
import gg.it.data.remote.api.middleware.InterceptorImpl
import gg.it.data.remote.api.middleware.RxErrorHandlingCallAdapterFactory
import gg.it.data.remote.repository.auth.TokenRepository
import gg.it.data.remote.service.ApiDomain
import gg.it.data.remote.service.ApiService
import gg.it.data.remote.service.ApiUrl
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val remoteModule = module {
    single { provideInterceptor(get()) }
    single { provideOkHttpClient(get(), get()) }
    single { provideRetrofitBuilder(get(), get()) }
    single(named(ApiDomain.LOGIN)) { provideServiceLogin(get(), get()) }
    single(named(ApiDomain.E_OFFICE)) { provideServiceEOffice(get(), get()) }
    single(named(ApiDomain.NOTIFY)) { provideServiceNotify(get(), get()) }
    single(named(ApiDomain.CHAT)) { provideServiceChat(get(), get()) }
    single(named(ApiDomain.FILE)) { provideServiceFile(get(), get()) }
    single(named(ApiDomain.FIREBASE)) { provideServiceFirebase(get(), get()) }
    single(named(ApiDomain.CALENDAR)) { provideServiceCalendar(get(), get()) }
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

fun provideServiceLogin(builder: Retrofit.Builder, okHttpClient: OkHttpClient): ApiService {
    val retrofit: Retrofit = builder
        .baseUrl(ApiUrl.HOST_SIGN_IN)
        .client(okHttpClient)
        .build()
    return retrofit.create(ApiService::class.java)
}

fun provideServiceEOffice(builder: Retrofit.Builder, okHttpClient: OkHttpClient): ApiService {
    val retrofit: Retrofit = builder
        .baseUrl(ApiUrl.HOST_E_OFFICE + ApiUrl.API)
        .client(okHttpClient)
        .build()
    return retrofit.create(ApiService::class.java)
}

fun provideServiceChat(builder: Retrofit.Builder, okHttpClient: OkHttpClient): ApiService {
    val retrofit: Retrofit = builder
        .baseUrl(ApiUrl.HOST_CHAT + ApiUrl.API)
        .client(okHttpClient)
        .build()
    return retrofit.create(ApiService::class.java)
}

fun provideServiceNotify(builder: Retrofit.Builder, okHttpClient: OkHttpClient): ApiService {
    val retrofit: Retrofit = builder
        .baseUrl(ApiUrl.HOST_NOTIFICATION + ApiUrl.API)
        .client(okHttpClient)
        .build()
    return retrofit.create(ApiService::class.java)
}

fun provideServiceFile(builder: Retrofit.Builder, okHttpClient: OkHttpClient): ApiService {
    val retrofit: Retrofit = builder
        .baseUrl(ApiUrl.HOST_FILE + ApiUrl.API)
        .client(okHttpClient)
        .build()
    return retrofit.create(ApiService::class.java)
}

fun provideServiceFirebase(builder: Retrofit.Builder, okHttpClient: OkHttpClient): ApiService {
    val retrofit: Retrofit = builder
        .baseUrl(ApiUrl.FIREBASE_URL_VERSON)
        .client(okHttpClient)
        .build()
    return retrofit.create(ApiService::class.java)
}

fun provideServiceCalendar(builder: Retrofit.Builder, okHttpClient: OkHttpClient): ApiService {
    val retrofit: Retrofit = builder
        .baseUrl(ApiUrl.HOST_MEETING_CALENDAR + ApiUrl.API)
        .client(okHttpClient)
        .build()
    return retrofit.create(ApiService::class.java)
}

object NetworkConstants {
    const val READ_TIMEOUT: Long = 30
    const val WRITE_TIMEOUT: Long = 30
    const val CONNECTION_TIMEOUT: Long = 30
}