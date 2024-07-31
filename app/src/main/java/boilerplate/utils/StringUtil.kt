package boilerplate.utils

import android.content.Context
import android.text.Html
import android.text.Spanned
import boilerplate.R
import boilerplate.constant.AccountManager
import boilerplate.model.message.Message
import boilerplate.utils.extension.notNull
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.regex.Pattern

object StringUtil {
    const val HOTLINE: String = "0975 909 779"

    const val KEY_MENTION_ALL: String = "ALL"
    const val KEY_MENTION_USER_ID: String = "userId="
    const val KEY_MENTION_PHONE: String = "phoneNumber="
    const val KEY_HREF_LINK: String = "<a href='%s'>%s</a>"
    const val MENTION_SPAN_START: String = "<span style=\"color:#1552DC;\">"
    const val MENTION_SPAN_END: String = "</span>&nbsp;"

    const val KEY_FORWARD_JSON: String = "[messageForwardJSON]"
    const val KEY_CHILD_FORWARD_JSON: String = "<messageForwardJSON>"
    const val KEY_HTML_HEADER: String = "<p>"
    const val KEY_HTML_HEADER_END: String = "</p>"
    const val KEY_FORWARD_JSON_REGEX: String = "\\[messageForwardJSON\\]"
    const val REGEX_MENTION: String = "\\[([^()@]+)(|\\([^()@]+\\))]\\((|[^()@]+)\\)"
    const val REGEX_COLOR: String =
        "((?:rgba|rgb)\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,?\\s*([0-9.]*)\\s*\\))"
    const val REGEX_WEB_LINK: String =
        "((https?://|www\\\\.)[-a-zA-Z0-9+&@#/%?=~_|!:.;]*[-a-zA-Z0-9+&@#/%=~_|])"
    const val REGEX_PHONE_NUMBER: String =
        "^\\s*\$|^(\\+?84|0)+([981735]{1,3})([0-9]|\\s|-|\\.){7,10}"
    const val REGEX_LINK_CONTAIN: String = "<a\\b[^>]*>(.*?)</a>"
    const val REGEX_MENTION_INPUT: String = "@[\\u4e00-\\u9fa5\\w\\-\\s]+"

    const val MENTION_SIGN: String = "@"

    private val htmlEscape: Array<Array<String>> = arrayOf(
        arrayOf("&lt;", "<"),
        arrayOf("&gt;", ">"),
        arrayOf("&amp;", "&"),
        arrayOf("&quot;", "\\\""),
        arrayOf("&agrave;", "à"),
        arrayOf("&Agrave;", "À"),
        arrayOf("&acirc;", "â"),
        arrayOf("&auml;", "ä"),
        arrayOf("&Auml;", "Ä"),
        arrayOf("&Acirc;", "Â"),
        arrayOf("&aring;", "å"),
        arrayOf("&Aring;", "Å"),
        arrayOf("&aelig;", "æ"),
        arrayOf("&AElig;", "Æ"),
        arrayOf("&ccedil;", "ç"),
        arrayOf("&Ccedil;", "Ç"),
        arrayOf("&eacute;", "é"),
        arrayOf("&Eacute;", "É"),
        arrayOf("&egrave;", "è"),
        arrayOf("&Egrave;", "È"),
        arrayOf("&ecirc;", "ê"),
        arrayOf("&Ecirc;", "Ê"),
        arrayOf("&euml;", "ë"),
        arrayOf("&Euml;", "Ë"),
        arrayOf("&iuml;", "ï"),
        arrayOf("&Iuml;", "Ï"),
        arrayOf("&ocirc;", "ô"),
        arrayOf("&Ocirc;", "Ô"),
        arrayOf("&ouml;", "ö"),
        arrayOf("&Ouml;", "Ö"),
        arrayOf("&oslash;", "ø"),
        arrayOf("&Oslash;", "Ø"),
        arrayOf("&szlig;", "ß"),
        arrayOf("&ugrave;", "ù"),
        arrayOf("&Ugrave;", "Ù"),
        arrayOf("&ucirc;", "û"),
        arrayOf("&Ucirc;", "Û"),
        arrayOf("&uuml;", "ü"),
        arrayOf("&Uuml;", "Ü"),
        arrayOf("&nbsp;", " "),
        arrayOf("\"", "\\\""),
        arrayOf("&copy;", "©"),
        arrayOf("&reg;", "®"),
        arrayOf("&euro;", "₠")
    )

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

    @JvmStatic
    fun countWord(text: String, regex: String): ArrayList<Int> {
        val i = ArrayList<Int>()
        val p = Pattern.compile(regex)
        val m = p.matcher(text)
        while (m.find()) {
            i.add(m.start())
        }
        return i
    }

    @JvmStatic
    fun getMessageForward(text: String): String {
        if (text.startsWith(KEY_FORWARD_JSON)) {
            var result: String =
                text.substring(text.indexOf(KEY_FORWARD_JSON) + KEY_FORWARD_JSON.length)
            result = result.substring(0, result.indexOf(KEY_FORWARD_JSON))
            return result
        }
        return ""
    }

    @JvmStatic
    fun getHtml(text: String?): CharSequence {
        if (text.isNullOrEmpty()) {
            return ""
        }
        return text.let {
            var temp = it.replace("\n".toRegex(), "<br>").replace("\t", "<br>")
            temp = replaceRGBColorsWithHex(temp)
            temp = formatSupport(temp)
            temp = formatWebLink(temp)
            removeEnterRow(getHtmlSpan(temp))
        }
    }

    @JvmStatic
    fun handleMessageContent(
        context: Context,
        message: Message?,
        isGroup: Boolean,
        isMyCloud: Boolean
    ): String {
        if (message != null) {
            val isMe: Boolean = AccountManager.getCurrentUserId().equals(message.getPersonSendId())
            val isShowSender = if (isGroup) {
                true
            } else {
                if (isMyCloud) {
                    false
                } else {
                    isMe
                }
            }
            val content: String =
                getHtml(message.getMainContent()[0]).toString().trim { it <= ' ' }
            val senderName =
                if (isMe) "Bạn" else if (isGroup) message.getPersonSend().name else ""
            if (message.isMsgSystem) {
                return content
            }
            if (message.isWithdraw) {
                return if (isShowSender) {
                    String.format(
                        "%s: %s",
                        senderName,
                        context.getString(R.string.message_is_withdraw)
                    )
                } else {
                    context.getString(R.string.message_is_withdraw)
                }
            } else {
                val hasQuote: Boolean = message.getForwardMessage().isNotEmpty()
                val hasSurveyFile: Boolean = message.getSurveyFiles().isNotEmpty()
                val hasFile: Boolean = message.getAttachedFiles().isNotEmpty()
                val isContentNotEmpty = content.isNotEmpty()
                if (isContentNotEmpty && !hasSurveyFile && !hasFile) {
                    return if (isShowSender) {
                        String.format("%s: %s", senderName, content)
                    } else {
                        content
                    }
                }
                if (hasSurveyFile) {
                    val isBreakFastFile: Boolean = message.getSurveyFiles()[0].isSurveyBreakfast
                    val text = context.getString(
                        if (isBreakFastFile
                        ) R.string.message_send_breakfast_file
                        else R.string.message_send_survey_file
                    )
                    return if (isShowSender) {
                        String.format("%s: %s", senderName, text)
                    } else {
                        text
                    }
                }
                if (hasFile) {
                    val text = context.getString(R.string.message_send_file)
                    return if (isShowSender) {
                        String.format("%s: %s", senderName, text)
                    } else {
                        text
                    }
                }
                if (hasQuote) {
                    val text = context.getString(R.string.message_quote)
                    return if (isShowSender) {
                        String.format("%s: %s", senderName, text)
                    } else {
                        text
                    }
                }
            }
        }
        return ""
    }


    private fun removeEnterRow(text: CharSequence): CharSequence {
        var temp = text
        val p = Pattern.compile("\\n")
        val m = p.matcher(temp)
        while (m.find()) {
            if (m.end() == temp.length) {
                temp = temp.subSequence(0, temp.length - 1)
            }
        }
        return temp
    }

    private fun formatSupport(text: String): String {
        val doc = Jsoup.parse(formatPhone(text))
        val elementsIns = doc.select("ins")
        if (elementsIns.text().isNotEmpty()) {
            elementsIns.tagName("u")
        }

        val mentionTag = doc.getElementsByTag("mention")
        if (mentionTag.text().isNotEmpty()) {
            for (element in mentionTag) {
                val s: String = formatMention(element.wholeText())
                element.html(s)
            }
        }

        val aTag = doc.getElementsByTag("a")

        if (aTag.text().isNotEmpty()) {
            for (element in aTag) {
                element.removeAttr("style")
                element.removeAttr("target")
            }
        }
        val elements = doc.getElementsByAttribute("style")
        if (elements.text().isNotEmpty()) {
            for (element in elements) {
                if (element.tagName() != "span") {
                    val style = element.attr("style")
                    if (!style.isEmpty()) {
                        element.removeAttr("style")
                        val spanStyle = Element("span")
                        spanStyle.attr("style", style)
                        element.wrap(spanStyle.toString())
                    }
                }
            }
        }
        return doc.toString()
    }

    private fun replaceRGBColorsWithHex(html: String): String {
        var temp = html
        val p = Pattern.compile(REGEX_COLOR, Pattern.CASE_INSENSITIVE)
        val m = p.matcher(temp)

        while (m.find()) {
            // get whole matched rgb(a,b,c) text
            val foundRGBColor = m.group(1) ?: return temp

            // get r value
            val rString = m.group(2)
            // get g value
            val gString = m.group(3)
            // get b value
            val bString = m.group(4)
            // get a value
            val aString = m.group(5)

            // converting numbers from string to int
            val rInt = (rString ?: "0").toInt()
            val gInt = (gString ?: "0").toInt()
            val bInt = (bString ?: "0").toInt()
            val aFloat = (if (aString == null || aString.isEmpty()) "1" else aString).toFloat()
            val alphaFixed = Math.round(aFloat * 255).toLong()

            // converting int to hex value
            val rHex = Integer.toHexString(rInt)
            val gHex = Integer.toHexString(gInt)
            val bHex = Integer.toHexString(bInt)
            val aHex = java.lang.Long.toHexString(alphaFixed)

            // add leading zero if number is small to avoid converting
            // rgb(1,2,3) to rgb(#123)
            val rHexFormatted = String.format("%2s", rHex).replace(" ", "0")
            val gHexFormatted = String.format("%2s", gHex).replace(" ", "0")
            val bHexFormatted = String.format("%2s", bHex).replace(" ", "0")
            val aHexFormatted = String.format("%2s", aHex).replace(" ", "0")

            // concatenate new color in hex
            val hexColorString = "#$rHexFormatted$gHexFormatted$bHexFormatted;"
            temp = temp.replace(Pattern.quote(foundRGBColor).toRegex(), hexColorString)
        }
        return temp
    }

    private fun formatWebLink(text: String): String {
        var result = StringBuilder(text)
        val aOpen = "<a href=\""
        val close = "\">"
        val aClose = "</a>"
        val pattern = Pattern.compile(
            REGEX_WEB_LINK,
            Pattern.CASE_INSENSITIVE
        )
        val urlMatcher = pattern.matcher(text)
        var count = 0
        while (urlMatcher.find()) {
            val matchStart = urlMatcher.start(1)
            val checkStart = matchStart - aOpen.length

            var format: Boolean
            if (checkStart <= 0) {
                format = true
            } else {
                val check = text.substring(checkStart, matchStart)
                format = check != aOpen
            }

            if (format) {
                val matchEnd = urlMatcher.end(1)
                val url = text.substring(matchStart, matchEnd)
                val builder = StringBuilder()
                builder.append(aOpen).append(url).append(close).append(url).append(aClose)

                result = result.replace(matchStart + count, matchEnd + count, builder.toString())
                count += builder.length - url.length
            }
        }
        return result.toString()
    }

    private fun formatPhone(text: String): String {
        var result = java.lang.StringBuilder(text)
        val pattern = Pattern.compile(
            REGEX_PHONE_NUMBER,
            Pattern.MULTILINE
        )
        val m = pattern.matcher(text)
        var count = 0
        while (m.find()) {
            val phone = m.group()
            val replacePhone: String = String.format(
                KEY_MENTION_PHONE + "%s",
                phone
            )
            val replaceText: String = String.format(
                KEY_HREF_LINK,
                replacePhone,
                phone
            )
            val start = m.start()
            val end = m.end()
            if (end > start) {
                result = result.replace(start + count, end + count, replaceText)
                count += replaceText.length - phone.length
            }
        }
        return result.toString()
    }

    private fun formatMention(text: String): String {
        var result = java.lang.StringBuilder(text)
        val pattern2 = Pattern.compile(REGEX_MENTION)
        val m2 = pattern2.matcher(text)
        var count = 0
        while (m2.find()) {
            val user = m2.group()
            val userName = m2.group(1)
            val userRole = m2.group(2)
            val userId = m2.group(3)
            if (userName != null) {
                val replaceUser =
                    "@" + userName + (if (userRole != null && user.isNotEmpty()) " $userRole" else "")
                val replaceUserId: String = String.format(
                    "$KEY_MENTION_USER_ID%s",
                    userId
                )
                val replaceText: String = String.format(
                    KEY_HREF_LINK,
                    replaceUserId,
                    replaceUser
                )
                val startIndex = text.indexOf(user)
                val endIndex = startIndex + user.length
                result = result.replace(startIndex + count, endIndex + count, replaceText)
                count += replaceText.length - user.length
            }
        }
        return result.toString()
    }

    fun unescapeHTML(s: String, start: Int): String {
        var holder = s
        val j: Int
        var k: Int

        val i = holder.indexOf("&", start)
        if (i > -1) {
            j = holder.indexOf(";", i)
            if (j > i) {
                // ok this is not most optimized way to
                // do it, a StringBuffer would be better,
                // this is left as an exercise to the reader!
                val temp = holder.substring(i, j + 1)
                // search in htmlEscape[][] if temp is there
                k = 0
                while (k < htmlEscape.size) {
                    if (htmlEscape.get(k)
                            .get(0) == temp
                    ) break
                    else k++
                }
                if (k < htmlEscape.size) {
                    holder = holder.substring(0, i) + htmlEscape[k][1] + holder.substring(j + 1)
                    return unescapeHTML(holder, i) // recursive call
                }
            }
        }
        return holder
    }

    fun findUserMentionId(text: String): ArrayList<String> {
        val list = ArrayList<String>()
        val doc = Jsoup.parse(text)
        val mentionTag = doc.getElementsByTag("mention")
        if (mentionTag.text().isNotEmpty()) {
            for (element in mentionTag) {
                val pattern2 = Pattern.compile(REGEX_MENTION)
                val m2 = pattern2.matcher(element.wholeText())
                while (m2.find()) {
                    m2.group(3).notNull { list.add(it) }
                }
            }
        }
        return list
    }

    fun getMentionMessage(id: String?, name: String?): String {
        return String.format("<mention>[%s](%s)</mention>", name, id)
    }
}