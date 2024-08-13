package boilerplate.data.remote.api.error

import com.google.gson.annotations.SerializedName

data class ApiError(
	@SerializedName("code") var code: Int = 0,
	@SerializedName("message") override var message: String? = "",
	@SerializedName("api") var api: String = ""
) : Throwable()
