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
}