package shape.widget.base

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.graphics.createBitmap
import java.lang.ref.WeakReference

abstract class DefaultView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
) : View(context, attrs), ValueAnimator.AnimatorUpdateListener {
	protected val density = Resources.getSystem().displayMetrics.density

	companion object {
		const val DURATION = 5000L
		const val FINISH_PERCENT = 100
	}

	private var width: Float = 0f
	private var height: Float = 0f

	protected var hasSafeVertical = false
	protected var hasSafeHorizontal = false
	protected var safePadding = density.times(5)

	protected var drawBitmap: WeakReference<Bitmap>? = null
	protected var drawCanvas: Canvas? = null

	protected var drawHeight: Int = 0
		get() = height.minus(safeVertical()).toInt()

	protected var drawWidth: Int = 0
		get() = width.minus(safeHorizontal()).toInt()

	protected val pathHolder = Path()
	protected val paintHolder = Paint().apply {
	}
	protected var duration: Long
		set(value) {
			_animator.duration = value
		}
		get() = _animator.duration

	protected var interpolator: TimeInterpolator
		set(value) {
			_animator.interpolator = value
		}
		get() = _animator.interpolator

	private var _animator: ValueAnimator = ValueAnimator.ofFloat(0f, 0f).apply {
		addUpdateListener(this@DefaultView)
		duration = DURATION
		interpolator = AccelerateDecelerateInterpolator()
		doOnEnd { onEnd() }
	}

	abstract fun generatePath()

	abstract fun generatePaint()

	abstract fun generateData()

	abstract fun drawCanvas()

	abstract fun onSizeChange()

	init {
		initData()
	}

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)

		width = measuredWidth.toFloat()
		height = measuredHeight.toFloat()

		onSizeChange()
	}

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)

		var bitmap = drawBitmap?.get()

		if (bitmap == null
			|| bitmap.height != drawHeight
			|| bitmap.width != drawWidth
		) {
			bitmap = createBitmap(
				drawWidth.minus(safeHorizontal()),
				drawHeight.minus(safeVertical())
			)
			drawBitmap = WeakReference<Bitmap>(bitmap)
			drawCanvas = Canvas(bitmap)
		}

		bitmap.eraseColor(Color.TRANSPARENT)

		drawCanvas()

		canvas.drawBitmap(
			bitmap,
			safeHorizontal().toFloat(),
			safeVertical().toFloat(),
			paintHolder
		)
	}

	override fun onAnimationUpdate(animation: ValueAnimator) {
		onUpdate(animation)
	}

	override fun onDetachedFromWindow() {
		super.onDetachedFromWindow()
		clearAnimation()
		releaseBitmap()
	}

	override fun clearAnimation() {
		super.clearAnimation()
		_animator.cancel()
	}

	open fun start(value: Float = 1f) {
		_animator.setFloatValues(0f, value)
		_animator.start()
	}

	open fun onUpdate(animation: ValueAnimator) {}

	open fun onEnd() {}

	open fun releaseBitmap() {
		drawCanvas?.setBitmap(null)
		drawCanvas = null

		drawBitmap?.let {
			it.get()?.apply { recycle() }
			it.clear()
		}

		drawBitmap = null
	}

	private fun initData() {
		setWillNotDraw(false)
		generateData()
		generatePaint()
		generatePath()
	}

	protected fun safeHorizontal(): Int {
		return if (hasSafeHorizontal) safePadding.toInt() else 0
	}

	protected fun safeVertical(): Int {
		return if (hasSafeVertical) safePadding.toInt() else 0
	}
}