package boilerplate.utils.extension

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import boilerplate.R
import boilerplate.constant.AccountManager
import boilerplate.constant.Constants.KEY_AUTH
import boilerplate.utils.ClickUtil
import boilerplate.utils.InternetManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.Headers
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_SLIDE
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import java.util.concurrent.TimeUnit


fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.isVisible(): Boolean {
    return visibility == View.VISIBLE
}

fun View.showSnackBar(
    @StringRes message: Int = R.string.no_text,
    @ColorRes color: Int = R.color.color_toast
) = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).apply {
    view.setBackgroundColor(ContextCompat.getColor(context, color))
}.show()

fun View.showSnackBar(
    message: String = context.getString(R.string.no_text),
    @ColorRes color: Int = R.color.color_toast
) = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).apply {
    animationMode = ANIMATION_MODE_SLIDE
    view.setBackgroundColor(ContextCompat.getColor(context, color))
}.show()

fun View.showSnackBarSuccess(
    message: String = ""
) = showSnackBar(
    message,
    R.color.color_toast_success
)

fun View.showSnackBarFail(
    message: String = ""
) = showSnackBar(
    message,
    R.color.color_toast_fail
)

fun View.showSnackBarWarning(
    message: String = ""
) = showSnackBar(
    message,
    R.color.color_toast_warning
)

fun WebView.loadWebViewUrl(url: String?, progressBar: ProgressBar?) {
    if (url.isNullOrEmpty()) return
    if (progressBar == null) {
        webViewClient = WebViewClient()
    } else {
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
            }
        }
    }
    with(settings) {
        scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
    }
    loadUrl(url)
}

fun View.clicks(isCheckNetwork: Boolean): Observable<View> {
    val source: ObservableOnSubscribe<View> = ObservableOnSubscribe { emitter ->
        emitter.setCancellable {
            setOnClickListener(null)
            emitter.onComplete()
        }

        setOnClickListener {
            val isConnected = InternetManager.isConnected()
            if (isCheckNetwork && !isConnected) {
                val errorMessage = context.getString(R.string.error_internet_connect)
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            emitter.onNext(this)
        }
    }
    return Observable.create(source).throttleFirst(
        1, TimeUnit.SECONDS,
        AndroidSchedulers.mainThread()
    )
}

fun EditText.hideKeyboard() {
    with(this) {
        clearFocus()
        val imm =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}

fun EditText.showKeyboard() {
    with(this) {
        isFocusable = true
        isFocusableInTouchMode = true
        if (requestFocus()) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}

fun ImageView.loadImage(url: String? = "", accessToken: String? = AccountManager.getToken()) {
    if (url.isNullOrEmpty()) {
        return
    }

    val context = context
    val glideUrl = GlideUrl(url, accessToken?.let {
        LazyHeaders.Builder()
            .addHeader(KEY_AUTH, it)
            .build()
    } ?: Headers.DEFAULT)

    val request = RequestOptions()
        .error(R.drawable.ic_avatar)
        .placeholder(R.drawable.ic_avatar)

    Glide
        .with(context)
        .asDrawable()
        .load(glideUrl)
        .apply(request)
        .into(object : CustomTarget<Drawable>() {
            override fun onLoadFailed(errorDrawable: Drawable?) {
                setImageDrawable(errorDrawable)
            }

            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                setImageDrawable(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                setImageDrawable(placeholder)
            }
        })
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

fun View.setClick(v: (v: View) -> Unit) {
    setOnClickListener(ClickUtil.onClick { v(it) })
}