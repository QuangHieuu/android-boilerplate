package boilerplate.di

import boilerplate.data.local.repository.server.ServerRepository
import boilerplate.data.local.repository.server.ServerRepositoryImpl
import boilerplate.data.local.repository.user.TokenRepository
import boilerplate.data.local.repository.user.TokenRepositoryImpl
import boilerplate.data.local.repository.user.UserRepository
import boilerplate.data.local.repository.user.UserRepositoryImpl
import boilerplate.data.local.sharedPrefs.SharedPrefsApi
import boilerplate.data.remote.api.ApiRequest
import boilerplate.data.remote.repository.auth.LoginRepository
import boilerplate.data.remote.repository.auth.LoginRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single { provideUserRepository(get()) }
    single { provideTokenRepository(get()) }
    single { provideServerRepository(get()) }
    single { providerLoginRepository(get()) }
}

fun provideUserRepository(share: SharedPrefsApi): UserRepository {
    return UserRepositoryImpl(share)
}

fun provideTokenRepository(share: SharedPrefsApi): TokenRepository {
    return TokenRepositoryImpl(share)
}

fun provideServerRepository(share: SharedPrefsApi): ServerRepository {
    return ServerRepositoryImpl(share)
}

fun providerLoginRepository(apiRequest: ApiRequest): LoginRepository {
    return LoginRepositoryImpl(apiRequest)
}