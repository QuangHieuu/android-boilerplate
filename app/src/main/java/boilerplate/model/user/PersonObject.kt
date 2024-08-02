package boilerplate.model.user

import com.google.gson.annotations.SerializedName

data class PersonObject(
    @SerializedName("doi_tuong_id")
    var id: String = "",
    @SerializedName("ma_doi_tuong")
    var code: String = "",
    @SerializedName("ten_doi_tuong")
    var name: String = ""
)
