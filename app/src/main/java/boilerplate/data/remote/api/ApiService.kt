package boilerplate.data.remote.api

import boilerplate.data.remote.api.response.LoginRes
import boilerplate.data.remote.api.response.Response
import boilerplate.data.remote.api.response.ResponseItems
import boilerplate.data.remote.api.response.Responses
import boilerplate.model.conversation.Conversation
import boilerplate.model.conversation.ConversationConfig
import boilerplate.model.conversation.SignalBody
import boilerplate.model.dashboard.Banner
import boilerplate.model.dashboard.Dashboard
import boilerplate.model.device.Device
import boilerplate.model.file.AttachedFile
import boilerplate.model.file.UploadFile
import boilerplate.model.message.Message
import boilerplate.model.user.Company
import boilerplate.model.user.Department
import boilerplate.model.user.Role
import boilerplate.model.user.UpdateBody
import boilerplate.model.user.User
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
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
    fun postDevice(@Body device: Device): Flowable<Response<Device>>

    @DELETE("device")
    fun deleteDevice(@Query("deviceId") deviceId: String): Flowable<Response<Boolean>>
}

interface UserService {
    @GET("me")
    fun getMe(): Flowable<Response<User>>

    @GET("nhanvien/{id}")
    fun getUser(@Path("id") id: String?): Flowable<Response<User>>

    @GET("quyen/nhanvien/{id}")
    fun getRolePermission(@Path("id") roleId: String?): Single<ResponseItems<Role>>

    @GET("donvi/multilevel")
    fun getContactLevel(): Single<ResponseItems<Company>>

    @GET("donvi/{id}/phongban")
    fun getCompanyDepartment(@Path("id") companyId: String): Flowable<ResponseItems<Department>>

    @PATCH("nhanvien")
    fun patchUser(@Body user: UpdateBody): Flowable<Response<Any>>
}

interface DashboardService {
    @GET("sliders?isActive=true")
    fun getBanner(): Flowable<ResponseItems<Banner>>

    @GET("dashboard")
    fun getDashBoardStatical(@Query("limit") limit: Int = 10): Flowable<Response<Dashboard>>
}

interface ConversationService {

    @GET("hoithoai?ghim=false")
    fun getConversations(
        @Query("hoithoaiId") id: String?,
        @Query("limit") limit: Int,
        @Query("unread") isUnread: Boolean?,
        @Query("isQuanTrong") isImportant: Boolean?,
        @Query("name") search: String?
    ): Flowable<ResponseItems<Conversation>>

    @GET("hoithoai/ghim")
    fun getPinConversations(): Flowable<ResponseItems<Conversation>>

    @GET("hoithoai/{id}")
    fun getConversationDetail(@Path("id") conversationId: String?): Flowable<Response<Conversation>>

    @GET("hoithoai/{id}/tinnhan")
    fun getConversationMessage(
        @Path("id") conversationId: String,
        @Query("limit") limit: Int,
        @Query("tinnhanId") lastMessage: String?
    ): Flowable<ResponseItems<Message>>

    @GET("hoithoai/{id}/tinnhan")
    @Headers("api-version: 2")
    fun getConversationMessage(
        @Path("id") conversationId: String,
        @Query("limit") limit: Int,
        @Query("tinnhanId") lastMessage: String,
        @Query("isDesc") isDesc: Boolean
    ): Flowable<ResponseItems<Message>>

    @GET("hoithoai/configuration")
    fun getConversationConfig(): Single<ConversationConfig>

    /**
     * @param type Loại file (0: image, 1: files)
     */
    @GET("hoithoai/{id}/filedinhkem")
    fun getConversationFile(
        @Path("id") conversationId: String?,
        @Query("type") type: Int,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Single<ResponseItems<AttachedFile.Conversation>>

    @PUT("hoithoai")
    fun putUpdateGroup(
        @Query("connectionId") id: String?,
        @Body body: SignalBody
    ): Flowable<ResponseItems<Any>>

    /**
     * @param isApproved - isDaDuyet = null: lấy tất cả, chưa duyệt và đã duyệt,
     * - isDaDuyet = true: đã duyệt,
     * - isDaDuyet  = false: chưa duyệt
     */
    @GET("hoithoai/{id}/nhanvien/timkiem")
    fun getMemberConversation(
        @Path("id") conversationId: String?,
        @Query("page") page: Int?,
        @Query("tenNhanVien") name: String?,
        @Query("isDaDuyet") isApproved: Boolean?,
        @Query("limit") limit: Int
    ): Flowable<ResponseItems<User>>

    @GET("tinnhan/link")
    fun getConversationLink(
        @Query("hoiThoaiId") conversationId: String?,
        @Query("tinNhanId") page: String?,
        @Query("limit") limit: Int
    ): Single<ResponseItems<Message>>

    @GET("tinnhan/ghim")
    fun getPinMessages(
        @Query("hoithoaiId") conversationId: String?,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Flowable<ResponseItems<Message>>

    /**
     * @param id same as conversationId
     */
    @GET("tinnhan/quantrong/hoithoai/{id}?isQuanTrong=true")
    fun getImportantMessage(
        @Path("id") conversationId: String?,
        @Query("hoiThoaiId") id: String?,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Flowable<ResponseItems<Message>>

    @POST("hoithoaicanhan")
    fun postPersonConversation(
        @Query("connectionId") id: String,
        @Body body: SignalBody
    ): Flowable<Response<Conversation>>
}

interface FileService {

    @Multipart
    @POST("file/chat")
    @Headers("api-version: 1")
    fun postConversationFile(@Part files: List<MultipartBody.Part>): Flowable<Responses<UploadFile>>

    @Multipart
    @POST("file/chat")
    @Headers("api-version: 1")
    fun postConversationFile(@Part files: MultipartBody.Part): Flowable<Responses<UploadFile>>

}

interface ApiService :
    LoginService, UserService, ConversationService, DashboardService, FileService