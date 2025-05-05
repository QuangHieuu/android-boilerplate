package calendar.widget.wheel.base

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.empty.calendar.R
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.Scroller
import androidx.core.animation.doOnEnd
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.withClip
import calendar.widget.wheel.model.WheelModel
import calendar.widget.wheel.model.WidthType
import calendar.widget.wheel.view.WheelPicker.Companion.KEY_DAY_OF_WEEK_FROM_SUNDAY
import calendar.widget.wheel.view.WheelPicker.Companion.KEY_DAY_ONLY_NUMBER
import calendar.widget.wheel.view.WheelPicker.Companion.KEY_HOUR_12_FORMAT
import calendar.widget.wheel.view.WheelPicker.Companion.KEY_MONTH_ONLY_NUMBER
import calendar.widget.wheel.view.WheelPicker.Companion.KEY_REVERSE
import calendar.widget.wheel.view.WheelPicker.Companion.KEY_SHOW_CURRENT
import calendar.widget.wheel.view.WheelPicker.Companion.KEY_TODAY_TEXT
import calendar.widget.wheel.view.WheelPicker.Companion.KEY_TYPE
import kotlin.math.*

interface IWheelListener {

	fun onScroll(value: Int, display: String)
}

abstract class DefaultWheelView : View {

	companion object {

		const val MAX_ANGLE: Int = 90
		const val ALIGN_CENTER: Int = 0
		const val ALIGN_LEFT: Int = 1
		const val ALIGN_RIGHT: Int = 2
	}

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
		setup(attrs)
	}

	constructor(
		context: Context,
		parent: AttributeSet? = null,
		attrs: AttributeSet
	) : super(context, parent) {
		setup(attrs)
	}

	protected val arguments: Bundle = Bundle()

	private val _density = Resources.getSystem().displayMetrics.density

	private val _padding: Float
		get() = _density.times(5)
	private val _minWidth: Float
		get() = _density.times(50)

	private var _tracker: VelocityTracker? = VelocityTracker.obtain()
	private val _scroller: Scroller = Scroller(context)

	private val _camera: Camera = Camera()

	private val _handle: Handler = Handler(Looper.getMainLooper())
	private val _adapter: Adapter = Adapter()

	private val _rectDraw: Rect = Rect()
	private val _paint = Paint().apply {
		style = Paint.Style.FILL
		color = Color.BLACK
		textSize = _density.times(20)
	}

	private var _visibleCount: Int = 7
		get() {
			return if (field % 2 == 0) field + 1 else field
		}
	private val _drawCount: Int
		get() = _visibleCount.plus(2)
	private val _halfCount: Int
		get() = _drawCount.div(2)

	private var _viewWidth: Int = 0
	private var _widthType: WidthType = WidthType.WRAP

	private var _viewHeight: Int = 0
	private var _itemHeight: Int = 0
	private var _halfItemHeight: Int = 0
	private var _halfWheelHeight: Int = 0
	private val _overScroll: Int
		get() = _density.times(10).toInt()

	private var _currentPosition: Int = 0
	private var _scrollPosition: Int = -1

	private var _angle = MAX_ANGLE
	private var _limitTop = 0
	private var _limitBottom = 0

	private var _isOverScroll: Boolean = false
	private var _isCurved: Boolean = true
	private var _isCyclic: Boolean = false

	private var _wheelCenterX: Int = 0
	private var _wheelCenterY: Int = 0
	private var _drawCenterX: Int = 0
	private var _drawCenterY: Int = 0

	private val _matrixRotate: Matrix = Matrix()
	private val _matrixDepth: Matrix = Matrix()

	private var _isClick: Boolean = false
	private var _isForceFinishScroll: Boolean = false

	private val _conf: ViewConfiguration = ViewConfiguration.get(context)
	private val _minimumVelocity: Int
		get() = _conf.scaledMinimumFlingVelocity
	private val _maximumVelocity: Int
		get() = _conf.scaledMaximumFlingVelocity
	private val _touchSlop: Int
		get() = _conf.scaledTouchSlop

	private var _minFlingY: Int = 0
	private var _maxFlingY: Int = 0
	private var _scrollOffsetY: Int = 0

	private var _firstPointY = 0
	private var _lastPointY = 0

	private var _align: Int = ALIGN_CENTER

	private var _iListener: IWheelListener? = null

	private val _runnable: Runnable = object : Runnable {
		override fun run() {
			val itemCount: Int = _adapter.itemCount
			if (itemCount == 0 || _itemHeight == 0) return
			if (_scroller.isFinished && !_isForceFinishScroll) {
				(-_scrollOffsetY).div(_itemHeight).rem(itemCount).let {
					if (it < 0) it + itemCount else it
				}.also {
					_currentPosition = it
					currentScroll(it)
				}
			}
			if (_scroller.computeScrollOffset()) {
				_scrollOffsetY = _scroller.currY
				postInvalidate()
				_handle.postDelayed(this, 20)
			}
		}
	}

	private fun setup(attrs: AttributeSet) {
		context.withStyledAttributes(attrs, R.styleable.DefaultWheelView) {
			_isOverScroll = getBoolean(R.styleable.DefaultWheelView_wheel_over_scroll, true)
			_isCyclic = getBoolean(R.styleable.DefaultWheelView_wheel_cycle, false)

			arguments.apply {
				putInt(KEY_TYPE, getInt(R.styleable.DefaultWheelView_wheel_type, -1))
				putBoolean(
					KEY_SHOW_CURRENT,
					getBoolean(R.styleable.DefaultWheelView_wheel_show_current, false)
				)
				putBoolean(
					KEY_REVERSE,
					getBoolean(R.styleable.DefaultWheelView_wheel_reverse_value, false)
				)
				putBoolean(
					KEY_HOUR_12_FORMAT,
					getBoolean(R.styleable.DefaultWheelView_wheel_hour_12_format, false)
				)
				putBoolean(
					KEY_MONTH_ONLY_NUMBER,
					getBoolean(R.styleable.DefaultWheelView_wheel_month_only_number, false)
				)
				putBoolean(
					KEY_REVERSE,
					getBoolean(R.styleable.DefaultWheelView_wheel_reverse_value, false)
				)
				putBoolean(
					KEY_DAY_ONLY_NUMBER,
					getBoolean(R.styleable.DefaultWheelView_wheel_day_show_only_number, true)
				)
				putString(
					KEY_TODAY_TEXT,
					getString(R.styleable.DefaultWheelView_wheel_day_show_today_text)
				)
				putBoolean(
					KEY_DAY_OF_WEEK_FROM_SUNDAY,
					getBoolean(R.styleable.DefaultWheelView_wheel_day_of_week_from_sunday, false)
				)
			}
		}
		reset()
	}

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		val modeWidth = MeasureSpec.getMode(widthMeasureSpec)
		val modeHeight = MeasureSpec.getMode(heightMeasureSpec)

		val sizeWidth = MeasureSpec.getSize(widthMeasureSpec)
		val sizeHeight = MeasureSpec.getSize(heightMeasureSpec)

		// Correct sizes of original content
		var resultWidth = _viewWidth
		var resultHeight: Int = _viewHeight.plus(_paint.textSize).times(_visibleCount).toInt().let {
			// Correct view sizes again if curved is enable
			if (_isCurved) {
				// The text is written on the circle circumference from -mMaxAngle to mMaxAngle.
				// 2 * sinDegree(mMaxAngle): Height of drawn circle
				// Math.PI: Circumference of half unit circle, `mMaxAngle / 90f`: The ratio of half-circle we draw on
				val angle = Math.PI.times(_angle).div(MAX_ANGLE)
				sinDegree(_angle.toFloat().times(2)).times(2).div(angle).times(it).toInt()
			} else {
				it
			}
		}

		// Consideration padding influence the view sizes
		resultWidth += paddingStart.plus(paddingEnd)
		resultHeight += paddingTop.plus(paddingBottom)

		// Consideration sizes of parent can influence the view sizes
		resultWidth = measureSize(modeWidth, sizeWidth, resultWidth)
		resultHeight = measureSize(modeHeight, sizeHeight, resultHeight)

		setMeasuredDimension(resultWidth, resultHeight)

		if (_currentPosition > 0) {
			post { computeFlingToPosition() }
		}
	}

	override fun onDraw(canvas: Canvas) {
		if (_adapter.isEmpty || _itemHeight - _halfCount <= 0) return
		val drawnDataStartPos = (-_scrollOffsetY).div(_itemHeight).minus(_halfCount)
		var drawnDataPos = drawnDataStartPos
		var drawnOffsetPos = -_halfCount

		while (drawnDataPos < drawnDataStartPos.plus(_drawCount)) {
			val data = when {
				_isCyclic -> {
					val itemCount = _adapter.itemCount
					val actualPos = drawnDataPos.rem(itemCount).let {
						if (it > 0) it.plus(itemCount) else it
					}
					_adapter.getItemDisplay(actualPos)
				}

				isPosInRang(drawnDataPos) -> _adapter.getItemDisplay(drawnDataPos)
				else -> ""
			}
			val height = drawnOffsetPos.times(_itemHeight)
			val centerY = _drawCenterY.plus(height).plus(_scrollOffsetY.rem(_itemHeight))

			val distanceToCenter: Float = if (_isCurved) {
				// Correct ratio of item's drawn center to wheel center
				val ratio: Float =
					(_drawCenterY.minus(abs(_drawCenterY.minus(centerY))).minus(_rectDraw.top))
						.times(1.0f)
						.div(_drawCenterY.minus(_rectDraw.top))

				// Correct unit
				val unit = when {
					centerY > _drawCenterY -> 1
					centerY < _drawCenterY -> -1
					else -> 0
				}

				val degree = clamp(
					-(1.minus(ratio)).times(_angle).times(unit),
					-_angle.toFloat(),
					_angle.toFloat()
				)
				computeYCoordinateAtAngle(degree).apply {
					val transX = when (_align) {
						ALIGN_LEFT -> _rectDraw.left.toFloat()
						ALIGN_RIGHT -> _rectDraw.right.toFloat()
						else -> _wheelCenterX.toFloat()
					}
					val transY: Float = _wheelCenterY.minus(this)

					_camera.apply {
						save()
						rotateX(degree)
						getMatrix(_matrixRotate)
						restore()
					}

					_matrixRotate.apply {
						preTranslate(-transX, -transY)
						postTranslate(transX, transY)
					}

					_camera.apply {
						save()
						translate(0f, 0f, computeDepth(degree.toInt().toFloat()))
						getMatrix(_matrixDepth)
						restore()
					}

					_matrixDepth.apply {
						preTranslate(-transX, -transY)
						postTranslate(transX, transY)
					}

					_matrixRotate.postConcat(_matrixDepth)
				}
			} else {
				0f
			}

			val alpha = (_drawCenterY.minus(abs((_drawCenterY.minus(centerY)))))
				.times(1.0f)
				.div(_drawCenterY)
				.times(255)
				.let { if (it > 0) it else 0 }

			_paint.setAlpha(alpha.toInt())

			// Correct item's drawn centerY base on curved state
			val drawCenterY = if (_isCurved) _drawCenterY.minus(distanceToCenter)
			else centerY.toFloat()

			canvas.withClip(_rectDraw) {
				if (_isCurved) concat(_matrixRotate)
				drawText(data, _drawCenterX.toFloat(), drawCenterY, _paint)
			}

			drawnDataPos++
			drawnOffsetPos++
		}
	}

	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
		// Set content region
		_rectDraw.set(
			paddingStart,
			paddingTop,
			width.minus(paddingEnd),
			height.minus(paddingBottom)
		)

		// Get the center coordinates of content region
		_wheelCenterX = _rectDraw.centerX()
		_wheelCenterY = _rectDraw.centerY()

		// Correct item drawn center
		computeDrawnCenter()

		_halfWheelHeight = _rectDraw.height().div(2)
		_itemHeight = _rectDraw.height().div(_visibleCount)
		_halfItemHeight = _itemHeight.div(2)

		_limitTop = _drawCenterY.minus(_itemHeight.times(_halfCount))
		_limitBottom = -_adapter.lastIndex.times(_itemHeight)
		// Initialize fling max Y-coordinates
		computeFlingLimitY()
	}

	override fun performClick(): Boolean {
		super.performClick()
		return true
	}

	override fun onTouchEvent(event: MotionEvent): Boolean {
		if (!isEnabled) return false

		when (event.action) {
			MotionEvent.ACTION_DOWN -> {
				parent?.requestDisallowInterceptTouchEvent(true)

				_tracker = VelocityTracker.obtain().also {
					it.addMovement(event)
				}

				if (!_scroller.isFinished) {
					_scroller.abortAnimation()
					_isForceFinishScroll = true
				}
				_firstPointY = event.y.toInt()
				_lastPointY = event.y.toInt()
			}

			MotionEvent.ACTION_MOVE -> {
				if (abs(_firstPointY.minus(event.y)) < _touchSlop &&
					computeDistanceToEndPoint(_scroller.finalY.rem(_itemHeight)) > 0
				) {
					_isClick = true
					return false
				}
				_isClick = false
				_tracker?.addMovement(event)

				// Scroll WheelPicker's content  -4307
				val move: Float = event.y.minus(_lastPointY)
				val checkNextScroll = _scrollOffsetY + move.toInt()
				_scrollOffsetY = when {
					!_isCyclic && checkNextScroll < _limitBottom && move <= 0
					-> {
						if (_isOverScroll) max(_limitBottom.minus(_overScroll), checkNextScroll)
						else _limitBottom
					}

					!_isCyclic && checkNextScroll >= _limitTop && move >= 0
					-> {
						if (_isOverScroll) min(_overScroll, checkNextScroll) else 0
					}

					else -> checkNextScroll
				}
				_lastPointY = event.y.toInt()
				invalidate()
			}

			MotionEvent.ACTION_UP -> {
				parent?.requestDisallowInterceptTouchEvent(false)
				if (_isClick) return false
				_tracker?.apply {
					addMovement(event)
					computeCurrentVelocity(1000, _maximumVelocity.toFloat())
				}

				// Judges the WheelPicker is scroll or fling base on current velocity
				_isForceFinishScroll = false
				val velocity = _tracker?.yVelocity?.toInt() ?: _minimumVelocity
				with(_scroller) {
					if (abs(velocity) > _minimumVelocity) {
						fling(0, _scrollOffsetY, 0, velocity, 0, 0, _minFlingY, _maxFlingY)
						setFinalY(finalY.plus(computeDistanceToEndPoint(finalY.rem(_itemHeight))))
					} else {
						startScroll(
							0,
							_scrollOffsetY,
							0,
							computeDistanceToEndPoint(_scrollOffsetY.rem(_itemHeight))
						)
					}
					// Correct coordinates
					if (!_isCyclic) {
						when {
							finalY > _maxFlingY -> setFinalY(_maxFlingY)
							finalY < _minFlingY -> setFinalY(_minFlingY)
						}
					}
				}
				finishScroll()
			}

			MotionEvent.ACTION_CANCEL -> {
				parent?.requestDisallowInterceptTouchEvent(false)
				finishScroll()
			}
		}
		return performClick()
	}

	fun addListener(listener: IWheelListener) {
		_iListener = listener
	}

//	fun setData(list: List<WheelModel>) {
//		_adapter.setData(list)
//		notifyDatasetChanged()
//	}

	fun isOverScroll(isOverScroll: Boolean = true) {
		_isOverScroll = isOverScroll
	}

	fun scrollTo(itemPosition: Int) {
		if (itemPosition != _currentPosition) {
			val differencesLines: Int = _currentPosition.minus(itemPosition)
			val newScrollOffsetY: Int = _scrollOffsetY.plus(differencesLines.times(_itemHeight))

			ValueAnimator.ofInt(_scrollOffsetY, newScrollOffsetY).apply {
				setDuration(300)
				addUpdateListener { animation ->
					_scrollOffsetY = animation.animatedValue as Int
					postInvalidate()
				}
				doOnEnd {
					_currentPosition = itemPosition
					currentScroll(_currentPosition)
				}
			}.start()
		}
	}
	/* --- */

	internal abstract fun generationData(bundle: Bundle): Pair<List<WheelModel>, Int>

	/* --- */
	protected fun reset() {
		val pair = generationData(arguments)
		initCurrent(pair.first, pair.second)
		notifyDatasetChanged()
	}

	protected fun setCurrent(value: Int) {
		val index = _adapter.getPosition(value)
		max(min(index, _adapter.lastIndex), 0).let {
			_currentPosition = it
			_scrollPosition = it
		}
		notifyDatasetChanged()
	}

	private fun initCurrent(list: List<WheelModel>, index: Int) {
		_adapter.setData(list)
		max(min(index, _adapter.lastIndex), 0).let {
			_currentPosition = it
			_scrollPosition = it
		}
	}

	private fun notifyDatasetChanged() {
		if (_adapter.isEmpty) return
		updateTextAlign()
		computeTextSize()
		post { computeFlingToPosition() }
	}

	private fun computeFlingToPosition() {
		with(_scroller) {
			fling(
				0,
				-_currentPosition.times(_itemHeight),
				0,
				_maximumVelocity,
				0,
				0,
				_minFlingY,
				_maxFlingY
			)
		}
		if (_scroller.computeScrollOffset()) {
			_scrollOffsetY = _scroller.currY
			postInvalidate()
		}
	}

	private fun computeTextSize() {
		var size = 0f
		for (i in 0 until _adapter.itemCount) {
			val text: String = _adapter.getItemDisplay(i)
			val width = _paint.measureText(text)
			size = max(size, width)
		}
		_viewWidth = max(_minWidth, size).plus(_padding).toInt()

		val metrics = _paint.getFontMetrics()
		_viewHeight = (metrics.bottom.minus(metrics.top)).toInt()
	}

	private fun computeDistanceToEndPoint(remainder: Int): Int {
		return if (abs(remainder.toDouble()) > _halfItemHeight) {
			if (_scrollOffsetY < 0) {
				-_itemHeight - remainder
			} else {
				_itemHeight - remainder
			}
		} else {
			-remainder
		}
	}

	private fun computeFlingLimitY() {
		_minFlingY = if (_isCyclic) {
			Int.MIN_VALUE
		} else {
			-_itemHeight.times(_adapter.lastIndex)
		}
		_maxFlingY = if (_isCyclic) Int.MAX_VALUE else 0
	}

	private fun computeYCoordinateAtAngle(degree: Float): Float {
		// Compute y-coordinate for item at degree. mMaxAngle is at mHalfWheelHeight
		return sinDegree(degree).div(sinDegree(_angle.toFloat())).times(_halfWheelHeight)
	}

	private fun updateTextAlign() {
		_paint.textAlign = when (_align) {
			ALIGN_LEFT -> Paint.Align.LEFT
			ALIGN_RIGHT -> Paint.Align.RIGHT
			else -> Paint.Align.CENTER
		}
	}

	private fun computeDrawnCenter() {
		_drawCenterX = when (_align) {
			ALIGN_LEFT -> _rectDraw.left
			ALIGN_RIGHT -> _rectDraw.right
			else -> _wheelCenterX
		}
		_drawCenterY = _wheelCenterY.minus((_paint.ascent().plus(_paint.descent())).div(2)).toInt()
	}

	private fun computeDepth(degree: Float): Float {
		return (_halfWheelHeight - cos(Math.toRadians(degree.toDouble())) * _halfWheelHeight).toFloat()
	}

	private fun currentScroll(position: Int) {
		val item = _adapter.getItem(position)
		if (_scrollPosition != position) {
			_iListener?.onScroll(item.value, item.display)
			_scrollPosition = position
		}
	}

	private fun clamp(value: Float, min: Float, max: Float): Float {
		if (value < min) return min
		if (value > max) return max
		return value
	}

	private fun isPosInRang(position: Int): Boolean {
		return position >= 0 && position < _adapter.itemCount
	}

	private fun sinDegree(degree: Float): Float {
		return sin(Math.toRadians(degree.toDouble())).toFloat()
	}

	private fun measureSize(mode: Int, sizeExpect: Int, sizeActual: Int): Int {
		return when (mode) {
			MeasureSpec.EXACTLY -> sizeExpect
			MeasureSpec.AT_MOST -> min(sizeActual, sizeExpect)
			else -> sizeActual
		}
	}

	private fun finishScroll() {
		_handle.post(_runnable)
		_tracker?.recycle()
		_tracker = null
		_firstPointY = 0
		_lastPointY = 0
	}

	private fun onFinishedLoop() {
	}
}

internal interface BaseAdapter {

	val itemCount: Int

	val lastIndex: Int

	val isEmpty: Boolean

	fun getItemDisplay(position: Int): String

	fun getItem(position: Int): WheelModel
}

internal class Adapter(
	private val data: MutableList<WheelModel> = arrayListOf()
) : BaseAdapter {

	override val itemCount: Int
		get() = data.size

	override val lastIndex: Int
		get() = itemCount.minus(1)

	override val isEmpty: Boolean
		get() = data.isEmpty()

	override fun getItemDisplay(position: Int): String {
		return if (itemCount == 0) "" else data[position].display
	}

	override fun getItem(position: Int): WheelModel {
		return data[position]
	}

	fun getData(): List<WheelModel> = data

	fun setData(input: List<WheelModel>) {
		data.clear()
		data.addAll(input)
	}

	fun getPosition(value: Any): Int {
		val find = data.find { it.value == value || it.display == value.toString() }
		return find?.let { data.indexOf(it) } ?: -1
	}
}