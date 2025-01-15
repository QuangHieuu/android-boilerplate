package boilerplate.di

import boilerplate.data.local.repository.server.ServerRepository
import boilerplate.data.local.repository.server.ServerRepositoryImpl
import boilerplate.data.local.repository.user.TokenRepository
import boilerplate.data.local.repository.user.TokenRepositoryImpl
import boilerplate.data.local.sharedPrefs.SharedPrefsApi
import org.koin.dsl.module

val repositoryModule = module {
	single { provideTokenRepository(get()) }
	single { provideServerRepository(get()) }
}

fun provideTokenRepository(share: SharedPrefsApi): TokenRepository {
	return TokenRepositoryImpl(share)
}

fun provideServerRepository(share: SharedPrefsApi): ServerRepository {
	return ServerRepositoryImpl(share)
}