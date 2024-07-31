package boilerplate.data.remote.repository.conversation

import boilerplate.data.local.repository.user.TokenRepository
import boilerplate.data.local.repository.user.UserRepository
import boilerplate.data.remote.api.ApiRequest
import boilerplate.data.remote.api.response.BaseResponse
import boilerplate.data.remote.api.response.BaseResult
import boilerplate.model.conversation.Conversation
import boilerplate.model.conversation.ConversationConfig
import boilerplate.model.conversation.SignalBody
import boilerplate.model.message.Message
import boilerplate.model.user.User
import boilerplate.utils.extension.checkInternet
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

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

    fun getConversationConfig(): Single<ConversationConfig>

    fun getConversationLink(
        conversationId: String?,
        page: String?,
        limit: Int
    ): Single<BaseResult<Message>>

    fun putConversation(body: SignalBody): Flowable<BaseResult<Any>>

    fun getMemberConversation(
        conversationId: String?,
        page: Int?,
        name: String?,
        isApproved: Boolean?,
        limit: Int
    ): Flowable<BaseResult<User>>
}

class ConversationRepositoryImpl(
    private val apiRequest: ApiRequest,
    private val userImpl: UserRepository,
    private val tokenImpl: TokenRepository
) : ConversationRepository {
    override fun getConversations(
        id: String,
        limit: Int,
        isUnread: Boolean?,
        isImportant: Boolean?,
        name: String?
    ): Flowable<BaseResult<Conversation>> {
        return apiRequest.eOffice.getConversations(
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

    override fun getConversationConfig(): Single<ConversationConfig> {
        return apiRequest.chat.getConversationConfig().checkInternet()
            .doOnSuccess { userImpl.saveConversationConfig(it) }
    }

    override fun getConversationLink(
        conversationId: String?,
        page: String?,
        limit: Int
    ): Single<BaseResult<Message>> {
        return apiRequest.chat.getConversationLink(conversationId, page, limit).checkInternet()
    }

    override fun putConversation(body: SignalBody): Flowable<BaseResult<Any>> {
        val id = tokenImpl.getConnectedId()
        return apiRequest.chat.putUpdateGroup(id, body).checkInternet()
    }

    override fun getMemberConversation(
        conversationId: String?,
        page: Int?,
        name: String?,
        isApproved: Boolean?,
        limit: Int
    ): Flowable<BaseResult<User>> {
        return apiRequest.chat.getMemberConversation(conversationId, page, name, isApproved, limit)
            .checkInternet()
    }
}