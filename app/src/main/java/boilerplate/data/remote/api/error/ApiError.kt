package boilerplate.data.remote.api.error

import com.google.gson.annotations.SerializedName

data class ApiError(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("api") val api: String
)
