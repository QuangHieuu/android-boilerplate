package boilerplate.utils.extension

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import boilerplate.service.signalr.SignalRManager
import boilerplate.service.signalr.SignalRResult
import com.google.gson.Gson

fun <T> ArrayList<T>?.ifEmpty(): ArrayList<T> = this ?: arrayListOf()

inline fun <T : Any> T?.notNull(f: (it: T) -> Unit) {
    if (this != null) f(this)
}

inline fun <T : Any> T?.isNull(f: () -> Unit) {
    if (this == null) f()
}

val <T> List<T>.lastIndex: Int
    get() = size - 1


fun MutableList<Any>.swap(index1: Int, index2: Int) {

    val tmp = this[index1]

    this[index1] = this[index2]

    this[index2] = tmp

}

operator fun <T> MutableLiveData<MutableList<T>>.plusAssign(values: List<T>) {
    val value = this.value ?: arrayListOf()
    value.addAll(values)
    this.value = value
}


fun <T> String.convertJsonToObject(classType: Class<T>): T {
    return Gson().fromJson(this, classType)
}

fun Context.sendResult(key: SignalRResult?, value: String) {
    key.notNull {
        Intent(SignalRManager.INTENT_FILTER_SIGNALR)
            .apply {
                putExtra(SignalRManager.SIGNALR_BUNDLE, Bundle().apply {
                    putString(SignalRManager.SIGNALR_KEY, it.key)
                    putString(SignalRManager.SIGNALR_DATA, value)
                })
            }
            .let { LocalBroadcastManager.getInstance(this).sendBroadcast(it) }
    }
}

@SuppressLint("InternalInsetResource", "DiscouragedApi")
fun AppCompatActivity.statusBarHeight(): Int {
    val resourceId: Int = getResources().getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        val statusBarHeight: Int = getResources().getDimensionPixelSize(resourceId)
        return statusBarHeight
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowInsets: WindowInsets = window.decorView.getRootWindowInsets()
        return windowInsets.getInsets(WindowInsets.Type.statusBars()).top
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val windowInsets: WindowInsets = window.decorView.getRootWindowInsets()
        val displayCutout = windowInsets.displayCutout
        if (displayCutout != null) {
            return displayCutout.safeInsetTop
        }
        return 0
    } else {
        val decorView: View = window.decorView
        val insets = decorView.rootWindowInsets
        if (insets != null) {
            return insets.systemWindowInsetTop
        }
    }
    return 0
}

fun Context.isAppInBackground(): Boolean {
    var isInBackground = true
    val runningProcesses =
        (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).runningAppProcesses
    for (processInfo in runningProcesses) {
        if (processInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
            for (activeProcess in processInfo.pkgList) {
                if (activeProcess == packageName) {
                    isInBackground = false
                }
            }
        }
    }
    return isInBackground
}

fun Context.isTablet(): Boolean = Configuration.SCREENLAYOUT_SIZE_LARGE <=
        (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK)

fun Float.toTextSize(): Float {
    val displayMetrics = Resources.getSystem().displayMetrics
    return this / displayMetrics.density
}