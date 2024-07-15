package boilerplate.model.message;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import boilerplate.model.user.User;

public class Reaction {
    @SerializedName("emoticon_id")
    private String emoticonId;
    @SerializedName("emoticon_name")
    private String emoticonName;
    @SerializedName("emoticon_properties")
    private String emoticonProperties;
    @SerializedName("image_path")
    private String imagePath;
    @SerializedName("order_index")
    private int orderIndex;
    private int count;
    @SerializedName("nhan_vien_list_count")
    private int nhanVienListCount;
    @SerializedName("nhan_vien_react_info_list")
    private ArrayList<User> nhanVienReactInfoList;
    @SerializedName("ds_nhan_vien")
    private ArrayList<User> dsNhanVien;
    private boolean isReacted;

    public boolean isReacted() {
        return isReacted;
    }

    public void setReacted(boolean reacted) {
        isReacted = reacted;
    }

    public ArrayList<User> getNhanVienReactInfoList() {
        if (nhanVienReactInfoList == null) return new ArrayList<>();
        return nhanVienReactInfoList;
    }

    public ArrayList<User> getDsNhanVien() {
        return dsNhanVien != null ? dsNhanVien : new ArrayList<User>();
    }

    public int getNhanVienListCount() {
        return nhanVienListCount;
    }

    public int getCount() {
        return count;
    }

    public String getEmoticonId() {
        if (emoticonId == null) return "";
        return emoticonId;
    }

    public void setEmoticonId(String emoticonId) {
        this.emoticonId = emoticonId;
    }

    public String getEmoticonName() {
        if (emoticonName == null) return "";
        return emoticonName;
    }

    public void setEmoticonName(String emoticonName) {
        this.emoticonName = emoticonName;
    }

    public String getEmoticonProperties() {
        if (emoticonProperties == null) return "";
        return emoticonProperties;
    }

    public void setEmoticonProperties(String emoticonProperties) {
        this.emoticonProperties = emoticonProperties;
    }

    public String getImagePath() {
        if (imagePath == null) return "";
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public static class Result {
        private ArrayList<Reaction> items;

        public ArrayList<Reaction> getItems() {
            if (items == null) return new ArrayList<>();
            return items;
        }
    }
}
