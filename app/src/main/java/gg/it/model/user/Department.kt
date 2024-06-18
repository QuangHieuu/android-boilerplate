package gg.it.model.user

import com.google.gson.annotations.SerializedName
import gg.it.model.ExpandModel

class Department : ExpandModel() {
    @SerializedName("phong_ban_id")
    var id: String? = null
        get() = if (field == null) "".also { field = it } else field
        private set

    @SerializedName("ten_phong_ban")
    var name: String? = null
        get() = if (field == null) "".also { field = it } else field
        private set

    @SerializedName("ma_phong_ban")
    var shortName: String? = null
        get() = if (field == null) "".also { field = it } else field

    @SerializedName("phong_ban_cha")
    var parentDepartment: String? = null
        get() = if (field == null) "".also { field = it } else field
        private set

    @SerializedName("ds_phong_ban_con")
    var childDepartments: ArrayList<Department>? = null
        get() = if (field == null) ArrayList<Department>().also { field = it } else field
        private set

    @SerializedName("ds_nhan_vien")
    var users: ArrayList<User>? = null
        get() = if (field == null) ArrayList<User>().also { field = it } else field

    @SerializedName("total_nhan_vien")
    val totalUser = 0

    @SerializedName("trang_thai")
    val status = 0

    @SerializedName("nguoi_ky")
    val signer: User? = null

    @SerializedName("ma_ky_so")
    val signCode: String? = null

    @SerializedName("is_active")
    val isActive = false

    @SerializedName("phong_ban_lanh_dao")
    val isLeaderDepartment = false
    val loai = 0
    val stt = 0

    @SerializedName("thoi_gian_duyet")
    val approveTime: String? = null

    class Result {
        var items: ArrayList<Department>? = null
            get() = if (field == null) ArrayList<Department>().also { field = it } else field
            private set
    }
}