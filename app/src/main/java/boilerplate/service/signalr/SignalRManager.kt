package boilerplate.service.signalr

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import boilerplate.utils.extension.notNull

object SignalRManager : ServiceConnection {
    private val _lock = Any()
    private var _service: SignalRService? = null
    private var _isBound = false
    private var _isRunning = false

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
            }
        }
    }

    private fun checkService(r: (service: SignalRService) -> Unit) {
        _service.notNull { if (_isBound) r(it) }
    }
}
