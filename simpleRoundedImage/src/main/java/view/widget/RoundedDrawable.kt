package view.widget

import android.content.res.ColorStateList
import view.widget.model.Corner
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader.TileMode
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.widget.ImageView.ScaleType
import androidx.annotation.ColorInt
import androidx.core.graphics.createBitmap
import kotlin.math.max
import kotlin.math.min

class RoundedDrawable(private val _source: Bitmap) : Drawable() {
	companion object {
		const val DEFAULT_BORDER_COLOR: Int = Color.BLACK

		@JvmStatic
		fun fromBitmap(bitmap: Bitmap?): RoundedDrawable? {
			return if (bitmap != null) {
				RoundedDrawable(bitmap)
			} else {
				null
			}
		}

		@JvmStatic
		fun fromDrawable(drawable: Drawable?): Drawable? {
			if (drawable != null) {
				if (drawable is RoundedDrawable) {
					// just return if it's already a RoundedDrawable
					return drawable
				} else if (drawable is LayerDrawable) {
					val cs = drawable.mutate().constantState
					val ld = (cs?.newDrawable() ?: drawable) as LayerDrawable

					val num = ld.numberOfLayers

					// loop through layers to and change to RoundedDrawables if possible
					for (i in 0..<num) {
						val d = ld.getDrawable(i)
						ld.setDrawableByLayerId(ld.getId(i), fromDrawable(d))
					}
					return ld
				}

				// try to get a bitmap from the drawable and
				val bm = drawableToBitmap(drawable)
				if (bm != null) {
					return RoundedDrawable(bm)
				}
			}
			return drawable
		}

		@JvmStatic
		fun drawableToBitmap(drawable: Drawable): Bitmap? {
			if (drawable is BitmapDrawable) {
				return drawable.bitmap
			}

			var bitmap: Bitmap?
			val width = max(drawable.intrinsicWidth, 2)
			val height = max(drawable.intrinsicHeight, 2)
			try {
				bitmap = createBitmap(width, height)
				val canvas = Canvas(bitmap)
				drawable.setBounds(0, 0, canvas.width, canvas.height)
				drawable.draw(canvas)
			} catch (e: Throwable) {
				bitmap = null
			}

			return bitmap
		}

		private fun only(index: Int, booleans: BooleanArray): Boolean {
			var i = 0
			val len = booleans.size
			while (i < len) {
				if (booleans[i] != (i == index)) {
					return false
				}
				i++
			}
			return true
		}

		private fun any(booleans: BooleanArray): Boolean {
			for (b in booleans) {
				if (b) {
					return true
				}
			}
			return false
		}

		private fun all(booleans: BooleanArray): Boolean {
			for (b in booleans) {
				if (b) {
					return false
				}
			}
			return true
		}
	}

	/**
	 * @return the corner radius.
	 */
	private var _cornerRadius: Float = 0f
	private var _tileModeX: TileMode? = TileMode.CLAMP
	private var _tileModeY: TileMode? = TileMode.CLAMP

	private val _bounds = RectF()
	private val _drawableRect = RectF()
	private val _bitmapRect = RectF()
	private val _bitmapPaint: Paint
	private val _bitmapWidth = _source.width
	private val _bitmapHeight = _source.height
	private val _borderRect = RectF()
	private val _borderPaint: Paint
	private val _shaderMatrix = Matrix()
	private val _squareCornersRect = RectF()

	private var _rebuildShader = true

	// [ topLeft, topRight, bottomLeft, bottomRight ]
	private val _cornersRounded = booleanArrayOf(true, true, true, true)
	private var _isOval: Boolean = false
	private var _borderWidth: Float = 0f
	private var _borderColors: ColorStateList = ColorStateList.valueOf(DEFAULT_BORDER_COLOR)
	private var _scaleType: ScaleType = ScaleType.FIT_CENTER

	init {
		_bitmapRect[0f, 0f, _bitmapWidth.toFloat()] = _bitmapHeight.toFloat()

		_bitmapPaint = Paint()
		_bitmapPaint.style = Paint.Style.FILL
		_bitmapPaint.isAntiAlias = true

		_borderPaint = Paint()
		_borderPaint.style = Paint.Style.STROKE
		_borderPaint.isAntiAlias = true
		_borderPaint.color = _borderColors.getColorForState(state, DEFAULT_BORDER_COLOR)
		_borderPaint.strokeWidth = _borderWidth
	}

	override fun isStateful(): Boolean {
		return _borderColors.isStateful
	}

	override fun onStateChange(state: IntArray): Boolean {
		val newColor = _borderColors.getColorForState(state, 0)
		if (_borderPaint.color != newColor) {
			_borderPaint.color = newColor
			return true
		} else {
			return super.onStateChange(state)
		}
	}

	private fun updateShaderMatrix() {
		val scale: Float
		var dx: Float
		var dy: Float

		when (_scaleType) {
			ScaleType.CENTER -> {
				_borderRect.set(_bounds)
				_borderRect.inset(_borderWidth.div(2), _borderWidth.div(2))

				_shaderMatrix.reset()
				_shaderMatrix.setTranslate(
					.5f.times(_borderRect.width().minus(_bitmapWidth).plus(.5f)),
					.5f.times(_borderRect.height().minus(_bitmapHeight).plus(.5f))
				)
			}

			ScaleType.CENTER_CROP -> {
				_borderRect.set(_bounds)
				_borderRect.inset(_borderWidth.div(2), _borderWidth.div(2))

				_shaderMatrix.reset()

				dx = 0f
				dy = 0f

				if (_bitmapWidth.times(_borderRect.height()) > _borderRect.width().times(_bitmapHeight)) {
					scale = _borderRect.height().div(_bitmapHeight)
					dx = .5f.times(_borderRect.width().minus(_bitmapWidth.times(scale)))
				} else {
					scale = _borderRect.width().div(_bitmapWidth)
					dy = .5f.times(_borderRect.height().minus(_bitmapHeight.times(scale)))
				}

				_shaderMatrix.setScale(scale, scale)
				_shaderMatrix.postTranslate(
					dx.plus(.5f).plus(_borderWidth.div(2)),
					dy.plus(.5f).plus(_borderWidth.div(2))
				)
			}

			ScaleType.CENTER_INSIDE -> {
				_shaderMatrix.reset()

				scale = if (_bitmapWidth <= _bounds.width() && _bitmapHeight <= _bounds.height()) {
					1.0f
				} else {
					min(_bounds.width().div(_bitmapWidth), _bounds.height().div(_bitmapHeight))
				}

				dx = (_bounds.width().minus(_bitmapWidth.times(scale))).times(.5f).plus(.5f)
				dy = (_bounds.height().minus(_bitmapHeight.times(scale))).times(.5f).plus(.5f)

				_shaderMatrix.setScale(scale, scale)
				_shaderMatrix.postTranslate(dx, dy)

				_borderRect.set(_bitmapRect)
				_shaderMatrix.mapRect(_borderRect)
				_borderRect.inset(_borderWidth.div(2), _borderWidth.div(2))
				_shaderMatrix.setRectToRect(_bitmapRect, _borderRect, Matrix.ScaleToFit.FILL)
			}

			ScaleType.FIT_CENTER -> {
				_borderRect.set(_bitmapRect)
				_shaderMatrix.setRectToRect(_bitmapRect, _bounds, Matrix.ScaleToFit.CENTER)
				_shaderMatrix.mapRect(_borderRect)
				_borderRect.inset(_borderWidth.div(2), _borderWidth.div(2))
				_shaderMatrix.setRectToRect(_bitmapRect, _borderRect, Matrix.ScaleToFit.FILL)
			}

			ScaleType.FIT_END -> {
				_borderRect.set(_bitmapRect)
				_shaderMatrix.setRectToRect(_bitmapRect, _bounds, Matrix.ScaleToFit.END)
				_shaderMatrix.mapRect(_borderRect)
				_borderRect.inset(_borderWidth.div(2), _borderWidth.div(2))
				_shaderMatrix.setRectToRect(_bitmapRect, _borderRect, Matrix.ScaleToFit.FILL)
			}

			ScaleType.FIT_START -> {
				_borderRect.set(_bitmapRect)
				_shaderMatrix.setRectToRect(_bitmapRect, _bounds, Matrix.ScaleToFit.START)
				_shaderMatrix.mapRect(_borderRect)
				_borderRect.inset(_borderWidth.div(2), _borderWidth.div(2))
				_shaderMatrix.setRectToRect(_bitmapRect, _borderRect, Matrix.ScaleToFit.FILL)
			}

			ScaleType.FIT_XY -> {
				_borderRect.set(_bounds)
				_borderRect.inset(_borderWidth.div(2), _borderWidth.div(2))
				_shaderMatrix.reset()
				_shaderMatrix.setRectToRect(_bitmapRect, _borderRect, Matrix.ScaleToFit.FILL)
			}

			else -> {
				_borderRect.set(_bitmapRect)
				_shaderMatrix.setRectToRect(_bitmapRect, _bounds, Matrix.ScaleToFit.CENTER)
				_shaderMatrix.mapRect(_borderRect)
				_borderRect.inset(_borderWidth.div(2), _borderWidth.div(2))
				_shaderMatrix.setRectToRect(_bitmapRect, _borderRect, Matrix.ScaleToFit.FILL)
			}
		}

		_drawableRect.set(_borderRect)
		_rebuildShader = true
	}

	override fun onBoundsChange(bounds: Rect) {
		super.onBoundsChange(bounds)

		_bounds.set(bounds)

		updateShaderMatrix()
	}

	override fun draw(canvas: Canvas) {
		if (_rebuildShader) {
			_bitmapPaint.setShader(BitmapShader(_source, _tileModeX!!, _tileModeY!!).also {
				if (_tileModeX == TileMode.CLAMP && _tileModeY == TileMode.CLAMP) {
					it.setLocalMatrix(_shaderMatrix)
				}
			})
			_rebuildShader = false
		}

		if (_isOval) {
			if (_borderWidth > 0) {
				canvas.drawOval(_drawableRect, _bitmapPaint)
				canvas.drawOval(_borderRect, _borderPaint)
			} else {
				canvas.drawOval(_drawableRect, _bitmapPaint)
			}
		} else {
			if (any(_cornersRounded)) {
				val radius = _cornerRadius
				if (_borderWidth > 0) {
					canvas.drawRoundRect(_drawableRect, radius, radius, _bitmapPaint)
					canvas.drawRoundRect(_borderRect, radius, radius, _borderPaint)
					redrawBitmapForSquareCorners(canvas)
					redrawBorderForSquareCorners(canvas)
				} else {
					canvas.drawRoundRect(_drawableRect, radius, radius, _bitmapPaint)
					redrawBitmapForSquareCorners(canvas)
				}
			} else {
				canvas.drawRect(_drawableRect, _bitmapPaint)
				if (_borderWidth > 0) {
					canvas.drawRect(_borderRect, _borderPaint)
				}
			}
		}
	}

	@Deprecated(
		"Deprecated in Java",
		ReplaceWith("PixelFormat.TRANSLUCENT", "android.graphics.PixelFormat")
	)
	override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

	override fun getAlpha(): Int = _bitmapPaint.alpha

	override fun setAlpha(alpha: Int) {
		_bitmapPaint.alpha = alpha
		invalidateSelf()
	}

	override fun getColorFilter(): ColorFilter? {
		return _bitmapPaint.colorFilter
	}

	override fun setColorFilter(cf: ColorFilter?) {
		_bitmapPaint.setColorFilter(cf)
		invalidateSelf()
	}

	@Deprecated("Deprecated in Java")
	override fun setDither(dither: Boolean) {
		_bitmapPaint.isDither = dither
		invalidateSelf()
	}

	override fun setFilterBitmap(filter: Boolean) {
		_bitmapPaint.isFilterBitmap = filter
		invalidateSelf()
	}

	override fun getIntrinsicWidth(): Int {
		return _bitmapWidth
	}

	override fun getIntrinsicHeight(): Int {
		return _bitmapHeight
	}

	/**
	 * @param corner the specific corner to get radius of.
	 * @return the corner radius of the specified corner.
	 */
	fun getCornerRadius(@Corner corner: Int): Float {
		return if (_cornersRounded[corner]) _cornerRadius else 0f
	}

	/**
	 * Sets all corners to the specified radius.
	 *
	 * @param radius the radius.
	 * @return the [RoundedDrawable] for chaining.
	 */
	fun setCornerRadius(radius: Float): RoundedDrawable {
		return setCornerRadius(radius, radius, radius, radius)
	}

	/**
	 * Sets the corner radius of one specific corner.
	 *
	 * @param corner the corner.
	 * @param radius the radius.
	 * @return the [RoundedDrawable] for chaining.
	 */
	fun setCornerRadius(@Corner corner: Int, radius: Float): RoundedDrawable {
		require(!(radius != 0f && _cornerRadius != 0f && _cornerRadius != radius)) { "Multiple nonzero corner radii not yet supported." }

		if (radius == 0f) {
			if (only(corner, _cornersRounded)) {
				_cornerRadius = 0f
			}
			_cornersRounded[corner] = false
		} else {
			if (_cornerRadius == 0f) {
				_cornerRadius = radius
			}
			_cornersRounded[corner] = true
		}

		return this
	}

	/**
	 * Sets the corner radii of all the corners.
	 *
	 * @param topLeft     top left corner radius.
	 * @param topRight    top right corner radius
	 * @param bottomRight bottom right corner radius.
	 * @param bottomLeft  bottom left corner radius.
	 */
	fun setCornerRadius(
		topLeft: Float,
		topRight: Float,
		bottomRight: Float,
		bottomLeft: Float
	): RoundedDrawable {
		val radiusSet: MutableSet<Float> = HashSet(4)
		radiusSet.add(topLeft)
		radiusSet.add(topRight)
		radiusSet.add(bottomRight)
		radiusSet.add(bottomLeft)

		radiusSet.remove(0f)

		require(radiusSet.size <= 1) { "Multiple nonzero corner radii not yet supported." }

		if (radiusSet.isNotEmpty()) {
			val radius = radiusSet.iterator().next()
			require(!(java.lang.Float.isInfinite(radius) || java.lang.Float.isNaN(radius) || radius < 0)) { "Invalid radius values: $radius" }
			_cornerRadius = radius
		} else {
			_cornerRadius = 0f
		}

		_cornersRounded[Corner.TOP_LEFT] = topLeft > 0
		_cornersRounded[Corner.TOP_RIGHT] = topRight > 0
		_cornersRounded[Corner.BOTTOM_RIGHT] = bottomRight > 0
		_cornersRounded[Corner.BOTTOM_LEFT] = bottomLeft > 0
		return this
	}

	fun setBorderWidth(width: Float): RoundedDrawable {
		_borderWidth = width
		_borderPaint.strokeWidth = _borderWidth
		return this
	}

	fun setBorderColor(@ColorInt color: Int): RoundedDrawable {
		return setBorderColor(ColorStateList.valueOf(color))
	}

	fun setBorderColor(colors: ColorStateList?): RoundedDrawable {
		_borderColors = colors ?: ColorStateList.valueOf(0)
		_borderPaint.color = _borderColors.getColorForState(state, DEFAULT_BORDER_COLOR)
		return this
	}

	fun setOval(oval: Boolean): RoundedDrawable {
		_isOval = oval
		return this
	}

	fun setScaleType(scale: ScaleType?): RoundedDrawable {
		val sc = scale ?: ScaleType.FIT_CENTER
		if (_scaleType != sc) {
			_scaleType = sc
			updateShaderMatrix()
		}
		return this
	}

	fun setTileModeX(tileModeX: TileMode?): RoundedDrawable {
		if (_tileModeX != tileModeX) {
			_tileModeX = tileModeX
			_rebuildShader = true
			invalidateSelf()
		}
		return this
	}

	fun setTileModeY(tileModeY: TileMode?): RoundedDrawable {
		if (_tileModeY != tileModeY) {
			_tileModeY = tileModeY
			_rebuildShader = true
			invalidateSelf()
		}
		return this
	}

	fun toBitmap(): Bitmap? = drawableToBitmap(this)

	private fun redrawBitmapForSquareCorners(canvas: Canvas) {
		if (all(_cornersRounded)) {
			// no square corners
			return
		}

		if (_cornerRadius == 0f) {
			return  // no round corners
		}

		val left = _drawableRect.left
		val top = _drawableRect.top
		val right = left.plus(_drawableRect.width())
		val bottom = top.plus(_drawableRect.height())
		val radius = _cornerRadius

		if (!_cornersRounded[Corner.TOP_LEFT]) {
			_squareCornersRect.set(left, top, left.plus(radius), top.plus(radius))
			canvas.drawRect(_squareCornersRect, _bitmapPaint)
		}

		if (!_cornersRounded[Corner.TOP_RIGHT]) {
			_squareCornersRect.set(right.minus(radius), top, right, radius)
			canvas.drawRect(_squareCornersRect, _bitmapPaint)
		}

		if (!_cornersRounded[Corner.BOTTOM_RIGHT]) {
			_squareCornersRect.set(right.minus(radius), bottom.minus(radius), right, bottom)
			canvas.drawRect(_squareCornersRect, _bitmapPaint)
		}

		if (!_cornersRounded[Corner.BOTTOM_LEFT]) {
			_squareCornersRect.set(left, bottom.minus(radius), left.plus(radius), bottom)
			canvas.drawRect(_squareCornersRect, _bitmapPaint)
		}
	}

	private fun redrawBorderForSquareCorners(canvas: Canvas) {
		if (all(_cornersRounded)) {
			// no square corners
			return
		}

		if (_cornerRadius == 0f) {
			return  // no round corners
		}

		val left = _drawableRect.left
		val top = _drawableRect.top
		val right = left.plus(_drawableRect.width())
		val bottom = top.plus(_drawableRect.height())
		val radius = _cornerRadius
		val offset = _borderWidth.div(2f)

		if (!_cornersRounded[Corner.TOP_LEFT]) {
			canvas.drawLine(left.minus(offset), top, left.plus(radius), top, _borderPaint)
			canvas.drawLine(left, top.minus(offset), left, top.plus(radius), _borderPaint)
		}

		if (!_cornersRounded[Corner.TOP_RIGHT]) {
			canvas.drawLine(right.minus(radius).minus(offset), top, right, top, _borderPaint)
			canvas.drawLine(right, top.minus(offset), right, top.plus(radius), _borderPaint)
		}

		if (!_cornersRounded[Corner.BOTTOM_RIGHT]) {
			canvas.drawLine(
				right.minus(radius).minus(offset),
				bottom,
				right.plus(offset),
				bottom,
				_borderPaint
			)
			canvas.drawLine(right, bottom.minus(radius), right, bottom, _borderPaint)
		}

		if (!_cornersRounded[Corner.BOTTOM_LEFT]) {
			canvas.drawLine(left.minus(offset), bottom, left.plus(radius), bottom, _borderPaint)
			canvas.drawLine(left, bottom.minus(radius), left, bottom, _borderPaint)
		}
	}
}