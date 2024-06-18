package gg.it.data.remote.api.middleware

import gg.it.data.remote.repository.auth.TokenRepository
import gg.it.utils.extension.notNull
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.*


class InterceptorImpl(private var token: TokenRepository) : Interceptor {

    companion object {
        private const val KEY_TOKEN = "Authorization"

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
                builder.addHeader(KEY_TOKEN, accessToken)
            }
        }
        return builder
    }
}