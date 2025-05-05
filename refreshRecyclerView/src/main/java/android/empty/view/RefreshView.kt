package android.empty.view

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.empty.listener.RefreshListener
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.graphics.toColorInt
import kotlin.math.min

class RefreshView @JvmOverloads constructor(
	context: Context?,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), RefreshListener {

	companion object {
		private const val MAX_ARC_DEGREE = 330f
		private const val DEFAULT_START_DEGREES = 285f
		private const val DEFAULT_STROKE_WIDTH = 2f
		private const val ANIMATION_DURATION = 888L
	}

	private val density = Resources.getSystem().displayMetrics.density

	private val _arcBounds = RectF()
	private val _paint = Paint()

	private var _startDegrees = DEFAULT_START_DEGREES
	private var _swipeDegrees = 0f
	private var _strokeWidth = density.times(DEFAULT_STROKE_WIDTH)
	private var _hasTriggeredRefresh = false

	private var iListener: RefreshListener? = null

	private val _rotateAnimator: ValueAnimator by lazy {
		ValueAnimator.ofFloat(0.0f, 1.0f).apply {
			duration = ANIMATION_DURATION
			interpolator = LinearInterpolator()
			repeatMode = ValueAnimator.RESTART
			repeatCount = ValueAnimator.INFINITE
		}
	}

	init {
		initPaint()
	}

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)
		canvas.drawArc(_arcBounds, _startDegrees, _swipeDegrees, false, _paint)
	}

	override fun onDetachedFromWindow() {
		resetAnimator()
		super.onDetachedFromWindow()
	}

	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
		super.onSizeChanged(w, h, oldw, oldh)
		val radius = min(w, h).div(2)

		val centerX = w.div(2f)
		val centerY = h.div(2f)

		_arcBounds.set(
			centerX.minus(radius),
			centerY.minus(radius),
			centerX.plus(radius),
			centerY.plus(radius)
		)
		_arcBounds.inset(_strokeWidth.div(2f), _strokeWidth.div(2f))
	}

	override fun reset() {
		iListener?.reset()
		resetAnimator()

		_hasTriggeredRefresh = false
		_startDegrees = DEFAULT_START_DEGREES
		_swipeDegrees = 0.0f
	}

	override fun refreshing() {
		iListener?.refreshing()
		_hasTriggeredRefresh = true
		_swipeDegrees = MAX_ARC_DEGREE
		startAnimator()
	}

	override fun pullProgress(pullDistance: Float, pullProgress: Float) {
		if (!_hasTriggeredRefresh) {
			val swipeProgress = min(1f, pullProgress)
			setSwipeDegrees(swipeProgress * MAX_ARC_DEGREE)
		}
	}

	fun setListener(listener: RefreshListener) {
		iListener = listener
	}

	private fun initPaint() {
		_paint.apply {
			isAntiAlias = true
			style = Paint.Style.STROKE
			strokeWidth = _strokeWidth
			color = "#FFD72263".toColorInt()
		}
	}

	private fun startAnimator() {
		_rotateAnimator.apply {
			addUpdateListener { animation ->
				val rotateProgress = animation.animatedValue as Float
				setStartDegrees(DEFAULT_START_DEGREES + rotateProgress * 360)
			}
			start()
		}
	}

	private fun resetAnimator() {
		_rotateAnimator.apply {
			removeAllListeners()
			cancel()
		}
	}

	private fun setStartDegrees(startDegrees: Float) {
		_startDegrees = startDegrees
		postInvalidate()
	}

	private fun setSwipeDegrees(swipeDegrees: Float) {
		_swipeDegrees = swipeDegrees
		postInvalidate()
	}
}