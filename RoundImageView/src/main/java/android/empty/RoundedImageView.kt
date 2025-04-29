package android.empty

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.empty.model.Corner
import android.empty.roundimageview.R
import android.graphics.Bitmap
import android.graphics.ColorFilter
import android.graphics.Shader.TileMode
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.util.AttributeSet
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.drawable.toDrawable
import kotlin.math.max

class RoundedImageView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet?,
	defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {
	companion object {
		private const val TILE_MODE_UNDEFINED = -2
		private const val TILE_MODE_CLAMP = 0
		private const val TILE_MODE_REPEAT = 1
		private const val TILE_MODE_MIRROR = 2

		const val DEFAULT_RADIUS: Float = 0f
		const val DEFAULT_BORDER_WIDTH: Float = 0f
		private val DEFAULT_TILE_MODE: TileMode = TileMode.CLAMP
		private val SCALE_TYPES = arrayOf(
			ScaleType.MATRIX,
			ScaleType.FIT_XY,
			ScaleType.FIT_START,
			ScaleType.FIT_CENTER,
			ScaleType.FIT_END,
			ScaleType.CENTER,
			ScaleType.CENTER_CROP,
			ScaleType.CENTER_INSIDE
		)

		private fun parseTileMode(tileMode: Int): TileMode? {
			return when (tileMode) {
				TILE_MODE_CLAMP -> TileMode.CLAMP
				TILE_MODE_REPEAT -> TileMode.REPEAT
				TILE_MODE_MIRROR -> TileMode.MIRROR
				else -> null
			}
		}
	}

	private val density = Resources.getSystem().displayMetrics.density

	private val _cornerRadii =
		floatArrayOf(DEFAULT_RADIUS, DEFAULT_RADIUS, DEFAULT_RADIUS, DEFAULT_RADIUS)

	private var _drawable: Drawable? = null
	private var _backgroundDrawable: Drawable? = null
	private var _resource = 0
	private var _backgroundResource = 0

	/**
	 * If `true`, we will also round the background drawable according to the settings on this
	 * ImageView.
	 *
	 * @return whether the background is mutated.
	 */
	private var _mutateBackground = false
	var mutatesBackground: Boolean
		get() = _mutateBackground
		set(mutate) {
			if (_mutateBackground == mutate) {
				return
			}

			_mutateBackground = mutate
			updateBackgroundDrawableAttrs(true)
			invalidate()
		}

	private var _borderColors: ColorStateList =
		ColorStateList.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR)
	var borderColor: Int
		get() = _borderColors.defaultColor
		set(color) {
			val colors = ColorStateList.valueOf(color)
			if (_borderColors == colors) {
				return
			}
			_borderColors = colors
			updateBackgroundDrawableAttrs(false)
			if (_borderWidth > 0) {
				invalidate()
			}
		}

	private var _borderWidth: Float = DEFAULT_BORDER_WIDTH

	private var _colorFilter: ColorFilter? = null
	private var _colorMod = false
	private var _hasColorFilter = false

	private var _isOval = false

	private var _scaleType: ScaleType? = null

	private var _tileModeX: TileMode? = DEFAULT_TILE_MODE
	var tileModeX: TileMode?
		get() = _tileModeX
		set(tileModeX) {
			if (this._tileModeX == tileModeX) {
				return
			}

			this._tileModeX = tileModeX
			updateDrawableAttrs()
			updateBackgroundDrawableAttrs(false)
			invalidate()
		}

	private var _tileModeY: TileMode? = DEFAULT_TILE_MODE
	var tileModeY: TileMode?
		get() = _tileModeY
		set(tileModeY) {
			if (this._tileModeY == tileModeY) {
				return
			}

			this._tileModeY = tileModeY
			updateDrawableAttrs()
			updateBackgroundDrawableAttrs(false)
			invalidate()
		}

	init {
		context.withStyledAttributes(attrs, R.styleable.RoundedImageView, defStyle, 0) {

			getInt(R.styleable.RoundedImageView_android_scaleType, -1).let {
				scaleType = if (it >= 0) SCALE_TYPES[it] else ScaleType.FIT_CENTER
			}

			var cornerRadiusOverride =
				getDimensionPixelSize(R.styleable.RoundedImageView_radius, -1).toFloat()

			_cornerRadii[Corner.TOP_LEFT] =
				getDimensionPixelSize(R.styleable.RoundedImageView_radius_top_left, -1).toFloat()
			_cornerRadii[Corner.TOP_RIGHT] =
				getDimensionPixelSize(R.styleable.RoundedImageView_radius_top_right, -1).toFloat()
			_cornerRadii[Corner.BOTTOM_RIGHT] =
				getDimensionPixelSize(R.styleable.RoundedImageView_radius_bottom_right, -1).toFloat()
			_cornerRadii[Corner.BOTTOM_LEFT] =
				getDimensionPixelSize(R.styleable.RoundedImageView_radius_bottom_left, -1).toFloat()

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
					cornerRadiusOverride = DEFAULT_RADIUS
				}
				var i = 0
				while (i < _cornerRadii.size) {
					_cornerRadii[i] = cornerRadiusOverride
					i++
				}
			}

			getDimension(R.styleable.RoundedImageView_border_width, DEFAULT_BORDER_WIDTH).let {
				_borderWidth = it
			}

			getColorStateList(R.styleable.RoundedImageView_border_color).let {
				_borderColors = it ?: ColorStateList.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR)
			}

			_mutateBackground = getBoolean(R.styleable.RoundedImageView_mutate_background, false)
			_isOval = getBoolean(R.styleable.RoundedImageView_oval, false)

			getInt(R.styleable.RoundedImageView_tile_mode, TILE_MODE_UNDEFINED).let {
				if (it != TILE_MODE_UNDEFINED) {
					tileModeX = parseTileMode(it)
					tileModeY = parseTileMode(it)
				}
			}
			getInt(R.styleable.RoundedImageView_tile_mode_x, TILE_MODE_UNDEFINED).let {
				if (it != TILE_MODE_UNDEFINED) {
					tileModeX = parseTileMode(it)
				}
			}
			getInt(R.styleable.RoundedImageView_tile_mode_y, TILE_MODE_UNDEFINED).let {
				if (it != TILE_MODE_UNDEFINED) {
					tileModeY = parseTileMode(it)
				}
			}

			updateDrawableAttrs()
			updateBackgroundDrawableAttrs(true)

			if (_mutateBackground) {
				super.setBackgroundDrawable(_backgroundDrawable)
			}

		}
	}

	override fun drawableStateChanged() {
		super.drawableStateChanged()
		invalidate()
	}

	override fun getScaleType(): ScaleType {
		return _scaleType!!
	}

	override fun setScaleType(scaleType: ScaleType) {
		if (_scaleType != scaleType) {
			_scaleType = scaleType

			when (scaleType) {
				ScaleType.CENTER,
				ScaleType.CENTER_CROP,
				ScaleType.CENTER_INSIDE,
				ScaleType.FIT_CENTER,
				ScaleType.FIT_START,
				ScaleType.FIT_END,
				ScaleType.FIT_XY -> super.setScaleType(ScaleType.FIT_XY)

				else -> super.setScaleType(scaleType)
			}

			updateDrawableAttrs()
			updateBackgroundDrawableAttrs(false)
			invalidate()
		}
	}

	override fun setImageDrawable(drawable: Drawable?) {
		_resource = 0
		_drawable = RoundedDrawable.fromDrawable(drawable)
		updateDrawableAttrs()
		super.setImageDrawable(_drawable)
	}

	override fun setImageBitmap(bm: Bitmap) {
		_resource = 0
		_drawable = RoundedDrawable.fromBitmap(bm)
		updateDrawableAttrs()
		super.setImageDrawable(_drawable)
	}

	override fun setImageResource(@DrawableRes resId: Int) {
		if (_resource != resId) {
			_resource = resId
			_drawable = resolveResource()
			updateDrawableAttrs()
			super.setImageDrawable(_drawable)
		}
	}

	override fun setImageURI(uri: Uri?) {
		super.setImageURI(uri)
		setImageDrawable(drawable)
	}

	override fun setBackground(background: Drawable) {
		setBackgroundDrawable(background)
	}

	override fun setBackgroundResource(@DrawableRes resId: Int) {
		if (_backgroundResource != resId) {
			_backgroundResource = resId
			_backgroundDrawable = resolveBackgroundResource()
			setBackgroundDrawable(_backgroundDrawable)
		}
	}

	override fun setBackgroundColor(color: Int) {
		_backgroundDrawable = color.toDrawable()
		setBackgroundDrawable(_backgroundDrawable)
	}

	override fun setColorFilter(cf: ColorFilter) {
		if (_colorFilter !== cf) {
			_colorFilter = cf
			_hasColorFilter = true
			_colorMod = true
			applyColorMod()
			invalidate()
		}
	}

	private fun applyColorMod() {
		if (_drawable != null && _colorMod) {
			_drawable = _drawable!!.mutate()
			if (_hasColorFilter) {
				_drawable!!.colorFilter = _colorFilter
			}
		}
	}

	private fun updateAttrs(drawable: Drawable?, scaleType: ScaleType?) {
		if (drawable == null) {
			return
		}

		if (drawable is RoundedDrawable) {
			drawable
				.setScaleType(scaleType)
				.setBorderWidth(_borderWidth)
				.setBorderColor(_borderColors)
				.setOval(_isOval)
				.setTileModeX(_tileModeX)
				.setTileModeY(_tileModeY)

			@Suppress("SENSELESS_COMPARISON")
			if (_cornerRadii != null) {
				drawable.setCornerRadius(
					_cornerRadii[Corner.TOP_LEFT],
					_cornerRadii[Corner.TOP_RIGHT],
					_cornerRadii[Corner.BOTTOM_RIGHT],
					_cornerRadii[Corner.BOTTOM_LEFT]
				)
			}

			applyColorMod()
		} else if (drawable is LayerDrawable) {
			// loop through layers to and set drawable attrs
			val ld = drawable
			var i = 0
			val layers = ld.numberOfLayers
			while (i < layers) {
				updateAttrs(ld.getDrawable(i), scaleType)
				i++
			}
		}
	}

	@Deprecated("")
	override fun setBackgroundDrawable(background: Drawable?) {
		_backgroundDrawable = background
		updateBackgroundDrawableAttrs(true)
		super.setBackgroundDrawable(_backgroundDrawable)
	}

	var cornerRadius: Float
		get() = maxCornerRadius
		/**
		 * Set the corner radii of all corners in px.
		 *
		 * @param radius the radius to set.
		 */
		set(radius) {
			setCornerRadius(radius, radius, radius, radius)
		}

	val maxCornerRadius: Float
		/**
		 * @return the largest corner radius.
		 */
		get() {
			var maxRadius = 0f
			for (r in _cornerRadii) {
				maxRadius = max(r.toDouble(), maxRadius.toDouble()).toFloat()
			}
			return maxRadius
		}

	/**
	 * Get the corner radius of a specified corner.
	 *
	 * @param corner the corner.
	 * @return the radius.
	 */
	fun getCornerRadius(@Corner corner: Int): Float {
		return _cornerRadii[corner]
	}

	/**
	 * Set all the corner radii from a dimension resource id.
	 *
	 * @param resId dimension resource id of radii.
	 */
	fun setCornerRadiusDimen(@DimenRes resId: Int) {
		val radius = resources.getDimension(resId)
		setCornerRadius(radius, radius, radius, radius)
	}

	/**
	 * Set the corner radius of a specific corner from a dimension resource id.
	 *
	 * @param corner the corner to set.
	 * @param resId  the dimension resource id of the corner radius.
	 */
	fun setCornerRadiusDimen(@Corner corner: Int, @DimenRes resId: Int) {
		setCornerRadius(corner, resources.getDimensionPixelSize(resId).toFloat())
	}

	/**
	 * Set the corner radius of a specific corner in px.
	 *
	 * @param corner the corner to set.
	 * @param radius the corner radius to set in px.
	 */
	fun setCornerRadius(@Corner corner: Int, radius: Float) {
		if (_cornerRadii[corner] == radius) {
			return
		}
		_cornerRadii[corner] = radius

		updateDrawableAttrs()
		updateBackgroundDrawableAttrs(false)
		invalidate()
	}

	/**
	 * Set the corner radii of each corner individually. Currently only one unique nonzero values is
	 * supported.
	 *
	 * @param topLeft     radius of the top left corner in px.
	 * @param topRight    radius of the top right corner in px.
	 * @param bottomRight radius of the bottom right corner in px.
	 * @param bottomLeft  radius of the bottom left corner in px.
	 */
	fun setCornerRadius(topLeft: Float, topRight: Float, bottomLeft: Float, bottomRight: Float) {
		if (_cornerRadii[Corner.TOP_LEFT] == topLeft
			&& _cornerRadii[Corner.TOP_RIGHT] == topRight
			&& _cornerRadii[Corner.BOTTOM_RIGHT] == bottomRight
			&& _cornerRadii[Corner.BOTTOM_LEFT] == bottomLeft
		) {
			return
		}

		_cornerRadii[Corner.TOP_LEFT] = topLeft
		_cornerRadii[Corner.TOP_RIGHT] = topRight
		_cornerRadii[Corner.BOTTOM_LEFT] = bottomLeft
		_cornerRadii[Corner.BOTTOM_RIGHT] = bottomRight

		updateDrawableAttrs()
		updateBackgroundDrawableAttrs(false)
		invalidate()
	}

	fun setBorderWidth(@DimenRes resId: Int) {
		setBorderWidth(resources.getDimension(resId))
	}

	fun setBorderWidth(width: Float) {
		if (_borderWidth == width) {
			return
		}

		_borderWidth = width.times(density)
		updateDrawableAttrs()
		updateBackgroundDrawableAttrs(false)
		invalidate()
	}

	private fun resolveResource(): Drawable? {
		return RoundedDrawable.fromDrawable(ResourcesCompat.getDrawable(resources, _resource, null))
	}

	private fun resolveBackgroundResource(): Drawable? {
		return RoundedDrawable.fromDrawable(ResourcesCompat.getDrawable(resources, _resource, null))
	}

	private fun updateDrawableAttrs() {
		updateAttrs(_drawable, _scaleType)
	}

	private fun updateBackgroundDrawableAttrs(convert: Boolean) {
		if (_mutateBackground) {
			if (convert) {
				_backgroundDrawable = RoundedDrawable.fromDrawable(_backgroundDrawable)
			}
			updateAttrs(_backgroundDrawable, ScaleType.FIT_XY)
		}
	}
}