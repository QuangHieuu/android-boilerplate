package gg.it.model.user

import com.google.gson.annotations.SerializedName
import gg.it.data.remote.service.ApiUrl
import gg.it.model.ExpandModel
import gg.it.model.file.AttachedFile
import gg.it.utils.ImageUtil.IMAGE_MAX_SIZE
import gg.it.utils.ImageUtil.IMAGE_THUMB_SIZE
import java.util.Locale

class User : ExpandModel() {
    @SerializedName("nhan_vien_id")
    var id: String? = null
        get() = if (field == null) "".also { field = it } else field

    @SerializedName("ten_nhan_vien")
    var name: String? = null
        get() = if (field == null) "".also { field = it } else field

    @SerializedName("anh_dai_dien")
    private var avatar: String? = null

    @SerializedName("so_dien_thoai")
    var phoneNumber: String? = null
        get() = if (field == null) "".also { field = it } else field

    @SerializedName("so_dien_thoai_khac")
    var diffPhoneNumber: String? = null
        get() = if (field == null) "".also { field = it } else field

    @SerializedName("chuc_danh_id")
    var titleId: String? = null
        get() = if (field == null) "".also { field = it } else field


    @SerializedName("ds_chuc_danh")
    private var titles: ArrayList<Title>? = null
        get() = if (field == null) ArrayList<Title>().also { field = it } else field

    @SerializedName("da_duyet")
    var isApproved = false

    @SerializedName("tam_trang")
    var statusDesc: String? = null
        get() = if (field == null) "".also { field = it } else field

    @SerializedName("ngay_sinh")
    var dayOfBirth: String? = null
        get() = if (field == null) "".also { field = it } else field

    var email: String? = null
        get() = if (field == null) "".also { field = it } else field

    @SerializedName("ten_viet_tat")
    val shortName: String? = null

    @SerializedName(value = "ten_viet_tat_don_vi", alternate = ["ten_don_vi_viet_tat"])
    val companyShort: String? = null

    @SerializedName("ma_phong_ban")
    var departmentShort: String? = null
        get() = if (field == null) "".also { field = it } else field

    @SerializedName("don_vi_cha_id")
    var parentCompanyId: String? = null
        get() = if (field == null) "".also { field = it } else field

    @SerializedName("phong_ban_chinh")
    var mainDepartmentName: String? = null
        get() = if (field == null) "".also { field = it } else field

    @SerializedName("don_vi_id")
    var companyId: String? = null
        get() = if (field == null) "".also { field = it } else field

    @SerializedName("phong_ban")
    var department: Department? = null
        get() = if (field == null) Department().also { field = it } else field

    @SerializedName("truc_tuyen")
    private var isOnline = 0

    @SerializedName("vai_tro")
    var vaiTro = 0

    @SerializedName("is_lanh_dao_don_vi")
    var isLeaderCompany = false

    @SerializedName("is_check_theo_doi")
    var isCheckWatcher = false

    @SerializedName("is_lanh_dao_phong_ban")
    var isLeaderDepartment = false

    @SerializedName("is_nhan_vien_chu_tri")
    var isLeader = false

    @SerializedName("nhom_hay_lien_lac_id")
    var regularGroupId: String? = null
        get() = if (field == null) "".also { field = it } else field

    @SerializedName("su_dung_evnca")
    var isUseEvnCa = false

    @SerializedName("su_dung_usb_token")
    var isUseUsbToken = false

    @SerializedName("so_dien_thoai_ky_so")
    var phoneNumberSignViettel: String? = null
        get() = if (field == null) "".also { field = it } else field

    @SerializedName("so_dien_thoai_ky_so_vnpt")
    var phoneNumberSignVNPT: String? = null
        get() = if (field == null) "".also { field = it } else field

    @SerializedName("trang_thai")
    var status = 0

    @SerializedName("buoc")
    var step = 0

    @SerializedName("truong_ky")
    var sign: String? = null
        get() = if (field == null) "".also { field = it } else field

    @SerializedName("thoi_gian")
    var time: String? = null
        get() = if (field == null) "".also { field = it } else field

    @SerializedName("thoi_gian_duyet")
    var approveTime: String? = null
        get() = if (field == null) "".also { field = it } else field

    @SerializedName("su_dung_sms")
    val isUseSMS = false

    @SerializedName("noi_dung_y_kien")
    var reportContent: String? = null
        get() = if (field == null) "".also { field = it } else field

    @SerializedName("ds_file_dinh_kem")
    var attachFiles: ArrayList<AttachedFile.Work>? = null
        get() = if (field == null) arrayListOf<AttachedFile.Work>().also { field = it }
        else field

    @SerializedName("ds_vai_tro")
    var roles: ArrayList<String>? = null
        get() = if (field == null) ArrayList<String>().also { field = it } else field

    @SerializedName("hinh_thuc")
    val form = 0

    @SerializedName("ngay_tao")
    var dayCreate: String? = null
        get() = if (field == null) "".also { field = it } else field

    var level = 0
    val listReacted = ArrayList<String>()

    var avatarId: String? = null
        get() = if (field == null) "".also { field = it } else field

    var isRegularMember = false

    fun isOnline() = isOnline == 1

    var mainCompany: Company? = null
        get() {
            if (field == null) {
                if (titles != null && titles!!.size > 0) {
                    for (title in titles!!) {
                        if (title.isMain) {
                            return title.company.also { field = it }
                        }
                    }
                } else {
                    return Company().also { field = it }
                }
            }
            return field
        }


    var mainDepartment: Department? = null
        get() {
            if (field == null) {
                if (titles != null && titles!!.size > 0) {
                    for (title in titles!!) {
                        if (title.isMain) {
                            return title.department.also { field = it }
                        }
                    }
                } else {
                    return Department().also { field = it }
                }
            }
            return field
        }


    val nameOnly: String
        get() {
            val ten = StringBuilder(name!!)
            val index = ten.lastIndexOf(" ")
            return ten.substring(index + 1, ten.length).trim { it <= ' ' }
        }

    fun getAvatar(): String {
        val avatar = avatarId!!.let {
            if (it.startsWith("/")) {
                it.replaceFirst("/".toRegex(), "")
            } else {
                it
            }
        }

        return String.format(
            Locale.getDefault(),
            "%s%s?w=%d",
            ApiUrl.HOST_IMAGE,
            avatar,
            IMAGE_MAX_SIZE
        )
    }

    fun getAvatar(isThumb: Boolean): String {
        val avatar = avatarId!!.let {
            if (it.startsWith("/")) {
                it.replaceFirst("/".toRegex(), "")
            } else {
                it
            }
        }
        return String.format(
            Locale.getDefault(),
            "%s%s?w=%d",
            ApiUrl.HOST_IMAGE,
            avatar,
            if (isThumb) IMAGE_THUMB_SIZE else IMAGE_MAX_SIZE
        )
    }

    fun setAvatar(avatar: String?) {
        this.avatar = avatar
    }

    val mainRole: String?
        get() {
            if (titles != null && titles!!.size > 0) {
                for (title in titles!!) {
                    if (title.isMain) {
                        return title.id
                    }
                }
            }
            return ""
        }

    class UpdateBody(
        @SerializedName("so_dien_thoai") val phone: String,
        @SerializedName("so_dien_thoai_khac") val otherPhone: String,
        @SerializedName("tam_trang") val status: String
    ) {
        @SerializedName("ngay_sinh")
        val dayOfBirth: String? = null
    }

    class Result {
        val items: ArrayList<User>? = null
            get() = field ?: ArrayList()
        val total = 0
    }

    class State {
        @SerializedName("user_id")
        val userId: String? = null
        val state = 0
    }

    class ChangePass(
        @SerializedName("current_password")
        private val currentPass: String,
        @SerializedName("new_password")
        private val newPass: String
    )
}