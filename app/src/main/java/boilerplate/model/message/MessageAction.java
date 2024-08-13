package boilerplate.model.message;

import java.util.ArrayList;

import boilerplate.R;
import boilerplate.constant.AccountManager;

public enum MessageAction {
    ANSWER(0, R.drawable.ic_quote_blue, "Trả lời"),
    FORWARD(1, R.drawable.ic_share_blue, "Chuyển tiếp"),
    MARK(2, R.drawable.ic_star_blue, "Đánh dấu tin nhắn"),
    UN_MARK(2, R.drawable.ic_star_blue, "Huỷ đánh dấu tin nhắn"),
    PIN(3, R.drawable.ic_pin_blue, "Ghim tin nhắn"),
    COPY(4, R.drawable.ic_copy_blue, "Sao chép tin nhắn"),
    EDIT(5, R.drawable.ic_edit_ver_1_blue, "Chỉnh sửa tin nhắn"),
    RECALL(6, R.drawable.ic_recall_blue, "Thu hồi"),
    DELETE(7, R.drawable.ic_delete, "Xóa tin nhắn"),
    SHARE(8, R.drawable.ic_forward_blue, "Chia sẻ"),
    MY_CLOUD(8, R.drawable.ic_setting_my_cloud_blue, "Lưu vào Cloud"),
    CREATE_WORK(9, R.drawable.ic_add_circle, "Tạo công việc");

    private final int index;
    private final int icon;
    private final String name;

    MessageAction(int index, int icon, String name) {
        this.index = index;
        this.icon = icon;
        this.name = name;
    }

    public static ArrayList<ArrayList<MessageAction>> getMenuAction(Message message, boolean isAllowReply, boolean allowPinMessage) {
        ArrayList<MessageAction> group1 = new ArrayList<>();
        ArrayList<MessageAction> group2 = new ArrayList<>();
        ArrayList<MessageAction> group3 = new ArrayList<>();
        ArrayList<MessageAction> group4 = new ArrayList<>();
        ArrayList<MessageAction> group5 = new ArrayList<>();
        if (!message.isWithdraw()) {
            group1.add(CREATE_WORK);
            if (isAllowReply) {
                group2.add(ANSWER);
            }
            group2.add(FORWARD);
//            list.add(SHARE);
            if (message.isImportant()) {
                group3.add(UN_MARK);
            } else {
                group3.add(MARK);
            }
            if (allowPinMessage) {
                group3.add(PIN);
            }
            group3.add(COPY);
            group4.add(MY_CLOUD);
            if (message.getPersonSendId().equals(AccountManager.getCurrentUserId())) {
                group4.add(EDIT);
            }
            if (message.getPersonSendId().equals(AccountManager.getCurrentUserId())) {
                group4.add(RECALL);
            }
        }
        group5.add(DELETE);
        ArrayList<ArrayList<MessageAction>> list = new ArrayList<>();
        list.add(group1);
        list.add(group2);
        list.add(group3);
        list.add(group4);
        list.add(group5);
        return list;
    }

    public static ArrayList<ArrayList<MessageAction>> getMenuAction() {
        ArrayList<MessageAction> group1 = new ArrayList<>();
        group1.add(EDIT);
        ArrayList<MessageAction> group2 = new ArrayList<>();
        group2.add(DELETE);
        ArrayList<ArrayList<MessageAction>> list = new ArrayList<>();
        list.add(group1);
        list.add(group2);
        return list;
    }

    public static ArrayList<ArrayList<MessageAction>> getMenuAction(Message message, boolean isAllowReply) {
        ArrayList<MessageAction> group1 = new ArrayList<>();
        if (!message.isWithdraw()) {
            if (isAllowReply) {
                group1.add(ANSWER);
            }
        }
        ArrayList<MessageAction> group2 = new ArrayList<>();
        ArrayList<ArrayList<MessageAction>> list = new ArrayList<>();
        list.add(group1);
        list.add(group2);
        return list;
    }

    public int getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }
}
