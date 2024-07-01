package boilerplate.utils

import android.text.Html
import android.text.Spanned

object StringUtil {
    const val KEY_MENTION_ALL = "ALL"
    const val KEY_MENTION_USER_ID = "userId="
    const val KEY_MENTION_PHONE = "phoneNumber="

    const val HOTLINE: String = "0975 909 779"

    @JvmStatic
    fun getHotLine(): String {
        return String.format(
            "Hotline: <font color=\"#FF0000\">%s</font>",
            HOTLINE
        )
    }

    @JvmStatic
    fun getHtmlSpan(text: String?): Spanned {
        return Html.fromHtml(
            text,
            Html.FROM_HTML_OPTION_USE_CSS_COLORS or Html.FROM_HTML_MODE_COMPACT
        )
    }
}