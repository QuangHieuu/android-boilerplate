package boilerplate.service.signalr

enum class SignalRResult(val key: String) {
    DEFAULT(""),
    DONE("DONE"),
    ERROR("ERROR"),
    CONNECTED("CONNECTED"),
    RECONNECTED("RECONNECTED"),
    RECONNECTING("RECONNECTING"),
    DISCONNECTED("DISCONNECTED"),
    READY_TO_CHAT("READY_TO_CHAT"),
    CREATE_CONVERSATION("CREATE_CONVERSATION"),
    SEND_MESSAGE("SEND_MESSAGE"),
    NEW_MESSAGE("NEW_MESSAGE"),
    UPDATE_MESSAGE("UPDATE_MESSAGE"),
    NEW_MESSAGE_SYSTEM("NEW_MESSAGE_SYSTEM"),
    NEW_REACTION("NEW_REACTION"),
    LAST_TIME_READ("LAST_TIME_READ"),
    ADD_TO_GROUP("ADD_TO_GROUP"),
    TOTAL_UNREAD_CONVERSATION("TOTAL_UNREAD_CONVERSATION"),
    DELETE_SINGLE_MESSAGE("DELETE_SINGLE_MESSAGE"),
    DELETE_MULTIPLE_MESSAGE("DELETE_MULTIPLE_MESSAGE"),
    DELETE_MESSAGE_CONVERSATION("DELETE_MESSAGE_CONVERSATION"),
    LEAVE_GROUP("LEAVE_GROUP"),
    ADD_MEMBER("ADD_MEMBER"),
    APPROVED_MEMBER("APPROVED_MEMBER"),
    DELETE_GROUP("DELETE_GROUP"),
    UPDATE_CONVERSATION_SETTING("UPDATE_CONVERSATION_SETTING"),
    UPDATE_CONVERSATION_INFORM("UPDATE_CONVERSATION_INFORM"),
    UPDATE_CONVERSATION_ROLE("UPDATE_CONVERSATION_ROLE"),
    UPDATE_CONVERSATION_DELETE_MEMBER("UPDATE_CONVERSATION_DELETE_MEMBER"),
    ON_OFF_CONVERSATION_NOTIFY("ON_OFF_CONVERSATION_NOTIFY"),
    DISABLE_CONVERSATION("DISABLE_CONVERSATION"),
    PIN_MESSAGE("PIN_MESSAGE"),
    REMOVE_PIN_MESSAGE("REMOVE_PIN_MESSAGE"),
    IMPORTANT_CONVERSATION("IMPORTANT_CONVERSATION"),
    SEEN_MESSAGE("SEEN_MESSAGE"),
    PIN_CONVERSATION("PIN_CONVERSATION");

    companion object {
        private val intToTypeMap: MutableMap<String, SignalRResult> = HashMap()

        init {
            for (type in entries) {
                intToTypeMap[type.key] = type
            }
        }

        fun fromKey(code: String): SignalRResult {
            val type = intToTypeMap[code] ?: return DEFAULT
            return type
        }
    }
}
