package boilerplate.model.user

import boilerplate.model.ExpandModel
import com.google.gson.annotations.SerializedName

data class Company(
	@SerializedName("don_vi_id")
	var id: String = "",
	@SerializedName("ten_don_vi")
	var name: String = "",
	@SerializedName("ten_viet_tat")
	var shortName: String = "",
	@SerializedName("ds_phong_ban")
	var departments: ArrayList<Department> = arrayListOf(),
	@SerializedName("ds_don_vi_con")
	var childCompanies: ArrayList<Company> = arrayListOf(),
	@SerializedName("phong_ban")
	var department: Department = Department(),
	val level: Int = 0,
	@SerializedName("gui_don_vi_lien_thong")
	val isSend: Boolean = false,
	@SerializedName("gui_don_vi_lien_thong_evn")
	val isSendEvn: Boolean = false,
	@SerializedName("gui_don_vi_truc_thuoc")
	val isSendCompany: Boolean = false,
	@SerializedName("gui_don_vi_khac")
	val isSendOtherCompany: Boolean = false,
	@SerializedName("dong_y_khong_can_ky_so")
	val isAcceptSign: Boolean = false,
	@SerializedName("don_vi_cha")
	var parentCompany: String = "",
	@SerializedName("nguoi_ky")
	var signer: User = User(),
	@SerializedName("trang_thai")
	var status: Int = 0,
	@SerializedName("thoi_gian_duyet")
	var approveTime: String = "",
) : ExpandModel()
