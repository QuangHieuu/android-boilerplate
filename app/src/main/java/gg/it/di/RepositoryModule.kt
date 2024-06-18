package gg.it.di

import gg.it.data.local.sharedPrefs.SharedPrefsApi
import gg.it.data.remote.repository.auth.TokenRepository
import gg.it.data.remote.repository.auth.TokenRepositoryImpl
import gg.it.data.remote.repository.auth.UserRepository
import gg.it.data.remote.repository.auth.UserRepositoryImpl
import gg.it.data.remote.service.ApiDomain
import gg.it.data.remote.service.ApiService
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.inject

val repositoryModule = module {
    val loginService: ApiService by inject(ApiService::class.java, named(ApiDomain.LOGIN))
    val userService: ApiService by inject(ApiService::class.java, named(ApiDomain.E_OFFICE))

    single { provideUserRepository(get(), loginService, userService) }
    single { provideTokenRepository(get()) }
}

fun provideUserRepository(
    share: SharedPrefsApi,
    loginService: ApiService,
    userService: ApiService
): UserRepository {
    return UserRepositoryImpl(share, loginService, userService)
}

fun provideTokenRepository(share: SharedPrefsApi): TokenRepository {
    return TokenRepositoryImpl(share)
}
