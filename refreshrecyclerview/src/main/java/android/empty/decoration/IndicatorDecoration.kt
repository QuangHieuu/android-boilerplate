package android.empty.decoration

import android.content.res.Resources
import android.empty.layoutManager.LoopingLayoutManager
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.IntDef
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import kotlin.math.max


private fun RecyclerView.LayoutManager?.getOrientation(): Int {
	return when (this) {
		is LinearLayoutManager -> orientation
		is LoopingLayoutManager -> orientation
		else -> RecyclerView.VERTICAL
	}
}

private fun RecyclerView.LayoutManager?.findFirstVisibleItemPosition(): Int {
	return when (this) {
		is LinearLayoutManager -> findFirstVisibleItemPosition()
		is LoopingLayoutManager -> findFirstVisibleItemPosition()
		else -> RecyclerView.NO_POSITION
	}
}

private fun RecyclerView.LayoutManager?.findLastVisibleItemPosition(): Int {
	return when (this) {
		is LinearLayoutManager -> findLastVisibleItemPosition()
		is LoopingLayoutManager -> findLastVisibleItemPosition()
		else -> RecyclerView.NO_POSITION
	}
}

class IndicatorBuilder<T : RecyclerView.Adapter<*>>(
	private val _recyclerView: RecyclerView,
	private val _adapter: T
) {
	private val _instance = IndicatorDecoration()

	private var _snapHelper: SnapHelper = LinearSnapHelper()

	init {
		_recyclerView.adapter = _adapter
		_instance.attachAdapter = _adapter

		orientation(_recyclerView.layoutManager.getOrientation())
	}

	fun isUnderView(): IndicatorBuilder<T> {
		_instance.isUnderView = true
		return this
	}

	fun indicatorTextSize(@Dimension size: Float): IndicatorBuilder<T> {
		_instance.paint.textSize = size
		return this
	}

	fun indicatorType(@IndicatorType type: Int): IndicatorBuilder<T> {
		_instance.indicatorType = type
		return this
	}

	fun indicatorCircleEffect(@CircleEffect effect: Int): IndicatorBuilder<T> {
		_instance.circleEffect = effect
		return this
	}

	fun indicatorWidth(@Dimension height: Float): IndicatorBuilder<T> {
		_instance.indicatorWidth = height
		return this
	}

	fun smallIndicatorWidth(@Dimension height: Float): IndicatorBuilder<T> {
		_instance.smallIndicatorWidth = height
		return this
	}

	fun indicatorPadding(@Dimension padding: Float): IndicatorBuilder<T> {
		_instance.indicatorPadding = padding
		return this
	}

	fun activeColor(@ColorInt color: Int): IndicatorBuilder<T> {
		_instance.paintActive.color = color
		return this
	}

	fun inactiveColor(@ColorInt color: Int): IndicatorBuilder<T> {
		_instance.paintInActive.color = color
		return this
	}

	fun orientation(orientation: Int): IndicatorBuilder<T> {
		_instance.orientation = orientation
		return this
	}

	fun snapHelper(snapHelper: SnapHelper): IndicatorBuilder<T> {
		_snapHelper = snapHelper
		return this
	}

	fun build(): IndicatorBuilder<T> {
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

	var attachAdapter: RecyclerView.Adapter<*>? = null

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
			return if (field == 0f) indicatorWidth - 5f else field
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
	var isUnderView = false

	val paint: Paint = Paint().apply {
		isAntiAlias = true
		textSize = 16 * density
		strokeWidth = 2f * density
	}
	val paintActive = Paint(paint).apply {
		color = "#000000".toColorInt()
	}
	val paintInActive = Paint(paint).apply {
		color = "#FFFFFF".toColorInt()
	}

	private val rectSize: Float = 15 * density
	private val circleRadius: Float
		get() {
			return indicatorWidth / 2
		}
	private val smallCircleRadius: Float
		get() = smallIndicatorWidth / 2.5f

	private val indicatorDistanceItem: Float
		get() {
			return indicatorWidth + indicatorPadding + if (circleEffect == CircleEffect.RECT) rectSize else 0f
		}

	override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
		super.onDrawOver(c, parent, state)

		if (parent.adapter == null || parent.layoutManager == null || attachAdapter == null) {
			return
		}

		if (isUnderView) {
			parent.setPadding(0, 0, 0, paddingBottom.toInt() * 2)
		}

		val indicatorStartX: Float
		val indicatorPosY: Float

		val itemCount = attachAdapter!!.itemCount

		if (indicatorTexts.isEmpty()) {
			for (i in 0 until itemCount) {
				indicatorTexts.add(i.toString())
			}
		}

		val lastItem = max(0, itemCount - 1)
		val totalWidth =
			indicatorWidth.plus(if (circleEffect == CircleEffect.RECT) rectSize else 0f).times(lastItem)

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
			drawIndicator(c, indicatorStartX, start, i, i - 1, itemCount, g = true)
			start += indicatorDistanceItem
		}

		val layoutManager = parent.layoutManager!!
		val lastActivePosition = layoutManager.findLastVisibleItemPosition()
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
			val change = if (progress == 0f) 0f else partialLength
			drawIndicator(
				c,
				highlightStart + change,
				indicatorPosY,
				activePosition,
				lastActivePosition,
				itemCount,
				change
			)
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
			drawIndicator(c, start, indicatorPosY, i, -1, itemCount, g = true)
			start += indicatorDistanceItem
		}

		val layoutManager = parent.layoutManager!!
		val lastActivePosition = layoutManager.findLastVisibleItemPosition()
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
			val change = if (progress == 0f) 0f else partialLength
			drawIndicator(
				c,
				highlightStart + change,
				indicatorPosY,
				activePosition,
				lastActivePosition,
				itemCount,
				change
			)
		}
	}

	/**
	 *
	 * @param a Canvas
	 * @param b x or moving x if orientation == RecyclerView.HORIZONTAL
	 * @param c y or moving y if orientation == RecyclerVIew.VERTICAL
	 * @param d active position
	 * @param l last active position
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
		l: Int,
		e: Int,
		f: Float = 0f,
		g: Boolean = false
	) {
		when (indicatorType) {
			IndicatorType.CIRCLE -> drawCircleEffect(a, b, c, d, l, e, f, g)
			IndicatorType.TEXT -> a.drawText(indicatorTexts[d], b, c, checkDrawInActive(g))
			IndicatorType.LINE -> drawLine(a, b, c, d, l, e, f, g)
		}
	}

	private fun drawCircleEffect(
		c: Canvas,
		x: Float,
		y: Float,
		activePosition: Int,
		lastActivePosition: Int,
		itemCount: Int,
		partialLength: Float = 0f,
		isDrawNormal: Boolean = false
	) {
		when (circleEffect) {
			CircleEffect.CUT_OFF -> drawCircleCutOffEffect(
				c,
				x,
				y,
				activePosition,
				itemCount,
				partialLength,
				isDrawNormal
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
				activePosition,
				lastActivePosition,
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
		isDrawNormal: Boolean,
	) {
		val percent = partialLength * 6
		var currentX = x - calculateOnHorizontal(partialLength)
		var currentY = y - calculateOnVertical(partialLength)

		c.drawCircle(currentX, currentY, circleRadius, checkDrawInActive(isDrawNormal))
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
		activePosition: Int,
		lastActivePosition: Int,
		itemCount: Int,
		partialLength: Float = 0f,
		isDrawNormal: Boolean = false
	) {
		if (isDrawNormal) {
			c.drawCircle(x, y, circleRadius, paintActive)
		} else {
			var center = x - partialLength
			c.drawRoundRect(
				center + (rectSize - partialLength / 2),
				y - indicatorWidth / 2f,
				center - (rectSize - partialLength / 2),
				y + indicatorWidth / 2f,
				circleRadius,
				circleRadius,
				paintActive
			)
			if (activePosition < itemCount - 1) {
				center = calculatorDistance(activePosition, lastActivePosition, itemCount, center)
				c.drawRoundRect(
					center - partialLength / 2,
					y - indicatorWidth / 2f,
					center + partialLength / 2,
					y + indicatorWidth / 2f,
					circleRadius,
					circleRadius,
					paintActive
				)
			}
		}
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
		activePosition: Int,
		lastActivePosition: Int,
		itemCount: Int,
		partialLength: Float = 0f,
		isDrawNormal: Boolean = false
	) {
		var start = x - partialLength
		c.drawLine(x, y, start + indicatorWidth, y, checkDrawInActive(isDrawNormal))

		if (activePosition < itemCount - 1) {
			start = calculatorDistance(activePosition, lastActivePosition, itemCount, start)
			c.drawLine(
				start,
				y,
				start + partialLength,
				y,
				checkDrawInActive(isDrawNormal)
			)
		}
	}

	private fun calculatorDistance(
		activePosition: Int,
		lastPosition: Int,
		itemCount: Int,
		currentDistance: Float,
	): Float {
		return currentDistance + if (lastPosition == itemCount - 1 && activePosition < lastPosition && activePosition == 0) {
			(itemCount - 1)
		} else {
			1
		} * indicatorDistanceItem
	}
}
