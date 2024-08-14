package boilerplate.service.signalr

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import boilerplate.model.conversation.Conversation
import boilerplate.model.message.Message
import boilerplate.model.user.UserSignalR
import boilerplate.service.signalr.SignalRService.Companion.TAG
import boilerplate.utils.extension.notNull
import com.google.gson.JsonSyntaxException
import java.util.Collections
import java.util.Date

object SignalRManager : ServiceConnection {

	const val INTENT_FILTER_SIGNALR = "INTENT_FILTER_SIGNALR"
	const val SIGNALR_BUNDLE = "SIGNALR_BUNDLE"
	const val SIGNALR_KEY = "SIGNALR_KEY"
	const val SIGNALR_DATA = "SIGNALR_DATA"

	private val _lock = Any()
	private var _service: SignalRService? = null
	private var _isBound = false
	private var _isRunning = false

	private val _subscriptions = Collections.synchronizedMap<String, SubscriptionSignalr>(HashMap())

	private var _receiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			intent.notNull { handleData(it) }
		}
	}

	override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
		if (!_isBound) {
			val binder: SignalRService.LocalBinder = service as SignalRService.LocalBinder
			binder.service.let {
				_service = it
				_service?.start()
			}
			_isBound = true
			_isRunning = true
		}
	}

	override fun onServiceDisconnected(componentName: ComponentName) {
		_isBound = false
		_isRunning = false

		_service?.remove()
		_service = null
	}

	fun serviceConnect(context: Context, intent: Intent) {
		if (!_isRunning) {
			context.bindService(
				intent,
				this@SignalRManager,
				Context.BIND_AUTO_CREATE or Context.BIND_ADJUST_WITH_ACTIVITY
			)
			LocalBroadcastManager.getInstance(context)
				.registerReceiver(_receiver, IntentFilter(INTENT_FILTER_SIGNALR))
		}
	}

	fun reconnectSignal() {
		synchronized(_lock) {
			checkService { it.reconnect() }
		}
	}

	fun stopSignal() {
		synchronized(_lock) { checkService { it.stop() } }
	}

	fun unbindServices(context: Context, intent: Intent?) {
		synchronized(_lock) {
			if (_isRunning) {
				context.stopService(intent)
				context.unbindService(this)
				_isBound = false
				_isRunning = false
				_service = null
				LocalBroadcastManager.getInstance(context).unregisterReceiver(_receiver)
				_subscriptions.clear()
			}
		}
	}

	fun addController(string: String): SubscriptionSignalr {
		val sub: SubscriptionSignalr
		if (_subscriptions.containsKey(string)) {
			sub = _subscriptions[string]!!
		} else {
			sub = SubscriptionSignalr()
			_subscriptions[string] = sub
		}
		return sub
	}

	fun removeController(string: String) {
		_subscriptions.remove(string)
	}

	private fun handleData(intent: Intent) {
		val bundle: Bundle = intent.getBundleExtra(SIGNALR_BUNDLE) ?: Bundle()
		val key = bundle.getString(SIGNALR_KEY, "")
		val data = bundle.getString(SIGNALR_DATA, "")
		if (key.isNotEmpty()) {
			Log.d(TAG, "Key: $key")
			for (keys in _subscriptions.keys) {
				try {
					_subscriptions[keys]?.receiver(key, data)
				} catch (e: JsonSyntaxException) {
					Log.d(TAG, "Key-error: $key")
					continue
				}
			}
		}
	}

	private fun checkService(r: (service: SignalRService) -> Unit) {
		_service.notNull { if (_isBound) r(it) }
	}


	fun sendMessage(message: Message, isSms: Boolean, isEmail: Boolean) {
		checkService { it.sendMessage(message, isSms, isEmail) }
	}

	fun sendLastTimeRead(conversationId: String, lastTimeRead: Date, count: Int, force: Boolean) {
		checkService { it.sendLastTimeRead(conversationId, lastTimeRead, count, force) }
	}

	fun leaveGroup(conversationId: String, isLeaveInSilent: Boolean) {
		checkService { it.leaveGroup(conversationId, isLeaveInSilent) }
	}

	fun deleteGroup(conversationId: String) {
		checkService { it.deleteGroup(conversationId) }
	}

	fun updateGroupSetting(conversation: Conversation) {
		checkService { it.updateConversationSetting(conversation) }
	}

	fun addMember(conversationId: String, member: ArrayList<UserSignalR>) {
		checkService { it.addMember(conversationId, member) }
	}

	fun deleteMember(conversationId: String?, user: String?) {
		checkService { it.deleteMember(conversationId, user) }
	}

	fun approveMemberOrNot(
		isApprove: Boolean,
		conversationId: String?,
		list: ArrayList<UserSignalR>
	) {
		checkService { it.approveMemberOrNot(isApprove, conversationId, list) }
	}

	fun updateMemberRole(userId: String, conversationId: String, role: Int) {
		checkService { it.assignConversationRole(userId, conversationId, role) }
	}

	fun turnNotifyConversation(conversationId: String, isOn: Boolean) {
		checkService { it.onOffConversationNotify(conversationId, isOn) }
	}

	fun disableConversation(id: String) {
		checkService { it.disableConversation(id) }
	}

	fun pinConversation(conversationId: String, isPin: Boolean) {
		checkService { it.pinConversation(conversationId, isPin) }
	}
}
