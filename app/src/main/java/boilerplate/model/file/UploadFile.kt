package boilerplate.model.file

import com.google.gson.annotations.SerializedName

data class UploadFile(
	@SerializedName("id") val id: String,
	@SerializedName("original_name") val originalName: String,
	@SerializedName("type") val type: String,
	@SerializedName("length") val length: Int,
	@SerializedName("path") val path: String
)