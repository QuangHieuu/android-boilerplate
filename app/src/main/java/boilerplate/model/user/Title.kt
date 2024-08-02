package boilerplate.model.user

import com.google.gson.annotations.SerializedName

data class Title(
    var id: String = "",
    @SerializedName("phong_ban_chinh")
    val isMain: Boolean = false,
    @SerializedName("don_vi")
    var company: Company = Company(),
    @SerializedName("phong_ban")
    var department: Department = Department(),
    @SerializedName("chuc_vu")
    var position: Position = Position(),
    @SerializedName("doi_tuong")
    var personObject: PersonObject? = null,
    @SerializedName("don_vi_id")
    var companyId: String = "",
    @SerializedName("phong_ban_id")
    var departmentId: String = "",
)