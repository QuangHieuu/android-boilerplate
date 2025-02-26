package boilerplate.widget.recyclerview

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.IntDef
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import kotlin.math.max


class IndicatorBuilder(
	private val _recyclerView: RecyclerView
) {
	private val _instance = IndicatorDecoration()

	private var _snapHelper: SnapHelper = LinearSnapHelper()

	init {
		(_recyclerView.layoutManager as? LinearLayoutManager)?.let { orientation(it.orientation) }
	}

	fun paddingBottom(@Dimension padding: Float): IndicatorBuilder {
		_instance.paddingBottom = padding
		return this
	}

	fun indicatorTextSize(@Dimension size: Float): IndicatorBuilder {
		_instance.paint.textSize = size
		return this
	}

	fun indicatorType(@IndicatorType type: Int): IndicatorBuilder {
		_instance.indicatorType = type
		return this
	}

	fun indicatorCircleEffect(@CircleEffect effect: Int): IndicatorBuilder {
		_instance.circleEffect = effect
		return this
	}

	fun indicatorWidth(@Dimension height: Float): IndicatorBuilder {
		_instance.indicatorWidth = height
		return this
	}

	fun smallIndicatorWidth(@Dimension height: Float): IndicatorBuilder {
		_instance.smallIndicatorWidth = height
		return this
	}

	fun indicatorPadding(@Dimension padding: Float): IndicatorBuilder {
		_instance.indicatorPadding = padding
		return this
	}

	fun activeColor(@ColorInt color: Int): IndicatorBuilder {
		_instance.paintActive.color = color
		return this
	}

	fun inactiveColor(@ColorInt color: Int): IndicatorBuilder {
		_instance.paintInActive.color = color
		return this
	}

	fun orientation(orientation: Int): IndicatorBuilder {
		_instance.orientation = orientation
		return this
	}

	fun snapHelper(snapHelper: SnapHelper): IndicatorBuilder {
		_snapHelper = snapHelper
		return this
	}

	fun build(): IndicatorBuilder {
		with(_recyclerView) {
			_snapHelper.attachToRecyclerView(this)
			addItemDecoration(_instance)
		}
		return this
	}
}

@Retention(AnnotationRetention.SOURCE)
@IntDef(
	CircleEffect.NORMAL,
	CircleEffect.SMALL,
	CircleEffect.INFINITY,
	CircleEffect.CUT_OFF,
	CircleEffect.RECT
)
annotation class CircleEffect {
	companion object {
		const val NORMAL = 0
		const val SMALL = 1
		const val INFINITY = 2
		const val CUT_OFF = 3
		const val RECT = 4
	}
}

@Retention(AnnotationRetention.SOURCE)
@IntDef(IndicatorType.LINE, IndicatorType.CIRCLE, IndicatorType.TEXT)
annotation class IndicatorType {
	companion object {
		const val LINE = 0
		const val CIRCLE = 1
		const val TEXT = 2
	}
}

private class IndicatorDecoration : RecyclerView.ItemDecoration() {
	private val density = Resources.getSystem().displayMetrics.density

	@Dimension
	var indicatorWidth: Float = 6F * density
		get() {
			return when (indicatorType) {
				IndicatorType.TEXT -> paint.textSize
				else -> field
			}
		}

	@Dimension
	var smallIndicatorWidth: Float = 0f
		get() {
			return if (field == 0f) indicatorWidth - 3f else field
		}

	@Dimension
	var indicatorPadding: Float = 0f
		get() {
			return if (field == 0f) {
				indicatorWidth / 2f
			} else {
				field
			}
		}

	@IndicatorType
	var indicatorType: Int = IndicatorType.CIRCLE

	@CircleEffect
	var circleEffect: Int = CircleEffect.NORMAL

	var paddingBottom: Float = 10 * density
	var orientation: Int = RecyclerView.HORIZONTAL
	var indicatorTexts: ArrayList<String> = arrayListOf()
	var interpolator: Interpolator = AccelerateDecelerateInterpolator()

	val paint: Paint = Paint().apply {
		isAntiAlias = true
		textSize = 16 * density
		strokeWidth = 2f * density
	}
	val paintActive = Paint(paint).apply {
		color = Color.parseColor("#03A959")
	}
	val paintInActive = Paint(paint).apply {
		color = Color.parseColor("#FFFFFF")
	}

	private val circleRadius: Float
		get() {
			return indicatorWidth / 2
		}
	private val smallCircleRadius: Float
		get() = smallIndicatorWidth / 2

	private val indicatorDistanceItem: Float
		get() {
			return indicatorWidth + indicatorPadding
		}

	override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
		super.onDrawOver(c, parent, state)

		if (parent.adapter == null || parent.layoutManager == null) {
			return
		}
		val totalWidth: Float
		val indicatorStartX: Float
		val indicatorPosY: Float

		val itemCount = parent.adapter!!.itemCount

		if (indicatorTexts.isEmpty()) {
			for (i in 0 until itemCount) {
				indicatorTexts.add(i.toString())
			}
		}

		val lastItem = max(0, itemCount - 1)
		totalWidth = indicatorWidth * lastItem

		val paddingBetweenItems = indicatorPadding * lastItem
		val indicatorTotalWidth = (totalWidth + paddingBetweenItems) / 2

		when (orientation) {
			RecyclerView.HORIZONTAL -> {
				indicatorStartX = parent.width / 2f - indicatorTotalWidth
				indicatorPosY = parent.height - paddingBottom
				drawHorizontal(c, parent, indicatorStartX, indicatorPosY, itemCount)
			}

			RecyclerView.VERTICAL -> {
				indicatorStartX = parent.width - indicatorWidth - paddingBottom
				indicatorPosY = parent.height / 2f - indicatorTotalWidth
				drawVertical(c, parent, indicatorStartX, indicatorPosY, itemCount)
			}
		}
	}

	private fun calculateOnHorizontal(input: Float): Float {
		return if (orientation == RecyclerView.HORIZONTAL) input else 0f
	}

	private fun calculateOnVertical(input: Float): Float {
		return if (orientation == RecyclerView.VERTICAL) input else 0f
	}

	private fun checkDrawInActive(isInActive: Boolean): Paint {
		return if (isInActive) paintInActive else paintActive
	}

	private fun checkUseCircleRadius(isDrawNormal: Boolean): Float {
		return if (isDrawNormal) smallCircleRadius else circleRadius
	}

	private fun drawVertical(
		c: Canvas,
		parent: RecyclerView,
		indicatorStartX: Float,
		indicatorPosY: Float,
		itemCount: Int
	) {
		var start = indicatorPosY
		for (i in 0 until itemCount) {
			drawIndicator(c, indicatorStartX, start, i, itemCount, g = true)
			start += indicatorDistanceItem
		}

		val layoutManager = parent.layoutManager as LinearLayoutManager
		val activePosition = layoutManager.findFirstVisibleItemPosition()
		if (activePosition == RecyclerView.NO_POSITION) {
			return
		}

		layoutManager.findViewByPosition(activePosition)?.let { activeChild ->
			val top = activeChild.top
			val height = activeChild.height
			val progress: Float = interpolator.getInterpolation(top * -1 / height.toFloat())
			val partialLength =
				progress * if (indicatorType == IndicatorType.CIRCLE) indicatorDistanceItem else indicatorWidth
			val highlightStart = indicatorPosY + indicatorDistanceItem * activePosition
			if (progress == 0f) {
				drawIndicator(c, indicatorStartX, highlightStart, activePosition, itemCount, 0f)
			} else {
				drawIndicator(
					c,
					indicatorStartX,
					highlightStart + partialLength,
					activePosition,
					itemCount,
					partialLength
				)
			}
		}
	}

	private fun drawHorizontal(
		c: Canvas,
		parent: RecyclerView,
		indicatorStartX: Float,
		indicatorPosY: Float,
		itemCount: Int
	) {
		var start = indicatorStartX
		for (i in 0 until itemCount) {
			drawIndicator(c, start, indicatorPosY, i, itemCount, g = true)
			start += indicatorDistanceItem
		}

		val layoutManager = parent.layoutManager as LinearLayoutManager
		val activePosition = layoutManager.findFirstVisibleItemPosition()
		if (activePosition == RecyclerView.NO_POSITION) {
			return
		}

		layoutManager.findViewByPosition(activePosition)?.let { activeChild ->
			val left = activeChild.left
			val width = activeChild.width
			val progress: Float = interpolator.getInterpolation(left * -1 / width.toFloat())
			val partialLength =
				progress * if (indicatorType == IndicatorType.CIRCLE) indicatorDistanceItem else indicatorWidth
			val highlightStart = indicatorStartX + indicatorDistanceItem * activePosition
			if (progress == 0f) {
				drawIndicator(c, highlightStart, indicatorPosY, activePosition, itemCount, 0f)
			} else {
				drawIndicator(
					c,
					highlightStart + partialLength,
					indicatorPosY,
					activePosition,
					itemCount,
					partialLength
				)
			}
		}
	}

	/**
	 *
	 * @param a Canvas
	 * @param b x or moving x if orientation == RecyclerView.HORIZONTAL
	 * @param c y or moving y if orientation == RecyclerVIew.VERTICAL
	 * @param d active position
	 * @param e partial length between drawer
	 * @param f number of item in recycler view
	 * @param g is current draw inactive indicator
	 *
	 */
	private fun drawIndicator(
		a: Canvas,
		b: Float,
		c: Float,
		d: Int,
		e: Int,
		f: Float = 0f,
		g: Boolean = false
	) {
		when (indicatorType) {
			IndicatorType.CIRCLE -> drawCircleEffect(a, b, c, d, e, f, g)
			IndicatorType.TEXT -> a.drawText(indicatorTexts[d], b, c, checkDrawInActive(g))
			IndicatorType.LINE -> drawLine(a, b, c, d, e, f, g)
		}
	}

	private fun drawCircleEffect(
		c: Canvas,
		x: Float,
		y: Float,
		highlightPosition: Int,
		itemCount: Int,
		partialLength: Float = 0f,
		isDrawNormal: Boolean = false
	) {
		when (circleEffect) {
			CircleEffect.CUT_OFF -> drawCircleCutOffEffect(
				c,
				x,
				y,
				highlightPosition,
				itemCount,
				partialLength
			)

			CircleEffect.SMALL -> drawCircle(
				c,
				x,
				y,
				checkUseCircleRadius(isDrawNormal),
				isDrawNormal
			)

			CircleEffect.RECT -> drawCircleRect(
				c,
				x,
				y,
				highlightPosition,
				itemCount,
				partialLength,
				isDrawNormal
			)

			CircleEffect.NORMAL -> drawCircle(c, x, y, circleRadius, isDrawNormal)
			else -> drawCircle(c, x, y, circleRadius, isDrawNormal)
		}
	}

	private fun drawCircleCutOffEffect(
		c: Canvas,
		x: Float,
		y: Float,
		highlightPosition: Int,
		itemCount: Int,
		partialLength: Float = 0f,
	) {
		val percent = partialLength * 6
		var currentX = x - calculateOnHorizontal(partialLength)
		var currentY = y - calculateOnVertical(partialLength)

		c.drawCircle(currentX, currentY, circleRadius, paintActive)
		c.drawArc(
			RectF(
				currentX - circleRadius,
				currentY - circleRadius,
				currentX + circleRadius,
				currentY + circleRadius
			),
			percent,
			-percent,
			true,
			paintInActive
		)
		if (highlightPosition < itemCount - 1) {
			currentX += calculateOnHorizontal(indicatorDistanceItem)
			currentY += calculateOnVertical(indicatorDistanceItem)

			c.drawArc(
				RectF(
					currentX - circleRadius,
					currentY - circleRadius,
					currentX + circleRadius,
					currentY + circleRadius
				),
				180 + percent,
				percent,
				true,
				paintActive
			)
		}
	}

	private fun drawCircleRect(
		c: Canvas,
		x: Float,
		y: Float,
		highlightPosition: Int,
		itemCount: Int,
		partialLength: Float = 0f,
		isDrawNormal: Boolean = false
	) {

	}

	private fun drawCircle(
		c: Canvas,
		x: Float,
		y: Float,
		radius: Float,
		isDrawNormal: Boolean = false
	) {
		c.drawCircle(x, y, radius, checkDrawInActive(isDrawNormal))
	}

	private fun drawLine(
		c: Canvas,
		x: Float,
		y: Float,
		highlightPosition: Int,
		itemCount: Int,
		partialLength: Float = 0f,
		isDrawNormal: Boolean = false
	) {
		var highlightStart = x - partialLength
		c.drawLine(x, y, highlightStart + indicatorWidth, y, checkDrawInActive(isDrawNormal))

		if (highlightPosition < itemCount - 1) {
			highlightStart += indicatorDistanceItem
			c.drawLine(
				highlightStart,
				y,
				highlightStart + partialLength,
				y,
				checkDrawInActive(isDrawNormal)
			)
		}
	}
}
