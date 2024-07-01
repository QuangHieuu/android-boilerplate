package boilerplate.data.remote.repository.auth

import boilerplate.data.remote.api.ApiRequest
import boilerplate.data.remote.api.response.BaseResponse
import boilerplate.data.remote.service.ApiService
import boilerplate.model.login.LoginRes
import boilerplate.model.user.User
import boilerplate.utils.extension.checkInternet
import io.reactivex.rxjava3.core.Flowable

interface LoginRepository {
    fun postUserLogin(userName: String, password: String): Flowable<LoginRes>

    fun getMe(): Flowable<BaseResponse<User>>
}

class LoginRepositoryImpl(
    private val apiRequest: ApiRequest
) : LoginRepository {

    override fun postUserLogin(userName: String, password: String) =
        apiRequest.login.postLogin(userName, password).checkInternet()

    override fun getMe() = apiRequest.login.getMe().checkInternet()

}
