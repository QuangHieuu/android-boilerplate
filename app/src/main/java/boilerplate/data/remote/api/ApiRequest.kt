package boilerplate.data.remote.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit

class ApiRequest(
    private var builder: Retrofit.Builder,
    private var okHttpClient: OkHttpClient
) {
    companion object {
        const val VERSION = "1.0"
        const val VERSION_2 = "2.0"
    }

    private fun createRequest(url: String, version: String = VERSION): ApiService {
        val okBuilder = okHttpClient.newBuilder().addInterceptor { chain ->
            chain.request().let {
                chain.proceed(it.newBuilder().apply {
                    header("api-version", version)
                    method(it.method, it.body)
                }.build())
            }
        }

        return builder
            .baseUrl(url)
            .client(okBuilder.build())
            .build()
            .create(ApiService::class.java)
    }

    val login get() = createRequest(ApiUrl.HOST_SIGN_IN)

    val eOffice get() = createRequest(ApiUrl.HOST_MAIN + ApiUrl.API)

    fun getEOffice(version: String): ApiService =
        createRequest(ApiUrl.HOST_MAIN + ApiUrl.API, version)

    val chat get() = createRequest(ApiUrl.HOST_CHAT + ApiUrl.API)

    val notify get() = createRequest(ApiUrl.HOST_NOTIFICATION + ApiUrl.API)

    val file get() = createRequest(ApiUrl.HOST_FILE + ApiUrl.API)

    val firebase get() = createRequest(ApiUrl.FIREBASE_URL_VERSON)

    val calender get() = createRequest(ApiUrl.HOST_MEETING_CALENDAR + ApiUrl.API)

}