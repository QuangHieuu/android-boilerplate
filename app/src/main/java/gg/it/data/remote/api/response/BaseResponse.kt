package gg.it.data.remote.api.response

import com.google.gson.annotations.SerializedName

open class BaseResponse<T> {
    val result: T? = null
    val items: T? = null
    val requests: T? = null
    val request: T? = null
    val attachments: T? = null

    var code: Int = 0
    var message: String = ""

    @SerializedName("error")
    val error: Int = 0
}