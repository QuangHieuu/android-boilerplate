package boilerplate.data.remote.service

import boilerplate.data.remote.api.response.BaseResponse
import boilerplate.model.login.LoginRes
import boilerplate.model.user.User
import io.reactivex.rxjava3.core.Flowable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface LoginService {
    @Headers(
        "Content-Type: application/x-www-form-urlencoded",
        "Authorization: Basic ZW9mZmljZS5yby5jbGllbnQ6dGN4LnNlY3JldA=="
    )
    @POST("connect/token")
    @FormUrlEncoded
    fun postLogin(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("grant_type") grant_type: String = "password"
    ): Flowable<LoginRes>
}

interface UserService {
    @GET("me")
    fun getMe(): Flowable<BaseResponse<User>>
}

interface ApiService : LoginService, UserService {

}