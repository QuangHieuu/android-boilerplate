package boilerplate.utils

import android.os.Handler
import android.os.Looper
import android.view.View
import boilerplate.constant.Constants.ANIMATION_DELAY

open class ClickUtil(
	private val longClick: Long,
	private val block: (v: View) -> Unit
) : View.OnClickListener {

	private val _handler = Handler(Looper.getMainLooper())
	private val _clickLockRunnable = Runnable { _isLocked = false }
	private var _isLocked = false

	@Synchronized
	fun isLocked(longClick: Long): Boolean {
		if (_isLocked) return true
		_handler.postDelayed(_clickLockRunnable, longClick)
		_isLocked = true
		return false
	}

	override fun onClick(v: View) {
		if (!isLocked(longClick)) {
			block(v)
		}
	}

	companion object {

		fun onClick(time: Long = ANIMATION_DELAY, block: (v: View) -> Unit): View.OnClickListener =
			ClickUtil(time, block)
	}
}