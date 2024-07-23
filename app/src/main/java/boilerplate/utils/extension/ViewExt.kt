package boilerplate.utils.extension

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import boilerplate.R
import boilerplate.constant.AccountManager
import boilerplate.constant.Constants.KEY_AUTH
import boilerplate.databinding.ViewToastBinding
import boilerplate.model.file.ExtensionType
import boilerplate.utils.ClickUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.Headers
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_SLIDE
import com.google.android.material.snackbar.Snackbar


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

@SuppressLint("RestrictedApi")
fun View.showSnackBar(
    @StringRes message: Int = R.string.no_text,
    @ColorRes color: Int = R.color.color_toast
) = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).apply {
    animationMode = ANIMATION_MODE_SLIDE
    view.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
    val binding =
        ViewToastBinding.inflate(LayoutInflater.from(context), rootView as ViewGroup, false)
    with(binding) {
        lnToast.setBackgroundColor(ContextCompat.getColor(context, color))
        tvToast.setText(message)
    }
    val layout = view as Snackbar.SnackbarLayout
    layout.addView(binding.root)
}.show()

@SuppressLint("RestrictedApi")
fun View.showSnackBar(
    message: String = context.getString(R.string.no_text),
    @ColorRes color: Int = R.color.color_toast
) = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).apply {
    animationMode = ANIMATION_MODE_SLIDE
    view.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
    val binding =
        ViewToastBinding.inflate(LayoutInflater.from(context), rootView as ViewGroup, false)
    with(binding) {
        lnToast.setBackgroundColor(ContextCompat.getColor(context, color))
        tvToast.text = message
    }
    val layout = view as Snackbar.SnackbarLayout
    layout.addView(binding.root)
}.show()

fun View.showSnackBarSuccess(
    message: String = ""
) = showSnackBar(
    message,
    R.color.color_toast_success
)

fun View.showSnackBarSuccess(
    @StringRes message: Int = R.string.no_text
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

fun View.showSnackBarFail(
    @StringRes message: Int = R.string.no_text
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

fun View.showSnackBarWarning(
    @StringRes message: Int = R.string.no_text
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

fun ImageView.loadImage(
    url: String? = "",
    accessToken: String? = AccountManager.getToken(),
    type: String? = "",
    requestOptions: RequestOptions? = null
) {
    if (url.isNullOrEmpty()) {
        return
    }

    val loading: CircularProgressDrawable = CircularProgressDrawable(context).apply {
        setColorSchemeColors(ContextCompat.getColor(context, R.color.color_1552DC))
        setCenterRadius(25f)
        setStrokeWidth(5f)
    }

    val isGif = ExtensionType.isFileGIF(type)

    val context = context
    val glideUrl = GlideUrl(url, accessToken?.let {
        LazyHeaders.Builder()
            .addHeader(KEY_AUTH, it)
            .build()
    } ?: Headers.DEFAULT)

    val request = (requestOptions ?: RequestOptions().error(R.drawable.ic_avatar))
        .placeholder(loading)

    if (isGif) {
        Glide
            .with(context)
            .asGif()
            .load(glideUrl)
            .apply(request)
            .into(object : CustomTarget<GifDrawable>() {
                override fun onLoadStarted(placeholder: Drawable?) {
                    setImageDrawable(placeholder)
                    loading.start()
                }

                override fun onResourceReady(
                    resource: GifDrawable,
                    transition: Transition<in GifDrawable>?
                ) {
                    setImageDrawable(resource)
                    resource.start()
                    loading.stop()
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    setImageResource(R.color.colorWhite)
                    loading.stop()
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    setImageDrawable(errorDrawable)
                    loading.stop()
                }
            })
    }
    Glide
        .with(context)
        .asDrawable()
        .load(glideUrl)
        .apply(request)
        .into(object : CustomTarget<Drawable>() {
            override fun onLoadStarted(placeholder: Drawable?) {
                setImageDrawable(placeholder)
                loading.start()
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                setImageDrawable(errorDrawable)
                loading.stop()
            }

            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                setImageDrawable(resource)
                loading.stop()
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                setImageDrawable(placeholder)
                loading.stop()
            }
        })
}

fun View.click(block: ((v: View) -> Unit)?) {
    block.notNull { setOnClickListener(ClickUtil.onClick { v -> it(v) }) }
}

//fun View.clicks(isCheckNetwork: Boolean): Observable<View> {
//    val source: ObservableOnSubscribe<View> = ObservableOnSubscribe { emitter ->
//        emitter.setCancellable {
//            setOnClickListener(null)
//            emitter.onComplete()
//        }
//
//        setOnClickListener {
//            val isConnected = InternetManager.isConnected()
//            if (isCheckNetwork && !isConnected) {
//                val errorMessage = context.getString(R.string.error_internet_connect)
//                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
//                return@setOnClickListener
//            }
//            emitter.onNext(this)
//        }
//    }
//    return Observable.create(source).throttleFirst(
//        1, TimeUnit.SECONDS,
//        AndroidSchedulers.mainThread()
//    )
//}

fun View.removeSelf() {
    val parent = parent as? ViewGroup ?: return
    parent.removeView(this)
}

fun View.addTo(parent: ViewGroup?) {
    parent ?: return
    parent.addView(
        this,
        ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    )
}

inline fun View.launch(delay: Long = 0, crossinline r: () -> Unit) {
    synchronized(this) {
        Handler(Looper.getMainLooper()).postDelayed({ r() }, delay)
    }
}
