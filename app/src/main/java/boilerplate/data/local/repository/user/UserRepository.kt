package boilerplate.data.local.repository.user

import boilerplate.data.local.sharedPrefs.SharedPrefsApi
import boilerplate.data.local.sharedPrefs.SharedPrefsKey
import boilerplate.model.user.User

interface UserRepository {

    fun getUser(): User

    fun getUserName(): String

    fun getUserPassword(): String

    fun saveUseLogin(userName: String, password: String)

    fun saveUser(user: User)

    fun wipeUserData();
}

class UserRepositoryImpl(private val share: SharedPrefsApi) : UserRepository {

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

    override fun saveUser(user: User) {
        share.put(SharedPrefsKey.USER_DATA, user)
    }

    override fun wipeUserData() {
        share.clearKey(SharedPrefsKey.USER_DATA)
    }
}