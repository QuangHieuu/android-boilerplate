package boilerplate.model.dashboard

import com.google.gson.annotations.SerializedName

class Dashboard {
    @SerializedName("ban_lam_viec")
    var desktop: ArrayList<Desktop>? = null
        get() = field ?: arrayListOf<Desktop>().also { field = it }

    @SerializedName("can_xu_ly")
    var statical: Statical? = null
        get() = field ?: Statical().also { field = it }
}