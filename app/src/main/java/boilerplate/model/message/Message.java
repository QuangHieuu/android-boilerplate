package boilerplate.model.message;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import boilerplate.model.conversation.Conversation;
import boilerplate.model.file.AttachedFile;
import boilerplate.model.user.User;
import boilerplate.utils.StringUtil;

public class Message {
    @SerializedName("tin_nhan_ghim_id")
    private String messagePinId;
    @SerializedName("tin_nhan_id")
    private String messageId;
    @SerializedName("nguoi_gui_id")
    private String personSendId;
    @SerializedName("nguoi_gui")
    private User personSend;
    @SerializedName("noi_dung")
    private String content;
    @SerializedName("gui_kem_email")
    private boolean isSendMail;
    @SerializedName("gui_kem_sms")
    private boolean isSendSms;
    @SerializedName("ngay_tao")
    private String dateCreate;
    @SerializedName("hoi_thoai_id")
    private String conversationId;
    @SerializedName("file_dinh_kem")
    private ArrayList<AttachedFile.Conversation> attachedFiles;
    @SerializedName("phieu_khao_sat")
    private ArrayList<AttachedFile.SurveyFile> surveyFiles;
    @SerializedName("ngay_tao_group")
    private String dateCreateGroup;
    /**
     * trạng thái = 2: offline
     */
    private int status = 0;
    @SerializedName("tuy_chon")
    private Option option;
    @SerializedName("danh_dau")
    private boolean bookmark;
    @SerializedName("ds_thong_ke_emoticon")
    private ArrayList<Reaction> reactions;
    @SerializedName("is_msg_system")
    private boolean isMsgSystem;
    private boolean isReaction;

    @SerializedName("hoi_thoai")
    private Conversation conversation;
    @SerializedName("is_evict")
    private boolean isWithdraw;
    @SerializedName("set_time_evict_message")
    private int timeWithdraw;
    @SerializedName("nguoi_ghim")
    private User personPin;
    @SerializedName("nhan_vien_ghim_tin_nhan_id")
    private String personPinId;
    @SerializedName("nguoi_nhan")
    ArrayList<ReceiverNotify> receiverNotifies;

    private boolean isFocus = false;
    private boolean isHide = false;
    private boolean isShow = true;
    private boolean isSelected = false;

    public ArrayList<ReceiverNotify> getReceiverNotifies() {
        if (receiverNotifies == null) return receiverNotifies = new ArrayList<>();
        return receiverNotifies;
    }

    public String getPersonPinId() {
        return personPinId;
    }

    public boolean isHide() {
        return isHide;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setHide(boolean hide) {
        isHide = hide;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public Message() {
    }

    public Message(String conversationId, String messageId) {
        this.messageId = messageId;
        this.conversationId = conversationId;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public boolean isFocus() {
        return isFocus;
    }

    public void setFocus(boolean focus) {
        isFocus = focus;
    }

    public boolean isMsgSystem() {
        return isMsgSystem;
    }

    public void setMsgSystem(boolean msgSystem) {
        isMsgSystem = msgSystem;
    }

    public String getMessagePinId() {
        if (messagePinId == null) return messagePinId = "";
        return messagePinId;
    }

    public ArrayList<Reaction> getReactions() {
        if (reactions == null) return new ArrayList<>();
        return reactions;
    }

    public boolean isReaction() {
        return isReaction;
    }

    public Option getOption() {
        return option;
    }

    public String getConversationId() {
        if (conversationId == null) return "";
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessageId() {
        if (messageId == null) return messageId = "";
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public User getPersonSend() {
        if (personSend == null) return personSend = new User();
        return personSend;
    }

    public void setPersonSend(User personSend) {
        this.personSend = personSend;
    }

    public boolean isSendMail() {
        return isSendMail;
    }

    public void setSendMail(boolean sendMail) {
        this.isSendMail = sendMail;
    }

    public boolean isSendSms() {
        return isSendSms;
    }

    public void setSendSms(boolean sendSms) {
        this.isSendSms = sendSms;
    }

    public String getContent() {
        if (content == null) return "";
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDateCreate() {
        if (dateCreate == null) return dateCreate = "";
        return dateCreate;
    }

    public void setDateCreate(String dateCreate) {
        this.dateCreate = dateCreate;
    }

    public ArrayList<AttachedFile.Conversation> getAttachedFiles() {
        if (attachedFiles == null) return attachedFiles = new ArrayList<>();
        return attachedFiles;
    }

    public void setAttachedFiles(ArrayList<AttachedFile.Conversation> attachedFiles) {
        getAttachedFiles().addAll(attachedFiles);
    }

    public void setNewAttachedFiles(ArrayList<AttachedFile.Conversation> attachedFiles) {
        this.attachedFiles = attachedFiles;
    }

    public String getPersonSendId() {
        if (personSendId == null) return "";
        return personSendId;
    }

    public void setPersonSendId(String personSendId) {
        this.personSendId = personSendId;
    }

    public String getDateCreateGroup() {
        if (dateCreateGroup == null) return "";
        return dateCreateGroup;
    }

    public void setDateCreateGroup(String dateCreateGroup) {
        this.dateCreateGroup = dateCreateGroup;
    }

    public ArrayList<AttachedFile.SurveyFile> getSurveyFiles() {
        if (surveyFiles == null) return surveyFiles = new ArrayList<>();
        return surveyFiles;
    }

    public void setSurveyFiles(ArrayList<AttachedFile.SurveyFile> surveyFiles) {
        this.surveyFiles = surveyFiles;
    }

    @Override
    public Message clone() {
        Message clone = new Message();
        clone.messageId = messageId;
        clone.personSendId = personSendId;
        clone.personSend = personSend;
        clone.content = content;
        clone.isSendMail = isSendMail;
        clone.isSendSms = isSendSms;
        clone.dateCreate = dateCreate;
        clone.conversationId = conversationId;
        clone.attachedFiles = attachedFiles;
        clone.surveyFiles = surveyFiles;
        clone.dateCreateGroup = dateCreateGroup;
        clone.status = status;
        clone.option = option;
        clone.bookmark = bookmark;
        clone.reactions = reactions;
        clone.isMsgSystem = isMsgSystem;
        clone.isReaction = isReaction;
        clone.conversation = conversation;
        clone.isWithdraw = isWithdraw;
        clone.timeWithdraw = timeWithdraw;
        clone.personPin = personPin;
        clone.isHide = false;
        clone.isShow = false;
        return clone;
    }

    public boolean isImportant() {
        return bookmark;
    }

    public void setImportant(boolean important) {
        bookmark = important;
    }

    public boolean isWithdraw() {
        return isWithdraw;
    }

    public void setWithdraw(boolean withdraw) {
        isWithdraw = withdraw;
    }

    public String[] getMainContent() {
        StringBuilder stringBuilder = new StringBuilder(getContent());
        String mainContent;
        ArrayList<Integer> list = StringUtil.countWord(stringBuilder.toString(), StringUtil.KEY_FORWARD_JSON_REGEX);
        for (int position : list) {
            int index = list.indexOf(position);
            if (index > 0 && index < list.size() - 1) {
                stringBuilder.replace(position, position + StringUtil.KEY_FORWARD_JSON.length(), StringUtil.KEY_CHILD_FORWARD_JSON);
            }
        }
        String json = StringUtil.getMessageForward(stringBuilder.toString());
        mainContent = stringBuilder
            .toString()
            .replace(StringUtil.KEY_FORWARD_JSON, "")
            .replace(json, "")
            .replaceAll("\"", "\"");

        if (mainContent.length() > 3 && mainContent.startsWith(StringUtil.KEY_HTML_HEADER)) {
            mainContent = mainContent.substring(0, 3) + mainContent.substring(3);
        }
        return new String[]{mainContent, json};
    }

    public String getForwardMessage() {
        StringBuilder stringBuilder = new StringBuilder(getContent());
        ArrayList<Integer> list = StringUtil.countWord(stringBuilder.toString(), StringUtil.KEY_FORWARD_JSON_REGEX);
        for (int position : list) {
            int index = list.indexOf(position);
            if (index > 0 && index < list.size() - 1) {
                stringBuilder.replace(position, position + StringUtil.KEY_FORWARD_JSON.length(), StringUtil.KEY_CHILD_FORWARD_JSON);
            }
        }
        return StringUtil.getMessageForward(stringBuilder.toString());
    }

    public User getPersonPin() {
        if (personPin == null) return personPin = new User();
        return personPin;
    }

    public static class Option {
        private boolean sms = false;
        private boolean email = false;

        public Option(boolean isSms, boolean isEmail) {
            sms = isSms;
            email = isEmail;
        }
    }

    public static class SendMessageBody {
        @SerializedName("noi_dung")
        private String content;
        @SerializedName("hoi_thoai_id")
        private String conversationId;
        @SerializedName("file_dinh_kem")
        private ArrayList<AttachedFile.Conversation> listFile;
        @SerializedName("tuy_chon")
        private Option option;
        @SerializedName("phieu_khao_sat")
        private ArrayList<AttachedFile.SurveyFile> listSurvey;
        @SerializedName("is_msg_system")
        private boolean isMsgSystem;

        public boolean isMsgSystem() {
            return isMsgSystem;
        }

        public void setIsMgsSystem(boolean system) {
            isMsgSystem = system;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public void setConversationId(String conversationId) {
            this.conversationId = conversationId;
        }

        public void setListFile(ArrayList<AttachedFile.Conversation> listFile) {
            this.listFile = listFile;
        }

        public void setOption(Option option) {
            this.option = option;
        }

        public void setListSurvey(ArrayList<AttachedFile.SurveyFile> listSurvey) {
            this.listSurvey = listSurvey;
        }
    }

    public static class Delete {
        private final ArrayList<String> items = new ArrayList<>();

        public void setItems(ArrayList<String> list) {
            items.clear();
            items.addAll(list);
        }

        public void addMessage(String messageId) {
            items.add(messageId);
        }
    }

    public static class Pin {
        @SerializedName("hoi_thoai_id")
        private final String conversationId;
        @SerializedName("tin_nhan_id")
        private final String messageId;

        private final Message message;

        public Pin(String conversationId, String messageId) {
            this.conversationId = conversationId;
            this.messageId = messageId;
            this.message = null;
        }

        public Pin(String conversationId, Message message) {
            this.conversationId = conversationId;
            this.message = message;
            this.messageId = "";
        }

        public String getConversationId() {
            return conversationId;
        }

        public Message getMessage() {
            return message;
        }

        public String getMessageId() {
            return messageId;
        }
    }

    public static class WithdrawRes {
        private static Message entity;

        public Message getEntity() {
            return entity;
        }
    }

    public static class ReceiverNotify {
        @SerializedName("nguoi_nhan_id")
        private String receiverId;

        @SerializedName("tat_thong_bao")
        private boolean offNotify;

        public boolean isOffNotify() {
            return offNotify;
        }

        public String getReceiverId() {
            return receiverId;
        }
    }

    public static class SendMessageResult {
        private final String value;
        private final String message;

        private Message entity;
        private int status;

        public SendMessageResult(String value, String message) {
            this.value = value;
            this.message = message;
        }

        public String getValue() {
            return value;
        }

        public String getMessage() {
            return message;
        }

        public Message getEntity() {
            return entity;
        }

        public int getStatus() {
            return status;
        }
    }
}
