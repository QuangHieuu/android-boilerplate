package boilerplate.data.remote.repository.auth

import boilerplate.data.local.repository.user.UserRepository
import boilerplate.data.remote.api.ApiRequest
import boilerplate.data.remote.api.response.BaseResponse
import boilerplate.data.remote.api.response.BaseResult
import boilerplate.model.device.Device
import boilerplate.model.login.LoginRes
import boilerplate.model.user.Company
import boilerplate.model.user.User
import boilerplate.utils.extension.checkInternet
import boilerplate.utils.extension.notNull
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

interface LoginRepository {
    fun postUserLogin(userName: String, password: String): Flowable<LoginRes>

    fun getMe(): Flowable<BaseResponse<User>>

    fun postRegisterDevice(device: Device): Flowable<BaseResponse<Device>>

    fun getRolePermission(): Single<BaseResult<User.Role>>

    fun getContact(): Single<BaseResult<Company>>
}

class LoginRepositoryImpl(
    private val apiRequest: ApiRequest,
    private val userRepository: UserRepository
) : LoginRepository {

    override fun postUserLogin(userName: String, password: String) =
        apiRequest.login.postLogin(userName, password).checkInternet()

    override fun getMe() = apiRequest.eOffice.getMe().checkInternet()
        .doOnNext { it.result.notNull { userRepository.saveUser(it) } }

    override fun postRegisterDevice(device: Device): Flowable<BaseResponse<Device>> {
        return apiRequest.notify.postDevice(device).checkInternet()
    }

    override fun getRolePermission(): Single<BaseResult<User.Role>> {
        val role = userRepository.getCurrentRole()
        return apiRequest.eOffice.getRolePermission(role).checkInternet()
            .doOnSuccess { userRepository.saveRolePermission(it.result?.items ?: arrayListOf()) }
    }

    override fun getContact(): Single<BaseResult<Company>> {
        return apiRequest.chat.getContactLevel().checkInternet()
            .doOnSuccess { userRepository.saveContact(it.result?.items ?: arrayListOf()) }
    }
}
