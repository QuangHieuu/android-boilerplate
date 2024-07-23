package boilerplate.widget.chatBox

import android.content.Context
import android.graphics.Rect
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import boilerplate.R
import boilerplate.constant.AccountManager
import boilerplate.databinding.ViewChatBoxBinding
import boilerplate.model.conversation.ConversationUser
import boilerplate.utils.StringUtil
import boilerplate.utils.SystemUtil
import boilerplate.utils.extension.click
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.show
import boilerplate.utils.extension.showKeyboard
import boilerplate.widget.customText.EditTextFont
import boilerplate.widget.customText.KeyboardReceiver
import java.util.regex.Pattern

class ChatBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var _pointClickX = 0
    private var _pointClickY = 0

    companion object {
        const val LIKE_MESSAGE: String = "\uD83D\uDC4D"
        const val HAPPY_BIRTHDAY_MESSAGE: String =
            "Happy Birthday! \uD83C\uDF81 \uD83C\uDF89 \uD83C\uDF82"
    }

    private var _binding =
        ViewChatBoxBinding.inflate(LayoutInflater.from(context), rootView as ViewGroup, false)

    private val _ranges = arrayListOf<Range>()
    private val _mentions: ArrayList<ConversationUser> = arrayListOf()

    private var _hasMentionFunc: Boolean = true

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                _pointClickX = ev.rawX.toInt()
                _pointClickY = ev.rawY.toInt()
            }

            MotionEvent.ACTION_UP -> {
                val editBox = _binding.edtMessage
                val rect = Rect()
                val location = IntArray(2)
                getLocationOnScreen(location)
                rect.set(
                    location[0],
                    location[1],
                    location[0] + measuredWidth,
                    location[1] + measuredHeight
                )
                Log.d("SSS", "dispatchTouchEvent: ")
                if (rect.contains(_pointClickX, _pointClickY)) {
                    val editRect = Rect()
                    val editLocation = IntArray(2)
                    _binding.edtMessage.getLocationOnScreen(editLocation)
                    editRect.set(
                        editLocation[0],
                        editLocation[1],
                        editLocation[0] + editBox.measuredWidth,
                        editLocation[1] + editBox.measuredHeight
                    )
                    if (editRect.contains(_pointClickX, _pointClickY)) {
                        editBox.showKeyboard()
                    }
                } else {
                }
                return super.dispatchTouchEvent(ev)
            }
        }
        return false
    }

    init {
        addView(_binding.root)
        with(_binding) {
            edtMessage.apply {
                ViewCompat.setOnReceiveContentListener(
                    this,
                    KeyboardReceiver.MIME_TYPES,
                    KeyboardReceiver()
                )
                textSize = SystemUtil.getFontSizeChat(context)
                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {

                    }

                    override fun onTextChanged(
                        s: CharSequence,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        handleInputMention(s)
                    }

                    override fun afterTextChanged(s: Editable?) {
                    }
                })
            }

            imgSendLike.click {
//                sendMessage()
//                mListener.onSendMessage(
//                    LIKE_MESSAGE,
//                    new ArrayList < > (),
//                    new ArrayList < > (),
//                    getSurveyFile(),
//                    false,
//                    false
//                );
            }
        }
    }

    fun enableChat(isEnable: Boolean) {
        with(_binding) {
            tvDisable.apply {
                if (isEnable) gone() else show()
            }
            ctlChatBox.apply {
                if (isEnable) show() else gone()
            }
        }
    }

    fun addMentions(list: ArrayList<ConversationUser>) {
        val mentions = ArrayList<ConversationUser>()
        for (s in list) {
            if (!s.user.id.equals(AccountManager.getCurrentUserId())) {
                mentions.add(s)
            }
        }
//        mMentionAdapter.addData(mentions)
    }


    fun getEditMessage(): EditTextFont {
        return _binding.edtMessage
    }

    private fun handleInputMention(s: CharSequence) {
        if (_hasMentionFunc) {
            if (s.isEmpty()) {
//                mMentionAdapter.clear()
                return
            }
            val cursorPos: Int = _binding.edtMessage.getSelectionStart()
            val checkPosition = mentionRemovePosition(cursorPos)
            val checkMention = s.toString().substring(checkPosition, cursorPos)
            if (checkMention.contains(StringUtil.MENTION_SIGN)) {
                val textSearch: String =
                    checkMention.substring(checkMention.lastIndexOf(StringUtil.MENTION_SIGN) + 1)
                filterMention(textSearch)
            } else {
//                mMentionAdapter.clear()
            }
            mentionStyle()
        }
    }

    private fun mentionRemovePosition(posCursor: Int): Int {
        synchronized(_ranges) {
            for (mention in _ranges) {
                if (mention.from < posCursor && mention.to < posCursor) {
                    return mention.to
                }
            }
            return 0
        }
    }

    private fun filterMention(textSearch: String) {
        val finds = java.util.ArrayList<ConversationUser>()
//        for (userTag in mMentionAdapter.getOriginMentions()) {
//            if (!alreadyMention(userTag.user.getId())
//                && (userTag.user.getName().toLowerCase()
//                    .contains(textSearch.lowercase(Locale.getDefault())) || textSearch.isEmpty())
//            ) {
//                finds.add(userTag)
//            }
//        }
//        mMentionAdapter.addFind(finds)
    }

    private fun alreadyMention(userId: String): Boolean {
        synchronized(_mentions) {
            var isMention = false
            for (value in _mentions) {
                if (value.user.id.equals(userId)) {
                    isMention = true
                    break
                }
            }
            return isMention
        }
    }

    private fun mentionStyle() {
        if (!_hasMentionFunc) {
            return
        }
        synchronized(_mentions) {
            val spannableText: Editable = _binding.edtMessage.editableText
            if (spannableText.toString().isEmpty()) {
                return
            }

            _ranges.clear()

            //remove previous spans
            val oldSpans =
                spannableText.getSpans(
                    0, spannableText.length,
                    ForegroundColorSpan::class.java
                )
            for (oldSpan in oldSpans) {
                spannableText.removeSpan(oldSpan)
            }

            //find mention string and color it
            val text = spannableText.toString()
            val iterator: MutableListIterator<ConversationUser> =
                _mentions.listIterator()
            while (iterator.hasNext()) {
                val user = iterator.next()
                val name: String = (user.user.name ?: "")
                    .replace(".", "\\.")
                    .replace("(", "\\(")
                    .replace(")", "\\)")
                val matcher =
                    Pattern.compile(String.format("(?<name>@%s)", name)).matcher(text)
                if (matcher.find()) {
                    val mentionText = matcher.group()
                    val start = text.indexOf(mentionText)
                    val end = start + mentionText.length
                    spannableText.setSpan(
                        ForegroundColorSpan(ContextCompat.getColor(context, R.color.color_1552DC)),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    _ranges.add(Range(start, end))
                } else {
                    iterator.remove()
                }
            }
        }
    }

}
