package boilerplate.model.dashboard

import com.google.gson.annotations.SerializedName

data class Statical(
    @SerializedName("cong_van_chua_xu_ly")
    val documentUnProcess: Int = 0,
    @SerializedName("cong_van_dang_xu_ly")
    val documentInProcess: Int = 0,
    @SerializedName("cong_van_di_can_ky_so")
    val signGoing: Int = 0,
    @SerializedName("cong_viec_can_thuc_hien")
    val workNeedDone: Int = 0,
    @SerializedName("cong_viec_chua_giao")
    val workNotAssign: Int = 0,
    @SerializedName("cong_viec_tre_han")
    val workOverTime: Int = 0,
)
