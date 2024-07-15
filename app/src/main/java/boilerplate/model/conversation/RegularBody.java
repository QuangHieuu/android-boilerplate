package boilerplate.model.conversation;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import boilerplate.model.user.User;

public class RegularBody {
    @SerializedName("nhom_lien_lac_id")
    private String nhomLienLacId;
    @SerializedName("ten_nhom_lien_lac")
    private String tenNhomLienLac;
    @SerializedName("nhan_vien_id")
    private String nhanVienId;
    @SerializedName("so_nhan_vien")
    private Integer soNhanVien;
    @SerializedName("ngay_tao")
    private String ngayTao;
    @SerializedName("nhan_viens")
    private List<User> userList;

    private ArrayList<User> ds_nhan_vien;


}
