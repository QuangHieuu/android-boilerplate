package gg.it.data.remote.api.error

import com.google.gson.Gson
import gg.it.data.remote.api.error.Type.HTTP
import gg.it.data.remote.api.error.Type.NETWORK
import gg.it.data.remote.api.error.Type.SERVER
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
            when (errorType) {
                NETWORK -> return if (cause != null) {
                    getNetworkErrorMessage(cause!!)
                } else {
                    SERVER_ERROR
                }

                HTTP -> {
                    val api = response?.raw()?.request?.url.toString()
                    val code = response?.code() ?: 0
                    val message: String = cause?.message ?: SERVER_ERROR
                    val error = ApiError(code, message, api)
                    return Gson().toJson(error)
                }

                SERVER -> return SERVER_ERROR
                else -> return SERVER_ERROR
            }
        }

    private fun getNetworkErrorMessage(throwable: Throwable): String {
        return throwable.message ?: SERVER_ERROR
    }

    companion object {
        private const val SERVER_ERROR = ""

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