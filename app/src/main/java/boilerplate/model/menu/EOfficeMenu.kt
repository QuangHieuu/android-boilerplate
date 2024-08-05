package boilerplate.model.menu

import boilerplate.R
import boilerplate.constant.AccountManager

enum class EOfficeMenu(val title: String, val index: Int, val icon: Int) {
    NOT_HANDLE("Chưa xử lý", 0, R.drawable.ic_menu_not_handle),
    IN_PROCESS("Đang xử lý", 1, R.drawable.ic_menu_in_process),
    MISTAKE("Chuyển nhầm", 2, R.drawable.ic_menu_mistake),

    DOCUMENT_GOING("Công văn đi", 3, R.drawable.ic_menu_document_going),
    DOCUMENT_GOING_REVOKE("Công văn đi thu hồi", 4, R.drawable.ic_menu_document_going_revoke),
    DOCUMENT_COMING_REVOKE("Công văn đến bị thu hồi", 5, R.drawable.ic_menu_document_coming_revoke),

    DEPARTMENT_NOT_ASSIGN("Phòng ban chưa giao", 6, R.drawable.ic_menu_department_not_assign),
    DEPARTMENT_ASSIGNED("Phòng ban đã giao", 7, R.drawable.ic_menu_assign),
    DEPARTMENT_MISTAKE("Phòng ban chuyển nhầm", 8, R.drawable.ic_menu_mistake),
    PERSONAL_NOT_DOING("Cá nhân chưa thực hiện", 9, R.drawable.ic_menu_personal_not_doing),
    PERSONAL_DOING("Cá nhân đang thực hiện", 10, R.drawable.ic_menu_personal_doing),
    PERSONAL_DONE("Cá nhân đã thực hiện", 11, R.drawable.ic_menu_personal_done),
    PERSONAL_ASSIGNED("Cá nhân đã giao", 12, R.drawable.ic_menu_assign),
    WATCH_TO_KNOW("Xem để biết", 13, R.drawable.ic_menu_watch_to_know),

    SIGN_GOING("Công văn đi", 14, R.drawable.ic_menu_sign_going),
    SIGN_INTERNAL("Công văn nội bộ", 15, R.drawable.ic_menu_sign_internal),
    SIGN_EXTERNAL("Công văn ký số mở rộng", 16, R.drawable.ic_menu_sign_external),
    SIGN_CONCENTRATE("Công văn ký số mở rộng", 17, R.drawable.ic_menu_sign_concentrate),

    CREATE_WORK("Giao việc mới", 18, R.drawable.ic_menu_create_work),
    SEARCH_DOCUMENT("Tìm kiếm công văn", 19, R.drawable.ic_menu_search_document);

    companion object {
        private val intToTypeMap: MutableMap<Int, EOfficeMenu> = HashMap()

        init {
            for (i in entries) {
                intToTypeMap[i.index] = i
            }
        }

        fun fromIndex(code: Int): EOfficeMenu {
            val type = intToTypeMap[code]
                ?: return NOT_HANDLE
            return type
        }

        fun listMenu(): ArrayList<Any?> {
            return arrayListOf<Any?>().apply {
                add(null)
                add("Công văn cần xử lý")
                if (AccountManager.hasIncomeDocument()) {
                    add(Menu(NOT_HANDLE, 0))
                    add(Menu(IN_PROCESS, 0))
                    add(Menu(MISTAKE, 0))
                }

                if (AccountManager.hasGoingDocument() || AccountManager.hasGoingManagerDocument()) {
                    add(Menu(DOCUMENT_GOING, 0))
                }
                if (AccountManager.hasGoingSuggestRevoke() || AccountManager.hasGoingAcceptRevoke()) {
                    add(Menu(DOCUMENT_GOING_REVOKE, 0))
                }
                if (AccountManager.hasIncomeDocument()) {
                    add(Menu(DOCUMENT_COMING_REVOKE, 0))
                }
                add(null)
                add("Công việc")
                if (AccountManager.hasDepartmentWorkManager()) {
                    add(Menu(DEPARTMENT_NOT_ASSIGN, 0))
                    add(Menu(DEPARTMENT_ASSIGNED, 0))
                    add(Menu(DEPARTMENT_MISTAKE, 0))
                }
                if (AccountManager.hasPersonalWorkManager()) {
                    add(Menu(PERSONAL_NOT_DOING, 0))
                    add(Menu(PERSONAL_DOING, 0))
                    add(Menu(PERSONAL_DONE, 0))
                    if (AccountManager.hasWorkAssign()) {
                        add(Menu(PERSONAL_ASSIGNED, 0))
                    }
                    add(Menu(WATCH_TO_KNOW, 0))
                }
                add(null)
                add("Công văn ký số")
                if (AccountManager.hasDigitalSignManage()) {
                    add(Menu(SIGN_GOING, 0))
                    add(Menu(SIGN_INTERNAL, 0))
                    add(Menu(SIGN_EXTERNAL, 0))
                }
                if (AccountManager.hasDigitalConcentrateSign()) {
                    add(Menu(SIGN_CONCENTRATE, 0))
                }
                add(null)
                add("Chức năng khác")
                add(Menu(CREATE_WORK, 0))
                add(null)
                add("Tiện ích")
                add(Menu(SEARCH_DOCUMENT, 0))
            }
        }
    }
}

data class Menu(
    var item: EOfficeMenu,
    var count: Int = 0
)