package gg.it.utils

import android.os.Handler
import android.view.View

class ClickUtil(private val mListener: OnClick) : View.OnClickListener {
    interface OnClick {
        fun onClick(v: View)
    }

    override fun onClick(v: View) {
        if (!isLocked) {
            mListener.onClick(v)
        }
    }

    companion object {
        fun onClick(block: (v: View) -> Unit): View.OnClickListener = ClickUtil(object : OnClick {
            override fun onClick(v: View) {
                block(v)
            }
        })

        private const val CLICK_LOCK_INTERVAL = 800
        private val mHandler = Handler()
        private val mClickLockRunnable = Runnable { sIsLocked = false }
        private var sIsLocked = false

        @get:Synchronized
        val isLocked: Boolean
            get() {
                if (sIsLocked) return true
                mHandler.postDelayed(mClickLockRunnable, CLICK_LOCK_INTERVAL.toLong())
                sIsLocked = true
                return false
            }

        @Synchronized
        fun isLocked(longClick: Int): Boolean {
            if (sIsLocked) return true
            mHandler.postDelayed(mClickLockRunnable, longClick.toLong())
            sIsLocked = true
            return false
        }
    }
}
