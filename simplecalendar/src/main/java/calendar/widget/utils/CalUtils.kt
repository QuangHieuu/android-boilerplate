package calendar.widget.utils

import android.text.TextUtils
import calendar.widget.utils.CalUtils.FORMAT_DAY
import calendar.widget.utils.CalUtils.FORMAT_MONTH
import calendar.widget.utils.CalUtils.FORMAT_NORMAL
import calendar.widget.utils.CalUtils.FORMAT_YEAR
import calendar.widget.utils.CalUtils.convertToString
import calendar.widget.utils.CalUtils.getCalendar
import calendar.widget.utils.CalUtils.getCalendarStringWith
import calendar.widget.utils.CalUtils.getSplitDay
import calendar.widget.utils.CalUtils.multiFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

// Lấy thời gian hiện tại
private val currentDay = getCalendar()

// Lấy ngày 20/05/2025
private val currentDayWith = getCalendar(2025, 4, 20)

// Lấy tháng trước so với ngày hiện tại
private val beforeOneMonth = getCalendarStringWith(Calendar.MONTH, -1)

// Chuyển thời gian thành định dạng mong muốn
private val convertInputToString = convertToString("21/05/2025", "dd/MM/yyyy")

internal object CalUtils {

	const val FORMAT_DAY: String = "dd"
	const val FORMAT_MONTH: String = "MM"
	const val FORMAT_YEAR: String = "yyyy"
	const val FORMAT_HOUR_12: String = "hh"
	const val FORMAT_HOUR_24: String = "HH"
	const val FORMAT_MINUTE: String = "mm"
	const val FORMAT_MARKER: String = "a"
	const val FORMAT_DAY_OF_WEEK: String = "EEE"
	const val FORMAT_DAY_OF_WEEK_FULL: String = "EEEE"

	const val FORMAT_MONTH_FULL: String = "MMMM"

	const val FORMAT_12_HOUR = "hh:mm a"
	const val FORMAT_24_HOUR = "HH:mm"

	const val FORMAT_REVERT_T: String = "yyyy-MM-dd'T'HH:mm:ss"
	const val FORMAT_REVERT_Z: String = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
	const val FORMAT_REVERT: String = "yyyy/MM/dd"
	const val FORMAT_REVERT_WITHOUT: String = "yyyyMMdd"
	const val FORMAT_REVERT_WITH_TIME: String = "yyyy/MM/dd HH:mm"

	const val FORMAT_REVERT_HYPHEN_WITH_TIME = "yyyy-MM-dd HH:mm:ss"

	const val FORMAT_NORMAL: String = "dd/MM/yyyy"
	const val FORMAT_NORMAL_WITH_TIME: String = "dd/MM/yyyy HH:mm"
	const val FORMAT_NORMAL_WITH_REVERT_TIME: String = "HH:mm-dd/MM/yyyy"

	const val FORMAT_CALENDAR: String = "EEE MMM dd HH:mm:ss zzz yyyy"

	private val multiFormat: Array<String> = arrayOf(
		FORMAT_CALENDAR,
		FORMAT_REVERT_Z,
		FORMAT_REVERT_T,
		FORMAT_NORMAL,
		FORMAT_REVERT_WITHOUT,
		FORMAT_REVERT,
		FORMAT_NORMAL_WITH_TIME,
		FORMAT_NORMAL_WITH_REVERT_TIME,
		FORMAT_REVERT_WITH_TIME,
		FORMAT_12_HOUR,
		FORMAT_24_HOUR,
		FORMAT_REVERT_HYPHEN_WITH_TIME
	)

	/**
	 * Trả thời gian hiện tại với các giá trị ngày tháng năm cụ thể
	 *
	 * @param month lưu ý giá trị ở đây phải là index của tháng chứ không phải tháng cụ thể
	 * vd: Tháng 12 -> [month] = 11
	 *
	 * @sample currentDay
	 * @sample currentDayWith
	 */
	fun getCalendar(
		year: Int? = null,
		month: Int? = null,
		day: Int? = null,
	): Calendar {
		val c = Calendar.getInstance(Locale.getDefault())
		year?.let { c.set(Calendar.YEAR, it) }
		month?.let { c.set(Calendar.MONTH, it) }
		day?.let { c.set(Calendar.DAY_OF_MONTH, it) }
		c.set(Calendar.HOUR_OF_DAY, 0)
		c.set(Calendar.MINUTE, 0)
		c.set(Calendar.SECOND, 0)
		c.set(Calendar.MILLISECOND, 0)
		return c
	}

	fun getCalendar(date: Date? = null): Calendar {
		val c = Calendar.getInstance(Locale.getDefault())
		date?.let { c.time = it }
		return c
	}

	fun getCalendarWith(
		set: Int? = null,
		value: Int? = null,
	): Calendar {
		val c = Calendar.getInstance(Locale.getDefault())
		set?.let { c.add(set, value ?: c.get(it)) }
		return c
	}

	/**
	 * Trả thời gian hiện tại
	 * @param set thiết lập giá trị mong muốn thay đổi [Calendar.DAY_OF_MONTH], [Calendar.MONTH], [Calendar.YEAR]
	 * @param value giá trị mong muốn thay đổi. Giá trị có thể âm
	 *
	 *@sample beforeOneMonth
	 */
	@JvmOverloads
	fun getCalendarStringWith(
		set: Int? = null,
		value: Int? = null,
		inputFormat: String = "dd/MM/yyyy",
	): String {
		try {
			val c = getCalendarWith(set, value)
			val df = SimpleDateFormat(inputFormat, Locale.getDefault())
			return df.format(c.time)
		} catch (e: Exception) {
			return ""
		}
	}

	/**
	 * @param monthIndex trả lại giá trị [FORMAT_MONTH] là index
	 *
	 *@return current day split format [getSplitDay]
	 */
	fun getSplitCurrentDay(
		outputFormat: String = FORMAT_NORMAL,
		monthIndex: Boolean = false
	): Array<Int> {
		val c = Calendar.getInstance(Locale.getDefault())
		return getSplitDay(c.time.toString(), outputFormat, monthIndex)
	}

	fun compare2Time(
		start: String,
		end: String,
		isNormal: Boolean,
		block: (newTime: String) -> Unit
	) {
		val format = SimpleDateFormat(FORMAT_24_HOUR, Locale.getDefault())
		val c = Calendar.getInstance()
		try {
			val dateStart = convertToDate(start, FORMAT_24_HOUR)
			val dateEnd = convertToDate(end, FORMAT_24_HOUR)
			if (dateEnd != null && dateStart != null) {
				val cal = dateStart.time - dateEnd.time
				if (abs(cal) < 30.times(60).times(1000) || cal < 0 && !isNormal) {
					c.timeInMillis = dateStart.time
					c.add(Calendar.MINUTE, if (isNormal) 30 else -30)
					block(format.format(c.time))
				}
			}
		} catch (_: ParseException) {
		}
	}

	/**
	 * @param monthIndex trả lại giá trị [FORMAT_MONTH] là index
	 *
	 *@return Array([FORMAT_DAY], [FORMAT_MONTH], [FORMAT_YEAR])
	 */
	fun getSplitDay(
		input: String,
		outputFormat: String = FORMAT_NORMAL,
		monthIndex: Boolean = false
	): Array<Int> {
		val array = arrayOf(0, 0, 0)
		val date = convertToDate(input, outputFormat) ?: return array

		SimpleDateFormat(FORMAT_DAY, Locale.getDefault()).run {
			try {
				array[0] = format(date).toInt()
			} catch (_: ParseException) {
				return array
			}
		}
		SimpleDateFormat(FORMAT_MONTH, Locale.getDefault()).run {
			try {
				array[1] = format(date).toInt().let { if (monthIndex) it - 1 else it }
			} catch (_: ParseException) {
				return array
			}
		}
		SimpleDateFormat(FORMAT_YEAR, Locale.getDefault()).run {
			try {
				array[2] = format(date).toInt()
			} catch (e: ParseException) {
				return array
			}
		}
		return array
	}

	fun getSplitTime(input: String, isShow12Hour: Boolean = true): Array<Int> {
		val array = arrayOf(0, 0, 0)
		val date =
			convertToDate(input, if (isShow12Hour) FORMAT_12_HOUR else FORMAT_24_HOUR) ?: return array

		SimpleDateFormat(
			if (isShow12Hour) FORMAT_HOUR_12 else FORMAT_HOUR_24,
			Locale.getDefault()
		).run {
			try {
				array[0] = format(date).toInt()
			} catch (_: ParseException) {
				return array
			}
		}

		SimpleDateFormat(FORMAT_MINUTE, Locale.getDefault()).run {
			try {
				array[1] = format(date).toInt()
			} catch (_: ParseException) {
				return array
			}
		}

		if (isShow12Hour) {
			SimpleDateFormat(FORMAT_MARKER, Locale.getDefault()).run {
				try {
					array[2] = if (format(date) == "AM") 0 else 1
				} catch (_: ParseException) {
					return array
				}
			}
		} else {
			array[2] = 0
		}
		return array
	}

	/**
	 * @param input thời gian đầu vào
	 * @param outputFormat định dạng thời gian mong muốn vd:[FORMAT_NORMAL]
	 * @param timezoneIn timezone của [input]
	 * @param timezoneOut timezone của giá trị trả lại
	 *
	 * [input] phải thuộc 1 trong nhưng định dạng được khai báo trong [multiFormat]
	 *
	 * @return [Date] định dạng thời gian theo [outputFormat]
	 */
	fun convertToDate(
		input: String,
		outputFormat: String,
		timezoneIn: String? = null,
		timezoneOut: String? = null
	): Date? {
		for (f in multiFormat) {
			val date = convertWithFormatToDate(input, f, outputFormat, timezoneIn, timezoneOut)
			if (date != null) return date
		}
		return null
	}

	/**
	 * @param input thời gian đầu vào
	 * @param outputFormat định dạng thời gian mong muốn vd:[FORMAT_NORMAL]
	 * @param timezoneIn timezone của [input]
	 * @param timezoneOut timezone của giá trị trả lại
	 *
	 * [input] phải thuộc 1 trong nhưng định dạng được khai báo trong [multiFormat]
	 *
	 * @return [String] định dạng thời gian theo [outputFormat]
	 *
	 * @sample convertInputToString
	 */
	fun convertToString(
		input: String,
		outputFormat: String,
		timezoneIn: String? = null,
		timezoneOut: String? = null
	): String {
		for (f in multiFormat) {
			val s = convertWithFormatToString(input, f, outputFormat, timezoneIn, timezoneOut)
			if (s.isNotEmpty()) return s
		}
		return ""
	}

	/**
	 * @param input thời gian đầu vào
	 * @param format định dạng thời gian mong muốn vd:[FORMAT_NORMAL]
	 * @param formatOut định dạng thời gian mong muốn vd:[FORMAT_NORMAL]
	 * @param timezoneIn timezone của [input]
	 * @param timezoneOut timezone của giá trị trả lại
	 *
	 * @return [Date] định dạng thời gian theo [formatOut]
	 *
	 */
	fun convertWithFormatToDate(
		input: String,
		format: String,
		formatOut: String,
		timezoneIn: String? = null,
		timezoneOut: String? = null
	): Date? {
		if (TextUtils.isEmpty(input)) return null
		val inputFormat = SimpleDateFormat(format, format.checkFormatLocale()).apply {
			isLenient = false
			timezoneIn?.let { timeZone = TimeZone.getTimeZone(it) }
		}
		val output = SimpleDateFormat(formatOut, Locale.getDefault()).apply {
			isLenient = false
			timezoneOut?.let { timeZone = TimeZone.getTimeZone(it) }
		}
		return try {
			val d = inputFormat.parse(input)
			if (d != null) {
				val string = output.format(d)
				output.parse(string)
			} else {
				null
			}
		} catch (e: ParseException) {
			return null
		}

	}

	/**
	 * @param input thời gian đầu vào
	 * @param format định dạng thời gian mong muốn vd:[FORMAT_NORMAL]
	 * @param formatOut định dạng thời gian mong muốn vd:[FORMAT_NORMAL]
	 * @param timezoneIn timezone của [input]
	 * @param timezoneOut timezone của giá trị trả lại
	 *
	 * @return [String] định dạng thời gian theo [formatOut]
	 *
	 */
	fun convertWithFormatToString(
		input: String,
		format: String,
		formatOut: String,
		timezoneIn: String? = null,
		timezoneOut: String? = null
	): String {
		if (TextUtils.isEmpty(input)) return ""
		val inputFormat = SimpleDateFormat(format, format.checkFormatLocale()).apply {
			isLenient = false
			timezoneIn?.let { timeZone = TimeZone.getTimeZone(it) }
		}
		val output = SimpleDateFormat(formatOut, Locale.getDefault()).apply {
			isLenient = false
			timezoneOut?.let { timeZone = TimeZone.getTimeZone(it) }
		}
		return try {
			val d = inputFormat.parse(input)
			if (d != null) output.format(d) else ""
		} catch (e: ParseException) {
			""
		}
	}

	fun String.formatZero(): String {
		return if (indexOf("0") == 0) {
			replaceFirst("0".toRegex(), "")
		} else {
			this
		}
	}

	fun getDayOfWeekName(isFromSunday: Boolean = false): List<String> {
		val c = getCalendar()
		return arrayListOf<String>().apply {
			val start = Calendar.MONDAY.minus(if (isFromSunday) 1 else 0)
			val end = start.plus(6)

			(start..end).forEach { dayOfWeek ->
				val isSunday = dayOfWeek == 8 && isFromSunday

				c.set(Calendar.DAY_OF_WEEK, if (isSunday) Calendar.SUNDAY else dayOfWeek)
				add(convertToString(c.time.toString(), FORMAT_DAY_OF_WEEK))
			}
		}
	}

	private fun String.checkFormatLocale(): Locale {
		return if (this == FORMAT_CALENDAR) Locale.US else Locale.getDefault()
	}
}
