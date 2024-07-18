package boilerplate.model.user

import com.google.gson.annotations.SerializedName

class Title {
    var id: String? = null
        get() = field ?: "".also { field = it }

    @SerializedName("phong_ban_chinh")
    val isMain = false

    @SerializedName("don_vi")
    var company: Company? = null
        get() = field ?: Company().also { field = it }

    @SerializedName("phong_ban")
    var department: Department? = null
        get() = field ?: Department().also { field = it }

    @SerializedName("chuc_vu")
    var position: Position? = null
        get() = field ?: Position().also { field = it }

    @SerializedName("doi_tuong")
    var `object`: PersonObject? = null
        get() = field ?: PersonObject().also { field = it }

}