package boilerplate.utils

import android.annotation.SuppressLint
import android.text.format.DateUtils
import java.text.SimpleDateFormat

object TimeConvert {

    @SuppressLint("SimpleDateFormat")
    fun checkDate(time: Long): String {
        return if (DateUtils.isToday(time)) {
            "Today"
        } else {
            val formatter = SimpleDateFormat("EEE")
            formatter.format(time)
        }
    }


    @SuppressLint("SimpleDateFormat")
    fun parseTimestampToString(time: String): Long? {
        val DATE_FORMAT = "yyyy-MM-dd"
        val dateFormat = SimpleDateFormat(DATE_FORMAT)
        return dateFormat.parse(time)?.time
    }

    @SuppressLint("SimpleDateFormat")
    fun formatDate(
        time: String,
        formatInput: String = "yyyy-MM-dd",
        formatOutput: String = "MM/dd"
    ): String {
        val formatter = SimpleDateFormat(formatOutput)
        val inputFormat = SimpleDateFormat(formatInput)
        val date = inputFormat.parse(time)
        return formatter.format(date!!)
    }
}
