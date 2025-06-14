package calendar.widget.table

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.empty.calendar.R
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.ViewGroup.LayoutParams
import android.widget.Scroller
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import androidx.core.graphics.toRectF
import androidx.core.graphics.withSave
import calendar.widget.table.model.DayOfMonth
import calendar.widget.utils.CalUtils
import calendar.widget.utils.TextAlign
import java.util.Calendar
import java.util.Date
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

abstract class CalenderListener {

	open fun onChange(date: Date) {}
	open fun onPick(date: Date) {}
	open fun onPickFromTo(from: Date?, to: Date?) {}
}

class SimpleTableCalendar(
	context: Context,
	attrs: AttributeSet
) : View(context, attrs) {

	companion object {

		internal const val NONE: Int = -1
		internal const val HORIZONTAL: Int = 0
		internal const val VERTICAL: Int = 1

		internal const val PREVIOUS = -1
		internal const val CURRENT = 0
		internal const val NEXT = 1
		internal const val CALENDAR_COLUMN = 7
		internal const val CALENDAR_ROW = CALENDAR_COLUMN

		internal const val LAST_FLING_THRESHOLD_MILLIS = 300
		internal const val ANIMATION_DURATION = 900
	}

	private var _listener: CalenderListener? = null

	private val _density = Resources.getSystem().displayMetrics.density

	private var _radius: Float = _density.times(16)
	private var _margin: Float = _density.times(8)

	private val _holderPath: Path = Path()
	private val _holderRect = Rect()

	private val _rangePath = Path()
	private val _linePath = Path()
	private val _stickyPath = Path()

	private val _stickyRect = Rect()
	private val _clipRect = Rect()

	private val _emptyPaint = Paint().apply {
		isAntiAlias = true
		style = Paint.Style.FILL
	}
	private val _holderBgPaint = Paint().apply {
		isAntiAlias = true
		style = Paint.Style.FILL
	}
	private val _strokePaint = Paint().apply {
		isAntiAlias = true
		style = Paint.Style.STROKE
		strokeWidth = _density.times(1)
	}

	private val _stickyBgPaint = Paint().apply {
		isAntiAlias = true
		style = Paint.Style.FILL
	}

	private val _selectedPaint = Paint().apply {
		isAntiAlias = true
		style = Paint.Style.FILL
	}
	private val _selectedRangePaint = Paint().apply {
		isAntiAlias = true
		style = Paint.Style.FILL
	}
	private val _textTodayPaint = TextPaint().apply {
		isAntiAlias = true
		style = Paint.Style.FILL
	}
	private val _textDisable = TextPaint().apply {
		flags = Paint.STRIKE_THRU_TEXT_FLAG
		style = Paint.Style.FILL
	}
	private val _textSelectedPaint = TextPaint().apply {
		isAntiAlias = true
		color = Color.WHITE
		style = Paint.Style.FILL
	}
	private val _textHeaderPaint = TextPaint().apply {
		isAntiAlias = true
		style = Paint.Style.FILL
	}
	private val _textDefaultPaint = TextPaint().apply {
		isAntiAlias = true
		style = Paint.Style.FILL
	}

	private var _bitmapHolder: Bitmap? = null

	private var _moveDirection: Int = NONE
	private var _direction: Int = HORIZONTAL

	private var _isSmoothScrolling: Boolean = false
	private var _isScrolling: Boolean = false
	private var _isShowStroke: Boolean = true
	private var _stickyHeader: Boolean = false
	private var _pickFromTo: Boolean = false
	private var _choseOnlyFuture: Boolean = true
	private var _showOnlyInMonth: Boolean = false

	private val _gestureDetector: GestureDetector

	private val _scroller: Scroller = Scroller(context)
	private var _tracker: VelocityTracker? = VelocityTracker.obtain()
	private val _conf: ViewConfiguration = ViewConfiguration.get(context)
	private val _minimumVelocity: Int
		get() = _conf.scaledMinimumFlingVelocity
	private val _maximumVelocity: Int
		get() = _conf.scaledMaximumFlingVelocity
	private val _touchSlop: Int
		get() = _conf.scaledTouchSlop

	private var _scrollDistance: Float = 0f
	private var _scrollOffset: Float = 0f

	private var _align = TextAlign.CENTER

	private var _distanceThreshHold: Int = 0
	private var _cellWidth: Float = 0f
	private var _cellHeight: Float = 0f

	private val _minCellHeight = _density.times(40).toInt()
	private val _minCellWidth = _density.times(40).toInt()

	private var _monthsScrollOffset = 0
	private var _tempLastFling: Long = 0
	private var _adjustedSnapVelocity = _density.times(400)

	private var _displayMonth: Date = CalUtils.getCalendar().time
	private var _selected: Date? = null
	private var _selectedTo: Date? = null

	private val _today: Date = CalUtils.getCalendar().time
	private val _todaySplit: Array<Int> by lazy { CalUtils.getSplitCurrentDay(monthIndex = true) }

	private val _dayOfWeeks: List<String> = CalUtils.getDayOfWeekName()

	init {
		context.withStyledAttributes(attrs, R.styleable.SimpleTableCalendar) {
			getSetting()
			getColor()
		}

		_gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
			override fun onFling(
				e1: MotionEvent?,
				e2: MotionEvent,
				velocityX: Float,
				velocityY: Float
			): Boolean {
				return true
			}

			override fun onDown(e: MotionEvent): Boolean {
				return true
			}

			override fun onSingleTapUp(e: MotionEvent): Boolean {
				handleClick(e)
				invalidate()
				return super.onSingleTapUp(e)
			}

			override fun onScroll(
				e1: MotionEvent?,
				e2: MotionEvent,
				distanceX: Float,
				distanceY: Float
			): Boolean {
				val absX = abs(distanceX.toDouble())
				val absY = abs(distanceY.toDouble())

				if (isVertical() && absY < _touchSlop && absX > 0 && absY == 0.0) {
					return true
				}
				if (isHorizontal() && absX < _touchSlop && absY > 0 && absX == 0.0) {
					return true
				}

				if ((absX > 0 && isHorizontal()) || (absY > 0 && isVertical())) {
					parent.requestDisallowInterceptTouchEvent(true)
					if (_isSmoothScrolling) {
						return true
					}

					if (_moveDirection == NONE) {
						_moveDirection = if (abs(distanceX.toDouble()) > abs(distanceY.toDouble())) {
							HORIZONTAL
						} else {
							VERTICAL
						}
					}

					_isScrolling = true
					_scrollDistance = if (isHorizontal()) distanceX else distanceY
					invalidate()
					return true
				}

				return false
			}
		})
	}

	private fun TypedArray.getSetting() {
		_direction = getInt(R.styleable.SimpleTableCalendar_calendar_orientation, HORIZONTAL)
		_radius = getDimension(R.styleable.SimpleTableCalendar_calendar_radius, 0f)
		_showOnlyInMonth = getBoolean(
			R.styleable.SimpleTableCalendar_calendar_show_only_in_month,
			false
		)
		_stickyHeader = getBoolean(
			R.styleable.SimpleTableCalendar_calendar_sticky_header,
			false
		)
		_isShowStroke = getBoolean(
			R.styleable.SimpleTableCalendar_calendar_show_stroke,
			true
		)
		_pickFromTo = getBoolean(
			R.styleable.SimpleTableCalendar_calendar_pick_from_to,
			false
		)
		_choseOnlyFuture = getBoolean(
			R.styleable.SimpleTableCalendar_calendar_chose_only_future,
			true
		)
	}

	private fun TypedArray.getColor() {
		_textTodayPaint.color = getColor(
			R.styleable.SimpleTableCalendar_calendar_text_today_color,
			"#03A959".toColorInt()
		)
		_textDefaultPaint.color = getColor(
			R.styleable.SimpleTableCalendar_calendar_text_color,
			Color.BLACK
		)
		_textHeaderPaint.color = getColor(
			R.styleable.SimpleTableCalendar_calendar_text_header_color,
			Color.BLACK
		)
		_textDisable.color = getColor(
			R.styleable.SimpleTableCalendar_calendar_text_disable_color,
			"#59081C36".toColorInt()
		)
		_selectedPaint.color = getColor(
			R.styleable.SimpleTableCalendar_calendar_selected_color,
			"#03A959".toColorInt()
		)
		_selectedRangePaint.color = getColor(
			R.styleable.SimpleTableCalendar_calendar_selected_range_color,
			"#1403A959".toColorInt()
		)
		_stickyBgPaint.color = getColor(
			R.styleable.SimpleTableCalendar_calendar_sticky_header_background_color,
			"#14747480".toColorInt()
		)
		_holderBgPaint.color = getColor(
			R.styleable.SimpleTableCalendar_calendar_background_color,
			Color.WHITE
		)
		_strokePaint.color = getColor(
			R.styleable.SimpleTableCalendar_calendar_stroke_color,
			"#14EF4141".toColorInt()
		)
	}

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		val modeWidth = MeasureSpec.getMode(widthMeasureSpec)
		val modeHeight = MeasureSpec.getMode(heightMeasureSpec)

		val widthSize = MeasureSpec.getSize(widthMeasureSpec)
		val heightSize = MeasureSpec.getSize(heightMeasureSpec)

		val paddingHorizontal = paddingStart.plus(paddingEnd)
		val paddingVertical = paddingTop.plus(paddingBottom)

		// Correct sizes of original content
		var resultWidth =
			if (layoutParams.width == LayoutParams.WRAP_CONTENT || layoutParams.width == 0) {
				_minCellWidth.times(CALENDAR_COLUMN)
			} else {
				widthMeasureSpec
			}

		var resultHeight: Int =
			if (layoutParams.height == LayoutParams.WRAP_CONTENT || layoutParams.height == 0) {
				_minCellHeight.times(CALENDAR_ROW)
			} else {
				heightMeasureSpec
			}

		// Consideration sizes of parent can influence the view sizes
		resultWidth = measureSize(modeWidth, widthSize, resultWidth)
		resultHeight = measureSize(modeHeight, heightSize, resultHeight)

		_cellWidth = resultWidth.minus(paddingHorizontal).div(CALENDAR_COLUMN).toFloat()
		_cellHeight = resultHeight.minus(paddingVertical).div(CALENDAR_ROW).toFloat()
		_distanceThreshHold = if (isHorizontal()) widthSize.div(4) else resultHeight.div(4)

		min(_cellHeight.div(3), 13.times(_density)).also {
			_textDefaultPaint.textSize = it
			_textTodayPaint.textSize = it
			_textHeaderPaint.textSize = it
			_textSelectedPaint.textSize = it
			_textDisable.textSize = it
		}

		val stroke = _strokePaint.strokeWidth.toInt()
		_holderRect.set(
			paddingStart.plus(stroke),
			paddingTop.plus(stroke),
			resultWidth.minus(paddingEnd).minus(stroke),
			resultHeight.minus(paddingBottom).minus(stroke)
		)
		_clipRect.set(
			_holderRect.left.plus(stroke),
			_holderRect.top.plus(stroke).plus(if (_stickyHeader) _cellHeight.toInt() else 0),
			_holderRect.right.minus(stroke),
			_holderRect.bottom.minus(stroke),
		)
		_stickyRect.set(
			_holderRect.left.plus(stroke),
			_holderRect.top.plus(stroke),
			_holderRect.right.minus(stroke),
			_cellHeight.plus(stroke).toInt()
		)
		_bitmapHolder = createHolder(resultWidth, resultHeight)
		setMeasuredDimension(resultWidth, resultHeight)
	}

	override fun onDraw(canvas: Canvas) {
		calculateXPositionOffset()
		_bitmapHolder?.let { canvas.drawBitmap(it, null, _holderRect, _holderBgPaint) }
		canvas.clipPath(_holderPath)
	}

	override fun dispatchDraw(canvas: Canvas) {

		if (_stickyHeader) {
			canvas.drawStickyHeader()
		}

		canvas.drawTable()
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
				}
				_isSmoothScrolling = false
			}

			MotionEvent.ACTION_MOVE -> {
				_tracker?.apply {
					addMovement(event)
					computeCurrentVelocity(500)
				}

				if (!_holderRect.contains(event.x.toInt(), event.y.toInt())) {
					parent?.requestDisallowInterceptTouchEvent(false)
					snapBackScroller()
					finishScroll()
					return false
				} else {
					parent?.requestDisallowInterceptTouchEvent(true)
				}
			}

			MotionEvent.ACTION_UP -> {
				_tracker?.apply {
					addMovement(event)
					computeCurrentVelocity(1000, _maximumVelocity.toFloat())
				}

				val velocity = when (_direction) {
					VERTICAL -> _tracker?.yVelocity
					else -> _tracker?.xVelocity
				} ?: _minimumVelocity.toFloat()

				handleSmoothScrolling(velocity)
				finishScroll()
			}
		}
		invalidate()

		if (_gestureDetector.onTouchEvent(event)) {
			return performClick()
		}
		return performClick()
	}

	override fun computeScroll() {
		super.computeScroll()
		if (computeCurrentScroll()) {
			invalidate()
		}
	}

	fun addListener(listener: CalenderListener) {
		_listener = listener
	}

	fun nextMonth() {
		scrollNextMonth()
		invalidate()
	}

	fun previousMonth() {
		scrollPreviousMonth()
		invalidate()
	}

	fun moveToToday() {
		_displayMonth = CalUtils.getCalendar().time
		_monthsScrollOffset = 0
		_scrollDistance = 0f
		_scrollOffset = 0f
		_scroller.startScroll(0, 0, 0, 0)
		getCurrentMonthScroll()
		invalidate()
	}

	private fun computeCurrentScroll(): Boolean {
		if (_scroller.computeScrollOffset()) {
			_scrollOffset = getEventDirection()
			return true
		}
		return false
	}

	private fun finishScroll() {
		_moveDirection = NONE

		_tracker?.recycle()
		_tracker?.clear()
		_tracker = null
		_isScrolling = false
		_isSmoothScrolling = false
	}

	private fun drawStroke(offset: Int) {
		val distance = getDirectionOffset().times(-_monthsScrollOffset.plus(offset))

		for (dayColumn in 0..<CALENDAR_COLUMN) {
			val xPosition: Float = _cellWidth.times(dayColumn)
				.plus(_strokePaint.strokeWidth)
				.plus(paddingStart)
				.let { if (isHorizontal()) it.plus(_scrollOffset).plus(distance) else it }
			_linePath.moveTo(xPosition, _holderRect.top.toFloat())
			_linePath.lineTo(xPosition, _holderRect.bottom.toFloat())
		}

		val limitRow = CALENDAR_ROW
			.minus(if (_stickyHeader) 1 else 0)
			.plus(if (isHorizontal()) 1 else 0)

		for (dayRow in 0..<limitRow) {
			val yPosition: Float = _cellHeight.times(dayRow)
				.plus(_strokePaint.strokeWidth)
				.plus(paddingTop)
				.let { if (isVertical()) it.plus(_scrollOffset).plus(distance) else it }

			_linePath.moveTo(_holderRect.left.toFloat(), yPosition)
			_linePath.lineTo(_holderRect.right.toFloat(), yPosition)
		}
	}

	private fun Canvas.drawTable() {
		drawBitmap(calculatorTable(PREVIOUS), null, _holderRect, _emptyPaint)
		drawBitmap(calculatorTable(CURRENT), null, _holderRect, _emptyPaint)
		drawBitmap(calculatorTable(NEXT), null, _holderRect, _emptyPaint)
		if (_isShowStroke) {
			_linePath.reset()
			drawStroke(PREVIOUS)
			drawStroke(CURRENT)
			drawStroke(NEXT)

			drawPath(_linePath, _strokePaint)
		}
	}

	private fun calculatorTable(offset: Int): Bitmap {
		return createBitmap(_holderRect.width(), _holderRect.height()).let {
			Canvas(it).apply {
				val splitCurrent = monthOffset(_displayMonth, -_monthsScrollOffset, offset)
				drawMonth(
					getDirectionOffset().times((-_monthsScrollOffset).plus(offset)),
					splitCurrent[2],
					splitCurrent[1]
				)
			}
			it
		}
	}

	private fun Canvas.drawMonth(offset: Int, year: Int, month: Int) {
		val list = initDataMonth(month, year)

		var dayColumn = 0
		var dayRow = if (_stickyHeader) 1 else 0
		while (dayColumn < CALENDAR_COLUMN) {
			if (dayRow == 7) {
				dayRow = if (_stickyHeader) 1 else 0
				dayColumn += 1
			}
			if (dayColumn == _dayOfWeeks.size) {
				break
			}

			val xPosition: Float = _cellWidth.times(dayColumn)
				.minus(paddingStart)
				.let { if (isHorizontal()) it.plus(_scrollOffset).plus(offset) else it }

			val yPosition: Float = _cellHeight.times(dayRow)
				.minus(paddingTop)
				.let { if (isVertical()) it.plus(_scrollOffset).plus(offset) else it }

			if (dayRow == 0) {
				val text = _dayOfWeeks[dayColumn]
				drawHeader(xPosition, yPosition, text)
			} else {
				val position = dayRow.minus(1).times(CALENDAR_COLUMN).plus(dayColumn)
				if (position < list.size) {
					val dayOfMonth = list[position]
					when {
						_pickFromTo && dayOfMonth.isInRange -> {
							drawSelectedInRange(xPosition, yPosition, dayOfMonth)
						}

						dayOfMonth.isSelected -> {
							drawSelectedCircle(xPosition, yPosition, dayOfMonth)
						}

						else -> drawText(xPosition, yPosition, dayOfMonth)
					}
				}
			}
			dayRow += 1
		}
	}

	private fun Canvas.drawStickyHeader() {
		_stickyPath.reset()
		withSave {
			drawRect(_stickyRect, _stickyBgPaint)
			for (column in 0..CALENDAR_COLUMN) {
				val xPosition: Float = _cellWidth.times(column)
					.plus(_strokePaint.strokeWidth)
					.minus(paddingStart)

				if (column < CALENDAR_COLUMN) {
					val text = _dayOfWeeks[column]
					drawHeader(xPosition, 0f, text)
				}

				if (column in 1..6) {
					_stickyPath.moveTo(xPosition, 0f)
					_stickyPath.lineTo(xPosition, _cellHeight)
				}
			}
			_stickyPath.moveTo(_stickyRect.left.toFloat(), _stickyRect.bottom.toFloat())
			_stickyPath.lineTo(_stickyRect.right.toFloat(), _stickyRect.bottom.toFloat())
			if (_isShowStroke) {
				drawPath(_stickyPath, _strokePaint)
			}
		}
	}

	private fun Canvas.drawHeader(xPosition: Float, yPosition: Float, text: String) {
		val size = computeTextCoordination(text, xPosition, yPosition)
		drawText(text, size.first, size.second, _textHeaderPaint)
	}

	private fun computeTextCoordination(
		text: String,
		xPosition: Float,
		yPosition: Float
	): Pair<Float, Float> {
		val halfCellWidth = _cellWidth.div(2)
		val halfCellHeight = _cellHeight.div(2)
		val measureText = _textDefaultPaint.measureText(text)
		val metrics = _textDefaultPaint.getFontMetrics()
		val width: Float
		val height: Float

		when (_align) {
			TextAlign.CENTER -> {
				width = halfCellWidth.minus(measureText.div(2)).plus(paddingStart).plus(paddingEnd)
				height = halfCellHeight.plus(metrics.bottom.minus(metrics.top).div(3))
					.plus(paddingTop)
					.plus(paddingBottom)
			}

			TextAlign.LEFT -> {
				width = paddingStart.plus(paddingEnd).toFloat()
				height = halfCellHeight.plus(metrics.bottom.minus(metrics.top).div(3))
					.plus(paddingTop)
					.plus(paddingBottom)
			}

			TextAlign.RIGHT -> {
				width = _cellWidth.minus(measureText).plus(paddingStart).plus(paddingEnd)
				height = halfCellHeight.plus(metrics.bottom.minus(metrics.top).div(3))
					.plus(paddingTop)
					.plus(paddingBottom)
			}
		}

		return Pair(xPosition.plus(width), yPosition.plus(height))
	}

	private fun Canvas.drawText(
		xPosition: Float,
		yPosition: Float,
		dayOfMonth: DayOfMonth,
	) {
		if (!dayOfMonth.isInMonth) return
		val text = dayOfMonth.day.toString()
		val size = computeTextCoordination(text, xPosition, yPosition)
		val paint = when {
			dayOfMonth.isDisable -> _textDisable
			dayOfMonth.isSelected -> _textSelectedPaint
			dayOfMonth.isToday -> _textTodayPaint
			dayOfMonth.isInRange -> _textDefaultPaint
			else -> _textDefaultPaint
		}
		withSave {
			drawText(text, size.first, size.second, paint)
		}
	}

	private fun Canvas.drawSelectedInRange(
		xPosition: Float,
		yPosition: Float,
		dayOfMonth: DayOfMonth,
	) {
		val centerX = _cellWidth.div(2)
		val centerY = _cellHeight.div(2)
		val radius = _textSelectedPaint.textSize.plus(_margin)
		val top = centerY.minus(radius)
		val bottom = centerY.plus(radius)

		_rangePath.reset()
		_rangePath.addRect(
			xPosition,
			yPosition.plus(top),
			xPosition.plus(_cellWidth),
			yPosition.plus(bottom),
			Path.Direction.CCW
		)
		withSave {
			drawPath(_rangePath, _selectedRangePaint)
			drawText(xPosition, yPosition, dayOfMonth)
		}
	}

	private fun Canvas.drawSelectedCircle(
		xPosition: Float,
		yPosition: Float,
		dayOfMonth: DayOfMonth,
	) {
		val centerX = _cellWidth.div(2)
		val centerY = _cellHeight.div(2)
		val radius = _textSelectedPaint.textSize.plus(_margin)
		val top = centerY.minus(radius)
		val bottom = centerY.plus(radius)

		_rangePath.reset()
		if (compareSameTwoDate(dayOfMonth.toDate, _selected) && _selectedTo != null) {
			_rangePath.addCircle(
				xPosition.plus(centerX),
				yPosition.plus(centerY),
				_textSelectedPaint.textSize.plus(_margin),
				Path.Direction.CCW
			)
			_rangePath.addRect(
				xPosition.plus(centerX),
				yPosition.plus(top),
				xPosition.plus(_cellWidth),
				yPosition.plus(bottom),
				Path.Direction.CCW
			)
		}
		if (compareSameTwoDate(dayOfMonth.toDate, _selectedTo)) {
			_rangePath.addCircle(
				xPosition.plus(centerX),
				yPosition.plus(centerY),
				_textSelectedPaint.textSize.plus(_margin),
				Path.Direction.CCW
			)
			_rangePath.addRect(
				xPosition,
				yPosition.plus(top),
				xPosition.plus(centerX),
				yPosition.plus(bottom),
				Path.Direction.CCW
			)
		}
		withSave {
			drawPath(_rangePath, _selectedRangePaint)
			drawCircle(
				xPosition.plus(centerX),
				yPosition.plus(centerY),
				radius.minus(_margin.times(0.5f)),
				_selectedPaint
			)
			drawText(xPosition, yPosition, dayOfMonth)
		}
	}

	private fun measureSize(mode: Int, sizeExpect: Int, sizeActual: Int): Int {
		return when (mode) {
			MeasureSpec.EXACTLY -> sizeExpect
			MeasureSpec.AT_MOST -> min(sizeActual, sizeExpect)
			else -> sizeActual
		}
	}

	private fun monthOffset(currentDate: Date, scrollOffset: Int, monthOffset: Int): Array<Int> {
		val calendar = CalUtils.getCalendar(currentDate)
		calendar.add(Calendar.MONTH, scrollOffset + monthOffset)
		return CalUtils.getSplitDay(calendar.time.toString(), monthIndex = true)
	}

	private fun initDataMonth(month: Int, year: Int): List<DayOfMonth> {

		fun checkInRange(date: DayOfMonth): Boolean {
			if (_selected == null || _selectedTo == null) return false
			val checkDate = date.toDate
			return checkDate.after(_selected) && checkDate.before(_selectedTo)
		}

		fun checkIsSelected(date: DayOfMonth): Boolean {
			return _selected?.let { date.toDate.compareTo(it) == 0 } == true ||
				_selectedTo?.let { date.toDate.compareTo(it) == 0 } == true
		}

		fun checkIsToday(date: DayOfMonth): Boolean {
			return _todaySplit[0] == date.day
				&& _todaySplit[1] == date.month
				&& _todaySplit[2] == date.year
		}

		val calendar = CalUtils.getCalendar(year, month, 1)
		val firstDayOfWeekMonth: Int = calendar.get(Calendar.DAY_OF_WEEK)

		val daysOfMonth: Int = CalUtils.getCalendar(year, month).getActualMaximum(Calendar.DAY_OF_MONTH)

		val preMonth = CalUtils.getCalendar(year, month, 1)
		preMonth.add(Calendar.MONTH, -1)
		val slipPreMonth = CalUtils.getSplitDay(preMonth.time.toString(), monthIndex = true)

		val nextMonth = CalUtils.getCalendar(year, month, 1)
		nextMonth.add(Calendar.MONTH, 1)

		val lastDayPrevMonth: Int =
			CalUtils.getCalendar(slipPreMonth[2], slipPreMonth[1]).getActualMaximum(Calendar.DAY_OF_MONTH)

		val firstDayPrevMonth: Int = if (firstDayOfWeekMonth != Calendar.SUNDAY) {
			lastDayPrevMonth.plus(1).minus(firstDayOfWeekMonth.minus(Calendar.MONDAY))
		} else {
			lastDayPrevMonth.minus(5)
		}

		val list = arrayListOf<DayOfMonth>()
		//day of prev month
		for (day in firstDayPrevMonth..lastDayPrevMonth) {
			list.add(DayOfMonth(preMonth[Calendar.YEAR], preMonth[Calendar.MONTH], day).apply {
				isInMonth = !_showOnlyInMonth
				isDisable = _choseOnlyFuture && toDate.before(_today)
			})
		}
		//day of current month
		for (day in 1..daysOfMonth) {
			list.add(DayOfMonth(year, month, day).apply {
				isDisable = _choseOnlyFuture && toDate.before(_today)
				isToday = checkIsToday(this)
				isSelected = checkIsSelected(this)
				isInRange = checkInRange(this) && !isSelected
			})
		}
		//day of next month
		val lastDayNextMonth: Int = 42 - list.size
		for (day in 1..lastDayNextMonth) {
			list.add(DayOfMonth(nextMonth[Calendar.YEAR], nextMonth[Calendar.MONTH], day).apply {
				isInMonth = !_showOnlyInMonth
				isDisable = _choseOnlyFuture && toDate.before(_today)
			})
		}
		return list
	}

	private fun handleSmoothScrolling(velocity: Float) {
		val distanceScrolled = _scrollOffset.minus(getDirectionOffset().times(_monthsScrollOffset))
		val sinceLastScroll: Boolean =
			System.currentTimeMillis().minus(_tempLastFling) > LAST_FLING_THRESHOLD_MILLIS

		when {
			velocity > _adjustedSnapVelocity && sinceLastScroll -> {
				scrollPreviousMonth()
			}

			_isScrolling && distanceScrolled > _distanceThreshHold -> {
				scrollPreviousMonth()
			}

			velocity < -_adjustedSnapVelocity && sinceLastScroll -> {
				scrollNextMonth()
			}

			_isScrolling && distanceScrolled < -_distanceThreshHold -> {
				scrollNextMonth()
			}

			else -> {
				snapBackScroller()
			}
		}
	}

	private fun scrollNextMonth() {
		_tempLastFling = System.currentTimeMillis()
		_monthsScrollOffset -= 1
		performScroll()
	}

	private fun scrollPreviousMonth() {
		_tempLastFling = System.currentTimeMillis()
		_monthsScrollOffset += 1
		performScroll()
	}

	private fun performScroll() {
		_isSmoothScrolling = true
		val targetScroll: Int = _monthsScrollOffset.times(getDirectionOffset())
		val fling = targetScroll.minus(_scrollOffset)
		_scroller.startScroll(
			if (isHorizontal()) _scrollOffset.toInt() else 0,
			if (isVertical()) _scrollOffset.toInt() else 0,
			if (isHorizontal()) fling.toInt() else 0,
			if (isVertical()) fling.toInt() else 0,
			abs(fling).div(getDirectionOffset()).times(ANIMATION_DURATION).toInt()
		)
		getCurrentMonthScroll()
	}

	private fun getCurrentMonthScroll() {
		val calendar = CalUtils.getCalendar(_displayMonth)
		calendar.add(Calendar.MONTH, -_monthsScrollOffset)
		calendar.set(Calendar.DAY_OF_MONTH, 1)
		_listener?.onChange(calendar.time)
	}

	private fun snapBackScroller() {
		_isSmoothScrolling = true
		val fling = _scrollOffset.minus(_monthsScrollOffset.times(getDirectionOffset()))
		_scroller.startScroll(
			if (isHorizontal()) _scrollOffset.toInt() else 0,
			if (isVertical()) _scrollOffset.toInt() else 0,
			if (isHorizontal()) -fling.toInt() else 0,
			if (isVertical()) -fling.toInt() else 0,
			abs(fling).div(getDirectionOffset()).times(ANIMATION_DURATION).toInt()
		)
	}

	private fun getEventDirection(): Float {
		return when (_direction) {
			VERTICAL -> _scroller.currY.toFloat()
			else -> _scroller.currX.toFloat()
		}
	}

	private fun calculateXPositionOffset() {
		if (_moveDirection == HORIZONTAL || _moveDirection == VERTICAL) {
			_scrollOffset -= _scrollDistance.toInt()
		}
	}

	private fun isVertical() = _direction == VERTICAL

	private fun isHorizontal() = _direction == HORIZONTAL

	private fun getDirectionOffset(): Int {
		val i = if (isHorizontal()) _cellWidth.times(CALENDAR_COLUMN)
		else _cellHeight.times(CALENDAR_ROW.minus(if (_stickyHeader) 1 else 0))
		return i.plus(_strokePaint.strokeWidth).toInt()
	}

	private fun isScrolling(): Boolean {
		val scrolledX: Float = abs(_scrollOffset)
		val expectedScrollX: Int = abs((getDirectionOffset() * _monthsScrollOffset).toDouble()).toInt()
		return scrolledX < expectedScrollX - 5 || scrolledX > expectedScrollX + 5
	}

	private fun handleClick(e: MotionEvent) {
		if (isScrolling()) {
			return
		}

		val dayColumn = e.x.plus(paddingStart)
			.minus(_cellWidth.div(2).plus(paddingEnd))
			.div(_cellWidth).roundToInt()
		val dayRow = e.y.minus(_cellHeight.div(2)).div(_cellHeight).roundToInt()

		if (dayRow < 1) return

		val split = monthOffset(_displayMonth, -_monthsScrollOffset, 0)
		val calendar = CalUtils.getCalendar(split[2], split[1], 1)

		val current = calendar[Calendar.DAY_OF_WEEK]

		val rowOffset: Int = if (current != Calendar.SUNDAY) {
			1.minus(current.minus(Calendar.MONDAY))
		} else {
			-5
		}
		val dayOffset = dayRow.minus(1).times(CALENDAR_ROW).plus(rowOffset).plus(dayColumn).minus(1)
		calendar.add(Calendar.DAY_OF_MONTH, dayOffset)

		val dateCheck = calendar.time

		when {
			//Không cho chọn ngày trong quá khứ
			_choseOnlyFuture && dateCheck.before(_today) -> {
				return
			}
			//Không cho chọn ngày khác tháng
			dayOffset < 0 || dayOffset > calendar.getActualMaximum(Calendar.DAY_OF_MONTH) -> {
				return
			}
		}

		when {
			//trường hợp chọn ngày bắt đầu, kết thúc
			_pickFromTo -> {
				when {
					//chọn ngày bắt đầu
					_selected == null -> {
						_selected = dateCheck
						_listener?.onPickFromTo(_selected, _selectedTo)
					}
					//không chọn trùng ngày bắt đầu
					_selected != null && compareSameTwoDate(_selected, dateCheck) -> {
						return
					}
					//chọn ngày kết thúc
					_selected != null && _selectedTo == null -> {
						_selectedTo = dateCheck
						switchDay(_selected, _selectedTo) { from, to ->
							_selected = from
							_selectedTo = to
						}
						_listener?.onPickFromTo(_selected, _selectedTo)
					}
					//không cho chọn trùng ngày kết thúc
					_selectedTo != null && compareSameTwoDate(_selectedTo, dateCheck) -> {
						return
					}
					//check ngày bắt đầu và kết thúc
					_selected != null && _selectedTo != null -> {
						when {
							//chọn ngày kết thúc < bắt đầu || nằm trong range đã chọn
							// -> lấy ngày hiện tại là bắt đầu chọn lại ngày kết thúc
							dateCheck.after(_selected) && dateCheck.before(_selectedTo) ||
								dateCheck.before(_selected)
							-> {
								_selected = dateCheck
								_selectedTo = null
							}
							//chọn ngày kết thúc > bắt đầu
							dateCheck.after(_selectedTo) -> {
								_selectedTo = dateCheck
							}
						}
						//đảo thứ tự chọn ngày
						switchDay(_selected, _selectedTo) { from, to ->
							_selected = from
							_selectedTo = to
						}
						_listener?.onPickFromTo(_selected, _selectedTo)
					}
				}
			}
			//trường hợp chọn 1 ngày
			else -> {
				_selected = dateCheck
				_listener?.onPick(dateCheck)
			}
		}

		invalidate()
	}

	private fun switchDay(
		start: Date?,
		end: Date?,
		block: (from: Date?, to: Date?) -> Unit
	) {
		if (start == null && end != null) {
			block(end, null)
		}
		when {
			start != null && end == null -> block(start, null)

			start != null && end != null -> {
				if (start.before(end)) {
					block(start, end)
				} else {
					block(end, start)
				}
			}
		}
	}

	private fun compareSameTwoDate(input1: Date?, input2: Date?): Boolean {
		if (input1 == null || input2 == null) return false
		val calendar1 = CalUtils.getCalendar(input1).apply {
			set(Calendar.HOUR_OF_DAY, 0)
			set(Calendar.MINUTE, 0)
			set(Calendar.SECOND, 0)
			set(Calendar.MILLISECOND, 0)
		}
		val calendar2 = CalUtils.getCalendar(input2).apply {
			set(Calendar.HOUR_OF_DAY, 0)
			set(Calendar.MINUTE, 0)
			set(Calendar.SECOND, 0)
			set(Calendar.MILLISECOND, 0)
		}

		return calendar1.time == calendar2.time
	}

	private fun createHolder(width: Int, height: Int): Bitmap {
		_holderPath.addRoundRect(_clipRect.toRectF(), _radius, _radius, Path.Direction.CCW)
		return createBitmap(width, height).let {

			with(Canvas(it)) {
				drawRoundRect(_holderRect.toRectF(), _radius, _radius, _holderBgPaint)
				drawRoundRect(_holderRect.toRectF(), _radius, _radius, _strokePaint)
			}
			it
		}
	}
}
