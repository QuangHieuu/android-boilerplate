package boilerplate.model.file

import android.net.Uri
import boilerplate.data.remote.service.ApiUrl
import boilerplate.model.ExpandModel
import boilerplate.utils.ImageUtil.IMAGE_MAX_SIZE
import boilerplate.utils.ImageUtil.IMAGE_THUMB_SIZE
import boilerplate.utils.SystemUtil.SYSTEM_DELETE
import com.google.gson.annotations.SerializedName
import java.util.Locale

open class AttachedFile : ExpandModel() {
    var id: String? = null
        get() = field ?: fileId

    @SerializedName(value = "file_id")
    var fileId: String? = null
        get() = field ?: "".also { field = it }

    @SerializedName("is_existed")
    var isExisted = false

    @SerializedName(value = "kieu_file", alternate = ["mime"])
    var fileType: String? = null
        get() = field ?: "".also { fileType = it }

    @SerializedName(value = "file_name", alternate = ["name"])
    var fileName: String? = null
        get() = field ?: "".also { field = it }

    @SerializedName(value = "ten_file")
    var realName: String? = null
        get() = field?.replace(SYSTEM_DELETE, "") ?: fileName

    @SerializedName(value = "full_name")
    var fullName: String? = null
        get() = field ?: "".also { field = it }

    @SerializedName(value = "kich_thuoc", alternate = ["size"])
    var fileSize = 0

    @SerializedName(value = "isMigrate", alternate = ["migrated"])
    var isMigrated = false

    @SerializedName(value = "ngay_tao")
    var createDate: String? = null
        get() = field ?: "".also { field = it }

    var fileSizeReal: String? = null
        get() = field ?: "".also { field = it }

    var application: String? = null
        get() = if (field == null) "".also { field = it } else field

    protected var url: String? = null
    protected var uri: Uri? = null

    var status = 0
    var isPreventRemove = true
    var isUpload = false

    //id của dữ liệu hỗ trợ cntt
    var supportId: String? = null
        get() = field ?: "".also { field = it }

    fun getPreviewUrl(isThumb: Boolean): String {
        return String.format(
            Locale.getDefault(),
            "%s%s?w=%d",
            ApiUrl.HOST_FILE_PREVIEW,
            id,
            if (isThumb) IMAGE_THUMB_SIZE else IMAGE_MAX_SIZE
        )
    }

    val filePreview: String
        get() = ApiUrl.HOST_FILE_PREVIEW + id
    val fileDownloadUrl: String
        get() = ApiUrl.HOST_FILE + "file/get/" + id + "?w=" + IMAGE_MAX_SIZE

    class Work : AttachedFile() {
        var idVB = 0

        @SerializedName("file_size_number")
        private val fileSizeNumber = 0
        val type: String? = null
        var isContentFile = false
        var isFirst = false
        var isFileLetter = false
        var isFileUploaded = false
        var count = 0
    }

    class Conversation : AttachedFile() {
        @SerializedName("hoi_thoai_id")
        var conversationId: String? = null

        @SerializedName("tin_nhan_id")
        var messageId: String? = null

        @SerializedName("file_dinh_kem_id")
        var fileAttachId: String? = null
        private val type: String? = null
    }

    class Question : AttachedFile() {
        var cau_hoi_id: String? = null
        var sttcauhoi = 0
        var cauhoi_loai = 0
        var positionCauHoi = 0
    }

    class SurveyFile : AttachedFile() {
        @SerializedName("phieu_khao_sat_id")
        var surveyId: String? = null
            get() = if (field == null) "".also { field = it } else field

        @SerializedName("tieu_de")
        var title: String? = null
            get() = if (field == null) "".also { field = it } else field

        val isSurveyBreakfast: Boolean
            get() = if (title!!.length > 5) {
                title!!.startsWith("[PĐT]")
            } else false

        val displayTitle: String
            get() = if (isSurveyBreakfast) {
                title!!.substring(5)
            } else {
                title!!
            }
    }
}