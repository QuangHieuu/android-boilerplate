package boilerplate.model.message;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import boilerplate.model.file.AttachedFile;
import boilerplate.model.user.Title;
import boilerplate.model.user.User;

public class Quote {
    @SerializedName("tin_nhan_id")
    private String messageId;
    @SerializedName("nguoi_gui")
    private PersonQuote personSend;
    @SerializedName("noi_dung")
    private String content;
    @SerializedName("ngay_tao")
    private String dateCreate;
    @SerializedName("hoi_thoai_id")
    private String conversationId;
    @SerializedName("file_dinh_kem")
    private ArrayList<AttachedFile.Conversation> attachedFiles;
    @SerializedName("phieu_khao_sat")
    private ArrayList<AttachedFile.SurveyFile> surveyFiles;

    public String getMessageId() {
        return messageId;
    }

    public PersonQuote getPersonSend() {
        if (personSend == null) return personSend = new PersonQuote();
        return personSend;
    }

    public String getContent() {
        if (content == null) return content = "";
        return content;
    }

    public String getDateCreate() {
        return dateCreate;
    }

    public String getConversationId() {
        return conversationId;
    }

    public ArrayList<AttachedFile.Conversation> getAttachedFiles() {
        if (attachedFiles == null) return attachedFiles = new ArrayList<>();
        return attachedFiles;
    }

    public ArrayList<AttachedFile.SurveyFile> getSurveyFiles() {
        if (surveyFiles == null) return surveyFiles = new ArrayList<>();
        return surveyFiles;
    }

    public Quote(Message message) {
        this.messageId = message.getMessageId();
        this.content = message.getMainContent()[0];
        this.conversationId = message.getConversationId();
        this.surveyFiles = message.getSurveyFiles();
        this.attachedFiles = message.getAttachedFiles();
        this.dateCreate = message.getDateCreate();
        this.personSend = new PersonQuote(message.getPersonSend());
    }

    public void setContent(String content) {
        this.content = content;
    }

    public static class TitleQuote {
        @SerializedName("phong_ban_chinh")
        private boolean isMain;
        @SerializedName("don_vi")
        private CompanyQuote company;
        @SerializedName("phong_ban")
        private DepartmentQuote department;

        public TitleQuote(Title title) {
            this.department = new DepartmentQuote(title.getDepartment().getName(), title.getDepartment().getShortName());
            this.company = new CompanyQuote(title.getCompany().getName(), title.getCompany().getShortName());
            this.isMain = title.isMain();
        }

        public boolean isMain() {
            return isMain;
        }

        public CompanyQuote getCompany() {
            return company;
        }

        public DepartmentQuote getDepartment() {
            return department;
        }

        public static ArrayList<TitleQuote> clone(ArrayList<Title> titles) {
            ArrayList<TitleQuote> list = new ArrayList<>();
            for (Title title : titles) {
                list.add(new TitleQuote(title));
            }
            return list;
        }
    }

    public static class PersonQuote {
        @SerializedName("ten_nhan_vien")
        private String name;
        @SerializedName("ds_chuc_danh")
        private ArrayList<TitleQuote> roles;

        public PersonQuote() {
        }

        public PersonQuote(User person) {
            this.name = person.getName();
            this.roles = TitleQuote.clone(person.getTitles());
        }

        public String getName() {
            if (name == null) {
                return name = "";
            }
            return name;
        }


        public DepartmentQuote getMainDepartment() {
            if (roles != null) {
                for (TitleQuote title : roles) {
                    if (title.isMain) {
                        return title.department;
                    }
                }
            }
            return new DepartmentQuote();
        }

        public CompanyQuote getMainCompany() {
            if (roles != null) {
                for (TitleQuote title : roles) {
                    if (title.isMain) {
                        return title.company;
                    }
                }
            }
            return new CompanyQuote();
        }
    }

    public static class CompanyQuote {
        @SerializedName("ten_don_vi")
        private String name;
        @SerializedName("ten_viet_tat")
        private String shortName;

        public CompanyQuote() {
        }

        public CompanyQuote(String name, String shortName) {
            this.name = name;
            this.shortName = shortName;
        }

        public String getName() {
            if (name == null) return "";
            return name;
        }

        public String getShortName() {
            if (shortName == null) return "";
            return shortName;
        }
    }

    public static class DepartmentQuote {
        @SerializedName("ten_phong_ban")
        private String name;
        @SerializedName("ma_phong_ban")
        private String shortName;

        public DepartmentQuote() {
        }

        public DepartmentQuote(String name, String shortName) {
            this.name = name;
            this.shortName = shortName;
        }

        public String getName() {
            if (name == null) return "";
            return name;
        }

        public String getShortName() {
            if (shortName == null) return "";
            return shortName;
        }
    }
}