package boilerplate.ui.conversationMessage.adapter

import boilerplate.model.file.AttachedFile
import boilerplate.model.message.Message
import boilerplate.model.user.User

interface ConversationMessageListener {
    fun onFile(file: AttachedFile.Conversation)

    fun onMention(id: String)

    fun onPhoneNumber(phone: String)

    fun onRemoveImportant(message: Message)

    fun onGoTo(message: Message)

    fun onRemovePin(message: Message)

    fun onAvatar(personSend: User)
}