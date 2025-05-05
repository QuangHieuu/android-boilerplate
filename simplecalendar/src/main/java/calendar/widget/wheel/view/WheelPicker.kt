package calendar.widget.wheel.view

import android.content.Context
import android.empty.calendar.R
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import calendar.widget.utils.CalUtils
import calendar.widget.wheel.base.DefaultWheelView
import calendar.widget.wheel.model.WheelModel
import calendar.widget.wheel.type.WheelType
import calendar.widget.wheel.type.WheelType.*
import java.text.DecimalFormat
import java.util.Calendar
import kotlin.math.abs

class WheelPicker : DefaultWheelView {

	companion object {

		private const val INVALIDATE: Int = -1
		private const val MIN_YEAR_DIFF: Int = 50
		private const val MAX_YEAR_DIFF: Int = 50

		internal const val KEY_MIN = "KEY_MIN"
		internal const val KEY_MAX = "KEY_MAX"
		internal const val KEY_TYPE = "KEY_TYPE"
		internal const val KEY_SHOW_CURRENT = "KEY_SHOW_CURRENT"
		internal const val KEY_HOUR_12_FORMAT = "KEY_HOUR_12_FORMAT"
		internal const val KEY_MONTH_ONLY_NUMBER = "KEY_MONTH_ONLY_NUMBER"
		internal const val KEY_SHOW_FUTURE = "KEY_SHOW_FUTURE"
		internal const val KEY_TODAY_TEXT = "KEY_TODAY_TEXT"
		internal const val KEY_REVERSE = "KEY_REVERSE"
		internal const val KEY_DAY_ONLY_NUMBER = "KEY_DAY_ONLY_NUMBER"
		internal const val KEY_DAY_OF_WEEK_FROM_SUNDAY = "KEY_DAY_OF_WEEK_FROM_SUNDAY"
	}

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

	constructor(
		context: Context,
		parent: AttributeSet? = null,
		attrs: AttributeSet
	) : super(context, parent, attrs)

	override fun generationData(bundle: Bundle): Pair<List<WheelModel>, Int> {
		val type = bundle.getInt(KEY_TYPE, INVALIDATE)
		val isShowCurrent = bundle.getBoolean(KEY_SHOW_CURRENT)
		val isReverse = bundle.getBoolean(KEY_REVERSE)

		val min: Int = bundle.getInt(KEY_MIN, INVALIDATE)
		val max: Int = bundle.getInt(KEY_MAX, INVALIDATE)
		val isShowFuture = bundle.getBoolean(KEY_SHOW_FUTURE)

		val list = arrayListOf<WheelModel>()
		var index = 0
		when (WheelType.getType(type)) {
			DAY_OF_WEEK -> {
				val isFromSunday = bundle.getBoolean(KEY_DAY_OF_WEEK_FROM_SUNDAY)
				val c = CalUtils.getCalendar()
				val currDayOfWeek = c.get(Calendar.DAY_OF_WEEK)

				//Bắt đầu là thứ 2
				val start = Calendar.MONDAY.minus(if (isFromSunday) 1 else 0)
				val end = start.plus(6)

				val minDay = min.validateElse(start)
				val maxDay = max.validateElse(end)

				(minDay..maxDay).forEach { dayOfWeek ->
					val isSunday = dayOfWeek == 8 && isFromSunday
					if (isShowCurrent && currDayOfWeek == dayOfWeek) {
						index = when {
							isSunday -> Calendar.SUNDAY
							else -> dayOfWeek
						}
					}

					c.set(Calendar.DAY_OF_WEEK, if (isSunday) Calendar.SUNDAY else dayOfWeek)
					val wheelModel = WheelModel().apply {
						value = dayOfWeek
						display = CalUtils.convertToString(c.time.toString(), CalUtils.FORMAT_DAY_OF_WEEK_FULL)
					}
					list.add(wheelModel)
				}
			}

			DAY -> {
				val decimal = DecimalFormat("00")
				val onlyNumber = bundle.getBoolean(KEY_DAY_ONLY_NUMBER)
				val today = bundle.getString(KEY_TODAY_TEXT)

				val c = CalUtils.getCalendar()
				val curr = c.get(Calendar.DAY_OF_MONTH)
				val daysOfMonth: Int = c.getActualMaximum(Calendar.DAY_OF_MONTH)

				for (i in 1..daysOfMonth) {
					val wheelModel = WheelModel()
					wheelModel.value = i
					wheelModel.display = if (i == curr) {
						index = i.minus(1)
						if (!onlyNumber || today != null) {
							today ?: context.getString(R.string.today)
						} else {
							decimal.format(i)
						}
					} else {
						decimal.format(i)
					}
					list.add(wheelModel)
				}
			}

			MONTH -> {
				val c = CalUtils.getCalendar(day = 1)
				val currMonth = c.get(Calendar.MONTH)
				val minMonth = min.validateElse(0)
				val maxMonth = max.validateElse(11)

				val onlyNumber = bundle.getBoolean(KEY_MONTH_ONLY_NUMBER)

				(minMonth..maxMonth).forEach { month ->
					val wheelModel = WheelModel()
					wheelModel.value = month
					wheelModel.display = if (onlyNumber) {
						month.plus(1).toString()
					} else {
						c[Calendar.MONTH] = month
						CalUtils.convertToString(c.time.toString(), CalUtils.FORMAT_MONTH_FULL)
					}
					list.add(wheelModel)

					if (month == currMonth && isShowCurrent) {
						index = month
					}
				}
			}

			YEAR -> {
				val c = Calendar.getInstance()
				val currYear = c.get(Calendar.YEAR)
				val minYear = min.validateElse(currYear.minus(MIN_YEAR_DIFF))
				val maxYear = if (isShowFuture) {
					max.validateElse(currYear.plus(MAX_YEAR_DIFF))
				} else {
					currYear
				}

				(minYear..maxYear).forEachIndexed { indexed, it ->
					val wheelModel = WheelModel()
					wheelModel.value = it
					wheelModel.display = it.toString()
					list.add(wheelModel)

					if (isShowCurrent && it == currYear) {
						index = indexed
					}
				}
				if (isReverse) {
					list.reverse()
					index = abs(list.lastIndex - index)
				}
			}

			HOUR -> {
				val is12 = bundle.getBoolean(KEY_HOUR_12_FORMAT)
				val end = max.validateElse(if (is12) 11 else 23)
				(1..end).forEach {
					val wheelModel = WheelModel().apply {
						value = it
						display = it.toString()
					}
					list.add(wheelModel)
				}
			}

			MINUTE -> {
				val decimal = DecimalFormat("00")
				val end = max.validateElse(60)
				repeat((0 until end).count()) {
					val wheelModel = WheelModel().apply {
						value = it
						display = decimal.format(it)
					}
					list.add(wheelModel)
				}
			}

			DAY_NIGHT -> {
				list.add(WheelModel().apply {
					value = 0
					display = context.getString(R.string.am)
				})
				list.add(WheelModel().apply {
					value = 1
					display = context.getString(R.string.pm)
				})
			}

			NONE -> index = 0
		}
		return Pair(list, index)
	}

	fun setCurrentSelected(value: Int) {
		setCurrent(value)
	}

	fun set12Format(is12Format: Boolean) {
		arguments.putBoolean(KEY_HOUR_12_FORMAT, is12Format)
		reset()
	}

	fun setType(wheel: WheelType) {
		arguments.putInt(KEY_TYPE, wheel.type)
		reset()
	}

	fun isShowCurrent(isShow: Boolean = true) {
		arguments.putBoolean(KEY_SHOW_CURRENT, isShow)
		reset()
	}

	fun isShowMonthNumber(isShow: Boolean = true) {
		arguments.putBoolean(KEY_MONTH_ONLY_NUMBER, isShow)
		reset()
	}

	fun isReverse(reverse: Boolean = true) {
		arguments.putBoolean(KEY_REVERSE, reverse)
		reset()
	}

	private fun Int.validateElse(elseInput: Int): Int {
		return if (this != INVALIDATE) {
			this
		} else {
			elseInput
		}
	}
}
