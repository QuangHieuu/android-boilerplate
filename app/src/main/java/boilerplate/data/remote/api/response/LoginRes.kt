package boilerplate.data.remote.api.response


import com.google.gson.annotations.SerializedName

data class LoginRes(
	@SerializedName("access_token")
	var accessToken: String? = null,
	@SerializedName("expires_in")
	var expiresIn: Int = 0,
	@SerializedName("token_type")
	var tokenType: String? = null,
	@SerializedName("refresh_token")
	var refreshToken: String? = null
)
