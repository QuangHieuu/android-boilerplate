package boilerplate.service.signalr

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import boilerplate.service.signalr.SignalRService.Companion.TAG
import boilerplate.utils.extension.notNull

class SignalRReceiver(private val app: Application) : BroadcastReceiver() {
    companion object {
        const val INTENT_FILTER = "INTENT_FILTER_SIGNALR"
        const val SIGNALR_BUNDLE = "SIGNALR_BUNDLE"
        const val SIGNALR_KEY = "SIGNALR_KEY"
        const val SIGNALR_DATA = "SIGNALR_DATA"
    }

    fun register() {
        LocalBroadcastManager.getInstance(app)
            .registerReceiver(this, IntentFilter(INTENT_FILTER))
    }

    fun unregister() {
        LocalBroadcastManager.getInstance(app).unregisterReceiver(this)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent.notNull {
            val bundle: Bundle = it.getBundleExtra(SIGNALR_BUNDLE) ?: Bundle()
            val key = bundle.getString(SIGNALR_KEY, "")
            val data = bundle.getString(SIGNALR_DATA, "")
            if (key.isNotEmpty()) {
                Log.d(TAG, "Key: $key")
                when (SignalRResult.fromKey(key)) {
                    SignalRResult.CONNECTED -> {}
                    SignalRResult.DISCONNECTED -> {}
                    SignalRResult.READY_TO_CHAT -> {}
                    SignalRResult.CREATE_CONVERSATION -> {}
                    SignalRResult.SEND_MESSAGE -> {}
                    SignalRResult.NEW_MESSAGE -> {}
                    SignalRResult.UPDATE_MESSAGE -> {}
                    SignalRResult.NEW_MESSAGE_SYSTEM -> {}
                    SignalRResult.NEW_REACTION -> {}
                    SignalRResult.LAST_TIME_READ -> {}
                    SignalRResult.ADD_TO_GROUP -> {}
                    SignalRResult.TOTAL_UNREAD_CONVERSATION -> {}
                    SignalRResult.READ_ALL_CONVERSATION -> {}
                    SignalRResult.DELETE_SINGLE_MESSAGE -> {}
                    SignalRResult.DELETE_MULTIPLE_MESSAGE -> {}
                    SignalRResult.DELETE_MESSAGE_CONVERSATION -> {}
                    SignalRResult.LEAVE_GROUP -> {}
                    SignalRResult.ADD_MEMBER -> {}
                    SignalRResult.APPROVED_MEMBER -> {}
                    SignalRResult.DELETE_GROUP -> {}
                    SignalRResult.UPDATE_CONVERSATION_SETTING -> {}
                    SignalRResult.UPDATE_CONVERSATION_INFORM -> {}
                    SignalRResult.UPDATE_CONVERSATION_ROLE -> {}
                    SignalRResult.UPDATE_CONVERSATION_DELETE_MEMBER -> {}
                    SignalRResult.ON_OFF_CONVERSATION_NOTIFY -> {}
                    SignalRResult.DISABLE_CONVERSATION -> {}
                    SignalRResult.PIN_MESSAGE -> {}
                    SignalRResult.REMOVE_PIN_MESSAGE -> {}
                    SignalRResult.IMPORTANT_CONVERSATION -> {}
                    SignalRResult.SEEN_MESSAGE -> {}
                    else -> {

                    }
                }
            }
        }
    }
}