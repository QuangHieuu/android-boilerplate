package boilerplate.model.conversation;

import com.google.gson.annotations.SerializedName;

public class ConversationConfig {

    @SerializedName("limit_member")
    private int limitMember = 1200;
    @SerializedName("minute_allow_edit")
    private int timeEdit = 5;
    @SerializedName("minute_allow_evict")
    private int timeWithdraw = 5;
    @SerializedName("allow_left_group")
    private boolean allowLeftGroup = true;

    public int getLimitMember() {
        return limitMember;
    }

    public int getTimeEdit() {
        return timeEdit;
    }

    public int getTimeWithdraw() {
        return timeWithdraw;
    }

    public boolean isAllowLeftGroup() {
        return allowLeftGroup;
    }

    public void setLimitMember(int limitMember) {
        this.limitMember = limitMember;
    }

    public void setTimeEdit(int timeEdit) {
        this.timeEdit = timeEdit;
    }

    public void setTimeWithdraw(int timeWithdraw) {
        this.timeWithdraw = timeWithdraw;
    }

    public void setAllowLeftGroup(boolean allowLeftGroup) {
        this.allowLeftGroup = allowLeftGroup;
    }
}
