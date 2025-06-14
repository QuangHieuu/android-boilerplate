package boilerplate.utils.extension

import android.graphics.Color
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.regex.Pattern

fun String.toInt(): Int {
	return try {
		Integer.parseInt(this)
	} catch (e: NumberFormatException) {
		Integer.MIN_VALUE
	}
}

fun String.toDouble(): Double {
	return try {
		java.lang.Double.parseDouble(this)
	} catch (e: NumberFormatException) {
		Double.MIN_VALUE
	}
}

@Throws(ParseException::class)
fun String.toDate(format: String): Date? {
	val parser = SimpleDateFormat(format, Locale.getDefault())
	return parser.parse(this)
}

@Throws(ParseException::class)
fun String.toDateWithFormat(inputFormat: String, outputFormat: String): String {
	val gmtTimeZone = TimeZone.getTimeZone("UTC")
	val inputDateTimeFormat = SimpleDateFormat(inputFormat, Locale.getDefault())
	inputDateTimeFormat.timeZone = gmtTimeZone

	val outputDateTimeFormat = SimpleDateFormat(outputFormat, Locale.getDefault())
	outputDateTimeFormat.timeZone = gmtTimeZone
	inputDateTimeFormat.parse(this).let {
		if (it != null) return outputDateTimeFormat.format(it)
		return ""
	}
}

fun String.validWithPattern(pattern: Pattern): Boolean {
	return pattern.matcher(lowercase(Locale.ROOT)).find()
}

fun String.validWithPattern(regex: String): Boolean {
	return Pattern.compile(regex).matcher(this).find()
}

fun List<String>.toStringWithFormatPattern(format: String): String {
	if (this.isEmpty()) {
		return ""
	}
	val builder = StringBuilder()
	for (s in this) {
		builder.append(s)
		builder.append(format)
	}
	var result = builder.toString()
	result = result.substring(0, result.length - format.length)
	return result
}

fun String.removeWhitespaces(): String {
	return this.replace("[\\s-]*".toRegex(), "")
}

fun String.subString(beginInDex: Int, endIndex: Int): String {
	return this.substring(beginInDex, endIndex)
}

fun String.insert(index: Int, contentInsert: String): String {
	val builder = StringBuilder(this)
	builder.insert(index, contentInsert)
	return builder.toString()
}

fun List<String>.convertStringToListStringWithFormatPattern(format: String): String {
	if (this.isEmpty()) {
		return ""
	}
	val builder = StringBuilder()
	for (s in this) {
		builder.append(s)
		builder.append(format)
	}
	var result = builder.toString()
	result = result.substring(0, result.length - format.length)
	return result
}

fun SpannableString.withClickableSpan(
	color: Int,
	textView: TextView,
	clickablePart: String, onClickListener: () -> Unit
): SpannableString {
	val clickableSpan = object : ClickableSpan() {
		override fun onClick(view: View) = onClickListener.invoke()
	}
	val clickablePartStart = indexOf(clickablePart)
	setSpan(
		clickableSpan,
		clickablePartStart,
		clickablePartStart + clickablePart.length,
		Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
	)

	setSpan(
		ForegroundColorSpan(color),
		clickablePartStart, clickablePartStart + clickablePart.length,
		Spanned.SPAN_INCLUSIVE_EXCLUSIVE
	)

	with(textView) {
		movementMethod = LinkMovementMethod.getInstance()
		isClickable = true
		highlightColor = Color.TRANSPARENT
	}

	return this
}

fun String.validateEmailOrPhoneNo(): Boolean {
	return !(!Patterns.EMAIL_ADDRESS.matcher(this).matches()
		&& !this.matches("[0-9]+".toRegex()))
}

fun String.validateEmail(): Boolean {
	return Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.validateTextAndNumber(): Boolean {
	val patternTextNumber = Pattern.compile("^[a-zA-Z0-9]+\$")
	val matcherTextNumber = patternTextNumber.matcher(this)
	return matcherTextNumber.find()
}

fun List<EditText>.isNotEmptyEditText(): Boolean {
	for (editText in this) {
		if (TextUtils.isEmpty(editText.text.trim())) {
			return false
		}
	}
	return true
}

fun String.getSubString(beginInDex: Int, endIndex: Int): String {
	return substring(beginInDex, endIndex)
}

fun TextView.setSpannableString(
	contentSp: String,
	startIndexSp: Int,
	endIndexSp: Int,
	isUnderLineSp: Boolean,
	color: Int
) {
	val spannableString = SpannableString(contentSp)
	spannableString.setSpan(
		ForegroundColorSpan(ContextCompat.getColor(context, color)),
		startIndexSp, endIndexSp, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
	)

	if (isUnderLineSp) {
		spannableString.setSpan(
			UnderlineSpan(), startIndexSp, endIndexSp,
			Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
		)
	}

	text = spannableString
	movementMethod = LinkMovementMethod.getInstance()
}

fun TextView.setSpannableString(
	clickableSpan: ClickableSpan,
	startIndexSp: Int,
	endIndexSp: Int
) {
	val spannableString = SpannableString(text.toString())
	spannableString.setSpan(
		clickableSpan, startIndexSp, endIndexSp,
		Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
	)

	text = spannableString
	movementMethod = LinkMovementMethod.getInstance()
}

fun String.setSpannableString(
	what: Any,
	sub: String
): SpannableString {
	if (!contains(sub)) {
		return SpannableString(this)
	}
	val startIndex = indexOf(sub)
	val endIndex = startIndex + sub.length
	val spannableString = SpannableString(this)
	spannableString.setSpan(what, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

	return spannableString
}

fun convertStringToNumber(input: String): Int {
	return try {
		Integer.parseInt(input)
	} catch (e: NumberFormatException) {
		Integer.MIN_VALUE
	}
}

fun Int.formatMoney(): String {
	val formatter = DecimalFormat("###,###,###")
	return formatter.format(this.toLong())
}

fun Long.formatMoney(): String {
	val formatter = DecimalFormat("###,###,###")
	return formatter.format(this)
}

fun Float.formatMoneyWithFloat(): String {
	val formatter = DecimalFormat("###,###,###")
	return formatter.format(toDouble())
}

fun Int.formatMoneyDisplay(): String {
	val formatter = DecimalFormat("###,###,###")
	return formatter.format(toLong()) + "P"
}

fun Int.formatMoneySpaceDisplay(): String {
	val formatter = DecimalFormat("###,###,###")
	return formatter.format(toLong()) + " P"
}

fun String.replaceString(regex: String, replacement: String): String {
	val msg = replace(regex.toRegex(), replacement)
	return Html.fromHtml(msg, Html.FROM_HTML_MODE_COMPACT).toString()
}

fun Int.formatMoneyCastClass(): String {
	val formatter = DecimalFormat("###,###,###")
	return formatter.format(toLong()) + "P/30分"
}

fun String.formatCreditCardNumber(): String {
	val result = StringBuilder()
	for (i in indices) {
		if (i % 4 == 0 && i != 0) {
			result.append(" ")
		}
		result.append(this[i])
	}
	return result.toString()
}