package boilerplate.model.conversation;

import com.google.gson.annotations.SerializedName;

import boilerplate.model.user.User;

public class ConversationUser {
    @SerializedName("nguoi_nhan_id")
    private String memberId;
    @SerializedName("nhan_vien")
    private User user;
    @SerializedName("so_tin_da_doc")
    private int readNumber;
    @SerializedName("quan_trong")
    private boolean isImportant;
    @SerializedName("vai_tro")
    public int vaiTro;

    @SerializedName("ghim_tin_nhan")
    private boolean isAllowPinMessage;
    @SerializedName("phe_duyet_thanh_vien")
    private boolean isAllowApproved;
    @SerializedName("phan_hoi_tin_nhan")
    private boolean isAllowSendMessage;
    @SerializedName("thay_doi_thong_tin")
    private boolean isChangeInform;
    @SerializedName("tat_thong_bao")
    private boolean isOffNotify;
    @SerializedName("roi_nhom")
    private int outGroup;

    public ConversationUser() {
    }

    public boolean isOutGroup() {
        return outGroup == 1;
    }

    public ConversationUser(String name) {
        user = new User();
        user.setName(name);
    }

    public int getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(int vaiTro) {
        this.vaiTro = vaiTro;
    }

    private boolean isGroupFounder;

    public void setUserConversation(User nhan_vien) {
        this.user = nhan_vien;
    }

    public void setReadNumber(int so_tin_da_doc) {
        this.readNumber = so_tin_da_doc;
    }

    public User getUser() {
        if (user == null)
            return new User();
        return user;
    }

    public int getReadNumber() {
        return readNumber;
    }

    public boolean isGroupFounder() {
        return isGroupFounder;
    }

    public void setGroupFounder(boolean groupFounder) {
        isGroupFounder = groupFounder;
    }


    public boolean isImportant() {
        return isImportant;
    }

    public void setImportant(boolean quan_trong) {
        this.isImportant = quan_trong;
    }

    public boolean isAllowPinMessage() {
        return isAllowPinMessage;
    }

    public boolean isAllowApproved() {
        return isAllowApproved;
    }

    public boolean isAllowSendMessage() {
        return isAllowSendMessage;
    }

    public boolean isChangeInform() {
        return isChangeInform;
    }

    public boolean isOffNotify() {
        return isOffNotify;
    }

    public void setOffNotify(boolean offNotify) {
        isOffNotify = offNotify;
    }

    public static class SignalrBody {
        @SerializedName("nguoi_nhan_id")
        private String id;
        @SerializedName("ten_nhan_vien")
        private String name;

        public SignalrBody(String id) {
            this.id = id;
        }

        public SignalrBody(String id, String name) {
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

    public static class LeaveGroup {
        private final String conversationId;
        private final Conversation conversation;
        private final String userId;

        public LeaveGroup(String conversationId, String userId, Conversation conversation) {
            this.conversation = conversation;
            this.userId = userId;
            this.conversationId = conversationId;
        }

        public LeaveGroup(String conversationId, String userId) {
            this.conversationId = conversationId;
            this.userId = userId;
            this.conversation = null;
        }

        public String getConversationId() {
            return conversationId;
        }

        public Conversation getConversation() {
            return conversation;
        }

        public String getUserId() {
            return userId;
        }
    }

    public static class UpdateRole {
        private final String conversationId;
        private final String userId;
        private final int role;

        public UpdateRole(String conversationId, String userId, int role) {
            this.conversationId = conversationId;
            this.userId = userId;
            this.role = role;
        }

        public String getConversationId() {
            return conversationId;
        }

        public String getUserId() {
            return userId;
        }

        public int getRole() {
            return role;
        }
    }

    public static class JoinGroup {
        private String id;
        private boolean isAccept;
        private String userId;

        public JoinGroup(String id, boolean isAccept, String userId) {
            this.id = id;
            this.isAccept = isAccept;
            this.userId = userId;
        }

        public String getId() {
            return id;
        }

        public boolean isAccept() {
            return isAccept;
        }

        public String getUserId() {
            return userId;
        }
    }
}
