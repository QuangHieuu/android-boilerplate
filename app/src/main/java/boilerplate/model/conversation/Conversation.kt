package boilerplate.model.conversation

import boilerplate.constant.AccountManager.getCurrentUserId
import boilerplate.data.remote.api.ApiUrl
import boilerplate.model.ExpandModel
import boilerplate.model.message.Message
import boilerplate.model.user.User
import boilerplate.utils.ImageUtil
import com.google.gson.annotations.SerializedName
import java.util.Locale

class Conversation(
	@SerializedName("hoi_thoai_id")
	var id: String = "",
	@SerializedName("ten_hoi_thoai")
	var name: String = "",

	@SerializedName("anh_dai_dien")
	var avatar: String = "",
	@SerializedName("nhan_vien_hoi_thoai")
	var members: ArrayList<Member> = arrayListOf(),

	@SerializedName("nguoi_tao_id")
	val creatorId: String = "",

	@SerializedName("tin_nhan_ghim")
	var pinMessage: ArrayList<Message> = arrayListOf(),

	@SerializedName("ngay_tao")
	val createDate: String = "",
	@SerializedName("ngay_tao_group")
	val createGroupDate: String = "",
	@SerializedName("tin_nhan_cuoi")
	var lastMessage: Message? = null,
	@SerializedName("lan_hoat_dong_cuoi")
	var lastActive: String = "",
	@SerializedName("thoi_gian_hoat_dong")
	var timeActive: String = "",

	@SerializedName("quan_trong")
	var isImportant: Boolean = false,
	@SerializedName("ghim_tin_nhan")
	var isAllowPinMessage: Boolean = false,
	@SerializedName("phe_duyet_thanh_vien")
	var isAllowApproved: Boolean = false,
	@SerializedName("phan_hoi_tin_nhan")
	var isAllowSendMessage: Boolean = false,
	@SerializedName("thay_doi_thong_tin")
	var isChangeInform: Boolean = false,

	@SerializedName("tong_so_nhan_vien")
	var totalMember: Int = 0,
	@SerializedName("tong_tin_nhan")
	var totalMessage: Int = 0,

	//Nhóm hay liên lạc params
	@SerializedName("nhan_vien_id")
	val userId: String = "",
	@SerializedName("nhom_lien_lac_id")
	val regularGroupId: String = "",
	@SerializedName("so_nhan_vien")
	var regularTotalMember: Int = 0,
	@SerializedName("ten_nhom_lien_lac")
	var regularName: String = "",
	@SerializedName("ds_nhan_vien")
	var regularMember: ArrayList<User> = arrayListOf(),

	@SerializedName("is_group")
	private var _isGroup: Int = 0,
) : ExpandModel() {
	var isClicked: Boolean = false
	var isBind: Boolean = false
	var isSelected: Boolean = false

	val pinDate: String
		get() {
			for (user in members) {
				if (user.user.id == getCurrentUserId()) {
					return user.pinDate
				}
			}
			return ""
		}

	/**
	 * isGroup
	 * - null: tất cả
	 * - 0: cá nhân
	 * - 1: nhóm
	 * - 2: my Cloud
	 *
	 * @return
	 */
	val isGroup: Boolean
		get() = _isGroup == 1

	val isMyCloud: Boolean
		get() = _isGroup == 2

	val thumb: String
		get() = if (avatar.isEmpty()) {
			""
		} else {
			String.format(
				Locale.getDefault(),
				"%s%s?w=%d",
				ApiUrl.HOST_FILE_PREVIEW,
				avatar,
				ImageUtil.IMAGE_THUMB_SIZE
			)
		}

	fun removeMember(userId: String) {
		val iterator = members.listIterator()
		while (iterator.hasNext()) {
			val user = iterator.next()
			if (user.user.id == userId) {
				iterator.remove()
				break
			}
		}
	}

}

data class ImportantConversation(
	var conversation: Conversation = Conversation(),
	var isImportant: Boolean = false
)

data class SeenMessage(
	var conversationId: String = "",
	var messageId: String = ""
)

data class PinConversation(
	var isPin: Boolean = false,
	var conversation: Conversation = Conversation()
)

data class ForwardBody(
	@SerializedName("hoi_thoai_id")
	val conversationId: String = ""
)

data class Setting(
	val conversationId: String,
	val isChangeInform: Boolean,
	val isAllowPinMessage: Boolean,
	val isAllowApproved: Boolean,
	val isAllowSendMessage: Boolean
)

data class AddMember(
	val conversationId: String,
	val addMember: ArrayList<Member>
)
