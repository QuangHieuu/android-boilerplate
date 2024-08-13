package boilerplate.model.dashboard

import boilerplate.utils.DateTimeUtil
import boilerplate.utils.DateTimeUtil.convertWithSuitableFormat
import com.google.gson.annotations.SerializedName

class Desktop {
	@SerializedName("id")
	var id: String? = null
		get() = field ?: "".also { field = it }

	@SerializedName("trich_yeu")
	var content: String? = null
		get() = field ?: "".also { field = it }

	@SerializedName("so_cong_van")
	var documentNumber: String? = null
		get() = field ?: "".also { field = it }

	@SerializedName("the_loai")
	var type: Int = 0

	@SerializedName("date")
	var date: String? = null
		get() = field ?: "".also { field = it }

	@SerializedName("han_giai_quyet")
	var deadline: String? = null
		get() {
			if (field.isNullOrEmpty()) {
				return "".also { field = it }
			} else {
				return convertWithSuitableFormat(field!!, DateTimeUtil.FORMAT_NORMAL)
			}
		}

	@SerializedName("ngay_cong_van")
	var documentTime: String? = null
		get() {
			if (field.isNullOrEmpty()) {
				return "".also { field = it }
			} else {
				return convertWithSuitableFormat(field!!, DateTimeUtil.FORMAT_NORMAL)
			}
		}

	@SerializedName("trang_thai_cong_viec_ca_nhan")
	var personalStatus: Int = 0

	@SerializedName("trang_thai_cong_viec_phong_ban")
	var departmentStatus: Int = 0

	@SerializedName("so_ngay_con_lai")
	private var isOverTime: Int? = null

	fun isOverTime(): Boolean {
		return isOverTime != null && isOverTime!! < 0
	}
}