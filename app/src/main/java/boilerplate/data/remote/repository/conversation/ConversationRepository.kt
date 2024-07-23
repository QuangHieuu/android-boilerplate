package boilerplate.data.remote.repository.conversation

import boilerplate.data.remote.api.ApiRequest
import boilerplate.data.remote.api.response.BaseResponse
import boilerplate.data.remote.api.response.BaseResult
import boilerplate.model.conversation.Conversation
import boilerplate.model.message.Message
import boilerplate.utils.extension.checkInternet
import io.reactivex.rxjava3.core.Flowable

interface ConversationRepository {

    fun getConversations(
        id: String,
        limit: Int = 10,
        isUnread: Boolean?,
        isImportant: Boolean?,
        name: String?
    ): Flowable<BaseResult<Conversation>>

    fun getPinConversation(): Flowable<BaseResult<Conversation>>

    fun getConversationDetail(id: String): Flowable<BaseResponse<Conversation>>

    fun getMessages(
        id: String,
        limit: Int,
        messageId: String?
    ): Flowable<BaseResult<Message>>

    fun getLatestMessages(
        conversationId: String,
        limit: Int,
        lastMessage: String,
        isDesc: Boolean
    ): Flowable<BaseResult<Message>>
}

class ConversationRepositoryImpl(private val apiRequest: ApiRequest) : ConversationRepository {
    override fun getConversations(
        id: String,
        limit: Int,
        isUnread: Boolean?,
        isImportant: Boolean?,
        name: String?
    ): Flowable<BaseResult<Conversation>> {
        return apiRequest.chat.getConversations(
            id, limit, isUnread, isImportant, name
        ).checkInternet()
    }

    override fun getPinConversation(): Flowable<BaseResult<Conversation>> {
        return apiRequest.chat.getPinConversations().checkInternet()
    }

    override fun getConversationDetail(id: String): Flowable<BaseResponse<Conversation>> {
        return apiRequest.chat.getConversationDetail(id).checkInternet()
    }

    override fun getMessages(
        id: String,
        limit: Int,
        messageId: String?
    ): Flowable<BaseResult<Message>> {
        return apiRequest.chat.getConversationMessage(id, limit, messageId).checkInternet()
    }

    override fun getLatestMessages(
        conversationId: String,
        limit: Int,
        lastMessage: String,
        isDesc: Boolean
    ): Flowable<BaseResult<Message>> {
        return apiRequest.chat
            .getConversationMessage(conversationId, limit, lastMessage, isDesc)
            .checkInternet()
    }
}