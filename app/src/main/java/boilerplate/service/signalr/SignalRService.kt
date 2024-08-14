package boilerplate.service.signalr

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import boilerplate.data.local.repository.user.TokenRepository
import boilerplate.data.remote.api.ApiUrl
import boilerplate.model.conversation.AddMember
import boilerplate.model.conversation.Conversation
import boilerplate.model.conversation.ConversationSignalR
import boilerplate.model.conversation.ImportantConversation
import boilerplate.model.conversation.JoinGroup
import boilerplate.model.conversation.LeaveGroup
import boilerplate.model.conversation.Member
import boilerplate.model.conversation.PinConversation
import boilerplate.model.conversation.SeenMessage
import boilerplate.model.conversation.Setting
import boilerplate.model.conversation.UpdateRole
import boilerplate.model.message.Message
import boilerplate.model.message.Option
import boilerplate.model.message.PinMessage
import boilerplate.model.message.SendBody
import boilerplate.model.user.UserSignalR
import boilerplate.utils.InternetManager
import boilerplate.utils.StringUtil
import boilerplate.utils.extension.notNull
import boilerplate.utils.extension.sendResult
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import microsoft.aspnet.signalr.client.ConnectionState
import microsoft.aspnet.signalr.client.hubs.HubConnection
import microsoft.aspnet.signalr.client.hubs.HubProxy
import microsoft.aspnet.signalr.client.transport.ClientTransport
import microsoft.aspnet.signalr.client.transport.WebsocketTransport
import org.koin.android.ext.android.inject
import java.util.Date
import java.util.concurrent.TimeUnit

class SignalRService : Service() {
	companion object {
		const val TAG: String = "socket"

		const val SIGNALR_TIMEOUT_MILLIS = 3 * 1000L

		const val RECONNECTING: String = "reconnecting"
		const val RECONNECTED: String = "reconnected"
		const val DISCONNECTED: String = "disconnected"
		const val CONNECTED: String = "connected"

		const val CLIENT_METHOD_READY_CHAT: String = "readyToChat"
		const val CLIENT_METHOD_NEW_MESSAGE: String = "newMessage"
		const val CLIENT_METHOD_UPDATE_MESSAGE: String = "updateMessage"
		const val CLIENT_METHOD_STATE_CHANGED: String = "stateChanged"
		const val CLIENT_METHOD_CREATED_CONVERSATION: String = "createdConversation"
		const val CLIENT_METHOD_CREATED_MESSAGE: String = "sendMessageResult"
		const val CLIENT_METHOD_ADD_TO_GROUP: String = "newGroup"
		const val CLIENT_METHOD_REFRESH_MESSAGE: String = "requestRefreshMessage"
		const val CLIENT_METHOD_LAST_TIME_READ: String = "syncLastTimeRead"
		const val CLIENT_METHOD_TOTAL_UNREAD_CONVERSATION: String = "totalConversationHasNewMessage"
		const val CLIENT_METHOD_READ_ALL_CONVERSATION: String = "readAllConversation"
		const val CLIENT_METHOD_MESSAGE_DELETED: String = "messageDeleted"
		const val CLIENT_METHOD_MESSAGE_DELETED_MULTIPLE: String = "messagesDeleted"
		const val CLIENT_METHOD_MESSAGE_DELETED_CONVERSATION: String = "messageConversationDeleted"
		const val CLIENT_METHOD_ADD_MEMBER: String = "addNhanVienHoiThoai"
		const val CLIENT_METHOD_APPROVE_MEMBER: String = "duyetNhanVienHoiThoai"
		const val CLIENT_METHOD_DELETE_GROUP: String = "deleteGroupChat"
		const val CLIENT_METHOD_CONVERSATION_SETTING: String = "updateCauHinhNhom"
		const val CLIENT_METHOD_CONVERSATION_INFORM: String = "updatedConversation"
		const val CLIENT_METHOD_CONVERSATION_ROLE: String = "updateVaiTroNhanVienHoiThoai"
		const val CLIENT_METHOD_CONVERSATION_DELETE_MEMBER: String = "deleteThanhVienHoiThoai"
		const val CLIENT_METHOD_LEAVE_GROUP: String = "roiHoiThoai"
		const val CLIENT_METHOD_NEW_MESSAGE_SYSTEM: String = "newMessageSystem"
		const val CLIENT_METHOD_ON_OFF_NOTIFY: String = "tatThongBaoHoiThoai"
		const val CLIENT_METHOD_DISABLE_CONVERSATION: String = "disableHoiThoai"
		const val CLIENT_METHOD_PIN_MESSAGE: String = "taoGhimTinNhan"
		const val CLIENT_METHOD_REMOVE_PIN_MESSAGE: String = "boGhimTinNhan"
		const val CLIENT_METHOD_DELETE_PIN_MESSAGE: String = "deleteGhimTinNhan"
		const val CLIENT_METHOD_IMPORTANT_CONVERSATION: String = "danhDauHoiThoai"
		const val CLIENT_METHOD_SEEN_MESSAGE: String = "seenMessage"
		const val CLIENT_METHOD_PIN_CONVERSATION: String = "ghimHoiThoai"

		//Phải kiểm tra kĩ key signalr khi sử dụng có thể viết hoa hoặc thường chữ cái đầu tiên
		const val SERVER_METHOD_SEND_MESSAGE: String = "SendMessage"
		const val SERVER_METHOD_CREATE_CONVERSATION: String = "CreateConversation"
		const val SERVER_METHOD_LAST_TIME_READ: String = "LastTimeRead"
		const val SERVER_METHOD_ADD_TO_GROUP: String = "AddMeToGroup"
		const val SERVER_METHOD_LEAVE_GROUP: String = "RoiHoiThoai"
		const val SERVER_METHOD_ADD_MEMBER: String = "AddNhanVienHoiThoai"
		const val SERVER_METHOD_APPROVE_MEMBER: String = "DuyetNhanVienHoiThoai"
		const val SERVER_METHOD_DELETE_GROUP: String = "DeleteGroupChat"
		const val SERVER_METHOD_CONVERSATION_SETTING: String = "updateCauHinhNhom"
		const val SERVER_METHOD_CONVERSATION_ROLE: String = "UpdateVaiTroNhanVienHoiThoai"
		const val SERVER_METHOD_CONVERSATION_DELETE_MEMBER: String = "DeleteThanhVienHoiThoai"
		const val SERVER_METHOD_ON_OFF_NOTIFY: String = "TatThongBaoHoiThoai"
		const val SERVER_METHOD_DISABLE_CONVERSATION: String = "DisableHoiThoai"
		const val SERVER_METHOD_PIN_CONVERSATION: String = "GhimHoiThoai"
	}

	override fun onBind(intent: Intent): IBinder {
		create()
		return _binder
	}

	override fun onDestroy() {
		super.onDestroy()
		_disposable.dispose()
	}

	private var _chatHub: HubConnection? = null
	private var _chatTransport: ClientTransport? = null
	private var _chatProxy: HubProxy? = null

	private val _disposable = CompositeDisposable()
	private val _binder = LocalBinder()
	private val _gson by inject<Gson>()
	private val _tokenImpl by inject<TokenRepository>()

	private val _subscription = Observable.timer(SIGNALR_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
		.repeat()
		.subscribeOn(Schedulers.io())
		.doOnNext {
			if (!InternetManager.isConnected()) {
				sendResult(SignalRResult.DISCONNECTED, DISCONNECTED)
				return@doOnNext
			}
			_chatHub.notNull {
				if (it.state == ConnectionState.Disconnected) {
					reconnect()
					return@doOnNext
				}
			}
		}

	private fun create() {
		_chatHub = HubConnection(ApiUrl.HOST_CHAT).apply {
			stateChanged { _, newState ->
				when (newState) {
					ConnectionState.Connected -> {
						_chatProxy?.removeAllSubscription()
						subscribe()
						_tokenImpl.saveConnectedId(connectionId)
						sendResult(SignalRResult.CONNECTED, CONNECTED)
					}

					ConnectionState.Reconnecting -> {
						_chatProxy?.removeAllSubscription()
						sendResult(SignalRResult.RECONNECTING, RECONNECTING)
					}

					else -> {}
				}
			}
			error { error ->
				Log.d(TAG, "error: " + _gson.toJson(error))
			}
		}.also {
			_chatTransport = WebsocketTransport(it.logger)
			_chatProxy = it.createHubProxy("chatHub")
		}
	}

	fun start() {
		val token = _tokenImpl.getOnlyToken()
		_chatHub.notNull {
			if (it.state == ConnectionState.Disconnected && token.isNotEmpty()) {
				with(it) {
					bearerToken = token
					start(_chatTransport)
				}
				_disposable.add(_subscription.subscribe())
			}
		}
	}

	fun remove() {
		_chatHub = null
		_chatProxy = null
		_chatTransport = null
	}

	fun stop() {
		_disposable.clear()
		_chatHub.notNull {
			it.stop()
			it.disconnect()
		}
		remove()
	}

	fun reconnect() {
		stop()
		create()
		start()
	}

	fun sendMessage(message: Message, isSms: Boolean, isEmail: Boolean) {
		_chatProxy.notNull { proxy ->
			var isWrongFormat = false
			if (message.content.contains(StringUtil.KEY_FORWARD_JSON)) {
				val s: String = StringUtil.unescapeHTML(message.content, 0)
				val list: ArrayList<Int> =
					StringUtil.countWord(s, StringUtil.KEY_FORWARD_JSON_REGEX)
				if (list.size < 2 || !s.startsWith(StringUtil.KEY_FORWARD_JSON)) {
					isWrongFormat = true
				}
			}
			if (isWrongFormat) {
				sendResult(
					SignalRResult.SEND_MESSAGE_ERROR,
					"Tin nhắn được gửi không đúng định dạng"
				)
			} else {
				if (message.content.length <= 42000) {
					SendBody(
						message.content,
						message.conversationId,
						message.attachedFiles,
						Option(isSms, isEmail),
						message.surveyFiles,
						message.isMsgSystem,
					).let {
						proxy.invoke(SERVER_METHOD_SEND_MESSAGE, it)
							.onError { sendResult(SignalRResult.SEND_MESSAGE_ERROR, "") }
							.onCancelled { sendResult(SignalRResult.SEND_MESSAGE_ERROR, "") }
					}
				} else {
					sendResult(
						SignalRResult.SEND_MESSAGE_ERROR,
						"Anh/chị không thể gửi tin nhắn do nội dung hoặc thông tin chuyển tiếp quá dài"
					)
				}
			}
		}
	}

	fun createConversation(conversation: ConversationSignalR) {
		_chatProxy.notNull {
			it.invoke(SERVER_METHOD_CREATE_CONVERSATION, conversation)
				.onError {
					sendResult(
						SignalRResult.CREATE_CONVERSATION,
						SignalRResult.ERROR.key
					)
				}
				.onCancelled {
					sendResult(
						SignalRResult.CREATE_CONVERSATION,
						SignalRResult.ERROR.key
					)
				}
		}
	}

	fun pinConversation(conversationId: String?, isPin: Boolean) {
		_chatProxy.notNull {
			it.invoke(SERVER_METHOD_PIN_CONVERSATION, conversationId, isPin)
				.onError {
					sendResult(
						SignalRResult.PIN_CONVERSATION,
						SignalRResult.ERROR.key
					)
				}
				.onCancelled {
					sendResult(
						SignalRResult.PIN_CONVERSATION,
						SignalRResult.ERROR.key
					)
				}
		}
	}

	fun sendLastTimeRead(
		conversationId: String,
		lastTimeRead: Date,
		countRead: Int,
		force: Boolean
	) {
		_chatProxy.notNull {
			it.invoke(
				SERVER_METHOD_LAST_TIME_READ,
				conversationId,
				lastTimeRead,
				countRead,
				force
			)
				.onError { sendResult(SignalRResult.LAST_TIME_READ, SignalRResult.ERROR.key) }
				.onCancelled { sendResult(SignalRResult.LAST_TIME_READ, SignalRResult.ERROR.key) }
		}
	}

	fun leaveGroup(id: String, isLeaveInSilent: Boolean) {
		_chatProxy.notNull {
			it.invoke(SERVER_METHOD_LEAVE_GROUP, id, isLeaveInSilent)
				.onError { sendResult(SignalRResult.LEAVE_GROUP, SignalRResult.ERROR.key) }
				.onCancelled { sendResult(SignalRResult.LEAVE_GROUP, SignalRResult.ERROR.key) }
		}
	}

	fun addMember(conversationId: String?, list: ArrayList<UserSignalR>) {
		_chatProxy.notNull {
			it.invoke(SERVER_METHOD_ADD_MEMBER, conversationId, list)
				.onError { sendResult(SignalRResult.ADD_MEMBER, SignalRResult.ERROR.key) }
				.onCancelled { sendResult(SignalRResult.ADD_MEMBER, SignalRResult.ERROR.key) }
		}
	}

	fun approveMemberOrNot(
		isApprove: Boolean,
		conversationId: String?,
		list: ArrayList<UserSignalR>
	) {
		_chatProxy.notNull {
			it.invoke(SERVER_METHOD_APPROVE_MEMBER, conversationId, isApprove, list)
				.onError { sendResult(SignalRResult.APPROVED_MEMBER, SignalRResult.ERROR.key) }
				.onCancelled { sendResult(SignalRResult.APPROVED_MEMBER, SignalRResult.ERROR.key) }
		}
	}

	fun deleteGroup(conversationId: String?) {
		_chatProxy.notNull {
			it.invoke(SERVER_METHOD_DELETE_GROUP, conversationId)
				.onError {
					sendResult(
						SignalRResult.DELETE_GROUP,
						SignalRResult.ERROR.key
					)
				}
				.onCancelled {
					sendResult(
						SignalRResult.DELETE_GROUP,
						SignalRResult.ERROR.key
					)
				}
		}
	}

	fun deleteMember(conversationId: String?, userId: String?) {
		_chatProxy.notNull {
			it.invoke(SERVER_METHOD_CONVERSATION_DELETE_MEMBER, conversationId, userId)
				.onError {
					sendResult(
						SignalRResult.UPDATE_CONVERSATION_DELETE_MEMBER,
						SignalRResult.ERROR.key
					)
				}
				.onCancelled {
					sendResult(
						SignalRResult.UPDATE_CONVERSATION_DELETE_MEMBER,
						SignalRResult.ERROR.key
					)
				}
		}
	}

	fun updateConversationSetting(conversation: Conversation) {
		_chatProxy.notNull {
			val conversationId = conversation.id
			val changeInform = conversation.isChangeInform
			val pinMessage = conversation.isAllowPinMessage
			val approvedMember = conversation.isAllowApproved
			val sendMessage = conversation.isAllowSendMessage
			it.invoke(
				SERVER_METHOD_CONVERSATION_SETTING,
				conversationId,
				changeInform,
				pinMessage,
				approvedMember,
				sendMessage
			)
				.onError { throwable: Throwable? ->
					sendResult(
						SignalRResult.UPDATE_CONVERSATION_SETTING,
						SignalRResult.ERROR.key
					)
				}
				.onCancelled {
					sendResult(
						SignalRResult.UPDATE_CONVERSATION_SETTING,
						SignalRResult.ERROR.key
					)
				}
		}
	}

	fun assignConversationRole(userId: String?, conversationId: String?, role: Int) {
		_chatProxy.notNull {
			it.invoke(SERVER_METHOD_CONVERSATION_ROLE, userId, conversationId, role)
				.onError {
					sendResult(
						SignalRResult.UPDATE_CONVERSATION_ROLE,
						SignalRResult.ERROR.key
					)
				}
				.onCancelled {
					sendResult(
						SignalRResult.UPDATE_CONVERSATION_ROLE,
						SignalRResult.ERROR.key
					)
				}
		}
	}

	fun onOffConversationNotify(conversationId: String?, isOn: Boolean) {
		_chatProxy.notNull {
			it.invoke(SERVER_METHOD_ON_OFF_NOTIFY, conversationId, isOn)
				.onError {
					sendResult(
						SignalRResult.ON_OFF_CONVERSATION_NOTIFY,
						SignalRResult.ERROR.key
					)
				}
				.onCancelled {
					sendResult(
						SignalRResult.ON_OFF_CONVERSATION_NOTIFY,
						SignalRResult.ERROR.key
					)
				}
		}
	}

	fun disableConversation(conversationId: String?) {
		_chatProxy.notNull {
			it.invoke(SERVER_METHOD_DISABLE_CONVERSATION, conversationId)
				.onError { sendResult(SignalRResult.DISABLE_CONVERSATION, SignalRResult.ERROR.key) }
				.onCancelled {
					sendResult(
						SignalRResult.DISABLE_CONVERSATION,
						SignalRResult.ERROR.key
					)
				}
		}
	}

	private fun sendResult(key: SignalRResult?, value: String) {
		key.notNull {
			applicationContext.sendResult(key, value)
		}
	}

	private fun subscribe() {
		_chatProxy.notNull { proxy ->
			with(proxy) {
				handle(CLIENT_METHOD_READY_CHAT) {
					sendResult(SignalRResult.READY_TO_CHAT, it[0].toString())
				}
				handle(CLIENT_METHOD_SEEN_MESSAGE) {
					val seen: SeenMessage = SeenMessage(
						it[0].asString,
						it[1].asString
					)
					sendResult(SignalRResult.SEEN_MESSAGE, _gson.toJson(seen))
				}
				handle(CLIENT_METHOD_IMPORTANT_CONVERSATION) {
					val pin = ImportantConversation(
						_gson.fromJson(it[0].toString(), Conversation::class.java),
						it[1].asBoolean
					)
					sendResult(SignalRResult.IMPORTANT_CONVERSATION, _gson.toJson(pin))
				}
				handle(CLIENT_METHOD_REMOVE_PIN_MESSAGE, CLIENT_METHOD_DELETE_PIN_MESSAGE) {
					val pin = PinMessage(
						conversationId = it[0].asString,
						messageId = it[1].asString
					)
					sendResult(SignalRResult.REMOVE_PIN_MESSAGE, _gson.toJson(pin))
				}
				handle(CLIENT_METHOD_PIN_MESSAGE) {
					val pin = PinMessage(
						conversationId = it[0].asString,
						message = _gson.fromJson(it[1].asJsonObject, Message::class.java)
					)
					sendResult(SignalRResult.REMOVE_PIN_MESSAGE, _gson.toJson(pin))
				}
				handle(CLIENT_METHOD_DISABLE_CONVERSATION) {
					sendResult(SignalRResult.DISABLE_CONVERSATION, it[0].asString)
				}
				handle(CLIENT_METHOD_UPDATE_MESSAGE) {
					sendResult(SignalRResult.UPDATE_MESSAGE, it[0].toString())
				}
				handle(CLIENT_METHOD_ON_OFF_NOTIFY) {
					sendResult(SignalRResult.ON_OFF_CONVERSATION_NOTIFY, it[0].asString)
				}
				handle(CLIENT_METHOD_NEW_MESSAGE_SYSTEM) {
					sendResult(SignalRResult.NEW_MESSAGE_SYSTEM, it[0].toString())
				}
				handle(CLIENT_METHOD_LEAVE_GROUP) {
					val leaveGroup = LeaveGroup(
						it[0].asString,
						it[1].asString
					)
					sendResult(SignalRResult.LEAVE_GROUP, _gson.toJson(leaveGroup))
				}
				handle(CLIENT_METHOD_CONVERSATION_DELETE_MEMBER) {
					val leaveGroup = LeaveGroup(
						it[0].asString,
						it[1].asString,
						_gson.fromJson(it[2].toString(), Conversation::class.java)
					)
					sendResult(
						SignalRResult.UPDATE_CONVERSATION_DELETE_MEMBER,
						_gson.toJson(leaveGroup)
					)
				}
				handle(CLIENT_METHOD_CONVERSATION_ROLE) {
					val updateRole = UpdateRole(it[0].asString, it[1].asString, it[2].asInt)
					sendResult(SignalRResult.UPDATE_CONVERSATION_ROLE, _gson.toJson(updateRole))
				}
				handle(CLIENT_METHOD_REFRESH_MESSAGE) {
					sendResult(SignalRResult.NEW_REACTION, it[0].toString())
				}
				handle(CLIENT_METHOD_CREATED_CONVERSATION) {
					sendResult(SignalRResult.CREATE_CONVERSATION, it[0].toString())
				}
				handle(CLIENT_METHOD_DELETE_GROUP) {
					sendResult(SignalRResult.DELETE_GROUP, it[0].asString)
				}
				handle(CLIENT_METHOD_CONVERSATION_SETTING) {
					val setting = Setting(
						it[0].asString,
						it[1].asBoolean,
						it[2].asBoolean,
						it[3].asBoolean,
						it[4].asBoolean
					)
					sendResult(
						SignalRResult.UPDATE_CONVERSATION_SETTING,
						_gson.toJson(setting)
					)
				}
				handle(CLIENT_METHOD_CONVERSATION_INFORM) {
					sendResult(SignalRResult.UPDATE_CONVERSATION_INFORM, it[0].toString())
				}
				handle(CLIENT_METHOD_NEW_MESSAGE) {
					sendResult(SignalRResult.NEW_MESSAGE, it[0].toString())
				}
				handle(CLIENT_METHOD_CREATED_MESSAGE) {
					sendResult(SignalRResult.SEND_MESSAGE, it[0].toString())
				}
				handle(CLIENT_METHOD_LAST_TIME_READ) {
					sendResult(SignalRResult.LAST_TIME_READ, it[0].toString())
				}
				handle(CLIENT_METHOD_ADD_TO_GROUP) {
					invoke(SERVER_METHOD_ADD_TO_GROUP, it[0].asString)
				}
				handle(CLIENT_METHOD_TOTAL_UNREAD_CONVERSATION) {
					sendResult(SignalRResult.TOTAL_UNREAD_CONVERSATION, it[0].toString())
				}
				handle(CLIENT_METHOD_MESSAGE_DELETED_CONVERSATION) {
					sendResult(SignalRResult.DELETE_MESSAGE_CONVERSATION, it[0].asString)
				}
				handle(CLIENT_METHOD_MESSAGE_DELETED_MULTIPLE) {
					sendResult(SignalRResult.DELETE_MULTIPLE_MESSAGE, it[0].toString())
				}
				handle(CLIENT_METHOD_MESSAGE_DELETED) {
					sendResult(SignalRResult.DELETE_SINGLE_MESSAGE, it[0].toString())
				}
				handle(CLIENT_METHOD_ADD_MEMBER) {
					val addMember = AddMember(
						it[0].asString,
						_gson.fromJson(
							it[2].toString(),
							object : TypeToken<ArrayList<Member?>?>() {}.type
						)
					)
					sendResult(SignalRResult.ADD_MEMBER, _gson.toJson(addMember))
				}
				handle(CLIENT_METHOD_APPROVE_MEMBER) {
					val joinGroup = JoinGroup(
						it[0].asString,
						it[1].asBoolean,
						it[2].asString
					)
					sendResult(SignalRResult.APPROVED_MEMBER, _gson.toJson(joinGroup))
				}
				handle(CLIENT_METHOD_PIN_CONVERSATION) {
					val pin = PinConversation(
						it[1].asBoolean,
						_gson.fromJson(it[0].toString(), Conversation::class.java)
					)
					sendResult(SignalRResult.PIN_CONVERSATION, _gson.toJson(pin))
				}
			}
		}
	}

	inner class LocalBinder : Binder() {
		val service: SignalRService
			get() = this@SignalRService
	}
}

fun HubProxy.handle(vararg keys: String, block: (it: Array<JsonElement>) -> Any) {
	for (key: String in keys) {
		subscribe(key).addReceivedHandler {
			if (!it.isNullOrEmpty()) {
				block(it)
			}
		}
	}
}