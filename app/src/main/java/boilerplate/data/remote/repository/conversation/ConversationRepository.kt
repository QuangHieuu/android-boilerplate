package boilerplate.data.remote.repository.conversation

import boilerplate.data.local.repository.user.TokenRepository
import boilerplate.data.local.repository.user.UserRepository
import boilerplate.data.remote.api.ApiRequest
import boilerplate.data.remote.api.response.BaseResponse
import boilerplate.data.remote.api.response.BaseResult
import boilerplate.data.remote.api.response.BaseResults
import boilerplate.model.conversation.Conversation
import boilerplate.model.conversation.ConversationConfig
import boilerplate.model.conversation.SignalBody
import boilerplate.model.file.AttachedFile
import boilerplate.model.file.UploadFile
import boilerplate.model.message.Message
import boilerplate.utils.extension.checkInternet
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import okhttp3.MultipartBody

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

    fun getConversationFile(
        conversationId: String?,
        type: Int,
        page: Int,
        limit: Int
    ): Single<BaseResult<AttachedFile.Conversation>>

    fun getConversationLink(
        conversationId: String?,
        page: String?,
        limit: Int
    ): Single<BaseResult<Message>>

    fun postFile(files: List<MultipartBody.Part>): Flowable<BaseResults<UploadFile>>

    fun postFile(file: MultipartBody.Part): Flowable<BaseResults<UploadFile>>

    fun putConversation(body: SignalBody): Flowable<BaseResult<Any>>
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

    override fun getConversationConfig(): Single<ConversationConfig> {
        return apiRequest.chat.getConversationConfig().checkInternet()
            .doOnSuccess { userImpl.saveConversationConfig(it) }
    }

    override fun getConversationFile(
        conversationId: String?,
        type: Int,
        page: Int,
        limit: Int
    ): Single<BaseResult<AttachedFile.Conversation>> {
        return apiRequest.chat.getConversationFile(conversationId, type, page, limit)
            .checkInternet()
    }

    override fun getConversationLink(
        conversationId: String?,
        page: String?,
        limit: Int
    ): Single<BaseResult<Message>> {
        return apiRequest.chat.getConversationLink(conversationId, page, limit).checkInternet()
    }

    override fun postFile(files: List<MultipartBody.Part>): Flowable<BaseResults<UploadFile>> {
        return apiRequest.chat.postConversationFile(files).checkInternet()
    }

    override fun postFile(file: MultipartBody.Part): Flowable<BaseResults<UploadFile>> {
        return apiRequest.chat.postConversationFile(file).checkInternet()
    }

    override fun putConversation(body: SignalBody): Flowable<BaseResult<Any>> {
        val id = tokenImpl.getConnectedId()
        return apiRequest.chat.putUpdateGroup(id, body).checkInternet()
    }
}