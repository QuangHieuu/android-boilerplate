package boilerplate.data.remote.repository.conversation

import boilerplate.data.remote.api.ApiRequest
import boilerplate.data.remote.api.response.BaseResponse
import boilerplate.model.conversation.Conversation
import boilerplate.utils.extension.checkInternet
import io.reactivex.rxjava3.core.Flowable

interface ConversationRepository {

    fun getConversations(
        id: String,
        limit: Int = 10,
        isUnread: Boolean?,
        isImportant: Boolean?,
        name: String?
    ): Flowable<BaseResponse<Conversation.Result>>
}

class ConversationRepositoryImpl(private val apiRequest: ApiRequest) : ConversationRepository {
    override fun getConversations(
        id: String,
        limit: Int,
        isUnread: Boolean?,
        isImportant: Boolean?,
        name: String?
    ): Flowable<BaseResponse<Conversation.Result>> {
        return apiRequest.chat.getConversations(
            id, limit, isUnread, isImportant, name
        ).checkInternet()
    }
}