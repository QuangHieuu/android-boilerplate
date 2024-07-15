package boilerplate.data.local.repository.user

import boilerplate.data.local.sharedPrefs.SharedPrefsApi
import boilerplate.data.local.sharedPrefs.SharedPrefsKey
import boilerplate.data.remote.api.ApiRequest
import boilerplate.data.remote.api.response.BaseResponse
import boilerplate.model.user.User
import boilerplate.utils.extension.checkInternet
import io.reactivex.rxjava3.core.Flowable

interface UserRepository {
    fun logout(): Flowable<BaseResponse<Boolean>>

    fun getUser(): User

    fun getUserName(): String

    fun getUserPassword(): String

    fun saveUseLogin(userName: String, password: String)

    fun saveUser(user: User)

    fun wipeUserData()

    fun getSystemTextSize(): Int

    fun saveSystemTextSize(size: Int)

    fun getSystemSound(): Boolean

    fun saveSystemSound(b: Boolean)
}

class UserRepositoryImpl(
    private val share: SharedPrefsApi,
    private val api: ApiRequest
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

    override fun saveUser(user: User) {
        share.put(SharedPrefsKey.USER_DATA, user)
    }

    override fun wipeUserData() {
        share.clearKey(SharedPrefsKey.USER_DATA)
        share.clearKey(SharedPrefsKey.SIZE)
        share.clearKey(SharedPrefsKey.SOUND)

        user = null
    }

    override fun getSystemTextSize(): Int {
        return share.get(SharedPrefsKey.SIZE, Int::class.java)
    }

    override fun saveSystemTextSize(size: Int) {
        share.put(SharedPrefsKey.SIZE, size)
    }

    override fun getSystemSound(): Boolean {
        return share.get(SharedPrefsKey.SOUND, Boolean::class.java)
    }

    override fun saveSystemSound(b: Boolean) {
        share.put(SharedPrefsKey.SOUND, b)
    }

    override fun logout(): Flowable<BaseResponse<Boolean>> {
        return api.notify.deleteDevice(share.get(SharedPrefsKey.DEVICE_ID, String::class.java))
            .checkInternet()
    }
}