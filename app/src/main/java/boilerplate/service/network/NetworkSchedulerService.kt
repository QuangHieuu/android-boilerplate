package boilerplate.service.network

import android.annotation.SuppressLint
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import androidx.annotation.RequiresApi
import boilerplate.utils.InternetManager

@SuppressLint("SpecifyJobSchedulerIdRange")
abstract class NetWorkJobService : JobService(), ConnectivityReceiver.ConnectivityReceiverListener

class NetworkSchedulerService : NetWorkJobService() {
	private var _receiver: ConnectivityReceiver? = null
	private var _isRegister = false
	private var _lastConnected = true

	override fun onCreate() {
		super.onCreate()
		_receiver = ConnectivityReceiver(this)
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		return START_NOT_STICKY
	}

	override fun onStartJob(params: JobParameters?): Boolean {
		if (!_isRegister) {
			_isRegister = true
			registerReceiver(_receiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
		}
		return true
	}

	override fun onStopJob(params: JobParameters?): Boolean {
		if (_isRegister) {
			_isRegister = false
			unregisterReceiver(_receiver)
		}
		return true
	}

	override fun onNetworkConnectionChanged() {
		with(InternetManager.isConnected()) {
			if (this) {
			} else {
			}
			_lastConnected = this
		}
	}

	@RequiresApi(Build.VERSION_CODES.P)
	override fun onNetworkChanged(params: JobParameters) {
	}
}