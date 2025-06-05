package shape.widget.processbar

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.graphics.toColorInt
import kotlin.math.atan2


class ArcProgressBar(
	context: Context,
	attrs: AttributeSet? = null
) : FrameLayout(context, attrs), ValueAnimator.AnimatorUpdateListener {

	companion object {
		private const val DURATION = 5000L
		private const val DEFAULT_ANGLE_START = 200f
		private const val DEFAULT_ANGLE_SWEEP = 138f
	}

	private val paintTriangle = Paint()
	private val paintHolder = Paint()
	private val paintProgress = Paint()
	private val paintCircle = Paint()
	private val paintStrokeIn = Paint()
	private val paintStrokeOut = Paint()

	private val pathTriangle: Path = Path()
	private val pathHolder: Path = Path()
	private val pathProgress: Path = Path()
	private val pathCircle: Path = Path()
	private val pathStrokeIn: Path = Path()
	private val pathStrokeOut: Path = Path()
	private val rectF: RectF = RectF()
	private val rectFBg: RectF = RectF()
	private val rectFTriangle: RectF = RectF()

	private val pathMeasure = PathMeasure()
	private val density = Resources.getSystem().displayMetrics.density

	private val circleSize: Float
	private var width: Int = 0
	private var height: Int = 0
	private val xyCoordinate = floatArrayOf(0f, 0f)
	private val padding: Float
	private val bitmapSize: Float

	private var startAngle = 0f
	private var sweepAngle = 0f

	private var bitmap: Bitmap? = null

	private var animator: ValueAnimator = ValueAnimator.ofFloat(0f, 0f).apply {
		addUpdateListener(this@ArcProgressBar)
		duration = DURATION
		interpolator = AccelerateDecelerateInterpolator()
	}

	init {

		setWillNotDraw(false)
		setPaint()
		padding = dimensionPixel(15f)

		bitmapSize = dimensionPixel(13f)
		circleSize = dimensionPixel(10f)
	}

	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
		super.onSizeChanged(w, h, oldw, oldh)

		calculatorView(w, h)
		invalidate()
	}

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)

		with(canvas) {
			drawPath(pathHolder, paintHolder)
			drawPath(pathProgress, paintProgress)

			if (pathMeasure.length > 0) {
				drawPath(pathStrokeOut, paintStrokeOut)
				drawPath(pathStrokeIn, paintStrokeIn)
				drawPath(pathCircle, paintCircle)

				drawUser(this)
			}
		}
	}

	override fun onAnimationUpdate(animation: ValueAnimator) {
		val value = animation.getAnimatedValue() as Float
		pathProgress.apply {
			reset()
			arcTo(rectF, startAngle, value)
		}
		pathMeasure.apply {
			setPath(pathProgress, false)
			getPosTan(pathMeasure.length, xyCoordinate, null)
		}
		addPath()
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
			setFloatValues(if (animation) 0f else percent, if (percent <= 10f) 10f else percent)
			start()
		}
	}

	private fun addPath() {
		pathCircle.reset()
		pathCircle.moveTo(xyCoordinate[0], xyCoordinate[1])
		pathCircle.addCircle(
			xyCoordinate[0],
			xyCoordinate[1],
			circleSize,
			Path.Direction.CW
		)

		pathStrokeIn.reset()
		pathStrokeIn.moveTo(xyCoordinate[0], xyCoordinate[1])
		pathStrokeIn.addCircle(
			xyCoordinate[0],
			xyCoordinate[1],
			circleSize.plus(5),
			Path.Direction.CW
		)

		pathStrokeOut.reset()
		pathStrokeOut.moveTo(xyCoordinate[0], xyCoordinate[1])
		pathStrokeOut.addCircle(
			xyCoordinate[0],
			xyCoordinate[1],
			circleSize.plus(18),
			Path.Direction.CW
		)
	}

	private fun setPaint() {
		paintTriangle.apply {
			color = "#77A5FF".toColorInt()
			strokeJoin = Paint.Join.ROUND
			val corEffect = CornerPathEffect(dimensionPixel(12f))
			setPathEffect(corEffect)
		}
		paintStrokeOut.apply {
			color = "#FDC7BA".toColorInt()
		}
		paintStrokeIn.apply {
			color = "#CE462E".toColorInt()
		}
		paintCircle.apply {
			color = "#03A959".toColorInt()
		}
		paintProgress.apply {
			color = "#1A9F5F".toColorInt()
			strokeWidth = dimensionPixel(14f)
			style = Paint.Style.STROKE
		}
		paintHolder.apply {
			color = "#FFFFFF".toColorInt()
			strokeWidth = dimensionPixel(14f)
			style = Paint.Style.STROKE
		}
	}

	private fun calculatorPercent(percent: Number) = sweepAngle.times(percent.toFloat()).div(100f)

	private fun calculatorPercent(current: Number, max: Number): Float {
		val percent = current.toFloat().times(100f).div(max.toFloat())
		return calculatorPercent(percent)
	}

	private fun calculatorView(w: Int, h: Int) {
		width = w
		height = h

		pathProgress.reset()
		pathTriangle.reset()

		val horizontal = padding
		val vertical = padding.times(2f)
		val heightRectF = height.times(3f)

		rectF.set(horizontal, vertical, width.minus(horizontal), heightRectF.minus(vertical))
		rectFBg.set(horizontal, vertical, width.minus(vertical), height.toFloat())

		val deg = calculatorAngle(
			heightRectF.div(2).minus(heightRectF).minus(vertical),
			width.plus(horizontal),
		)

		startAngle = DEFAULT_ANGLE_START + deg
		sweepAngle = DEFAULT_ANGLE_SWEEP - deg

		xyCoordinate[0] = padding
		xyCoordinate[1] = height.plus(padding)

		pathHolder.apply {
			reset()
			arcTo(rectF, startAngle, sweepAngle)
		}
	}

	private fun drawUser(canvas: Canvas) {
		val x = xyCoordinate[0].minus(runValue(-1f, -1f))
		val y = xyCoordinate[1].minus(runValue(1.5f, 0.6f))

		val startX = x.minus(bitmapSize.times(3)).plus(runValue(0f, .5f))
		val startY = y.minus(bitmapSize.times(3))
		val endX = x.minus(bitmapSize).plus(runValue(0f, .5f))
		val endY = y.minus(bitmapSize)

		rectFTriangle.set(startX, startY, endX, endY)
		pathTriangle.reset()

		pathTriangle.moveTo(
			startX.plus(bitmapSize.times(0.35f)),
			endY.minus(bitmapSize.times(0.35f))
		)
		pathTriangle.lineTo(
			xyCoordinate[0].minus(circleSize.minus(runValue(.5f, 0.5f))),
			xyCoordinate[1].minus(circleSize.plus(runValue(.5f, 0f)))
		)
		pathTriangle.lineTo(
			endX.minus(2),
			endY.minus(bitmapSize.times(0.75f))
		)

		canvas.drawPath(pathTriangle, paintTriangle)
		bitmap?.let { canvas.drawBitmap(it, null, rectFTriangle, paintTriangle) }
	}

	private fun runValue(percentStart: Float, percentEnd: Float = 1f): Float {
		val value = animator.animatedValue as Float
		return if (value < 30) {
			30.minus(value).times(percentStart)
		} else {
			if (value > 80) {
				value.minus(80).times(percentEnd)
			} else {
				0f
			}
		}
	}

	private fun calculatorAngle(deltaX: Float, deltaY: Float): Float {
		val rad = atan2(deltaY, deltaX)
		return rad
	}

	private fun dimensionPixel(size: Number): Float = density.times(size.toFloat())
}