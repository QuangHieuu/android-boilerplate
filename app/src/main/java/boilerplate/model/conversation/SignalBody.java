package boilerplate.model.conversation;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import boilerplate.constant.AccountManager;
import boilerplate.model.user.User;

public class SignalBody {
    @SerializedName("nhan_vien_hoi_thoai")
    private final ArrayList<ConversationUser> member;
    @SerializedName("hoi_thoai_id")
    private String id;
    @SerializedName("anh_dai_dien")
    private String avatar;
    @SerializedName("nguoi_tao_id")
    private String creatorId;
    @SerializedName("ten_hoi_thoai")
    private String groupName;
    @SerializedName("ten_nhom_lien_lac")
    private String regularName;
    @SerializedName("nhan_viens")
    private ArrayList<User> regularUser;

    @SerializedName("ghim_tin_nhan")
    private boolean isAllowPinMessage;
    @SerializedName("phe_duyet_thanh_vien")
    private boolean isAllowApproved;
    @SerializedName("phan_hoi_tin_nhan")
    private boolean isAllowSendMessage;
    @SerializedName("thay_doi_thong_tin")
    private boolean isChangeInform;
    @SerializedName("check_exist")
    private boolean checkExist;

    @SerializedName("nhom_lien_lac_id")
    private String regularGroupId;

    public SignalBody() {
        creatorId = AccountManager.getCurrentUserId();
        member = new ArrayList<>();
        isAllowPinMessage = true;
        isAllowApproved = false;
        isAllowSendMessage = true;
        isChangeInform = true;
    }

    public SignalBody(String id, String name, String avatarId) {
        creatorId = AccountManager.getCurrentUserId();
        member = new ArrayList<>();
        member.add(new ConversationUser(id));
        groupName = name;
        avatar = avatarId;

        isAllowPinMessage = true;
        isAllowApproved = false;
        isAllowSendMessage = true;
        isChangeInform = true;
    }

    public void setCheckExist(boolean checkExist) {
        this.checkExist = checkExist;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getRegularName() {
        if (regularName == null) return regularName = "";
        return regularName;
    }

    public void setRegularName(String regularName) {
        this.regularName = regularName;
    }

    public ArrayList<User> getRegularUser() {
        if (regularUser == null) return regularUser = new ArrayList<>();
        return regularUser;
    }

    public void setRegularUser(ArrayList<User> regularUser) {
        this.regularUser = regularUser;
    }

    public String getRegularGroupId() {
        return regularGroupId;
    }

    public void setRegularGroupId(String regularGroupId) {
        this.regularGroupId = regularGroupId;
    }

    public ArrayList<ConversationUser> getMember() {
        return member;
    }

    public void setMember(ArrayList<ConversationUser> list) {
        member.clear();
        member.addAll(list);
    }

    public boolean isAllowPinMessage() {
        return isAllowPinMessage;
    }

    public void setAllowPinMessage(boolean allowPinMessage) {
        isAllowPinMessage = allowPinMessage;
    }

    public boolean isAllowApproved() {
        return isAllowApproved;
    }

    public void setAllowApproved(boolean allowApproved) {
        isAllowApproved = allowApproved;
    }

    public boolean isAllowSendMessage() {
        return isAllowSendMessage;
    }

    public void setAllowSendMessage(boolean allowSendMessage) {
        isAllowSendMessage = allowSendMessage;
    }

    public boolean isChangeInform() {
        return isChangeInform;
    }

    public void setChangeInform(boolean changeInform) {
        isChangeInform = changeInform;
    }


    public static class ConversationUser {
        @SerializedName("nguoi_nhan_id")
        private final String id;
        @SerializedName("ten_nhan_vien")
        private String name;

        public ConversationUser(String id) {
            this.id = id;
        }

        public ConversationUser(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

}