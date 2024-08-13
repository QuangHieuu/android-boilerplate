package boilerplate.data.remote.repository.auth

import boilerplate.data.local.repository.user.UserRepository
import boilerplate.data.remote.api.ApiRequest
import boilerplate.data.remote.api.response.LoginRes
import boilerplate.data.remote.api.response.Response
import boilerplate.data.remote.api.response.ResponseItems
import boilerplate.model.device.Device
import boilerplate.model.user.Company
import boilerplate.model.user.Role
import boilerplate.model.user.User
import boilerplate.utils.extension.checkInternet
import boilerplate.utils.extension.notNull
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

interface LoginRepository {
	fun postUserLogin(userName: String, password: String): Flowable<LoginRes>

	fun getMe(): Flowable<Response<User>>

	fun postRegisterDevice(device: Device): Flowable<Response<Device>>

	fun getRolePermission(): Single<ResponseItems<Role>>

	fun getContact(): Single<ResponseItems<Company>>

	fun getUser(id: String): Flowable<Response<User>>
}

class LoginRepositoryImpl(
	private val apiRequest: ApiRequest,
	private val userRepository: UserRepository
) : LoginRepository {

	override fun postUserLogin(userName: String, password: String) =
		apiRequest.login.postLogin(userName, password).checkInternet()

	override fun getMe() = apiRequest.eOffice.getMe().checkInternet()
		.doOnNext { it.result.notNull { user -> userRepository.saveUser(user) } }

	override fun postRegisterDevice(device: Device): Flowable<Response<Device>> {
		return apiRequest.notify.postDevice(device).checkInternet()
	}

	override fun getRolePermission(): Single<ResponseItems<Role>> {
		val role = userRepository.getCurrentRole()
		return apiRequest.eOffice.getRolePermission(role).checkInternet()
			.doOnSuccess { userRepository.saveRolePermission(it.result?.items ?: arrayListOf()) }
	}

	override fun getContact(): Single<ResponseItems<Company>> {
		return apiRequest.chat.getContactLevel().checkInternet()
			.doOnSuccess { userRepository.saveContact(it.result?.items ?: arrayListOf()) }
	}

	override fun getUser(id: String): Flowable<Response<User>> {
		return apiRequest.chat.getUser(id).checkInternet()
	}
}
