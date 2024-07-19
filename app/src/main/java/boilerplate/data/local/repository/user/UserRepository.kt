package boilerplate.data.local.repository.user

import boilerplate.data.local.sharedPrefs.SharedPrefsApi
import boilerplate.data.local.sharedPrefs.SharedPrefsKey
import boilerplate.data.remote.api.ApiRequest
import boilerplate.data.remote.api.response.BaseResponse
import boilerplate.data.remote.api.response.BaseResult
import boilerplate.data.remote.api.response.BaseResults
import boilerplate.model.user.Company
import boilerplate.model.user.Department
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

    fun saveContact(c: ArrayList<Company>)

    fun getContactCompany(): ArrayList<Company>

    fun getCurrentCompany(): Company

    fun getCurrentDepartment(): Department

    fun getCompanyDepartment(id: String): Flowable<BaseResult<Department>>
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
        for (title in user.titles) {
            if (title.isMain) {
                if (title.company?.id.isNullOrEmpty()) {
                    title.company?.id = title.companyId
                }
                if (title.department?.id.isNullOrEmpty()) {
                    title.department?.id = title.departmentId
                }
            }
        }
        share.put(SharedPrefsKey.USER_DATA, user)
        setCurrentRole(user.mainRole)
    }

    override fun wipeUserData() {
        share.clearKey(SharedPrefsKey.USER_DATA)
        share.clearKey(SharedPrefsKey.USER_COMPANY_DATA)
        share.clearKey(SharedPrefsKey.USER_ROLE_PERMISSION)
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
        share.put(SharedPrefsKey.USER_ROLE_PERMISSION, list)
    }

    override fun getRolePermission(): ArrayList<String> {
        val json = share.get(SharedPrefsKey.USER_ROLE_PERMISSION, String::class.java)

        return if (json.isEmpty()) {
            arrayListOf()
        } else {
            try {
                gson.fromJson(json, object : TypeToken<ArrayList<String>>() {}.type)
            } catch (ignore: Exception) {
                arrayListOf()
            }
        }
    }

    override fun saveContact(c: ArrayList<Company>) {
        share.put(SharedPrefsKey.USER_COMPANY_DATA, c)
    }

    override fun getContactCompany(): ArrayList<Company> {
        val json = share.get(SharedPrefsKey.USER_COMPANY_DATA, String::class.java)
        return if (json.isEmpty()) {
            arrayListOf()
        } else {
            try {
                gson.fromJson(json, object : TypeToken<ArrayList<Company>>() {}.type)
            } catch (ignore: Exception) {
                arrayListOf()
            }
        }
    }

    override fun getCurrentCompany(): Company {
        val user = getUser()
        val role = getCurrentRole()

        var company: Company? = null
        for (title in user.titles) {
            if (title.id.equals(role)) {
                company = title.company
            }
        }
        return company ?: Company()
    }

    override fun getCurrentDepartment(): Department {
        val user = getUser()
        val role = getCurrentRole()

        var department: Department? = null
        for (title in user.titles) {
            if (title.id.equals(role)) {
                department = title.department
            }
        }
        return department ?: Department()
    }

    override fun getCompanyDepartment(id: String): Flowable<BaseResult<Department>> {
        return api.chat.getCompanyDepartment(id).checkInternet()
    }

    override fun logout(): Flowable<BaseResponse<Boolean>> {
        return api.notify.deleteDevice(share.get(SharedPrefsKey.DEVICE_ID, String::class.java))
            .checkInternet()
    }
}