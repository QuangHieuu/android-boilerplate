package boilerplate.model.conversation

import boilerplate.constant.AccountManager.getCurrentUserId
import boilerplate.model.user.User
import boilerplate.model.user.UserSignalR
import com.google.gson.annotations.SerializedName

class ConversationSignalR {
	@SerializedName("nhan_vien_hoi_thoai")
	var member: ArrayList<UserSignalR>

	@SerializedName("hoi_thoai_id")
	var id: String = ""

	@SerializedName("anh_dai_dien")
	var avatar: String = ""

	@SerializedName("nguoi_tao_id")
	var creatorId: String = ""

	@SerializedName("ten_hoi_thoai")
	var groupName: String = ""

	@SerializedName("ten_nhom_lien_lac")
	var regularName: String = ""

	@SerializedName("nhan_viens")
	var regularUser: ArrayList<User> = arrayListOf()

	@SerializedName("ghim_tin_nhan")
	var isAllowPinMessage: Boolean = false

	@SerializedName("phe_duyet_thanh_vien")
	var isAllowApproved: Boolean = false

	@SerializedName("phan_hoi_tin_nhan")
	var isAllowSendMessage: Boolean = false

	@SerializedName("thay_doi_thong_tin")
	var isChangeInform: Boolean = false

	@SerializedName("check_exist")
	private var checkExist = false

	@SerializedName("nhom_lien_lac_id")
	var regularGroupId: String = ""

	constructor() {
		creatorId = getCurrentUserId()
		member = ArrayList()
		isAllowPinMessage = true
		isAllowApproved = false
		isAllowSendMessage = true
		isChangeInform = true
	}

	constructor(id: String, name: String, avatarId: String) {
		creatorId = getCurrentUserId()
		member = ArrayList()
		member.add(UserSignalR(id))
		groupName = name
		avatar = avatarId

		isAllowPinMessage = true
		isAllowApproved = false
		isAllowSendMessage = true
		isChangeInform = true
	}

	fun setCheckExist(checkExist: Boolean) {
		this.checkExist = checkExist
	}
}