package boilerplate.model.user

import com.google.gson.annotations.SerializedName

class Title {
    val id: String? = null

    @SerializedName("phong_ban_chinh")
    val isMain = false

    @SerializedName("don_vi")
    val company: Company? = null

    @SerializedName("phong_ban")
    val department: Department? = null

    @SerializedName("chuc_vu")
    val position: Position? = null

    @SerializedName("doi_tuong")
    val `object`: Any? = null
}