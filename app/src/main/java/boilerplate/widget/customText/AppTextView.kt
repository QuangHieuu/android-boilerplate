package boilerplate.widget.customText

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.withStyledAttributes
import boilerplate.R

class AppTextView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
	init {
		initView(attrs)
	}

	fun setFontMedium() {
		val type = ResourcesCompat.getFont(context, R.font.roboto_medium)
		typeface = type
	}

	fun setFontBold() {
		val type = ResourcesCompat.getFont(context, R.font.roboto_bold)
		typeface = type
	}

	fun setFontRegularItalic() {
		val type = ResourcesCompat.getFont(context, R.font.roboto_italic)
		typeface = type
	}

	fun setFontRegular() {
		val type = ResourcesCompat.getFont(context, R.font.roboto_regular)
		typeface = type
	}

	private fun initView(attrs: AttributeSet?) {
		if (attrs == null) {
			return
		}
		context.withStyledAttributes(attrs, R.styleable.AppTextView, 0, 0) {
			val typefaceAssetPath =
				getResourceId(R.styleable.AppTextView_customTypeface, R.font.roboto_regular)
			val typeface = ResourcesCompat.getFont(context, typefaceAssetPath)
			setTypeface(typeface)
			val requireTitle: Boolean = getBoolean(R.styleable.AppTextView_require_title, false)
			if (requireTitle) {
				val builder = SpannableStringBuilder(text)
				builder.append(" *")
				builder.setSpan(
					ForegroundColorSpan(Color.RED),
					builder.length - 1,
					builder.length,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
				)
				text = builder
			}
		}
	}
}
