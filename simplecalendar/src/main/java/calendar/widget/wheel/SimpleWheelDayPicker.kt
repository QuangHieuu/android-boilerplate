package calendar.widget.wheel

import android.content.Context
import android.content.res.TypedArray
import android.empty.calendar.R
import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout
import calendar.widget.utils.CalUtils
import calendar.widget.wheel.base.DefaultSimpleWheelView
import calendar.widget.wheel.base.IWheelListener
import calendar.widget.wheel.type.WheelType
import calendar.widget.wheel.view.WheelPicker
import java.util.Calendar
import java.util.Date

class SimpleWheelDayPicker(
	context: Context,
	attrs: AttributeSet,
) : DefaultSimpleWheelView(context, attrs) {

	private lateinit var _pickerDay: WheelPicker
	private lateinit var _pickerMonth: WheelPicker
	private lateinit var _pickerYear: WheelPicker

	private var _dateFormat: String = CalUtils.FORMAT_NORMAL

	override fun TypedArray.styleAttribute() {
		_dateFormat = getString(R.styleable.DefaultSimpleWheelView_wheel_day_format_style)
			?: CalUtils.FORMAT_NORMAL

	}

	override fun initView(attrs: AttributeSet) {
		_pickerDay = WheelPicker(context, null, attrs).apply {
			layoutParams = computeChildLayoutParams()
			setType(WheelType.DAY)
		}
		_pickerMonth = WheelPicker(context, null, attrs).apply {
			layoutParams = computeChildLayoutParams()
			setType(WheelType.MONTH)
			isReverse()
		}
		_pickerYear = WheelPicker(context, null, attrs).apply {
			layoutParams = computeChildLayoutParams()
			setType(WheelType.YEAR)
			isReverse()
		}
	}

	override fun LinearLayout.addView() {
		//TODO sử dụng _dateFormat để dựng view theo format
		addView(_pickerDay)
		addView(_pickerMonth)
		addView(_pickerYear)
	}

	override fun initDataAndEvent() {
		_pickerDay.addListener(object : IWheelListener {
			override fun onScroll(value: Int, display: String) {
				currentSplit[0] = value
				combineSplit()
			}
		})
		_pickerMonth.addListener(object : IWheelListener {
			override fun onScroll(value: Int, display: String) {
				currentSplit[1] = value
				combineSplit()
			}
		})
		_pickerYear.addListener(object : IWheelListener {
			override fun onScroll(value: Int, display: String) {
				currentSplit[2] = value
				combineSplit()
			}
		})

		if (currentSelected.isNotEmpty()) {
			split()
		} else {
			currentSplit[0] = _pickerDay.getValue()
			currentSplit[1] = _pickerMonth.getValue()
			currentSplit[2] = _pickerYear.getValue()
		}
	}

	override fun addListener(listener: SimpleWheelListener) {
		iListener = listener
	}

	override fun setCurrent(date: Date) {
		currentSelected = CalUtils.convertToString(
			date.toString(),
			CalUtils.FORMAT_NORMAL
		)
		split()
	}

	override fun setCurrent(string: String) {
		currentSelected = CalUtils.convertToString(
			string,
			CalUtils.FORMAT_NORMAL
		)
		split()
	}

	private fun split() {
		CalUtils.getSplitDay(currentSelected).let {
			currentSplit[0] = it[0]
			currentSplit[1] = it[1].minus(1)
			currentSplit[2] = it[2]
		}
		_pickerDay.setCurrentSelected(currentSplit[0])
		_pickerMonth.setCurrentSelected(currentSplit[1])
		_pickerYear.setCurrentSelected(currentSplit[2])
	}

	private fun combineSplit() {
		val c = CalUtils.getCalendar().apply {
			set(Calendar.DAY_OF_MONTH, currentSplit[0])
			set(Calendar.MONTH, currentSplit[1])
			set(Calendar.YEAR, currentSplit[2])
		}
		currentSelected = CalUtils.convertToString(
			c.time.toString(),
			CalUtils.FORMAT_NORMAL
		)
		iListener?.onPickDay(currentSelected)
	}
}