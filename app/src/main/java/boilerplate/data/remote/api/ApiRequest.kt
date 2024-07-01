package boilerplate.data.remote.api

import boilerplate.data.remote.service.ApiService
import boilerplate.data.remote.service.ApiUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit

class ApiRequest(
    private var builder: Retrofit.Builder,
    private var okHttpClient: OkHttpClient
) {

    private fun createRequest(url: String): ApiService = builder
        .baseUrl(url)
        .client(okHttpClient)
        .build()
        .create(ApiService::class.java)

    val login get() = createRequest(ApiUrl.HOST_SIGN_IN)

    val eOffice get() = createRequest(ApiUrl.HOST_E_OFFICE + ApiUrl.API)

    val chat get() = createRequest(ApiUrl.HOST_CHAT + ApiUrl.API)

    val notify get() = createRequest(ApiUrl.HOST_NOTIFICATION + ApiUrl.API)

    val file get() = createRequest(ApiUrl.HOST_FILE + ApiUrl.API)

    val firebase get() = createRequest(ApiUrl.FIREBASE_URL_VERSON)

    val calender get() = createRequest(ApiUrl.HOST_MEETING_CALENDAR + ApiUrl.API)

}