package boilerplate.model.dashboard

import com.google.gson.annotations.SerializedName

data class Statical(
	@SerializedName("cong_van_chua_xu_ly")
	var documentUnProcess: Int = 0,
	@SerializedName("cong_van_dang_xu_ly")
	var documentInProcess: Int = 0,
	@SerializedName("cong_van_di_can_ky_so")
	var signGoing: Int = 0,
	@SerializedName(value = "cong_viec_can_thuc_hien", alternate = ["cong_viec_chua_thuc_hien"])
	var workNeedDone: Int = 0,
	@SerializedName("cong_viec_chua_giao")
	var workNotAssign: Int = 0,
	@SerializedName("cong_viec_tre_han")
	var workOverTime: Int = 0,
	@SerializedName("cong_van_bao_nham")
	var documentMistake: Int = 0,
	@SerializedName("cong_viec_bao_nham")
	var workMistake: Int = 0,
	@SerializedName("xem_de_biet_chua_doc")
	var watch: Int = 0,
)
