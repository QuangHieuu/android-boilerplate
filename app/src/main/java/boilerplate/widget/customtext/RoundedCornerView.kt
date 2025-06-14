package boilerplate.widget.customtext

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toRectF
import androidx.core.view.*
import boilerplate.R
import view.widget.RoundedImageView
import kotlin.math.min

class RoundedCornerView(
	context: Context,
	attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

	companion object {

		const val DEFAULT_RADIUS: Float = 0f

		const val TOP_LEFT: Int = 0
		const val TOP_RIGHT: Int = 1
		const val BOTTOM_RIGHT: Int = 2
		const val BOTTOM_LEFT: Int = 3
	}

	private val _cornerRadii =
		floatArrayOf(DEFAULT_RADIUS, DEFAULT_RADIUS, DEFAULT_RADIUS, DEFAULT_RADIUS)

	private val _holderRect = Rect()
	private val _holderPaint = Paint().apply {
		style = Paint.Style.FILL
	}

	private val _clipPath = Path()

	private val _strokePaint = Paint().apply {
		style = Paint.Style.STROKE
	}

	private var _backgroundBitmap: Bitmap? = null

	init {
		clipChildren = true
		clipToPadding = true
		background = null
		setWillNotDraw(false)

		context.withStyledAttributes(attrs, R.styleable.RoundedCornerView) {
			_strokePaint.apply {
				color = getColor(R.styleable.RoundedCornerView_stroke_color, Color.BLACK)
				strokeWidth =
					getDimension(R.styleable.RoundedCornerView_stroke_width, 0f)
			}

			var cornerRadiusOverride =
				getDimensionPixelSize(R.styleable.RoundedCornerView_radius, -1).toFloat()

			_cornerRadii[TOP_LEFT] =
				getDimensionPixelSize(
					R.styleable.RoundedCornerView_topLeftCornerRadius,
					-1
				).toFloat()
			_cornerRadii[TOP_RIGHT] =
				getDimensionPixelSize(
					R.styleable.RoundedCornerView_topRightCornerRadius,
					-1
				).toFloat()
			_cornerRadii[BOTTOM_RIGHT] =
				getDimensionPixelSize(
					R.styleable.RoundedCornerView_bottomRightCornerRadius,
					-1
				).toFloat()
			_cornerRadii[BOTTOM_LEFT] =
				getDimensionPixelSize(
					R.styleable.RoundedCornerView_bottomLeftCornerRadius,
					-1
				).toFloat()

			var any = false
			var index = 0
			while (index < _cornerRadii.size) {
				if (_cornerRadii[index] < 0) {
					_cornerRadii[index] = 0f
				} else {
					any = true
				}
				index++
			}

			if (!any) {
				if (cornerRadiusOverride < 0) {
					cornerRadiusOverride = RoundedImageView.Companion.DEFAULT_RADIUS
				}
				var i = 0
				while (i < _cornerRadii.size) {
					_cornerRadii[i] = cornerRadiusOverride
					i++
				}
			}

			setLayerType(LAYER_TYPE_SOFTWARE, null)
		}
	}

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		val stroke = _strokePaint.strokeWidth.toInt()

		val iterator = children.iterator()
		while (iterator.hasNext()) {
			val child = iterator.next()
			measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)
		}

		val maxWidth = children.maxOf { it.measuredWidth.plus(it.marginEnd).plus(it.marginLeft) }
		val maxHeight = children.maxOf { it.measuredHeight.plus(it.marginTop).plus(it.marginBottom) }

		val modeWidth = MeasureSpec.getMode(widthMeasureSpec)
		val modeHeight = MeasureSpec.getMode(heightMeasureSpec)

		val sizeWidth = MeasureSpec.getSize(widthMeasureSpec)
		val sizeHeight = MeasureSpec.getSize(heightMeasureSpec)

		var resultWidth = maxWidth

		var resultHeight: Int = maxHeight

		resultWidth += paddingStart.plus(paddingEnd)
		resultHeight += paddingTop.plus(paddingBottom)

		resultWidth = measureSize(modeWidth, sizeWidth, resultWidth)
		resultHeight = measureSize(modeHeight, sizeHeight, resultHeight)


		setMeasuredDimension(resultWidth, resultHeight)

		_holderRect.set(
			stroke,
			stroke,
			resultWidth.minus(stroke),
			resultHeight.minus(stroke)
		)
	}

	override fun onDraw(canvas: Canvas) {
		_backgroundBitmap?.let { canvas.drawBitmap(it, null, _holderRect, _holderPaint) }

		val cornerDimensions = floatArrayOf(
			_cornerRadii[TOP_LEFT], _cornerRadii[TOP_LEFT],
			_cornerRadii[TOP_RIGHT], _cornerRadii[TOP_RIGHT],
			_cornerRadii[BOTTOM_RIGHT], _cornerRadii[BOTTOM_RIGHT],
			_cornerRadii[BOTTOM_LEFT], _cornerRadii[BOTTOM_LEFT]
		)
		_clipPath.reset()
		_clipPath.addRoundRect(
			_holderRect.toRectF(),
			cornerDimensions,
			Path.Direction.CW
		)
		if (_strokePaint.strokeWidth != 0f) {
			canvas.drawPath(_clipPath, _strokePaint)
		}
		canvas.clipPath(_clipPath)
	}

	private fun measureSize(mode: Int, sizeExpect: Int, sizeActual: Int): Int {
		return when (mode) {
			MeasureSpec.EXACTLY -> sizeExpect
			MeasureSpec.AT_MOST -> min(sizeActual, sizeExpect)
			else -> sizeActual
		}
	}

	private fun getBitmapFromDrawable(drawableId: Int): Bitmap? {
		val drawable = ContextCompat.getDrawable(context, drawableId) ?: return null
		val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
		val canvas = Canvas(bitmap)
		drawable.setBounds(0, 0, canvas.width, canvas.height)
		drawable.draw(canvas)
		return bitmap
	}
}