package boilerplate.service.signalr

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import boilerplate.data.local.repository.user.TokenRepository
import boilerplate.data.remote.service.ApiUrl
import boilerplate.model.conversation.Conversation
import boilerplate.model.conversation.ConversationUser
import boilerplate.model.conversation.ConversationUser.JoinGroup
import boilerplate.model.message.Message
import boilerplate.service.signalr.SignalRReceiver.Companion.INTENT_FILTER
import boilerplate.service.signalr.SignalRReceiver.Companion.SIGNALR_BUNDLE
import boilerplate.service.signalr.SignalRReceiver.Companion.SIGNALR_DATA
import boilerplate.service.signalr.SignalRReceiver.Companion.SIGNALR_KEY
import boilerplate.utils.InternetManager
import boilerplate.utils.extension.notNull
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
    }

    override fun onBind(intent: Intent): IBinder {
        create()
        return _binder
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
        _disposable.dispose()
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

    private fun sendResult(key: SignalRResult?, value: String) {
        key.notNull {
            Intent(INTENT_FILTER)
                .apply {
                    putExtra(SIGNALR_BUNDLE, Bundle().apply {
                        putString(SIGNALR_KEY, it.key)
                        putString(SIGNALR_DATA, value)
                    })
                }
                .let { intent ->
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                }
        }
    }

    private fun subscribe() {
        _chatProxy.notNull { proxy ->
            with(proxy) {
                handle(CLIENT_METHOD_READY_CHAT) {
                    sendResult(SignalRResult.READY_TO_CHAT, it[0].toString())
                }
                handle(CLIENT_METHOD_SEEN_MESSAGE) {
                    val seen: Conversation.SeenMessage = Conversation.SeenMessage(
                        it[0].asString,
                        it[1].asString
                    )
                    sendResult(SignalRResult.SEEN_MESSAGE, _gson.toJson(seen))
                }
                handle(CLIENT_METHOD_IMPORTANT_CONVERSATION) {
                    val pin = Conversation.Important(
                        it[0].toString(),
                        it[1].asBoolean
                    )
                    sendResult(SignalRResult.IMPORTANT_CONVERSATION, _gson.toJson(pin))
                }
                handle(CLIENT_METHOD_REMOVE_PIN_MESSAGE, CLIENT_METHOD_DELETE_PIN_MESSAGE) {
                    val pin = Message.Pin(
                        it[0].asString,
                        it[1].asString
                    )
                    sendResult(SignalRResult.REMOVE_PIN_MESSAGE, _gson.toJson(pin))
                }
                handle(CLIENT_METHOD_PIN_MESSAGE) {
                    val pin = Message.Pin(
                        it[0].asString,
                        _gson.fromJson(it[1].asJsonObject, Message::class.java)
                    )
                    sendResult(SignalRResult.REMOVE_PIN_MESSAGE, _gson.toJson(pin))
                }
                handle(CLIENT_METHOD_DISABLE_CONVERSATION) {
                    sendResult(SignalRResult.REMOVE_PIN_MESSAGE, it[0].asString)
                }
                handle(CLIENT_METHOD_UPDATE_MESSAGE) {
                    sendResult(SignalRResult.REMOVE_PIN_MESSAGE, it[0].toString())
                }
                handle(CLIENT_METHOD_ON_OFF_NOTIFY) {
                    sendResult(SignalRResult.ON_OFF_CONVERSATION_NOTIFY, it[0].asString)
                }
                handle(CLIENT_METHOD_NEW_MESSAGE_SYSTEM) {
                    sendResult(SignalRResult.NEW_MESSAGE_SYSTEM, it.get(0).toString())
                }
                handle(CLIENT_METHOD_LEAVE_GROUP) {
                    val leaveGroup = ConversationUser.LeaveGroup(
                        it[0].asString,
                        it[1].asString
                    )
                    sendResult(SignalRResult.LEAVE_GROUP, _gson.toJson(leaveGroup))
                }
                handle(CLIENT_METHOD_CONVERSATION_DELETE_MEMBER) {
                    val leaveGroup = ConversationUser.LeaveGroup(
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
                    val updateRole = ConversationUser.UpdateRole(
                        it[0].asString,
                        it[1].asString,
                        it[2].asInt
                    )
                    sendResult(
                        SignalRResult.UPDATE_CONVERSATION_ROLE,
                        _gson.toJson(updateRole)
                    )
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
                    val setting: Conversation.Setting = Conversation.Setting(
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
                    val addMember = Conversation.AddMember(
                        it[0].asString,
                        _gson.fromJson(
                            it[2].toString(),
                            object : TypeToken<ArrayList<ConversationUser?>?>() {
                            }.type
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