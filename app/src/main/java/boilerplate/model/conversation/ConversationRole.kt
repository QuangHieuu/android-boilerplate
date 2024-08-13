package boilerplate.model.conversation

enum class ConversationRole(val type: Int, val title: String) {
	MAIN(1, "Trưởng nhóm"),
	SUB(2, "Phó nhóm"),
	MEMBER(0, "Thành viên"),
	ALLOW_MEMBER(3, "Thành viên được nhắn tin");

	companion object {
		private val intToTypeMap: MutableMap<Int, ConversationRole> = HashMap()

		init {
			for (type in entries) {
				intToTypeMap[type.type] = type
			}
		}

		fun fromType(code: Int): ConversationRole {
			val type = intToTypeMap[code] ?: return MEMBER
			return type
		}
	}
}
