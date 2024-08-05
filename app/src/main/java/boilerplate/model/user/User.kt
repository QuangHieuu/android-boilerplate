package boilerplate.model.user

import boilerplate.data.remote.api.ApiUrl
import boilerplate.model.ExpandModel
import boilerplate.model.file.AttachedFile
import com.google.gson.annotations.SerializedName
import java.util.Locale

data class User(
    @SerializedName("nhan_vien_id")
    var id: String = "",
    @SerializedName("ngay_sinh")
    var dayOfBirth: String = "",
    @SerializedName("tam_trang")
    var mood: String = "",
    @SerializedName("ten_nhan_vien")
    var name: String = "",
    @SerializedName("anh_dai_dien")
    var avatarId: String = "",
    @SerializedName("so_dien_thoai")
    var phoneNumber: String = "",
    @SerializedName("so_dien_thoai_khac")
    var diffPhoneNumber: String = "",
    @SerializedName("chuc_danh_id")
    var titleId: String = "",
    @SerializedName("ds_chuc_danh")
    var titles: ArrayList<Title> = arrayListOf(),
    @SerializedName("da_duyet")
    var isApproved: Boolean = false,
    var email: String = "",
    @SerializedName("ten_viet_tat")
    val shortName: String = "",
    @SerializedName(value = "ten_viet_tat_don_vi", alternate = ["ten_don_vi_viet_tat"])
    val companyShort: String = "",
    @SerializedName("ma_phong_ban")
    var departmentShort: String = "",
    @SerializedName("don_vi_cha_id")
    var parentCompanyId: String = "",
    @SerializedName("phong_ban_chinh")
    var mainDepartmentName: String = "",
    @SerializedName("don_vi_id")
    var companyId: String = "",
    @SerializedName("phong_ban")
    var department: Department = Department(),
    @SerializedName("truc_tuyen")
    private var isOnline: Int = 0,
    @SerializedName("vai_tro")
    var role: Int = 0,
    @SerializedName("is_lanh_dao_don_vi")
    var isLeaderCompany: Boolean = false,
    @SerializedName("is_check_theo_doi")
    var isCheckWatcher: Boolean = false,
    @SerializedName("is_lanh_dao_phong_ban")
    var isLeaderDepartment: Boolean = false,
    @SerializedName("is_nhan_vien_chu_tri")
    var isLeader: Boolean = false,
    @SerializedName("nhom_hay_lien_lac_id")
    var regularGroupId: String = "",
    @SerializedName("su_dung_evnca")
    var isUseEvnCa: Boolean = false,
    @SerializedName("su_dung_usb_token")
    var isUseUsbToken: Boolean = false,
    @SerializedName("so_dien_thoai_ky_so")
    var phoneNumberSignViettel: String = "",
    @SerializedName("so_dien_thoai_ky_so_vnpt")
    var phoneNumberSignVNPT: String = "",
    @SerializedName("trang_thai")
    var status: Int = 0,
    @SerializedName("buoc")
    var step: Int = 0,
    @SerializedName("truong_ky")
    var sign: String = "",
    @SerializedName("thoi_gian")
    var time: String = "",
    @SerializedName("thoi_gian_duyet")
    var approveTime: String = "",
    @SerializedName("su_dung_sms")
    val isUseSMS: Boolean = false,
    @SerializedName("noi_dung_y_kien")
    var reportContent: String = "",
    @SerializedName("ds_file_dinh_kem")
    var attachFiles: ArrayList<AttachedFile.Work> = arrayListOf(),
    @SerializedName("ds_vai_tro")
    var roles: ArrayList<String>? = arrayListOf(),
    @SerializedName("hinh_thuc")
    var form: Int = 0,
    @SerializedName("ngay_tao")
    var dayCreate: String = ""
) : ExpandModel() {

    val listReacted = ArrayList<String>()
    var isRegularMember: Boolean = false
    var level: Int = 0

    fun isOnline() = isOnline == 1

    fun setOnline(i: Int) {
        isOnline = i
    }

    val mainCompany: Company
        get() {
            if (titles.size > 0) {
                for (title in titles) {
                    if (title.isMain) {
                        return title.company
                    }
                }
            }
            return Company()
        }

    val mainDepartment: Department
        get() {
            if (titles.size > 0) {
                for (title in titles) {
                    if (title.isMain) {
                        return title.department
                    }
                }
            }
            return Department()
        }

    val avatar: String
        get() {
            return avatarId.let {
                if (it.startsWith("/")) {
                    it.replaceFirst("/".toRegex(), "")
                } else {
                    it
                }
            }.let { String.format(Locale.getDefault(), "%s%s", ApiUrl.HOST_FILE, it) }
        }

    val nameOnly: String
        get() {
            val ten = StringBuilder(name)
            val index = ten.lastIndexOf(" ")
            return ten.substring(index + 1, ten.length).trim { it <= ' ' }
        }

    val mainRole: String
        get() {
            if (titles.isNotEmpty()) {
                for (title in titles) {
                    if (title.isMain) {
                        return title.id
                    }
                }
            }
            return ""
        }
}

data class UpdateBody(
    @SerializedName("so_dien_thoai") val phone: String,
    @SerializedName("so_dien_thoai_khac") val otherPhone: String,
    @SerializedName("tam_trang") val mood: String,
    @SerializedName("anh_dai_dien") val avatar: String?
)

data class State(
    @SerializedName("user_id") var userId: String,
    var state: Int
)

data class ChangePass(
    @SerializedName("current_password")
    val currentPass: String,
    @SerializedName("new_password")
    val newPass: String
)

data class Role(
    @SerializedName("ma_quyen")
    val codeId: String,
    @SerializedName("mo_ta")
    val description: String,
    @SerializedName("is_active")
    val isActive: Boolean
)