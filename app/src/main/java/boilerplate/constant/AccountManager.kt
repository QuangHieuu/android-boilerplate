package boilerplate.constant

import boilerplate.data.local.repository.user.TokenRepository
import boilerplate.data.local.repository.user.UserRepository
import boilerplate.model.user.User
import org.koin.java.KoinJavaComponent.inject

object AccountManager {
    private val userImpl by inject<UserRepository>(UserRepository::class.java)
    private val tokenImpl by inject<TokenRepository>(TokenRepository::class.java)

    @JvmStatic
    fun getCurrentUserId(): String? {
        return userImpl.getUser().id
    }

    @JvmStatic
    fun getCurrentNhanVien(): User = userImpl.getUser()

    @JvmStatic
    fun getToken(): String = tokenImpl.getToken()
}