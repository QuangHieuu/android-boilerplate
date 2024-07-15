package boilerplate.model.conversation;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

import boilerplate.data.remote.service.ApiUrl;
import boilerplate.model.ExpandModel;
import boilerplate.model.message.Message;
import boilerplate.model.user.User;
import boilerplate.utils.DateTimeUtil;

public class Conversation extends ExpandModel {
    @SerializedName("hoi_thoai_id")
    private String conversationId;
    @SerializedName("nguoi_tao_id")
    private String creatorId;
    @SerializedName("ten_hoi_thoai")
    private String conversationName;
    @SerializedName("anh_dai_dien")
    private String conversationAvatar;
    @SerializedName("nhan_vien_hoi_thoai")
    private ArrayList<ConversationUser> conversationUsers;

    @SerializedName("ghim_tin_nhan")
    private boolean isAllowPinMessage;
    @SerializedName("phe_duyet_thanh_vien")
    private boolean isAllowApproved;
    @SerializedName("phan_hoi_tin_nhan")
    private boolean isAllowSendMessage;
    @SerializedName("thay_doi_thong_tin")
    private boolean isChangeInform;
    @SerializedName("tong_so_nhan_vien")
    private int totalUser;

    @SerializedName("tong_tin_nhan")
    private int totalMessage;
    @SerializedName("tin_nhan_cuoi")
    private Message lastMessage;
    @SerializedName("lan_hoat_dong_cuoi")
    private String lastActive;
    @SerializedName("ngay_tao")
    private String createDate;
    @SerializedName("ngay_tao_group")
    private String groupCreateDate;
    @SerializedName("is_group")
    private int isGroup;
    @SerializedName("thoi_gian_hoat_dong")
    private String timeActive;
    @SerializedName("tin_nhan_ghim")
    private ArrayList<Message> pinMessage;

    //Nhóm hay liên lạc params
    @SerializedName("nhan_vien_id")
    private String userId;
    @SerializedName("nhom_lien_lac_id")
    private String regularGroupId;
    @SerializedName("so_nhan_vien")
    private int regularMember;
    @SerializedName("ten_nhom_lien_lac")
    private String regularName;
    @SerializedName("ds_nhan_vien")
    private ArrayList<User> regularUser;

    private boolean isSelected = false;
    private boolean isClicked = false;
    @SerializedName("quan_trong")
    private boolean isImportant = false;
    private boolean isCheck = false;
    private boolean isBind = false;

    /**
     * isGroup
     * - null: tất cả
     * - 0: cá nhân
     * - 1: nhóm
     * - 2: my Cloud
     *
     * @return
     */
    public boolean isGroup() {
        return isGroup == 1;
    }

    public boolean isMyCloud() {
        return isGroup == 2;
    }

    public int getIsGroup() {
        return isGroup;
    }

    public void setIsGroup(int isGroup) {
        this.isGroup = isGroup;
        isGroup();
    }


    public String getThumb(int size) {
        if (conversationAvatar == null || conversationAvatar.isEmpty()) {
            return null;
        } else {
            return String.format(
                Locale.getDefault(),
                "%s%s?w=%d",
                ApiUrl.HOST_FILE_PREVIEW,
                conversationAvatar,
                size
            );
        }
    }

    public String getAvatarId() {
        return conversationAvatar;
    }

    public String getCreatorId() {
        if (creatorId == null) return "";
        return creatorId;
    }

    public String getConversationId() {
        if (conversationId == null) return "";
        return conversationId;
    }

    public void setConversationId(String id) {
        this.conversationId = id;
    }

    public String getConversationName() {
        if (conversationName == null) return "";
        return conversationName;
    }

    public void setConversationName(String name) {
        this.conversationName = name;
    }

    public void setConversationAvatar(String conversationAvatar) {
        this.conversationAvatar = conversationAvatar;
    }

    public String getConversationAvatar() {
        return conversationAvatar;
    }

    public void setTotalUser(int number) {
        totalUser = number;
    }

    public int getTongSoNhanVien() {
        return totalUser;
    }

    public String getLastActive() {
        if (lastActive == null) {
            return "";
        }
        return lastActive;
    }

    public void setLastActive(String lastActive) {
        this.lastActive = lastActive;
    }

    public ArrayList<ConversationUser> getConversationUsers() {
        if (conversationUsers == null) return conversationUsers = new ArrayList<>();
        return conversationUsers;
    }

    public void setConversationUsers(ArrayList<ConversationUser> list) {
        if (list != null) {
            getConversationUsers().clear();
            getConversationUsers().addAll(list);
        }
    }

    public int getTotalMessage() {
        return totalMessage;
    }

    public void setTotalMessage(int totalMessage) {
        this.totalMessage = totalMessage;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message tin_nhan_cuoi) {
        this.lastMessage = tin_nhan_cuoi;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isImportant() {
        return isImportant;
    }

    public void setImportant(boolean quanTrong) {
        isImportant = quanTrong;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
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

    public void setAllowPinMessage(boolean allowPinMessage) {
        isAllowPinMessage = allowPinMessage;
    }

    public void setAllowApproved(boolean allowApproved) {
        isAllowApproved = allowApproved;
    }

    public void setAllowSendMessage(boolean allowSendMessage) {
        isAllowSendMessage = allowSendMessage;
    }

    public void setChangeInform(boolean changeInform) {
        isChangeInform = changeInform;
    }

    public String getUserId() {
        return userId;
    }

    public String getRegularGroupId() {
        return regularGroupId;
    }

    public int getRegularMember() {
        return regularMember;
    }

    public void setRegularMember(int number) {
        regularMember = number;
    }

    public String getRegularName() {
        if (regularName == null) return regularName = "";
        return regularName;
    }

    public void setRegularName(String regularName) {
        this.regularName = regularName;
    }

    public void setRegularUser(ArrayList<User> regularUser) {
        this.regularUser = regularUser;
    }

    public ArrayList<User> getRegularUser() {
        if (regularUser == null) return regularUser = new ArrayList<>();
        return regularUser;
    }

    public String getTimeActive() {
        return timeActive;
    }

    public ArrayList<Message> getPinMessage() {
        if (pinMessage == null) return pinMessage = new ArrayList<>();
        return pinMessage;
    }

    public void removeUser(String userId) {
        ArrayList<ConversationUser> users = getConversationUsers();
        for (ListIterator<ConversationUser> iterator = users.listIterator(); iterator.hasNext(); ) {
            ConversationUser user = iterator.next();
            if (user.getUser().getId().equals(userId)) {
                iterator.remove();
                break;
            }
        }
    }

    public static class Result {
        private ArrayList<Conversation> items;
        private int total;

        private boolean result;

        public ArrayList<Conversation> getItems() {
            return items;
        }

        public int getTotal() {
            return total;
        }

        public boolean isExist() {
            return result;
        }
    }

    public static class ForwardBody {
        @SerializedName("hoi_thoai_id")
        private String conversationId;

        public ForwardBody(String conversationId) {
            this.conversationId = conversationId;
        }
    }

    public static class SignalBody {
        @SerializedName("hoi_thoai_id")
        private String id;
        @SerializedName("anh_dai_dien")
        private String avatar;
        @SerializedName("nguoi_tao_id")
        private String creatorId;
        @SerializedName("ten_hoi_thoai")
        private String groupName;
        @SerializedName("nhan_vien_hoi_thoai")
        private ArrayList<ConversationUser.SignalrBody> member;

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
            creatorId = "";
            member = new ArrayList<>();
            isAllowPinMessage = true;
            isAllowApproved = false;
            isAllowSendMessage = true;
            isChangeInform = true;
        }

        public SignalBody(String id, String name, String avatarId) {
            creatorId = "";
            member = new ArrayList<>();
            member.add(new ConversationUser.SignalrBody(id));
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

        public void setRegularName(String regularName) {
            this.regularName = regularName;
        }

        public ArrayList<ConversationUser.SignalrBody> getMember() {
            return member;
        }

        public void setMember(ArrayList<ConversationUser.SignalrBody> list) {
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
    }

    public static class Setting {
        private final String conversationId;
        private final boolean isAllowPinMessage;
        private final boolean isAllowApproved;
        private final boolean isAllowSendMessage;
        private final boolean isChangeInform;

        public Setting(String conversationId, boolean isChangeInform, boolean isAllowPinMessage, boolean isAllowApproved, boolean isAllowSendMessage) {
            this.conversationId = conversationId;
            this.isChangeInform = isChangeInform;
            this.isAllowPinMessage = isAllowPinMessage;
            this.isAllowApproved = isAllowApproved;
            this.isAllowSendMessage = isAllowSendMessage;
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

        public String getConversationId() {
            return conversationId;
        }
    }

    public static class Important {
        private final String conversation;
        private final boolean isImportant;

        public Important(String conversation, boolean isImportant) {
            this.conversation = conversation;
            this.isImportant = isImportant;
        }

        public String getConversation() {
            return conversation;
        }

        public boolean isImportant() {
            return isImportant;
        }
    }

    public static class SeenMessage {
        private final String conversationId;
        private final String messageId;

        public SeenMessage(String conversationId, String messageId) {
            this.conversationId = conversationId;
            this.messageId = messageId;
        }

        public String getConversationId() {
            return conversationId;
        }

        public String getMessageId() {
            return messageId;
        }
    }

    public static class Contact extends Search {

        public Contact() {
            super(false);
            setMore(true);
            setOneToOne(false);
            setImportant(false);
            setName(true);
        }
    }

    public static class Search {
        private Map<String, Object> data = new HashMap<>();

        public Map<String, Object> getData() {
            return data;
        }

        public Search(boolean withDay) {
            setPage(1);
            setLimit(10);
            if (withDay) {
                setFrom(DateTimeUtil.getCurrentDateWith(Calendar.MONTH, -3, DateTimeUtil.FORMAT_REVERT));
                setTo(DateTimeUtil.getCurrentDate(DateTimeUtil.FORMAT_REVERT));
            }
        }

        public void setPage(int page) {
            data.put("page", page);
        }

        public void setLimit(int limit) {
            data.put("limit", limit);
        }

        public void setTextSearch(String textSearch) {
            data.put("searchText", textSearch);
        }

        public void setFrom(String from) {
            data.put("tuNgay", from);
        }

        public void setTo(String to) {
            data.put("denNgay", to);
        }

        public void setOneToOne(boolean oneToOne) {
            data.put("hoiThoaiMotMot", oneToOne);
        }

        public void setMore(boolean more) {
            data.put("hoiThoaiNhieuNguoi", more);
        }

        public void setName(boolean setName) {
            data.put("isDatTen", setName);
        }

        public void setImportant(boolean important) {
            data.put("isQuanTrong", important);
        }

        public void setUserId(String userId) {
            data.put("nhanVienId", userId);
        }

        public Integer getPage() {
            Object a = data.get("page");
            return (int) a;
        }

        public String getTo() {
            Object a = data.get("denNgay");
            if (a != null) {
                return (String) a;
            }
            return DateTimeUtil.getCurrentDate();
        }

        public String getFrom() {
            Object a = data.get("tuNgay");
            if (a != null) {
                return (String) a;
            }
            return DateTimeUtil.getCurrentDateWith(Calendar.MONTH, -1);
        }
    }

    public static class AddMember {
        private final String mConversationId;
        private final ArrayList<ConversationUser> mAddMember;

        public AddMember(String mConversationId, ArrayList<ConversationUser> mAddMember) {
            this.mConversationId = mConversationId;
            this.mAddMember = mAddMember;
        }

        public String getConversationId() {
            return mConversationId;
        }

        public ArrayList<ConversationUser> getAddMember() {
            return mAddMember;
        }
    }
}

