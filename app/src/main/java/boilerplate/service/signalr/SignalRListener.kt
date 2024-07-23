package boilerplate.service.signalr

import boilerplate.model.conversation.Conversation
import boilerplate.model.conversation.Conversation.AddMember
import boilerplate.model.conversation.Conversation.Important
import boilerplate.model.conversation.Conversation.SeenMessage
import boilerplate.model.conversation.Conversation.Setting
import boilerplate.model.conversation.ConversationUser.JoinGroup
import boilerplate.model.conversation.ConversationUser.LeaveGroup
import boilerplate.model.conversation.ConversationUser.UpdateRole
import boilerplate.model.message.Message
import boilerplate.model.message.SyncRead
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.koin.java.KoinJavaComponent.inject


private interface SignalRListener {
    fun readyToChat(list: ArrayList<Conversation>)
    fun syncRead(conversationId: String)
    fun deleteMessageConversation(conversationId: String)
    fun createConversation(conversation: Conversation)
    fun newMessage(message: Message)
    fun sendMessage(sendMessage: Message.SendMessageResult)
    fun updateConversation(conversation: Conversation)
    fun leaveGroup(leaveGroup: LeaveGroup)
    fun deleteMember(leaveGroup: LeaveGroup)
    fun deleteGroup(conversationId: String)
    fun onOffNotify(conversationId: String)
    fun disableConversation(conversationId: String)
    fun updateMessage(message: Message)
    fun deleteMessage(list: ArrayList<String>)
    fun importConversation(important: Important)
    fun addMember(addMember: AddMember)
    fun newReaction(message: Message)
    fun updateRole(updateRole: UpdateRole)
    fun seenMessage(seenMessage: SeenMessage)
    fun updateConversationSetting(setting: Setting)
    fun approvedMember(joinGroup: JoinGroup)
    fun pinMessage(pinMessage: Message.Pin)
    fun removePin(pinMessage: Message.Pin)
    fun pinConversation(pinConversation: Conversation.Pin)
}

abstract class SignalRImpl : SignalRListener {
    override fun readyToChat(list: ArrayList<Conversation>) {
    }

    override fun syncRead(conversationId: String) {
    }

    override fun deleteMessageConversation(conversationId: String) {
    }

    override fun createConversation(conversation: Conversation) {
    }

    override fun newMessage(message: Message) {
    }

    override fun sendMessage(sendMessage: Message.SendMessageResult) {
    }

    override fun updateConversation(conversation: Conversation) {
    }

    override fun leaveGroup(leaveGroup: LeaveGroup) {
    }

    override fun deleteMember(leaveGroup: LeaveGroup) {
    }

    override fun deleteGroup(conversationId: String) {
    }

    override fun onOffNotify(conversationId: String) {
    }

    override fun disableConversation(conversationId: String) {
    }

    override fun updateMessage(message: Message) {
    }

    override fun deleteMessage(list: ArrayList<String>) {
    }

    override fun importConversation(important: Important) {
    }

    override fun addMember(addMember: AddMember) {
    }

    override fun newReaction(message: Message) {
    }

    override fun updateRole(updateRole: UpdateRole) {
    }

    override fun seenMessage(seenMessage: SeenMessage) {
    }

    override fun updateConversationSetting(setting: Setting) {
    }

    override fun approvedMember(joinGroup: JoinGroup) {
    }

    override fun pinMessage(pinMessage: Message.Pin) {
    }

    override fun removePin(pinMessage: Message.Pin) {
    }

    override fun pinConversation(pinConversation: Conversation.Pin) {
    }
}

class SubscriptionSignalr {

    private val _gson: Gson by inject(Gson::class.java)

    private var signalr: SignalRListener? = null

    fun setListener(listener: SignalRImpl) {
        signalr = listener
    }

    fun receiver(key: String, data: String) {
        when (SignalRResult.fromKey(key)) {
            SignalRResult.CONNECTED, SignalRResult.DISCONNECTED -> {

            }

            SignalRResult.READY_TO_CHAT -> {
                val list: ArrayList<Conversation> =
                    _gson.fromJson(data, object : TypeToken<ArrayList<Conversation>>() {}.type)
                signalr?.readyToChat(list)
            }

            SignalRResult.LAST_TIME_READ -> {
                val syncRead = _gson.fromJson(data, SyncRead::class.java)
                signalr?.syncRead(syncRead.conversationId ?: "")
            }

            SignalRResult.DELETE_MESSAGE_CONVERSATION -> {
                signalr?.deleteMessageConversation(data)
            }

            SignalRResult.CREATE_CONVERSATION -> {
                val conversation = _gson.fromJson(data, Conversation::class.java)
                signalr?.createConversation(conversation)
            }

            SignalRResult.SEND_MESSAGE -> {
                val sendMessage = _gson.fromJson(data, Message.SendMessageResult::class.java)
                signalr?.sendMessage(sendMessage)
            }

            SignalRResult.NEW_MESSAGE -> {
                val message: Message = _gson.fromJson(data, Message::class.java)
                signalr?.newMessage(message)
            }

            SignalRResult.UPDATE_MESSAGE -> {
                val message = _gson.fromJson(data, Message::class.java)
                signalr?.updateMessage(message)
            }

            SignalRResult.NEW_REACTION -> {
                val message = _gson.fromJson(data, Message::class.java)
                signalr?.newReaction(message)
            }

            SignalRResult.DELETE_MULTIPLE_MESSAGE -> {
                val list: ArrayList<String> =
                    _gson.fromJson(data, object : TypeToken<ArrayList<String>>() {}.type)
                signalr?.deleteMessage(list)
            }

            SignalRResult.LEAVE_GROUP -> {
                val leaveGroup = _gson.fromJson(data, LeaveGroup::class.java)
                signalr?.leaveGroup(leaveGroup)
            }

            SignalRResult.ADD_MEMBER -> {
                val addMember = _gson.fromJson(data, AddMember::class.java)
                signalr?.addMember(addMember)
            }

            SignalRResult.APPROVED_MEMBER -> {
                val joinGroup = _gson.fromJson(data, JoinGroup::class.java)
                signalr?.approvedMember(joinGroup)
            }

            SignalRResult.DELETE_GROUP -> {
                signalr?.deleteGroup(data)
            }

            SignalRResult.UPDATE_CONVERSATION_SETTING -> {
                val setting = _gson.fromJson(data, Setting::class.java)
                signalr?.updateConversationSetting(setting)
            }

            SignalRResult.UPDATE_CONVERSATION_INFORM -> {
                val conversation = _gson.fromJson(data, Conversation::class.java)
                signalr?.updateConversation(conversation)
            }

            SignalRResult.UPDATE_CONVERSATION_ROLE -> {
                val role = _gson.fromJson(data, UpdateRole::class.java)
                signalr?.updateRole(role)
            }

            SignalRResult.UPDATE_CONVERSATION_DELETE_MEMBER -> {
                val leaveGroup = _gson.fromJson(data, LeaveGroup::class.java)
                signalr?.deleteMember(leaveGroup)
            }

            SignalRResult.ON_OFF_CONVERSATION_NOTIFY -> {
                signalr?.onOffNotify(data)
            }

            SignalRResult.DISABLE_CONVERSATION -> {
                signalr?.disableConversation(data)
            }

            SignalRResult.PIN_CONVERSATION -> {
                val conversation = _gson.fromJson(data, Conversation.Pin::class.java)
                signalr?.pinConversation(conversation)
            }

            SignalRResult.PIN_MESSAGE -> {
                val pin = _gson.fromJson(data, Message.Pin::class.java)
                signalr?.pinMessage(pin)
            }

            SignalRResult.REMOVE_PIN_MESSAGE -> {
                val pin = _gson.fromJson(data, Message.Pin::class.java)
                signalr?.removePin(pin)
            }

            SignalRResult.IMPORTANT_CONVERSATION -> {
                val important = _gson.fromJson(data, Important::class.java)
                signalr?.importConversation(important)
            }

            SignalRResult.SEEN_MESSAGE -> {
                val seenMessage = _gson.fromJson(data, SeenMessage::class.java)
                signalr?.seenMessage(seenMessage)
            }

            SignalRResult.NEW_MESSAGE_SYSTEM -> {
                val message = _gson.fromJson(data, Message::class.java).apply { isMsgSystem = true }
            }

            else -> {}
        }
    }
}
