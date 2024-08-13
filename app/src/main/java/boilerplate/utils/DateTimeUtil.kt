package boilerplate.utils

import android.text.TextUtils
import android.text.format.DateUtils
import java.math.RoundingMode
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object DateTimeUtil {

	const val FORMAT_OLD: String = "yyyy-MM-dd'T'HH:mm:ss"
	const val DATE_FORMAT_Z: String = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
	const val FORMAT_FROM_SERVER: String = "yyyy-MM-dd HH:mm:ss"
	const val FORMAT_NORMAL: String = "dd/MM/yyyy"
	const val FORMAT_REVERT: String = "yyyy/MM/dd"
	const val FORMAT_NORMAL_WITH_TIME: String = "dd/MM/yyyy HH:mm"
	const val FORMAT_NORMAL_WITH_TIME_NO_SPACE: String = "ddMMyyyyHHmm"
	const val FORMAT_NORMAL_WITH_TIME_REVERT: String = "HH:mm dd/MM/yyyy"
	const val FORMAT_POST_SERVER: String = "yyyy-MM-dd"
	const val FORMAT_ONLY_TIME: String = "HH:mm"
	const val FORMAT_NOT_YEAR: String = "dd/MM"

	const val FORMAT_DAY: String = "dd"
	const val FORMAT_MONTH: String = "MM"
	const val FORMAT_YEAR: String = "yyyy"

	const val SECOND_MILLIS: Long = 1000
	const val MINUTE_MILLIS: Long = 60 * SECOND_MILLIS
	const val HOUR_MILLIS: Long = 60 * MINUTE_MILLIS
	const val DAY_MILLIS: Long = 24 * HOUR_MILLIS
	const val MONTH_MILLIS: Long = 20 * DAY_MILLIS

	private val multiFormat: Array<String> = arrayOf(
		FORMAT_OLD,
		FORMAT_FROM_SERVER,
		FORMAT_NORMAL,
		FORMAT_REVERT,
		FORMAT_NORMAL_WITH_TIME,
		FORMAT_NORMAL_WITH_TIME_REVERT,
		FORMAT_POST_SERVER,
		DATE_FORMAT_Z
	)

	fun convertWithSuitableFormat(
		input: String,
		outputFormat: String,
		timezoneIn: String,
		timezoneOut: String
	): String {
		for (f in multiFormat) {
			val s: String =
				convert(input, f, outputFormat, timezoneIn, timezoneOut)
			if (!s.isEmpty()) return s
		}
		return ""
	}

	fun convertWithSuitableFormat(input: String, outputFormat: String): String {
		for (f in multiFormat) {
			val s: String = convert(input, f, outputFormat)
			if (!s.isEmpty()) return s
		}
		return ""
	}

	fun convertWithSuitableFormat(input: String): Date {
		var date: Date? = null
		for (f in multiFormat) {
			val inputFormat = SimpleDateFormat(f, Locale.getDefault())
			inputFormat.isLenient = false
			try {
				date = inputFormat.parse(input)!!
				break
			} catch (ignored: ParseException) {
			}
		}
		return date ?: Date()
	}

	fun getCurrentDateWith(set: Int, value: Int): String {
		val c = Calendar.getInstance()
		c.add(set, value)
		val df = SimpleDateFormat(FORMAT_NORMAL, Locale.getDefault())
		return df.format(c.time)
	}

	fun getCurrentDateWith(set: Int, value: Int, format: String): String {
		val c = Calendar.getInstance()
		c.add(set, value)
		val df = SimpleDateFormat(format, Locale.getDefault())
		return df.format(c.time)
	}

	fun getCurrentDate(format: String): String {
		val c = Calendar.getInstance()
		val df = SimpleDateFormat(format, Locale.getDefault())
		return df.format(c.time)
	}

	fun getCurrentDate(): String {
		val c = Calendar.getInstance()
		val df = SimpleDateFormat(FORMAT_NORMAL, Locale.getDefault())
		return df.format(c.time)
	}

	private fun convert(input: String, format: String, formatOut: String): String {
		if (TextUtils.isEmpty(input)) return ""
		val inputFormat = SimpleDateFormat(format, Locale.getDefault())
		val output = SimpleDateFormat(formatOut, Locale.getDefault())
		try {
			val d = inputFormat.parse(input)
			return if (d != null) {
				output.format(d)
			} else {
				""
			}
		} catch (e: ParseException) {
			return ""
		}
	}

	private fun convert(
		input: String,
		format: String,
		formatOut: String,
		timezoneIn: String,
		timezoneOut: String
	): String {
		if (TextUtils.isEmpty(input)) return ""
		val inputFormat = SimpleDateFormat(format, Locale.getDefault())
		inputFormat.isLenient = false
		inputFormat.timeZone = TimeZone.getTimeZone(timezoneIn)
		val output = SimpleDateFormat(formatOut, Locale.getDefault())
		output.isLenient = false
		output.timeZone = TimeZone.getTimeZone(timezoneOut)
		try {
			val d = inputFormat.parse(input)
			return if (d != null) {
				output.format(d)
			} else {
				""
			}
		} catch (e: ParseException) {
			return ""
		}
	}

	fun convertToTextTimePast(date: String): String {
		val time: Long = convertToTimestamp(date) / 1000
		val now = System.currentTimeMillis() / 1000
		val diff = now - time

		//0 to 44 seconds
		if (diff <= 0 || (TimeUnit.SECONDS.toSeconds(0) < diff && diff <= TimeUnit.SECONDS.toSeconds(
				44
			))
		) {
			return "Vài giây"
		}
		//45 to 89 seconds
		if (TimeUnit.SECONDS.toSeconds(44) < diff && diff <= TimeUnit.SECONDS.toSeconds(89)) {
			return "1 phút"
		}
		//90 seconds to 44 minutes
		if (TimeUnit.SECONDS.toSeconds(89) < diff && diff <= TimeUnit.MINUTES.toSeconds(44)) {
			return diff.toBigDecimal().divide(
				TimeUnit.MINUTES.toSeconds(1).toBigDecimal(),
				RoundingMode.HALF_DOWN
			).toString() + " phút"
		}
		//45 to 89 minutes
		if (TimeUnit.MINUTES.toSeconds(44) < diff && diff <= TimeUnit.MINUTES.toSeconds(89)) {
			return "1 giờ"
		}
		//90 minutes to 21 hours
		if (TimeUnit.MINUTES.toSeconds(89) < diff && diff <= TimeUnit.HOURS.toSeconds(21)) {
			return diff.toBigDecimal().divide(
				TimeUnit.HOURS.toSeconds(1).toBigDecimal(),
				RoundingMode.HALF_DOWN
			).toString() + " giờ"
		}
		//22 to 35 hours
		if (TimeUnit.HOURS.toSeconds(21) < diff && diff <= TimeUnit.HOURS.toSeconds(35)) {
			return "Hôm qua"
		}
		//36 hours to 25 days
		if (TimeUnit.HOURS.toSeconds(35) < diff && diff <= TimeUnit.DAYS.toSeconds(25)) {
			return diff.toBigDecimal().divide(
				TimeUnit.DAYS.toSeconds(1).toBigDecimal(),
				RoundingMode.HALF_DOWN
			).toString() + " ngày"
		}
		//26 to 45 days
		if (TimeUnit.DAYS.toSeconds(25) < diff && diff <= TimeUnit.DAYS.toSeconds(45)) {
			return "1 tháng"
		}
		//46 days to 10 months
		if (TimeUnit.DAYS.toSeconds(45) < diff && diff <= TimeUnit.DAYS.toSeconds((30 * 10).toLong())) {
			return diff.toBigDecimal().divide(
				TimeUnit.DAYS.toSeconds(30).toBigDecimal(),
				RoundingMode.HALF_DOWN
			).toString() + " tháng"
		}
		//11 months to 17 months
		if (TimeUnit.DAYS.toSeconds((30 * 10).toLong()) < diff && diff <= TimeUnit.DAYS.toSeconds((30 * 17).toLong())) {
			return "1 năm"
		}
		//18 months+
		if (TimeUnit.DAYS.toSeconds((30 * 17).toLong()) < diff && diff <= TimeUnit.DAYS.toSeconds((365 * 5).toLong())) {
			return diff.toBigDecimal().divide(
				TimeUnit.DAYS.toSeconds(365).toBigDecimal(),
				RoundingMode.HALF_DOWN
			).toString() + " năm"
		}
		return convertWithSuitableFormat(date, FORMAT_NORMAL)
	}

	fun convertToTimestamp(time: String, inputFormat: String): Long {
		try {
			val formatter = SimpleDateFormat(inputFormat, Locale.getDefault())
			formatter.isLenient = false
			val date = formatter.parse(time)
			return date?.time ?: 0L
		} catch (ex: ParseException) {
			return 0L
		}
	}

	fun convertToTimestamp(time: String): Long {
		val date = convertWithSuitableFormat(time)
		return date.time
	}

	fun compareTwoDateWithFormat(input1: String, input2: String): Boolean {
		val date1 = convertWithSuitableFormat(input1)
		val date2 = convertWithSuitableFormat(input2)
		return date1 < date2
	}

	fun isToDay(input: String?): Boolean {
		val time = convertToTimestamp(input!!)
		return DateUtils.isToday(time)
	}
}
