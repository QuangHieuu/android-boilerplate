package boilerplate.model.file

import android.content.Context
import android.net.Uri
import boilerplate.data.remote.api.ApiUrl
import boilerplate.model.ExpandModel
import boilerplate.utils.ImageUtil.IMAGE_MAX_SIZE
import boilerplate.utils.ImageUtil.IMAGE_THUMB_SIZE
import boilerplate.utils.SystemUtil
import boilerplate.utils.SystemUtil.SYSTEM_DELETE
import com.google.gson.annotations.SerializedName

data class AttachedFile(
	@SerializedName(value = "id", alternate = ["file_id"])
	var id: String = "",
	@SerializedName(value = "kieu_file", alternate = ["mime"])
	var fileType: String = "",
	@SerializedName("url")
	var filePath: String = "",
	@SerializedName(value = "file_name", alternate = ["name", "ten_file", "full_name"])
	var fileName: String = "",
	@SerializedName(value = "kich_thuoc", alternate = ["size"])
	var fileSize: Int = 0,
	@SerializedName(value = "ngay_tao")
	var createDate: String = "",
	@SerializedName("application")
	var application: String = "",

	@SerializedName("is_existed")
	var isExisted: Boolean = false,
	@SerializedName(value = "isMigrate", alternate = ["migrated"])
	var isMigrated: Boolean = false,

	/**
	 * file conversation
	 */
	@SerializedName("hoi_thoai_id")
	var conversationId: String = "",

	@SerializedName("tin_nhan_id")
	var messageId: String = "",
	@SerializedName("file_dinh_kem_id")
	var fileAttachId: String = "",

	@SerializedName("file_size_number")
	var fileSizeNumber: Int = 0,

	var supportId: String = "",

	@SerializedName("cau_hoi_id")
	var questionId: String = "",
	@SerializedName("sttcauhoi")
	var questionIndex: Int = 0,
	@SerializedName("cauhoi_loai")
	var questionType: Int = 0,
	@SerializedName("positionCauHoi")
	var questionPosition: Int = 0,

	/**
	 * file survey
	 */
	@SerializedName("phieu_khao_sat_id")
	var surveyId: String = "",
	@SerializedName("tieu_de")
	var surveyTitle: String = "",
) : ExpandModel() {
	var isUpload: Boolean = false
	var isPreventRemove: Boolean = true

	var isContentFile = false
	var isFirst = false
	var isFileLetter = false

	var status: Int = 0
	var type: String = ""

	var uri: Uri? = null

	fun getDisplayName(context: Context): String {
		if (uri == null) return fileName.replace(SYSTEM_DELETE, "")
		return SystemUtil.getDisplayFilename(context, uri!!)
	}

	fun getFileSize(context: Context): String {
		if (fileSize != 0 || uri == null) return SystemUtil.getDisplayFileSize(fileSize)
		return SystemUtil.getDisplayFileSize(context, uri!!)
	}

	val filePreview: String
		get() = ApiUrl.HOST_FILE_PREVIEW + id + "?w=" + IMAGE_MAX_SIZE

	val fileThumb: String
		get() = ApiUrl.HOST_FILE_PREVIEW + id + "?w=" + IMAGE_THUMB_SIZE

	val fileDownload: String
		get() = ApiUrl.HOST_FILE + "file/get/" + id

	val isSurveyBreakfast: Boolean
		get() = if (surveyTitle.length > 5) {
			surveyTitle.startsWith("[PĐT]")
		} else false

	val displayTitle: String
		get() = if (isSurveyBreakfast) {
			surveyTitle.substring(5)
		} else {
			surveyTitle
		}
}

fun ArrayList<UploadFile>.convertFile(): ArrayList<AttachedFile> {
	val list = arrayListOf<AttachedFile>()
	for (upload in this) {
		list.add(AttachedFile().apply {
			id = upload.id
			fileName = upload.originalName
			fileSize = upload.length
			fileType = upload.type
		})
	}
	return list
}