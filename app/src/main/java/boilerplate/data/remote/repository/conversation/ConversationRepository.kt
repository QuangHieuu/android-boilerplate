package boilerplate.data.remote.repository.conversation

import boilerplate.data.local.repository.user.TokenRepository
import boilerplate.data.local.repository.user.UserRepository
import boilerplate.data.remote.api.ApiRequest
import boilerplate.data.remote.api.response.Response
import boilerplate.data.remote.api.response.ResponseItems
import boilerplate.model.conversation.Conversation
import boilerplate.model.conversation.ConversationConfig
import boilerplate.model.conversation.ConversationSignalR
import boilerplate.model.message.Message
import boilerplate.model.message.PinMessage
import boilerplate.model.message.Reaction
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
	): Flowable<ResponseItems<Conversation>>

	fun getPinConversation(): Flowable<ResponseItems<Conversation>>

	fun getConversationDetail(id: String): Flowable<Response<Conversation>>

	fun getMessages(id: String, limit: Int, messageId: String?): Flowable<ResponseItems<Message>>

	fun getLatestMessages(
		conversationId: String,
		limit: Int,
		lastMessage: String,
		isDesc: Boolean
	): Flowable<ResponseItems<Message>>

	fun getConversationConfig(): Single<ConversationConfig>

	fun getConversationLink(
		conversationId: String?,
		page: String?,
		limit: Int
	): Single<ResponseItems<Message>>

	fun putConversation(body: ConversationSignalR): Flowable<ResponseItems<Any>>

	fun getMemberConversation(
		conversationId: String?,
		page: Int?,
		name: String?,
		isApproved: Boolean?,
		limit: Int
	): Flowable<ResponseItems<User>>

	fun getPinMessage(
		conversationId: String?,
		page: Int,
		limit: Int
	): Flowable<ResponseItems<Message>>

	fun getImportantMessage(
		conversationId: String,
		page: Int,
		limit: Int
	): Flowable<ResponseItems<Message>>

	fun postPersonConversation(body: ConversationSignalR): Flowable<Response<Conversation>>

	fun postPinMessage(body: PinMessage): Flowable<Response<Any>>

	fun putPinMessages(messageId: String, conversationId: String): Flowable<Response<Any>>

	fun putMarkImportant(messageId: String, conversationId: String): Flowable<Response<Any>>

	fun getStatusMessage(messageId: String, type: String): Flowable<Response<Int>>

	fun putWithdrawMessage(conversationId: String, messageId: String): Flowable<Response<Any>>

	fun getReactions(): Flowable<ResponseItems<Reaction>>

	fun postReaction(
		messageId: String,
		conversationId: String,
		emotionId: String
	): Flowable<Response<Any>>

	fun putEditMessage(message: Message): Flowable<Response<Any>>

	fun getSearchConversations(data: Map<String, Any>): Flowable<ResponseItems<Conversation>>

	fun putEditGroup(id: String, body: ConversationSignalR): Flowable<Response<Conversation>>
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
	): Flowable<ResponseItems<Conversation>> {
		return apiRequest.chat.getConversations(
			id, limit, isUnread, isImportant, name
		).checkInternet()
	}

	override fun getPinConversation(): Flowable<ResponseItems<Conversation>> {
		return apiRequest.chat.getPinConversations().checkInternet()
	}

	override fun getConversationDetail(id: String): Flowable<Response<Conversation>> {
		return apiRequest.chat.getConversationDetail(id).checkInternet()
	}

	override fun getMessages(
		id: String,
		limit: Int,
		messageId: String?
	): Flowable<ResponseItems<Message>> {
		return apiRequest.chat.getConversationMessage(id, limit, messageId).checkInternet()
	}

	override fun getLatestMessages(
		conversationId: String,
		limit: Int,
		lastMessage: String,
		isDesc: Boolean
	): Flowable<ResponseItems<Message>> {
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
	): Single<ResponseItems<Message>> {
		return apiRequest.chat.getConversationLink(conversationId, page, limit).checkInternet()
	}

	override fun putConversation(body: ConversationSignalR): Flowable<ResponseItems<Any>> {
		val id = tokenImpl.getConnectedId()
		return apiRequest.chat.putUpdateGroup(id, body).checkInternet()
	}

	override fun getMemberConversation(
		conversationId: String?,
		page: Int?,
		name: String?,
		isApproved: Boolean?,
		limit: Int
	): Flowable<ResponseItems<User>> {
		return apiRequest.chat.getMemberConversation(conversationId, page, name, isApproved, limit)
			.checkInternet()
	}

	override fun getPinMessage(
		conversationId: String?,
		page: Int,
		limit: Int
	): Flowable<ResponseItems<Message>> {
		return apiRequest.chat.getPinMessages(conversationId, page, limit).checkInternet()
	}

	override fun getImportantMessage(
		conversationId: String,
		page: Int,
		limit: Int
	): Flowable<ResponseItems<Message>> {
		return apiRequest.chat.getImportantMessage(conversationId, conversationId, page, limit)
			.checkInternet()
	}

	override fun postPersonConversation(body: ConversationSignalR): Flowable<Response<Conversation>> {
		val connectedId = tokenImpl.getConnectedId()
		return apiRequest.chat.postPersonConversation(connectedId, body).checkInternet()
	}

	override fun postPinMessage(body: PinMessage): Flowable<Response<Any>> {
		return apiRequest.chat.postPinMessages(body).checkInternet()
	}

	override fun putPinMessages(messageId: String, conversationId: String): Flowable<Response<Any>> {
		return apiRequest.chat.putPinMessages(messageId, conversationId).checkInternet()
	}

	override fun putMarkImportant(
		messageId: String,
		conversationId: String
	): Flowable<Response<Any>> {
		return apiRequest.chat.putMarkImportant(messageId, conversationId)
	}

	override fun getStatusMessage(messageId: String, type: String): Flowable<Response<Int>> {
		return apiRequest.chat.getStatusMessage(messageId, type)
	}

	override fun putWithdrawMessage(
		conversationId: String,
		messageId: String
	): Flowable<Response<Any>> {
		return apiRequest.chat.putWithdrawMessage(conversationId, messageId)
	}

	override fun getReactions(): Flowable<ResponseItems<Reaction>> {
		return apiRequest.chat.getReactions().checkInternet()
	}

	override fun postReaction(
		messageId: String,
		conversationId: String,
		emotionId: String
	): Flowable<Response<Any>> {
		return apiRequest.chat.postReaction(messageId, messageId, conversationId, emotionId)
			.checkInternet()
	}

	override fun putEditMessage(message: Message): Flowable<Response<Any>> {
		return apiRequest.chat.putEditMessage(message).checkInternet()
	}

	override fun getSearchConversations(data: Map<String, Any>): Flowable<ResponseItems<Conversation>> {
		return apiRequest.chat.getSearchConversations(data).checkInternet()
	}

	override fun putEditGroup(id: String, body: ConversationSignalR): Flowable<Response<Conversation>> {
		return apiRequest.chat.putEditGroup(id, body)
	}
}