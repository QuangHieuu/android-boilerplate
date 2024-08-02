package boilerplate.model.user

import com.google.gson.annotations.SerializedName

data class Position(
    @SerializedName("chuc_vu_id")
    var id: String = "",
    @SerializedName("ma_chuc_vu")
    var code: String = "",
    @SerializedName("ten_chuc_vu")
    var name: String = "",
    @SerializedName("trang_thai")
    var status: Int = 0
)