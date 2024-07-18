package boilerplate.data.local.repository.user

import boilerplate.data.local.sharedPrefs.SharedPrefsApi
import boilerplate.data.local.sharedPrefs.SharedPrefsKey
import boilerplate.data.local.sharedPrefs.SharedPrefsKey.USER_ROLE_PERMISSION
import boilerplate.data.remote.api.ApiRequest
import boilerplate.data.remote.api.response.BaseResponse
import boilerplate.model.user.User
import boilerplate.utils.extension.checkInternet
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

    fun setCurrentRole(role: String)

    fun getCurrentRole(): String

    fun getCurrentRoleFullName(): String

    fun saveRolePermission(r: ArrayList<User.Role>)

    fun getRolePermission(): ArrayList<String>
}

class UserRepositoryImpl(
    private val share: SharedPrefsApi,
    private val api: ApiRequest,
    private val gson: Gson
) : UserRepository {

    override fun getUser(): User {
        return share.get(SharedPrefsKey.USER_DATA, User::class.java)
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
        setCurrentRole(user.mainRole)
    }

    override fun wipeUserData() {
        share.clearKey(SharedPrefsKey.USER_DATA)
        share.clearKey(SharedPrefsKey.SIZE)
        share.clearKey(SharedPrefsKey.SOUND)
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

    override fun setCurrentRole(role: String) {
        share.put(SharedPrefsKey.USER_ROLE, role)
    }

    override fun getCurrentRole(): String {
        return share.get(SharedPrefsKey.USER_ROLE, String::class.java)
    }

    override fun getCurrentRoleFullName(): String {
        val user = getUser()
        val role = getCurrentRole()

        var name = ""
        for (title in user.titles) {
            if (title.id.equals(role)) {
                name = "${title.position?.ten_chuc_vu} ${title.department?.shortName}"
            }
        }
        return name
    }

    override fun saveRolePermission(r: ArrayList<User.Role>) {
        val list = arrayListOf<String>()
        for (role in r) {
            if (role.isActive) list.add(role.codeId)
        }
        share.put(USER_ROLE_PERMISSION, list)
    }

    override fun getRolePermission(): ArrayList<String> {
        val json = share.get(USER_ROLE_PERMISSION, String::class.java)

        return if (json.isEmpty()) {
            arrayListOf()
        } else {
            gson.fromJson(json, object : TypeToken<ArrayList<String>>() {}.type)
        }
    }

    override fun logout(): Flowable<BaseResponse<Boolean>> {
        return api.notify.deleteDevice(share.get(SharedPrefsKey.DEVICE_ID, String::class.java))
            .checkInternet()
    }
}