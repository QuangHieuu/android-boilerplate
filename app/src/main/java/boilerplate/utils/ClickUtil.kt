package boilerplate.utils

import android.os.Handler
import android.os.Looper
import android.view.View

open class ClickUtil(
    private var longClick: Int,
    private val mListener: OnClick
) : View.OnClickListener {
    interface OnClick {
        fun onClick(v: View)
    }

    override fun onClick(v: View) {
        if (!isLocked(longClick)) {
            mListener.onClick(v)
        }
    }

    companion object {
        fun onClick(
            time: Int = CLICK_LOCK_INTERVAL,
            block: (v: View) -> Unit
        ): View.OnClickListener = ClickUtil(time, object : OnClick {
            override fun onClick(v: View) {
                block(v)
            }
        })

        private const val CLICK_LOCK_INTERVAL = 800
        private val mHandler = Handler(Looper.getMainLooper())
        private val mClickLockRunnable = Runnable { sIsLocked = false }
        private var sIsLocked = false

        @Synchronized
        fun isLocked(longClick: Int): Boolean {
            if (sIsLocked) return true
            mHandler.postDelayed(mClickLockRunnable, longClick.toLong())
            sIsLocked = true
            return false
        }
    }
}
