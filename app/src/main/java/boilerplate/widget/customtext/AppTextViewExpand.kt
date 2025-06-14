package boilerplate.widget.customtext

import android.content.Context
import android.graphics.Typeface
import android.graphics.text.LineBreaker
import android.os.Build
import android.text.Layout
import android.text.Spanned
import android.text.util.Linkify
import android.util.AttributeSet
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.withStyledAttributes
import androidx.core.text.util.LinkifyCompat
import androidx.core.view.isGone
import boilerplate.R
import boilerplate.utils.extension.click
import boilerplate.utils.extension.toTextSize

open class AppTextViewExpand @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleAttr) {

	abstract class SimpleEvent : OnTextListener {

		override fun onReadMore(isCollapse: Boolean) {}
		override fun onMention(userId: String) {}
		override fun onViewIsExpand(isExpand: Boolean) {}
		override fun onPhoneNumber(number: String) {}
	}

	interface OnTextListener {

		fun onReadMore(isCollapse: Boolean)
		fun onMention(userId: String)
		fun onPhoneNumber(number: String)
		fun onViewIsExpand(isExpand: Boolean)
	}

	private val animAlphaStart = DEFAULT_ANIM_ALPHA_START

	private var maxLines = 0
	private var isShowText = false
	private var isEnableCollapse = false
	private var isNoClick = false
	private var isAnimating = false
	private var isCollapse = true
	private var isNeedReMeasure = true
	private var contentRealHeight = 0
	private var collapsedHeight = 0
	private var otherHeight = 0
	private var size = 0f

	private lateinit var typeface: Typeface
	private lateinit var tvContent: AppTextView
	private lateinit var tvLabel: AppTextView

	private var mListener: OnTextListener? = null

	init {
		initView(attrs)
	}

	private fun initView(attrs: AttributeSet?) {
		if (attrs != null) {
			context.withStyledAttributes(attrs, R.styleable.AppTextViewExpand, 0, 0) {
				maxLines = getInt(R.styleable.AppTextViewExpand_max_line, MAX_LINES_ON_SHRINK)
				isShowText = getBoolean(R.styleable.AppTextViewExpand_show_text_collapse, true)
				isEnableCollapse = getBoolean(R.styleable.AppTextViewExpand_enable_collapse, true)
				size = getDimension(
					R.styleable.AppTextViewExpand_android_textSize,
					R.dimen.dp_14.toFloat()
				).toTextSize()

				val typefaceAssetPath = getResourceId(
					R.styleable.AppTextViewExpand_customTypeface,
					R.font.roboto_regular
				)
				typeface = ResourcesCompat.getFont(context, typefaceAssetPath)!!
			}
		}
	}

	private fun init() {
		inflate(context, R.layout.textview_expandable, this)
		tvContent = findViewById(R.id.content)
		tvLabel = findViewById(R.id.moreLess)

		with(tvLabel) {
			textSize = size
			typeface = typeface
		}

		with(tvContent) {
			maxLines = 1
			textSize = size
			typeface = typeface
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
				breakStrategy = LineBreaker.BREAK_STRATEGY_SIMPLE
				hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_NORMAL
			}
			movementMethod = InternalLinkMovementMethod.newInstance()
				.setClick(object : InternalLinkMovementMethod.OnLinkListener {
					override fun onLinkClicked(textView: TextView, link: String): Boolean {
						return false
					}
				})
		}

		LinkifyCompat.addLinks(tvContent, Linkify.ALL)
		tvLabel.click { collapseText() }
	}

	override fun onFinishInflate() {
		super.onFinishInflate()
		init()
	}

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)
		if (isGone || !isNeedReMeasure) {
			return
		}
		isNeedReMeasure = false
		if (Patterns.WEB_URL.matcher(tvContent.text.toString()).matches()) {
			isNoClick = true
			tvLabel.visibility = GONE
			tvContent.maxLines = Int.MAX_VALUE
			mListener?.onViewIsExpand(false)
			return
		}
		if (tvContent.lineCount <= maxLines) {
			isNoClick = true
			tvLabel.visibility = GONE
			tvContent.maxLines = maxLines
			mListener?.onViewIsExpand(false)
			super.onMeasure(widthMeasureSpec, heightMeasureSpec)
		} else {
			isNoClick = false
			post { contentRealHeight = tvContent.measuredHeight }
			tvLabel.visibility = if (isShowText) VISIBLE else GONE
			if (isCollapse) {
				tvContent.maxLines = maxLines
			} else {
				tvContent.maxLines = Int.MAX_VALUE
			}
			if (mListener != null) {
				mListener?.onViewIsExpand(true)
			}
			super.onMeasure(widthMeasureSpec, heightMeasureSpec)
			if (isCollapse) {
				post {
					otherHeight =
						(if (isShowText) tvLabel.measuredHeight else 0) + paddingBottom + paddingTop
					collapsedHeight = measuredHeight
				}
			}
		}
	}

	override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
		return isAnimating
	}

	fun setShowText(showText: Boolean) {
		isShowText = showText
	}

	fun setMaxLine(value: Int) {
		maxLines = value
		tvContent.maxLines = value
		reMeasure()
	}

	fun setText(text: Spanned?) {
		tvContent.text = text
		reMeasure()
	}

	fun setText(text: CharSequence?) {
		tvContent.text = text!!
		reMeasure()
	}

	fun setText(text: Int) {
		tvContent.setText(text)
		reMeasure()
	}

	fun setEnableCollapse(b: Boolean) {
		isEnableCollapse = b
		reMeasure()
	}

	fun setTextSize(textSize: Float) {
		tvContent.textSize = textSize
		tvLabel.textSize = textSize
	}

	fun setTextColor(color: Int) {
		tvContent.setTextColor(color)
	}

	fun addListener(listener: SimpleEvent?) {
		mListener = listener
	}

	val isExpandable: Boolean
		get() = tvContent.lineCount > maxLines

	fun openUserId(userId: String) {
		mListener?.onMention(userId)
	}

	fun collapseText() {
		if (isNoClick) {
			return
		}
		if (!isEnableCollapse) {
			return
		}
		isCollapse = !isCollapse
		val animation: ExpandCollapseAnimation = if (isCollapse) {
			tvLabel.setText(R.string.show_more)
			ExpandCollapseAnimation(
				this,
				tvContent,
				height + collapsedHeight - otherHeight,
				collapsedHeight,
				otherHeight
			)
		} else {
			tvLabel.setText(R.string.collapse)
			ExpandCollapseAnimation(
				this,
				tvContent,
				height,
				height + contentRealHeight - otherHeight,
				otherHeight
			)
		}
		clearAnimation()
		animation.fillAfter = true
		animation.setAnimationListener(object : Animation.AnimationListener {
			override fun onAnimationStart(animation: Animation) {
				isAnimating = true
			}

			override fun onAnimationEnd(animation: Animation) {
				isAnimating = false
				if (isCollapse) {
					tvContent.maxLines = maxLines
				} else {
					tvContent.maxLines = Int.MAX_VALUE
				}
				if (mListener != null) {
					mListener?.onReadMore(isCollapse)
				}
				reMeasure()
				clearAnimation()
			}

			override fun onAnimationRepeat(animation: Animation) {}
		})
		startAnimation(animation)
	}

	private fun reMeasure() {
		isNeedReMeasure = true
		post {
			clearAnimation()
			requestLayout()
		}
	}

	protected class ExpandCollapseAnimation(
		private val main: View,
		private val tv: AppTextView?,
		private val startValue: Int,
		private val endValue: Int,
		private val otherHeight: Int
	) : Animation() {

		init {
			duration = 200
			interpolator = AccelerateDecelerateInterpolator()
		}

		override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
			super.applyTransformation(interpolatedTime, t)
			val height = ((endValue - startValue) * interpolatedTime + startValue).toInt()
			tv?.maxHeight = height - otherHeight
			main.requestLayout()
		}

		override fun willChangeBounds(): Boolean {
			return true
		}
	}

	companion object {

		private const val DEFAULT_ANIM_ALPHA_START = 1f
		private const val MAX_LINES_ON_SHRINK = 6
	}
}