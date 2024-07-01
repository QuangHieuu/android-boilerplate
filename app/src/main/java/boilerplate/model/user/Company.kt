package boilerplate.model.user

import com.google.gson.annotations.SerializedName
import boilerplate.model.ExpandModel

class Company : ExpandModel {
    @SerializedName("don_vi_id")
    private var id: String? = null

    @SerializedName("ten_don_vi")
    private var name: String? = null

    @SerializedName("ten_viet_tat")
    private var shortName: String? = null

    @SerializedName("ds_phong_ban")
    var departments: ArrayList<Department>? = null
        get() = if (field == null) ArrayList<Department>().also { field = it } else field

    @SerializedName("ds_don_vi_con")
    var childCompanies: ArrayList<Company>? = null
        get() = if (field == null) ArrayList<Company>().also { field = it } else field
        private set

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
        private set

    @SerializedName("nguoi_ky")
    val signer: User? = null

    @SerializedName("trang_thai")
    val status = 0

    @SerializedName("thoi_gian_duyet")
    var approveTime: String? = null
        get() = if (field == null) "".also { field = it } else field
        private set

    constructor()
    constructor(ten: String?) {
        name = ten
        shortName = ten
        id = ""
    }

    fun getId(): String {
        return id ?: "".also { id = it }
    }

    fun getName(): String {
        return name ?: "".also { name = it }
    }

    fun getShortName(): String {
        return shortName ?: "".also { shortName = it }
    }

    class Result {
        val items: ArrayList<Company>? = null
    }
}