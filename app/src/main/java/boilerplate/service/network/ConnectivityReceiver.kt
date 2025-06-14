package boilerplate.service.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

open class ConnectivityReceiver(
	private val _listener: ConnectivityReceiverListener
) : BroadcastReceiver() {

	override fun onReceive(context: Context, intent: Intent) {
		_listener.onNetworkConnectionChanged()
	}

	interface ConnectivityReceiverListener {

		fun onNetworkConnectionChanged()
	}
}
