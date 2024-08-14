package boilerplate.model.user

import com.google.gson.annotations.SerializedName

data class Title(
	var id: String = "",
	@SerializedName("phong_ban_chinh")
	val isMain: Boolean = false,
	@SerializedName("don_vi_id")
	var companyId: String = "",
	@SerializedName("phong_ban_id")
	var departmentId: String = "",
) {
	@SerializedName("phong_ban")
	private var _department: Department? = null
	val department: Department
		get() = _department ?: Department().also { _department = it }

	@SerializedName("don_vi")
	private var _company: Company? = null
	val company: Company
		get() = _company ?: Company().also { _company = it }

	@SerializedName("chuc_vu")
	private var _position: Position? = null
	val position: Position
		get() = _position ?: Position().also { _position = it }

	@SerializedName("doi_tuong")
	private var _personObject: PersonObject? = null
	val personObject: PersonObject
		get() = _personObject ?: PersonObject().also { _personObject = it }

}