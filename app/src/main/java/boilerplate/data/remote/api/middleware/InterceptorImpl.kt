package boilerplate.data.remote.api.middleware

import boilerplate.constant.Constants.KEY_AUTH
import boilerplate.data.local.repository.user.TokenRepository
import boilerplate.utils.extension.notNull
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


class InterceptorImpl(private var token: TokenRepository) : Interceptor {

	companion object {
		const val CODE_REFRESH_TOKEN: Int = 401
	}

	@Throws(IOException::class)
	override fun intercept(chain: Interceptor.Chain): Response {
		val builder = initializeHeader(chain)
		val request = builder.build()
		val response = chain.proceed(request)

		if (response.code == CODE_REFRESH_TOKEN) {
			val resStr = response.body?.string().toString()
			val json = JSONObject(resStr)
		}
		return response
	}

	private fun initializeHeader(chain: Interceptor.Chain): Request.Builder {
		val originRequest = chain.request()
		val builder = originRequest.newBuilder()
			.addHeader("Accept", "application/json")
			.addHeader("Content-Type", "application/json")
			.method(originRequest.method, originRequest.body)

		token.getToken().notNull { accessToken ->
			if (accessToken.isNotEmpty()) {
				builder.addHeader(KEY_AUTH, accessToken)
			}
		}
		return builder
	}
}