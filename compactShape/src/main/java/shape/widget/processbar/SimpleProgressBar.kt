package shape.widget.processbar

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt

class SimpleProgressBar(
	context: Context,
	attrs: AttributeSet? = null
) : FrameLayout(context, attrs), ValueAnimator.AnimatorUpdateListener {

	companion object {
		private const val DURATION = 5000L
		private const val FINISH_PERCENT = 100
	}

	private val rectF: RectF = RectF()
	private val rectFProgress: RectF = RectF()

	private val density = Resources.getSystem().displayMetrics.density

	private var width: Int = 0
	private var height: Int = 0
	private val xyCoordinate = floatArrayOf(0f, 0f)
	private val cornerRadius: Float

	private val pathProgress = Path()

	private val paintStrip = Paint()
	private val paintProgress = Paint()
	private val paintHolder = Paint()
	private val paintBackground = Paint()

	private var gradient: LinearGradient? = null

	private var bitmapHolder: Bitmap? = null

	private var animator: ValueAnimator = ValueAnimator.ofFloat(0f, 0f).apply {
		addUpdateListener(this@SimpleProgressBar)
		duration = DURATION
		interpolator = AccelerateDecelerateInterpolator()
	}

	init {
		setWillNotDraw(false)
		setPaint()
		cornerRadius = dimensionPixel(12f)
	}

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)

		with(canvas) {
			bitmapHolder?.let { drawBitmap(it, rectF.left, rectF.top, paintHolder) }
			drawPath(pathProgress, paintProgress)
		}
	}

	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
		super.onSizeChanged(w, h, oldw, oldh)

		calculatorView(w, h)
		invalidate()
	}

	override fun onAnimationUpdate(animation: ValueAnimator) {
		val value = animation.getAnimatedValue() as Float
		pathProgress.apply {
			reset()
			addRoundRect(
				rectFProgress.apply { right = value },
				cornerRadius,
				cornerRadius,
				Path.Direction.CW
			)
		}
		invalidate()
	}

	override fun onDetachedFromWindow() {
		animator.cancel()
		animator.removeAllListeners()
		animator.removeAllUpdateListeners()
		super.onDetachedFromWindow()
	}

	fun getAnimator(): ValueAnimator {
		return animator
	}

	fun setPercent(percent: Number, animation: Boolean = false) {
		measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
		post {
			calculatorView(measuredWidth, measuredHeight)
			startAnim(calculatorPercent(percent), animation)
		}
	}

	fun setPercent(current: Number, max: Number, animation: Boolean = false) {
		measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
		post {
			calculatorView(measuredWidth, measuredHeight)
			startAnim(calculatorPercent(current, max), animation)
		}
	}

	private fun startAnim(percent: Float, animation: Boolean) {
		animator.apply {
			setFloatValues(if (animation) 0f else percent, if (percent == 0f) 1f else percent)
			start()
		}
	}

	private fun calculatorPercent(percent: Number) =
		width.times(percent.toFloat()).div(FINISH_PERCENT)

	private fun calculatorPercent(current: Number, max: Number): Float {
		val percent = current.toFloat().times(FINISH_PERCENT).div(max.toFloat())
		return calculatorPercent(percent)
	}

	private fun dimensionPixel(size: Number): Float = density.times(size.toFloat())

	private fun setPaint() {
		paintProgress.apply {
			isAntiAlias = true
			style = Paint.Style.FILL
		}
		paintStrip.apply {
			isAntiAlias = true
			style = Paint.Style.FILL
			color = "#332EA167".toColorInt()
		}
		paintHolder.apply {
			isAntiAlias = true
			style = Paint.Style.FILL
		}
		paintBackground.apply {
			isAntiAlias = true
			style = Paint.Style.FILL
			color = "#E1FAE7".toColorInt()
		}
	}

	private fun calculatorView(w: Int, h: Int) {
		width = w
		height = h

		rectF.set(0f, 0f, width.toFloat(), height.toFloat())
		rectFProgress.set(0f, 0f, 0f, height.toFloat())

		bitmapHolder = createHolder()

		if (gradient == null) {
			gradient = LinearGradient(
				0f,
				1f,
				width.toFloat(),
				0f,
				intArrayOf("#33CC80".toColorInt(), "#65ECA8".toColorInt()),
				null,
				Shader.TileMode.MIRROR
			)
		}

		paintProgress.setShader(gradient)

		xyCoordinate[0] = 0f
		xyCoordinate[1] = rectF.centerY()
	}

	private fun createHolder(): Bitmap {
		return createBitmap(width, height).let {
			var start = 0f

			val child = createBackground()
			val childCanvas = Canvas(it)
			while (start < width.toFloat()) {
				childCanvas.drawBitmap(child, start, rectF.top, paintHolder)
				start += child.width
			}
			createRound(it)
		}
	}

	private fun createBackground(): Bitmap {
		val w = dimensionPixel(20f)
		val h = height.toFloat()
		val size = dimensionPixel(5f)
		return createBitmap(w.toInt(), h.toInt()).apply {
			val tile = Canvas(this)

			val path = Path()
			var x = 0f
			var y = 0f
			var position = 0

			while (y < height + size * 5) {
				if (y == 0f && x == 0f) {
					path.moveTo(x, y)
					path.lineTo(x + size, 0f)
					path.lineTo(0f, y + size)
					tile.drawPath(path, paintStrip)
				} else {
					path.reset()
					path.moveTo(0f, y)
					path.lineTo(0f, y + size)
					path.lineTo(x + size, 0f)
					path.lineTo(x, 0f)

					tile.drawPath(path, if (position % 2 == 0) paintStrip else paintBackground)
				}

				position += 1

				x += size
				y += size
			}
		}
	}

	private fun createRound(bitmap: Bitmap): Bitmap {
		val roundedBitmap = createBitmap(bitmap.width, bitmap.height)
		val canvas = Canvas(roundedBitmap)

		val paint = Paint()
		val frame = Rect(0, 0, bitmap.width, bitmap.height)

		paint.isAntiAlias = true
		paint.color = -0x1
		paint.style = Paint.Style.FILL
		canvas.drawARGB(0, 0, 0, 0)

		canvas.drawRoundRect(RectF(frame), cornerRadius, cornerRadius, paint)

		paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
		canvas.drawBitmap(bitmap, frame, frame, paint)

		return roundedBitmap
	}
}