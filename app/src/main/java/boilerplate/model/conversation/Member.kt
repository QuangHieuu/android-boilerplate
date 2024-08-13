package boilerplate.model.conversation

import boilerplate.model.user.User
import com.google.gson.annotations.SerializedName

data class Member(
	@SerializedName("nhan_vien")
	var user: User = User(),
	@SerializedName("vai_tro")
	var vaiTro: Int = 0,
	@SerializedName("ngay_ghim")
	var pinDate: String = "",
	@SerializedName("so_tin_da_doc")
	var readNumber: Int = 0,
	@SerializedName("quan_trong")
	var isImportant: Boolean = false,
	@SerializedName("ghim_tin_nhan")
	val isAllowPinMessage: Boolean = false,
	@SerializedName("phe_duyet_thanh_vien")
	val isAllowApproved: Boolean = false,
	@SerializedName("phan_hoi_tin_nhan")
	val isAllowSendMessage: Boolean = false,
	@SerializedName("thay_doi_thong_tin")
	val isChangeInform: Boolean = false,
	@SerializedName("tat_thong_bao")
	var isOffNotify: Boolean = false,
	@SerializedName("roi_nhom")
	private val status: Int = 0,
) {

	constructor(name: String) : this() {
		user.name = name
	}

	val isOutGroup: Boolean
		get() = status == 1
}

class LeaveGroup {
	val conversationId: String
	val conversation: Conversation?
	val userId: String

	constructor(conversationId: String, userId: String, conversation: Conversation?) {
		this.conversation = conversation
		this.userId = userId
		this.conversationId = conversationId
	}

	constructor(conversationId: String, userId: String) {
		this.conversationId = conversationId
		this.userId = userId
		this.conversation = null
	}
}

class UpdateRole(val conversationId: String, val userId: String, val role: Int)

class JoinGroup(val id: String, val isAccept: Boolean, val userId: String)