package boilerplate.model.message

import boilerplate.model.conversation.Conversation
import boilerplate.model.file.AttachedFile
import boilerplate.model.user.User
import boilerplate.utils.StringUtil
import boilerplate.utils.StringUtil.countWord
import boilerplate.utils.StringUtil.getMessageForward
import com.google.gson.annotations.SerializedName

data class Message(
	@SerializedName("tin_nhan_id")
	var messageId: String = "",
	@SerializedName("tin_nhan_ghim_id")
	var messagePinId: String = "",
	@SerializedName("hoi_thoai_id")
	var conversationId: String = "",

	@SerializedName("hoi_thoai")
	var conversation: Conversation = Conversation(),

	@SerializedName("nguoi_gui_id")
	var personSendId: String = "",
	@SerializedName("nguoi_gui")
	var personSend: User = User(),

	@SerializedName("nhan_vien_ghim_tin_nhan_id")
	val personPinId: String = "",
	@SerializedName("nguoi_ghim")
	var personPin: User = User(),

	@SerializedName("nguoi_nhan")
	private var _receiverNotifies: ArrayList<ReceiverNotify>? = null,

	@SerializedName("noi_dung")
	var content: String = "",

	@SerializedName("file_dinh_kem")
	private var _attachedFiles: ArrayList<AttachedFile>? = null,

	@SerializedName("phieu_khao_sat")
	private var _surveyFiles: ArrayList<AttachedFile>? = null,

	@SerializedName("ds_thong_ke_emoticon")
	private var _reactions: ArrayList<Reaction>? = null,

	@SerializedName("gui_kem_email")
	var isSendMail: Boolean = false,

	@SerializedName("gui_kem_sms")
	var isSendSms: Boolean = false,

	@SerializedName("danh_dau")
	var isImportant: Boolean = false,

	@SerializedName("is_evict")
	var isWithdraw: Boolean = false,

	@SerializedName("is_msg_system")
	var isMsgSystem: Boolean = false,

	@SerializedName("ngay_tao")
	var dateCreate: String = "",

	@SerializedName("ngay_tao_group")
	var dateCreateGroup: String = "",
) {

	val receiverNotifies: ArrayList<ReceiverNotify>
		get() {
			return _receiverNotifies ?: arrayListOf<ReceiverNotify>().also { _receiverNotifies = it }
		}

	val attachedFiles: ArrayList<AttachedFile>
		get() {
			return _attachedFiles ?: arrayListOf<AttachedFile>().also { _attachedFiles = it }
		}

	val surveyFiles: ArrayList<AttachedFile>
		get() {
			return _surveyFiles ?: arrayListOf<AttachedFile>().also { _surveyFiles = it }
		}
	val reactions: ArrayList<Reaction>
		get() {
			return _reactions ?: arrayListOf<Reaction>().also { _reactions = it }
		}

	/**
	 * trạng thái = 2: offline
	 */
	var status: Int = 0
	var isFocus: Boolean = false
	var isHide: Boolean = false
	var isShow: Boolean = true
	var isSelected: Boolean = false

	fun addAttachFile(list: ArrayList<AttachedFile>) {
		attachedFiles.addAll(list)
	}

	fun setAttachFiles(list: ArrayList<AttachedFile>) {
		_attachedFiles = list
	}

	fun setSurveyFiles(surveyFile: ArrayList<AttachedFile>) {
		_attachedFiles = surveyFile
	}

	val mainContent: Array<String>
		get() {
			if (content.isEmpty()) return arrayOf("", "")
			val stringBuilder = StringBuilder(content)
			var mainContent: String
			val list = countWord(stringBuilder.toString(), StringUtil.KEY_FORWARD_JSON_REGEX)
			for (position in list) {
				val index = list.indexOf(position)
				if (index > 0 && index < list.size - 1) {
					stringBuilder.replace(
						position,
						position + StringUtil.KEY_FORWARD_JSON.length,
						StringUtil.KEY_CHILD_FORWARD_JSON
					)
				}
			}
			val json = getMessageForward(stringBuilder.toString())
			mainContent = stringBuilder
				.toString()
				.replace(StringUtil.KEY_FORWARD_JSON, "")
				.replace(json, "")
				.replace("\"".toRegex(), "\"")

			if (mainContent.length > 3 && mainContent.startsWith(StringUtil.KEY_HTML_HEADER)) {
				mainContent = mainContent.substring(0, 3) + mainContent.substring(3)
			}
			return arrayOf(mainContent, json)
		}

	val forwardMessage: String
		get() {
			if (content.isEmpty()) return ""
			val stringBuilder = StringBuilder(content)
			val list = countWord(stringBuilder.toString(), StringUtil.KEY_FORWARD_JSON_REGEX)
			for (position in list) {
				val index = list.indexOf(position)
				if (index > 0 && index < list.size - 1) {
					stringBuilder.replace(
						position,
						position + StringUtil.KEY_FORWARD_JSON.length,
						StringUtil.KEY_CHILD_FORWARD_JSON
					)
				}
			}
			return getMessageForward(stringBuilder.toString())
		}
}

data class SendMessageResult(
	var entity: Message = Message(),
	var status: Int = -1
)

data class Option(
	var sms: Boolean = false,
	var email: Boolean = false
)

data class SendBody(
	@SerializedName("noi_dung")
	var content: String = "",
	@SerializedName("hoi_thoai_id")
	var conversationId: String = "",
	@SerializedName("file_dinh_kem")
	var listFile: ArrayList<AttachedFile>? = null,
	@SerializedName("tuy_chon")
	var option: Option = Option(),
	@SerializedName("phieu_khao_sat")
	var listSurvey: ArrayList<AttachedFile>? = null,
	@SerializedName("is_msg_system")
	var isMsgSystem: Boolean = false
)

data class ReceiverNotify(
	@SerializedName("nguoi_nhan_id")
	val receiverId: String = "",
	@SerializedName("tat_thong_bao")
	val isOffNotify: Boolean = false
)

data class DeleteBody(
	var items: ArrayList<String>? = null
)

data class PinMessage(
	@SerializedName("hoi_thoai_id")
	var conversationId: String = "",
	@SerializedName("tin_nhan_id")
	var messageId: String = "",
	var message: Message = Message()
)