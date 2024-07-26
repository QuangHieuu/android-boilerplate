package boilerplate.utils

import android.os.Handler
import android.os.Looper
import android.view.View
import boilerplate.utils.extension.ANIMATION_DELAY

open class ClickUtil(
    private var longClick: Long,
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
            time: Long = ANIMATION_DELAY,
            block: (v: View) -> Unit
        ): View.OnClickListener = ClickUtil(time, object : OnClick {
            override fun onClick(v: View) {
                block(v)
            }
        })

        private val mHandler = Handler(Looper.getMainLooper())
        private val mClickLockRunnable = Runnable { sIsLocked = false }
        private var sIsLocked = false

        @Synchronized
        fun isLocked(longClick: Long): Boolean {
            if (sIsLocked) return true
            mHandler.postDelayed(mClickLockRunnable, longClick)
            sIsLocked = true
            return false
        }
    }
}
