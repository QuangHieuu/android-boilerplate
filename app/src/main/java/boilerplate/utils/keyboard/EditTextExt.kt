package boilerplate.utils.keyboard

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import boilerplate.utils.extension.ANIMATION_DELAY

fun EditText.hideKeyboard() {
	with(this) {
		clearFocus()
		postDelayed({
			val imm =
				context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
			imm.hideSoftInputFromWindow(windowToken, 0)
		}, ANIMATION_DELAY)
	}
}

fun EditText.showKeyboard() {
	with(this) {
		isFocusable = true
		isFocusableInTouchMode = true
		if (requestFocus()) {
			postDelayed({
				val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
				imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
			}, ANIMATION_DELAY)
		}
	}
}

inline fun EditText.addListener(
	crossinline before: (s: CharSequence, start: Int, count: Int, after: Int) -> Unit = { _: CharSequence, _: Int, _: Int, _: Int -> },
	crossinline change: (s: CharSequence, start: Int, before: Int, count: Int) -> Unit = { _: CharSequence, _: Int, _: Int, _: Int -> },
	crossinline after: (s: Editable) -> Unit = {}
): TextWatcher {
	val textWatcher = object : TextWatcher {
		override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
			before(s, start, count, after)
		}

		override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
			change(s, start, before, count)
		}

		override fun afterTextChanged(s: Editable) {
			after(s)
		}
	}
	addTextChangedListener(textWatcher)
	return textWatcher
}

fun EditText.beforeTextChange(action: (s: CharSequence, start: Int, count: Int, after: Int) -> Unit) =
	addListener(before = action)

fun EditText.onTextChanged(action: (s: CharSequence, start: Int, before: Int, count: Int) -> Unit) =
	addListener(change = action)

fun EditText.afterTextChanged(action: (s: Editable) -> Unit) =
	addListener(after = action)
