package gg.it.data.remote.repository.auth

import com.google.gson.Gson
import gg.it.data.local.sharedPrefs.SharedPrefsApi
import gg.it.data.local.sharedPrefs.SharedPrefsKey
import gg.it.data.remote.api.response.BaseResponse
import gg.it.data.remote.service.ApiService
import gg.it.model.login.LoginRes
import gg.it.model.user.User
import gg.it.utils.extension.checkInternet
import io.reactivex.rxjava3.core.Flowable

interface UserRepository {

    fun getUser(): User

    fun getUserName(): String

    fun getUserPassword(): String

    fun saveUseLogin(userName: String, password: String)

    fun postUserLogin(userName: String, password: String): Flowable<LoginRes>

    fun getMe(): Flowable<BaseResponse<User>>

    fun saveUser(user: User)

    fun wipeUserData();
}

class UserRepositoryImpl(
    private val share: SharedPrefsApi,
    private val loginService: ApiService,
    private val userService: ApiService,
) : UserRepository {

    private var user: User? = null

    override fun getUser(): User {
        return user ?: share.get(SharedPrefsKey.USER_DATA, User::class.java).also {
            user = it
        }
    }

    override fun getUserName(): String =
        share.get(SharedPrefsKey.USER_NAME_LOGIN, String::class.java)

    override fun getUserPassword(): String =
        share.get(SharedPrefsKey.USER_PASSWORD_LOGIN, String::class.java)

    override fun saveUseLogin(userName: String, password: String) {
        share.put(SharedPrefsKey.USER_NAME_LOGIN, userName)
        share.put(SharedPrefsKey.USER_PASSWORD_LOGIN, password)
    }

    override fun postUserLogin(userName: String, password: String) =
        loginService.postLogin(userName, password).checkInternet()

    override fun getMe() = userService.getMe().checkInternet()

    override fun saveUser(user: User) {
        share.put(SharedPrefsKey.USER_DATA, user)
    }

    override fun wipeUserData() {
        share.clearKey(SharedPrefsKey.USER_DATA)
    }
}