package boilerplate.model.user

import com.google.gson.annotations.SerializedName

class PersonObject {
    @SerializedName("doi_tuong_id")
    var id: String? = null
        get() = field ?: "".also { field = it }

    @SerializedName("ma_doi_tuong")
    var code: String? = null
        get() = field ?: "".also { field = it }

    @SerializedName("ten_doi_tuong")
    var name: String? = null
        get() = field ?: "".also { field = it }
}
