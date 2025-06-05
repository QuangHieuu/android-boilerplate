package calendar.widget.wheel.base

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.empty.calendar.R
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import calendar.widget.utils.CalUtils
import calendar.widget.wheel.SimpleWheelListener
import calendar.widget.wheel.type.WidthType
import calendar.widget.wheel.view.CurtainView
import java.util.Date

abstract class DefaultSimpleWheelView(
	context: Context,
	attrs: AttributeSet
) : FrameLayout(context, attrs) {

	private val _density = Resources.getSystem().displayMetrics.density

	protected var currentSelected: String = ""
	protected val currentSplit: Array<Int> = arrayOf(0, 0, 0)
	protected var iListener: SimpleWheelListener? = null
	protected var is12Format: Boolean = false

	private var _widthType: WidthType = WidthType.WRAP
	private var _showCurrent: Boolean = false
	private var _showCurtain: Boolean = true
	private val _padding = _density.times(15)

	private val _mainContain: LinearLayout by lazy {
		LinearLayout(context).apply {
			orientation = LinearLayout.HORIZONTAL
			layoutParams = computeParentLayoutParams()
			addView()
		}
	}
	private val _curtainView: CurtainView by lazy {
		CurtainView(context).apply {
			layoutParams =
				LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
		}
	}

	init {
		context.withStyledAttributes(attrs, R.styleable.DefaultSimpleWheelView) {
			styleAttribute()
			is12Format = getBoolean(R.styleable.DefaultSimpleWheelView_wheel_hour_12_format, false)
			_widthType = WidthType.to(getInt(R.styleable.DefaultSimpleWheelView_wheel_width_type, -1))

			_showCurtain = getBoolean(R.styleable.DefaultSimpleWheelView_wheel_show_curtain, true)
			_showCurrent = getBoolean(R.styleable.DefaultSimpleWheelView_wheel_show_current, false)

			if (_showCurrent) {
				currentSelected = CalUtils.getCalendar().time.toString()
			}
		}
		init(attrs)
	}

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)

		computeCurtain()
	}

	private fun computeCurtain() {
		if (_showCurtain) {
			post {
				val start = when (_widthType) {
					WidthType.FILL -> paddingStart
					else -> width.div(2).minus(_mainContain.width.div(2)).minus(_padding)
				}.toFloat()

				val end = when (_widthType) {
					WidthType.FILL -> width.minus(paddingEnd)
					else -> width.div(2).plus(_mainContain.width.div(2)).plus(_padding)
				}.toFloat()

				_curtainView.computeWidth(start, end)
			}
		}
	}

	protected fun computeChildLayoutParams(): LinearLayout.LayoutParams {
		return when (_widthType) {
			WidthType.FILL -> LinearLayout.LayoutParams(
				0,
				LayoutParams.MATCH_PARENT,
				1f
			)

			else -> LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.MATCH_PARENT
			)
		}
	}

	private fun init(attrs: AttributeSet) {
		initView(attrs)
		addView(_mainContain)
		if (_showCurtain) {
			addView(_curtainView)
		}
		initDataAndEvent()
	}

	private fun computeParentLayoutParams(): LayoutParams {
		return when (_widthType) {
			WidthType.FILL -> LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT,
			)

			else -> LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.MATCH_PARENT,
			).apply { gravity = Gravity.CENTER_HORIZONTAL }
		}
	}

	abstract fun TypedArray.styleAttribute()
	abstract fun initView(attrs: AttributeSet)
	abstract fun LinearLayout.addView()
	abstract fun initDataAndEvent()
	abstract fun addListener(listener: SimpleWheelListener)
	abstract fun setCurrent(date: Date)
	abstract fun setCurrent(string: String)

}