package boilerplate.utils.extension

import android.app.AlertDialog
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import boilerplate.R
import boilerplate.constant.Constants.KEY_AUTH
import boilerplate.utils.InternetManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.Headers
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_SLIDE
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import java.util.concurrent.TimeUnit

fun View.show(isShow: Boolean = true) {
    visibility = if (isShow) View.VISIBLE else View.INVISIBLE
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

fun ImageView.loadImage(url: String?, accessToken: String? = null) {
    val context = context
    val glideUrl = GlideUrl(url, accessToken?.let {
        LazyHeaders.Builder()
            .addHeader(KEY_AUTH, it)
            .build()
    } ?: Headers.DEFAULT)
    Glide
        .with(context)
        .asDrawable()
        .load(glideUrl)
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

fun Context.showDialog(@LayoutRes viewRes: Int, viewInit: (v: View) -> Unit) {
    val view = LayoutInflater.from(this).inflate(viewRes, null).also { viewInit(it) }
    val dialogBuilder = AlertDialog.Builder(this).apply {
        setView(view)
        setCancelable(false)
    }
    val dialog = dialogBuilder.create()
    dialog.show()
}

fun Window.statusBarHeight(): Int {
    return Rect().apply { decorView.getWindowVisibleDisplayFrame(this) }.top
}