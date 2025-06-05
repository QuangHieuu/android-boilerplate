package calendar.widget.wheel

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.widget.LinearLayout
import calendar.widget.utils.CalUtils
import calendar.widget.wheel.base.DefaultSimpleWheelView
import calendar.widget.wheel.base.IWheelListener
import calendar.widget.wheel.type.WheelType
import calendar.widget.wheel.view.WheelPicker
import java.util.Calendar
import java.util.Date

class SimpleWheelTimePicker(
	context: Context,
	attrs: AttributeSet,
) : DefaultSimpleWheelView(context, attrs) {

	private lateinit var _pickerHour: WheelPicker
	private lateinit var _pickerMinute: WheelPicker
	private lateinit var _pickerDayNight: WheelPicker

	override fun TypedArray.styleAttribute() {

	}

	override fun initView(attrs: AttributeSet) {
		_pickerHour = WheelPicker(context, null, attrs).apply {
			layoutParams = computeChildLayoutParams()
			setType(WheelType.HOUR)
		}
		_pickerMinute = WheelPicker(context, null, attrs).apply {
			layoutParams = computeChildLayoutParams()
			setType(WheelType.MINUTE)
		}
		_pickerDayNight = WheelPicker(context, null, attrs).apply {
			layoutParams = computeChildLayoutParams()
			setType(WheelType.DAY_NIGHT)
			isOverScroll(false)
		}
	}

	override fun LinearLayout.addView() {
		addView(_pickerHour)
		addView(_pickerMinute)
		if (is12Format) {
			addView(_pickerDayNight)
		}
	}

	override fun initDataAndEvent() {
		_pickerHour.set12Format(is12Format)
		_pickerDayNight.visibility = if (is12Format) VISIBLE else GONE

		_pickerHour.addListener(object : IWheelListener {
			override fun onScroll(value: Int, display: String) {
				currentSplit[0] = value
				combineSplit()
			}
		})
		_pickerMinute.addListener(object : IWheelListener {
			override fun onScroll(value: Int, display: String) {
				currentSplit[1] = value
				combineSplit()
			}
		})
		_pickerDayNight.addListener(object : IWheelListener {
			override fun onScroll(value: Int, display: String) {
				currentSplit[2] = value
				combineSplit()
			}
		})
		if (currentSelected.isNotEmpty()) {
			split()
		} else {
			currentSplit[0] = _pickerHour.getValue()
			currentSplit[1] = _pickerMinute.getValue()
			currentSplit[2] = _pickerDayNight.getValue()
		}
	}

	override fun addListener(listener: SimpleWheelListener) {
		iListener = listener
	}

	override fun setCurrent(date: Date) {
		currentSelected = CalUtils.convertToString(
			date.toString(),
			if (is12Format) CalUtils.FORMAT_12_HOUR else CalUtils.FORMAT_24_HOUR
		)
		split()
	}

	override fun setCurrent(string: String) {
		currentSelected = CalUtils.convertToString(
			string,
			if (is12Format) CalUtils.FORMAT_12_HOUR else CalUtils.FORMAT_24_HOUR
		)
		split()
	}

	private fun split() {
		CalUtils.getSplitTime(currentSelected, is12Format).let {
			currentSplit[0] = it[0]
			currentSplit[1] = it[1]
			currentSplit[2] = it[2]
		}

		_pickerHour.setCurrentSelected(currentSplit[0])
		_pickerMinute.setCurrentSelected(currentSplit[1])
		_pickerDayNight.setCurrentSelected(currentSplit[2])
	}

	private fun combineSplit() {
		val c = CalUtils.getCalendar().apply {
			set(Calendar.MINUTE, currentSplit[1])
			if (is12Format) {
				set(Calendar.HOUR, currentSplit[0])
				set(Calendar.AM_PM, currentSplit[2])
			} else {
				set(Calendar.HOUR_OF_DAY, currentSplit[0])
			}
		}
		currentSelected = CalUtils.convertToString(
			c.time.toString(),
			if (is12Format) CalUtils.FORMAT_12_HOUR else CalUtils.FORMAT_24_HOUR
		)
		iListener?.onPickDay(currentSelected)
	}
}