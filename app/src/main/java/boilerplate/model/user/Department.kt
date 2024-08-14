package boilerplate.model.user

import boilerplate.model.ExpandModel
import com.google.gson.annotations.SerializedName

data class Department(
	@SerializedName("phong_ban_id")
	var id: String = "",
	@SerializedName("ten_phong_ban")
	var name: String = "",
	@SerializedName("ma_phong_ban")
	var shortName: String = "",
	@SerializedName("phong_ban_cha")
	var parentDepartment: String = "",
	@SerializedName("total_nhan_vien")
	var totalUser: Int = 0,
	@SerializedName("trang_thai")
	var status: Int = 0,
	@SerializedName("ma_ky_so")
	var signCode: String = "",
	@SerializedName("is_active")
	var isActive: Boolean = false,
	@SerializedName("phong_ban_lanh_dao")
	var isLeaderDepartment: Boolean = false,
	@SerializedName("thoi_gian_duyet")
	var approveTime: String = ""
) : ExpandModel() {
	@SerializedName("nguoi_ky")
	private var _signer: User? = null
	val signer: User
		get() = _signer ?: User().also { _signer = it }

	@SerializedName("ds_phong_ban_con")
	private var _childDepartments: ArrayList<Department>? = null
	val childDepartments: ArrayList<Department>
		get() = _childDepartments ?: arrayListOf<Department>().also { _childDepartments = it }

	@SerializedName("ds_nhan_vien")
	private var _users: ArrayList<User>? = null
	val users: ArrayList<User>
		get() = _users ?: arrayListOf<User>().also { _users = it }

	fun setUser(list: ArrayList<User>) {
		_users = list
	}

	var loai: Int = 0
	var stt: Int = 0
}