package boilerplate.model.dashboard

import boilerplate.R

enum class EOfficeMenu(val title: String, val index: Int, val icon: Int, val color: String) {
    REFERENCE_NOT_HANDLED("Chưa xử lý", 0, R.drawable.ic_menu_unprocess, "#FF4858"),
    COMING_REVOKE("Đến thu hồi", 1, R.drawable.ic_menu_internal_sign, "#FF4858"),
    OUT_REVOKE("Đi thu hồi", 2, R.drawable.ic_menu_sign, "#20A8F5"),
    LETTER_OUT_GOING("Công văn đi", 3, R.drawable.ic_menu_out_going, "#5F98FF"),
    WORK_NOT_ASSIGNED("Chưa giao", 4, R.drawable.ic_menu_work_unassign, "#FF4858"),
    WORK_NEW("Chưa thực hiện", 5, R.drawable.ic_menu_work_unprocess, "#FF4858"),
    WORK_MANAGE_NEW("Chưa thực hiện", 6, R.drawable.ic_menu_manager_unprocess, "#FF4858"),
    WORK_MANAGE_DOING("Đang thực hiện", 7, R.drawable.ic_menu_manager_processing, "#5F98FF"),
    REFERENCE_SIGNING("Đi ký số", 8, R.drawable.ic_menu_sign, "#12AA97"),
    INTERNAL_DOCUMENT_SIGNING("Nội bộ ký số", 9, R.drawable.ic_menu_internal_sign, "#2357B6"),
    EXPAND_SIGNING("Ký số mở rộng", 10, R.drawable.ic_menu_external_sign, "#E8A60E"),
    CONCENTRATE_SIGNING("Ký số tập trung", 11, R.drawable.ic_menu_comment_sign, "#3478F3"),
    RECEIVE_FEEDBACK("Nhận góp ý", 12, R.drawable.ic_menu_receiver_comment, "#5F98FF"),
    SENT_FEEDBACK("Gửi góp ý", 13, R.drawable.ic_menu_send_comment, "#FF4858"),
    COMMENTS("Danh sách ý kiến", 14, R.drawable.ic_menu_comment_sign, "#20A8F5"),

    REFERENCE_HANDLE("Chưa xử lý", 15, R.drawable.ic_menu_inprocess_version_2, "#FBE9D7"),
    REFERENCE_HANDLING("Đang xử lý", 16, R.drawable.ic_menu_inprocess, "#1A1677FF"),
    SIGN_GOING("ký số đi", 17, R.drawable.ic_menu_sign_version_2, "#1A2FAFD0"),
    WORK_NO_ASSIGN("Chưa giao", 18, R.drawable.ic_menu_inprocess_version_2, "#FBE9D7"),
    WORK_NEED_DONE("Chưa thực hiện", 19, R.drawable.ic_menu_work_need_done, "#1A57C22D"),
    WORK_OVER_TIME("Trễ hạn", 20, R.drawable.ic_menu_overtime_version_2, "#1ADE3023");

    companion object {
        private val intToTypeMap: MutableMap<Int, EOfficeMenu> = HashMap()

        init {
            for (i in entries) {
                intToTypeMap[i.index] = i
            }
        }


        @JvmStatic
        fun fromIndex(code: Int): EOfficeMenu {
            val type = intToTypeMap[code]
                ?: return REFERENCE_NOT_HANDLED
            return type
        }
    }
}