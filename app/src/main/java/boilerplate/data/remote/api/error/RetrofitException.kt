package boilerplate.data.remote.api.error

import boilerplate.data.remote.api.error.Type.HTTP
import boilerplate.data.remote.api.error.Type.NETWORK
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException

open class RetrofitException : RuntimeException {

	private val errorType: String
	private var response: Response<*>? = null

	constructor(errorType: String, cause: Throwable) : super(cause.message, cause) {
		this.errorType = errorType
	}

	constructor(cause: Throwable, response: Response<*>?) : super(cause.message, cause) {
		this.errorType = HTTP
		this.response = response
	}

	val errorMessage: String
		get() {
			val api = response?.raw()?.request?.url.toString()
			val code = response?.code() ?: 0
			val message: String = when (errorType) {
				HTTP -> getNetworkErrorMessage()
				else -> cause?.message ?: SERVER_ERROR
			}
			val error = ApiError(code, message, api)
			return Gson().toJson(error)
		}

	private fun getNetworkErrorMessage(): String {
		val errorBody = response?.errorBody()?.string() ?: ""
		return if (errorBody.isEmpty()) {
			cause?.message ?: SERVER_ERROR
		} else {
			try {
				JSONObject(errorBody.trim { it <= ' ' }).getString("message")
			} catch (e: JSONException) {
				SERVER_ERROR
			}
		}
	}

	companion object {

		private const val SERVER_ERROR = "Server not found"

		fun networkError(throwable: IOException): RetrofitException {
			return RetrofitException(NETWORK, throwable)
		}

		fun unexpectedError(throwable: Throwable): RetrofitException {
			return RetrofitException(Type.UNEXPECTED, throwable)
		}

		fun httpError(throwable: Throwable, response: Response<*>?): RetrofitException {
			return RetrofitException(throwable, response)
		}
	}
}