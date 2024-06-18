package gg.it.model.login


import com.google.gson.annotations.SerializedName
import gg.it.data.remote.api.response.BaseResponse

class LoginRes : BaseResponse<Any>() {
    @SerializedName("access_token")
    var accessToken: String? = null
        get() = if (field == null) "".also { field = it } else field

    @SerializedName("expires_in")
    var expiresIn: Int = 0

    @SerializedName("token_type")
    var tokenType: String? = null
        get() = field ?: "".also { field = it }

    @SerializedName("refresh_token")
    var refreshToken: String? = null
        get() = field ?: "".also { field = it }
}
