package boilerplate.utils.extension

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.text.Layout
import android.text.StaticLayout
import android.view.*
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.LinearInterpolator
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnRepeat
import androidx.core.animation.doOnResume
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import boilerplate.R
import boilerplate.constant.Constants.KEY_AUTH
import boilerplate.databinding.ViewToastBinding
import boilerplate.model.file.ExtensionType
import boilerplate.utils.ClickUtil
import boilerplate.utils.ImageUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.Headers
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_SLIDE
import com.google.android.material.snackbar.Snackbar


fun View.show(b: Boolean = true) {
	visibility = if (b) View.VISIBLE else View.GONE
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
	message: String? = context.getString(R.string.no_text),
	@ColorRes color: Int = R.color.color_toast
) = Snackbar.make(rootView, message ?: "", Snackbar.LENGTH_SHORT).apply {
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

fun View.showSuccess(
	message: String? = ""
) = showSnackBar(
	message,
	R.color.color_toast_success
)

fun View.showSuccess(
	@StringRes message: Int = R.string.no_text
) = showSnackBar(
	message,
	R.color.color_toast_success
)

fun View.showFail(
	message: String? = ""
) = showSnackBar(
	message,
	R.color.color_toast_fail
)

fun View.showFail(
	@StringRes message: Int = R.string.no_text
) = showSnackBar(
	message,
	R.color.color_toast_fail
)

fun View.showWarning(
	message: String? = ""
) = showSnackBar(
	message,
	R.color.color_toast_warning
)

fun View.showWarning(
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

fun ImageView.loadAvatar(url: String? = "") {
	loadImage(
		url, requestOptions = RequestOptions()
			.override(ImageUtil.AVATAR_MAX_SIZE)
			.placeholder(R.drawable.ic_avatar)
			.error(R.drawable.ic_avatar)
			.circleCrop()
	)
}

fun ImageView.loadImage(
	url: String? = "",
	accessToken: String? = "",
	type: String? = "",
	requestOptions: RequestOptions = RequestOptions().error(R.drawable.bg_error)
) {
	if (url.isNullOrEmpty()) {
		return
	}
	val isGif = ExtensionType.isFileGIF(type)

	val context = context
	val glideUrl = GlideUrl(url, accessToken?.let {
		LazyHeaders.Builder()
			.addHeader(KEY_AUTH, it)
			.build()
	} ?: Headers.DEFAULT)

	if (isGif) {
		Glide
			.with(context)
			.asGif()
			.load(glideUrl)
			.apply(requestOptions)
			.into(this)
	} else {
		Glide
			.with(context)
			.asDrawable()
			.load(glideUrl)
			.apply(requestOptions)
			.into(this)
	}
}

fun <T : View> T.click(function: ((v: View) -> Unit)?): T {
	function.notNull { setOnClickListener(ClickUtil.onClick(block = it)) }
	return this
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

fun View.performClickOn(view: View) {
	setOnClickListener { view.performClick() }
}

fun PopupWindow.dimBehind() {
	val container = contentView.rootView
	val context = contentView.context
	val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
	val p = container.layoutParams as WindowManager.LayoutParams
	p.flags =
		WindowManager.LayoutParams.FLAG_DIM_BEHIND or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
	p.dimAmount = 0.3f
	wm.updateViewLayout(container, p)
}

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
	postDelayed({ r() }, delay)
}

fun DialogFragment.setWidthPercent(widthPercent: Int = 60, heightPercent: Int = 60) {
	try {
		val w = widthPercent.toFloat() / 100
		val h = heightPercent.toFloat() / 100
		val dm = Resources.getSystem().displayMetrics
		val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
		val width = rect.width() * w
		val height = rect.height() * h
		dialog?.let { it.window?.setLayout(width.toInt(), height.toInt()) }
	} catch (_: Exception) {
	}
}

inline fun <T : ViewBinding> ViewGroup.viewBinding(factory: (LayoutInflater, ViewGroup, Boolean) -> T) =
	factory(LayoutInflater.from(context), this, false)

/**
 *
 * @param [listString] default use [TextView.getText] to animation, can pass [String] or [Array]
 * @param [container] default use self to calculate width and height of text in view
 * @param [isFitHeight] default false for calculator width and height of text
 * @param [mode] The type of repetition that will occur when repeatMode is nonzero.
 * [ValueAnimator.RESTART] means the animation will start from the beginning on every new cycle.
 * [ValueAnimator.REVERSE] means the animation will reverse directions on each iteration.
 * @param[count] Sets how many times the animation should be repeated. If the repeat count is 0, the animation is never repeated.
 * If the repeat count is greater than 0 or [ValueAnimator.INFINITE], the repeat mode will be taken into account.
 *
 */
fun TextView.textWriter(
	vararg listString: CharSequence = arrayOf(text),
	container: View = this,
	isFitHeight: Boolean = false,
	mode: Int = ValueAnimator.RESTART,
	count: Int = 0,
	delay: Long = 25L,
	period: Long = 2000L,
): ValueAnimator {
	hide()
	if (isFitHeight) {

		val frameLp = container.layoutParams as MarginLayoutParams
		val frameHorizontalMargin = frameLp.marginStart + frameLp.marginEnd

		val textLp = layoutParams as MarginLayoutParams
		val textHorizontalMargin = textLp.marginStart + textLp.marginEnd

		val frameHorizontalPadding = container.paddingStart + container.paddingEnd
		val textHorizontalPadding = paddingStart + paddingEnd

		val space = frameHorizontalMargin
			.plus(textHorizontalMargin)
			.plus(frameHorizontalPadding)
			.plus(textHorizontalPadding)

		val widthScreen = Resources.getSystem().displayMetrics.widthPixels.minus(space)

		val alignment = when (gravity) {
			Gravity.CENTER -> Layout.Alignment.ALIGN_CENTER
			Gravity.END -> Layout.Alignment.ALIGN_OPPOSITE
			else -> Layout.Alignment.ALIGN_NORMAL
		}

		val longestText = listString.maxBy { it.length }

		val textVerticalPadding = paddingTop + paddingBottom
		val frameVerticalPadding = container.paddingTop + container.paddingBottom

		// calculate height use StaticLayout
		val textHeight = StaticLayout.Builder
			.obtain(longestText, 0, longestText.length, paint, widthScreen)
			.setLineSpacing(lineSpacingExtra, lineSpacingMultiplier)
			.setAlignment(alignment)
			.build().height
			.plus(textVerticalPadding)
			.plus(frameVerticalPadding)

		container.layoutParams.height = textHeight
	}

	var currentPosition = 0
	var currentText = listString[currentPosition]
	var textLength = currentText.length

	fun setData() {
		if (listString.isEmpty()) return
		currentPosition += 1
		if (currentPosition >= listString.size) {
			currentPosition = 0
		}
		currentText = listString[currentPosition]
		textLength = currentText.length
	}

	fun calculatorDuration(): Long {
		return if (textLength % 2 == 0) {
			textLength
		} else {
			textLength + 1
		} * delay
	}

	return ValueAnimator.ofInt(0, textLength).apply {
		interpolator = LinearInterpolator()
		duration = calculatorDuration()
		repeatMode = mode
		repeatCount = if (mode == ValueAnimator.RESTART) 0 else count
		addUpdateListener {
			val process = it.animatedValue as Int
			val animatedText = currentText.subSequence(0, process)
			this@textWriter.text = animatedText
		}
		doOnStart {
			if (!isShown) {
				show()
			}
		}
		when (mode) {
			ValueAnimator.RESTART -> {
				doOnEnd {
					setData()
					duration = calculatorDuration()
					setIntValues(0, textLength)
					postDelayed({
						hide()
						start()
					}, period)
				}
			}

			ValueAnimator.REVERSE -> {
				var isRunReverse = false
				doOnResume { isRunReverse = true }
				doOnRepeat {
					if (isRunReverse) {
						cancel()
						setData()
						duration = calculatorDuration()
						setIntValues(0, textLength)
						isRunReverse = false
						postDelayed({
							hide()
							start()
						}, period)
					} else {
						pause()
						postDelayed({ resume() }, period)
					}
				}
			}
		}
	}
}