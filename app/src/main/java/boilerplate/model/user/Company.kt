package boilerplate.model.user

import boilerplate.model.ExpandModel
import com.google.gson.annotations.SerializedName

class Company : ExpandModel {
    @SerializedName("don_vi_id")
    var id: String? = null
        get() = if (field == null) "".also { field = it } else field

    @SerializedName("ten_don_vi")
    var name: String? = null
        get() = if (field == null) "".also { field = it } else field

    @SerializedName("ten_viet_tat")
    var shortName: String? = null
        get() = if (field == null) "".also { field = it } else field

    @SerializedName("ds_phong_ban")
    var departments: ArrayList<Department>? = null
        get() = if (field == null) ArrayList<Department>().also { field = it } else field

    @SerializedName("ds_don_vi_con")
    var childCompanies: ArrayList<Company>? = null
        get() = if (field == null) ArrayList<Company>().also { field = it } else field
        set

    @SerializedName("phong_ban")
    val department: Department? = null
    val level = 0

    @SerializedName("gui_don_vi_lien_thong")
    val isSend = false

    @SerializedName("gui_don_vi_lien_thong_evn")
    val isSendEvn = false

    @SerializedName("gui_don_vi_truc_thuoc")
    val isSendCompany = false

    @SerializedName("gui_don_vi_khac")
    val isSendOtherCompany = false

    @SerializedName("dong_y_khong_can_ky_so")
    val isAcceptSign = false

    @SerializedName("don_vi_cha")
    var parentCompany: String? = null
        get() = if (field == null) "".also { field = it } else field
        set

    @SerializedName("nguoi_ky")
    var signer: User? = null

    @SerializedName("trang_thai")
    var status = 0

    @SerializedName("thoi_gian_duyet")
    var approveTime: String? = null
        get() = if (field == null) "".also { field = it } else field
        set

    constructor()

    constructor(ten: String?) {
        name = ten
        shortName = ten
        id = ""
    }

    class Result {
        val items: ArrayList<Company>? = null
    }
}