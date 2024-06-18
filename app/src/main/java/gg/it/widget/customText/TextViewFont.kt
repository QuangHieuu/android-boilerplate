package gg.it.widget.customText

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import gg.it.R

class TextViewFont @JvmOverloads constructor(
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
        val array = context.obtainStyledAttributes(attrs, R.styleable.TextViewFont, 0, 0)
        val typefaceAssetPath =
            array.getResourceId(R.styleable.TextViewFont_customTypeface, R.font.roboto_regular)
        val typeface = ResourcesCompat.getFont(context, typefaceAssetPath)
        setTypeface(typeface)
        val requireTitle: Boolean = array.getBoolean(R.styleable.TextViewFont_require_title, false)
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
        array.recycle()
    }

    override fun setText(text: CharSequence, type: BufferType) {
        val s: Spannable = SpannableString(text)
        val spans: Array<URLSpan> = s.getSpans(0, s.length, URLSpan::class.java)
        if (spans.isNotEmpty()) {
            for (span in spans) {
                span.let {
                    val start: Int = s.getSpanStart(it)
                    val end: Int = s.getSpanEnd(it)
                    val nsp = URLSpanNoUnderline(span.url)
                    s.removeSpan(it)
                    s.setSpan(nsp, start, end, 0)
                }
            }
            super.setText(s, type)
        } else {
            super.setText(text, type)
        }
    }

    private class URLSpanNoUnderline(url: String?) : URLSpan(url) {
        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = false
        }
    }
}