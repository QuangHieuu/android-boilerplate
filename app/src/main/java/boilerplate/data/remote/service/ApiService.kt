package boilerplate.data.remote.service

import boilerplate.data.remote.api.response.BaseResponse
import boilerplate.data.remote.api.response.BaseResult
import boilerplate.model.conversation.Conversation
import boilerplate.model.dashboard.Banner
import boilerplate.model.dashboard.Dashboard
import boilerplate.model.device.Device
import boilerplate.model.login.LoginRes
import boilerplate.model.user.Company
import boilerplate.model.user.User
import io.reactivex.rxjava3.core.Flowable
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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

    @POST("device")
    fun postDevice(@Body device: Device): Flowable<BaseResponse<Device>>

    @DELETE("device")
    fun deleteDevice(@Query("deviceId") deviceId: String): Flowable<BaseResponse<Boolean>>
}

interface UserService {
    @GET("me")
    fun getMe(): Flowable<BaseResponse<User>>

    @GET("quyen/nhanvien/{id}")
    fun getRolePermision(@Path("id") roleId: String?): Flowable<BaseResult<User.Role>>

    @GET("donvi/multilevel")
    fun getContactLevel(): Flowable<BaseResult<Company>>

//    var apiRole: Call<BaseRes<Role.Result>> = BaseRequest.getEOffice().getRoles(roleId)
//    var apiContact: Call<BaseRes<Company.Result>> = BaseRequest.getChat().getContactLevel()
}

interface DashboardService {
    @GET("sliders?isActive=true")
    fun getBanner(): Flowable<BaseResult<Banner>>

    @GET("dashboard")
    fun getDashBoardStatical(@Query("limit") limit: Int = 10): Flowable<BaseResponse<Dashboard>>
}

interface ConversationService {

    @GET("hoithoai")
    fun getConversations(
        @Query("hoithoaiId") id: String?,
        @Query("limit") limit: Int,
        @Query("unread") isUnread: Boolean?,
        @Query("isQuanTrong") isImportant: Boolean?,
        @Query("name") search: String?
    ): Flowable<BaseResponse<Conversation.Result>>

}

interface ApiService :
    LoginService, UserService, ConversationService, DashboardService