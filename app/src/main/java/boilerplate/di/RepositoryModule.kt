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
import boilerplate.data.remote.repository.conversation.ConversationRepository
import boilerplate.data.remote.repository.conversation.ConversationRepositoryImpl
import boilerplate.data.remote.repository.dashboard.DashboardImpl
import boilerplate.data.remote.repository.dashboard.DashboardRepository
import boilerplate.data.remote.repository.file.FileImpl
import boilerplate.data.remote.repository.file.FileRepository
import com.google.gson.Gson
import org.koin.dsl.module

val repositoryModule = module {
	single { provideUserRepository(get(), get(), get()) }
	single { provideTokenRepository(get()) }
	single { provideServerRepository(get()) }
	single { providerLoginRepository(get(), get()) }
	single { providerConversationRepository(get(), get(), get()) }
	single { providerDashBoard(get()) }
	single { providerFile(get()) }
}

fun provideUserRepository(
	share: SharedPrefsApi,
	apiRequest: ApiRequest,
	gson: Gson
): UserRepository {
	return UserRepositoryImpl(share, apiRequest, gson)
}

fun provideTokenRepository(share: SharedPrefsApi): TokenRepository {
	return TokenRepositoryImpl(share)
}

fun provideServerRepository(share: SharedPrefsApi): ServerRepository {
	return ServerRepositoryImpl(share)
}

fun providerLoginRepository(
	apiRequest: ApiRequest,
	userRepository: UserRepository
): LoginRepository {
	return LoginRepositoryImpl(apiRequest, userRepository)
}

fun providerConversationRepository(
	apiRequest: ApiRequest,
	userRepository: UserRepository,
	tokenRepository: TokenRepository
): ConversationRepository {
	return ConversationRepositoryImpl(apiRequest, userRepository, tokenRepository)
}

fun providerDashBoard(apiRequest: ApiRequest): DashboardRepository {
	return DashboardImpl(apiRequest)
}

fun providerFile(apiRequest: ApiRequest): FileRepository {
	return FileImpl(apiRequest)
}