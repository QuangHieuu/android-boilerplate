package calendar.widget.table.model

import calendar.widget.utils.CalUtils
import java.util.Date

data class DayOfMonth(
	val year: Int,
	val month: Int,
	val day: Int
) {

	var isDisable: Boolean = false
	var isInRange: Boolean = false
	var isSelected: Boolean = false
	var isToday: Boolean = false
	var isInMonth: Boolean = true

	val toDate: Date = CalUtils.getCalendar(year, month, day).time

}