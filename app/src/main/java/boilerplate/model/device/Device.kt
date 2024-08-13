package boilerplate.model.device

import com.google.gson.annotations.SerializedName

class Device {
	@SerializedName("device_id")
	var deviceId: String? = null
		get() = if (field == null) "".also { field = it } else field

	@SerializedName("nhan_vien_id")
	var id: String? = null

	@SerializedName("device_token")
	var deviceToken: String? = null

	@SerializedName("device_type")
	var deviceType: Int? = null
}
