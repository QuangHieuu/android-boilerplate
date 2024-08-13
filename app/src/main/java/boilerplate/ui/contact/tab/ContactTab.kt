package boilerplate.ui.contact.tab

enum class ContactTab(val index: Int, val type: String) {
	TYPE_TAB_DEPARTMENT(0, "TYPE_TAB_DEPARTMENT"),
	TYPE_TAB_COMPANY(1, "TYPE_TAB_COMPANY"),
	TYPE_TAB_DEPARTMENT_ADD(0, "TYPE_TAB_DEPARTMENT_ADD"),
	TYPE_TAB_COMPANY_ADD(1, "TYPE_TAB_COMPANY_ADD"),
	TYPE_TAB_DEPARTMENT_CREATE(0, "TYPE_TAB_DEPARTMENT_CREATE"),
	TYPE_TAB_COMPANY_CREATE(1, "TYPE_TAB_COMPANY_CREATE"),
	TYPE_TAB_DEPARTMENT_SHARE(0, "TYPE_TAB_DEPARTMENT_SHARE"),
	TYPE_TAB_COMPANY_SHARE(1, "TYPE_TAB_COMPANY_SHARE"),
	TYPE_TAB_DEPARTMENT_REGULAR(0, "TYPE_TAB_DEPARTMENT_REGULAR"),
	TYPE_TAB_COMPANY_REGULAR(1, "TYPE_TAB_COMPANY_REGULAR"),
	TYPE_TAB_REGULAR(2, "TYPE_TAB_REGULAR"),
	TYPE_TAB_GROUP(3, "TYPE_TAB_GROUP"),
	TYPE_TAB_CONVERSATION(3, "TYPE_TAB_CONVERSATION");

	companion object {
		private val intToTypeMap: MutableMap<String, ContactTab> = HashMap()
		private val intToIndexMap: MutableMap<Int, ContactTab> = HashMap()

		init {
			for (type in entries) {
				intToTypeMap[type.type] = type
			}
			for (type in entries) {
				intToIndexMap[type.index] = type
			}
		}

		fun fromType(code: String): ContactTab {
			val type = intToTypeMap[code] ?: return TYPE_TAB_DEPARTMENT
			return type
		}

		fun isTabRegular(type: String): Boolean {
			return type == TYPE_TAB_REGULAR.type || type == TYPE_TAB_GROUP.type || type == TYPE_TAB_CONVERSATION.type
		}
	}
}