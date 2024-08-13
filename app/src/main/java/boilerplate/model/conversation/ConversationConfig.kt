package boilerplate.model.conversation

import com.google.gson.annotations.SerializedName

class ConversationConfig {
	@SerializedName("limit_member")
	var limitMember: Int = 1200

	@SerializedName("minute_allow_edit")
	var timeEdit: Int = 5

	@SerializedName("minute_allow_evict")
	var timeWithdraw: Int = 5

	@SerializedName("allow_left_group")
	var isAllowLeftGroup: Boolean = true
}
