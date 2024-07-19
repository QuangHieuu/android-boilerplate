package boilerplate.model.user

import boilerplate.model.ExpandModel
import com.google.gson.annotations.SerializedName

class Department : ExpandModel() {
    @SerializedName("phong_ban_id")
    var id: String? = null
        get() = if (field == null) "".also { field = it } else field

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
    var totalUser: Int = 0

    @SerializedName("trang_thai")
    var status = 0

    @SerializedName("nguoi_ky")
    var signer: User? = null

    @SerializedName("ma_ky_so")
    var signCode: String? = null

    @SerializedName("is_active")
    var isActive = false

    @SerializedName("phong_ban_lanh_dao")
    var isLeaderDepartment = false
    var loai = 0
    var stt = 0

    @SerializedName("thoi_gian_duyet")
    var approveTime: String? = null
}