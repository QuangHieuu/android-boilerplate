package calendar.widget.wheel.base

import android.content.Context
import android.content.res.TypedArray
import android.empty.calendar.R
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import calendar.widget.utils.CalUtils
import calendar.widget.wheel.SimpleWheelListener
import calendar.widget.wheel.view.CurtainView
import java.util.Date

abstract class DefaultSimpleWheelView(
	context: Context,
	attrs: AttributeSet
) : FrameLayout(context, attrs) {

	companion object {
	}

	protected var currentSelected: String = CalUtils.getCalendar().time.toString()
	protected val currentSplit: Array<Int> = arrayOf(0, 0, 0)
	protected var iListener: SimpleWheelListener? = null
	protected var is12Format: Boolean = false

	private var _showCurtain: Boolean = true

	private val _mainContain: LinearLayout by lazy {
		LinearLayout(context).apply {
			layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT).apply {
				gravity = Gravity.CENTER_HORIZONTAL
			}
			orientation = LinearLayout.HORIZONTAL

			addView()
		}
	}
	private val _curtainView: CurtainView by lazy {
		CurtainView(context).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
		}
	}

	init {
		context.withStyledAttributes(attrs, R.styleable.DefaultSimpleWheelView) {
			styleAttribute()
			is12Format = getBoolean(R.styleable.DefaultSimpleWheelView_wheel_hour_12_format, false)

			_showCurtain = getBoolean(R.styleable.DefaultSimpleWheelView_wheel_show_curtain, true)
		}
		init(attrs)
	}

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)

		if (_showCurtain) {
			post {
				_curtainView.computeWidth(_mainContain.width.div(2f))
			}
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

	abstract fun TypedArray.styleAttribute()
	abstract fun initView(attrs: AttributeSet)
	abstract fun LinearLayout.addView()
	abstract fun initDataAndEvent()
	abstract fun addListener(listener: SimpleWheelListener)
	abstract fun setCurrent(date: Date)
	abstract fun setCurrent(string: String)

}