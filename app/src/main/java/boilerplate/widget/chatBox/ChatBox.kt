package boilerplate.widget.chatBox

import android.content.ClipboardManager
import android.content.Context
import android.text.Editable
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import boilerplate.R
import boilerplate.constant.AccountManager
import boilerplate.databinding.ViewChatBoxBinding
import boilerplate.model.conversation.ConversationUser
import boilerplate.model.message.Message
import boilerplate.model.message.Quote
import boilerplate.model.user.User
import boilerplate.utils.StringUtil
import boilerplate.utils.SystemUtil
import boilerplate.utils.extension.addListener
import boilerplate.utils.extension.click
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.hideKeyboard
import boilerplate.utils.extension.isVisible
import boilerplate.utils.extension.notNull
import boilerplate.utils.extension.show
import boilerplate.utils.extension.showKeyboard
import boilerplate.utils.extension.showSnackBarWarning
import boilerplate.widget.chatBox.adapter.BoxAdapter
import boilerplate.widget.chatBox.adapter.MentionAdapter
import boilerplate.widget.customText.EditTextFont
import boilerplate.widget.customText.KeyboardReceiver
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import java.util.Locale
import java.util.regex.Pattern

class ChatBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        const val LIKE_MESSAGE: String = "\uD83D\uDC4D"
        const val HAPPY_BIRTHDAY_MESSAGE: String =
            "Happy Birthday! \uD83C\uDF81 \uD83C\uDF89 \uD83C\uDF82"
    }

    private lateinit var _mentionAdapter: MentionAdapter
    private lateinit var _boxAdapter: BoxAdapter

    private var _binding =
        ViewChatBoxBinding.inflate(LayoutInflater.from(context), rootView as ViewGroup, true)

    private val _ranges = arrayListOf<Range>()
    private val _mentions: ArrayList<ConversationUser> = arrayListOf()

    private var _listener: OnBoxListener? = null

    private var _hasRecordFunc = false
    private var _hasMentionFunc: Boolean = false
    private var _tempMessage: Message? = null
    private var _isSpeechToText = false
    private var _isRecording = false
    private var _isSendSms = false
    private var _isSendEmail = false
    private var _isForward = false
    private var _isEdit = false

    init {
        with(_binding) {
            _boxAdapter = BoxAdapter()

            edtMessage.apply {
                ViewCompat.setOnReceiveContentListener(
                    this,
                    KeyboardReceiver.MIME_TYPES,
                    KeyboardReceiver()
                )
                textSize = SystemUtil.getFontSizeChat(context)
                addListener(
                    change = { handleInputMention(it) },
                    after = { checkSendButton(it.toString()) }
                )
                setListener(object : EditTextFont.SimpleEvent() {
                    override fun onPaste() {
                        val clipboard =
                            getContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = clipboard.primaryClip
                        if (clip != null) {
                            val pasteIntent = clip.getItemAt(0).intent
                            if (pasteIntent != null) {
                                val json = pasteIntent.getStringExtra(SystemUtil.CLIPBOARD_MESSAGE)
                                val message: Message = Gson().fromJson(json, Message::class.java)
                                parseMessage(message, false)
                            }
                        }
                    }
                })
            }

            btnSendLike.click {
                _listener.notNull {
                    sendMessage()
                    it.onSendMessage(
                        LIKE_MESSAGE,
                        arrayListOf(),
                        arrayListOf(),
                        _boxAdapter.getSurveyFile(),
                        false,
                        false
                    )
                }
            }

            edtMessage.setOnFocusChangeListener { view, focus ->
                if (focus && _hasRecordFunc) {
                    showHideRecord(false)
                }
                _listener?.onEditFocus(focus)
            }

            btnCancelEdit.click { finishSendingMessage() }
            btnSend.click {
                _listener.notNull {
                    sendMessage()
                    val messageContent: String = getMessageContent()
                    if (validate(messageContent)) {
                        if (isFileLimit()) {
                            finishSendWhenError()
                            root.showSnackBarWarning(R.string.warning_file_maximum)
                        } else {
                            if (_isEdit) {
                                it.onEditMessage(
                                    _tempMessage!!,
                                    messageContent,
                                    _boxAdapter.getFileUpload(),
                                    _boxAdapter.getCurrentFile(),
                                    _boxAdapter.getSurveyFile(),
                                    _isSendSms,
                                    _isSendEmail
                                )
                            } else {
                                it.onSendMessage(
                                    messageContent,
                                    _boxAdapter.getFileUpload(),
                                    _boxAdapter.getCurrentFile(),
                                    _boxAdapter.getSurveyFile(),
                                    _isSendSms,
                                    _isSendEmail
                                )
                            }
                        }
                    } else {
                        finishSendWhenError()
                    }
                }
            }
        }
    }

    fun setListener(listener: SimpleBoxListener) {
        _listener = listener
    }

    fun setupForChat() {
        _hasMentionFunc = true

        _mentionAdapter = MentionAdapter(object : MentionAdapter.OnViewListener {
            override fun onChosen(user: ConversationUser) {
                _mentions.add(user)
                _mentionAdapter.clear()

                val posCursor: Int = getEditMessage().selectionStart
                val text: CharSequence = getEditMessage().editableText.subSequence(0, posCursor)

                val mentionPos = text.toString().lastIndexOf(StringUtil.MENTION_SIGN)

                setContent(mentionPos + 1, user.user.name + " ")
            }
        })
        with(_binding) {
            rcvMention.apply {
                adapter = _mentionAdapter
                setItemAnimator(null)
                show()
            }

            imgRecord.show()
            btnSendLike.show()

            btnSend.gone()
            progressBar.gone()
        }
    }

    fun setupForShare() {
        with(_binding) {
            btnSendLike.gone()
            progressBar.gone()

            btnSend.show()
        }
        enableChat(true)
    }

    fun sendMessage() {
        with(_binding) {
            if (!progressBar.isVisible()) {
                progressBar.show()
                btnSendLike.gone()
                btnSend.apply {
                    show()
                    isEnabled = false
                }
                btnCancelEdit.isEnabled = false
                enableAction(false)
                if (_hasRecordFunc) {
                    showHideRecord(false)
                }
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
        _mentionAdapter.addData(mentions)
    }

    fun parseMessage(message: Message, isEdit: Boolean) {
        finishSendingMessage()

        _tempMessage = message
        _isEdit = isEdit

        with(_binding) {

            btnSend.setEnabled(false)
            btnCancelEdit.apply { if (_isEdit) show() else gone() }
            for (file in message.attachedFiles) {
                file.isUpload = false
                file.isPreventRemove = false
//                _boxAdapter.addFile(file)
            }
            for (file in message.surveyFiles) {
                file.isUpload = false
                file.isPreventRemove = false
//                _boxAdapter.addFile(file)
            }
            val json = message.forwardMessage
            try {
                if (json.isNotEmpty()) {
                    val listQuote: ArrayList<Quote> =
                        Gson().fromJson(json, object : TypeToken<ArrayList<Quote?>?>() {}.type)
                    if (listQuote.isNotEmpty()) {
//                        _boxAdapter.addQuote(listQuote)
                    }
                }
            } catch (ignore: JsonParseException) {
            }
            val copyText = StringUtil.getHtml(message.mainContent[0]).toString()

            if (_hasMentionFunc && isEdit) {
                val list: ArrayList<String> =
                    StringUtil.findUserMentionId(message.mainContent[0])
                for (user in _mentionAdapter.originMentions) {
                    if (list.contains(user.user.id)) {
                        _mentions.add(user)
                        val name = StringUtil.MENTION_SIGN + user.user.name
                        val start = copyText.indexOf(name)
                        val end = start + name.length
                        _ranges.add(Range(start, end))
                    }
                }
            }
            setContentKeyboard(copyText)

            _isSendSms = message.isSendSms
            _isSendEmail = message.isSendMail
            checkAttachment()

            if (copyText.isEmpty()) {
                checkSendButton("")
            }
        }
    }

    fun setContentKeyboard(copyText: String) {
        setContent(0, copyText)
        if (copyText.isNotEmpty() || _boxAdapter.hasFile() || _boxAdapter.hasQuote()) {
            _binding.edtMessage.showKeyboard()
        }
    }

    fun finishSendWhenError() {
        with(_binding) {
            progressBar.gone()
            btnSend.isEnabled = true
            btnSendLike.isEnabled = true
            btnCancelEdit.isEnabled = true
        }
        enableAction(true)
    }

    fun finishSendingMessage() {
        _tempMessage = null
        _isEdit = false
        with(_binding) {
            progressBar.gone()
            btnSend.apply {
                gone()
                isEnabled = false
            }
            btnSendLike.apply {
                show()
                isEnabled = true
            }
            btnCancelEdit.apply {
                gone()
                isEnabled = false
            }

            if (_hasRecordFunc) {
                imgRecord.setImageResource(R.drawable.ic_microphone_grey)
                showHideRecord(false)
            }

            edtMessage.editableText.clear()

        }
        _mentions.clear()
        _boxAdapter.clearAll()
        enableAction(true)
        resetEmailAndSMS()
        checkAttachment()
    }

    fun getEditMessage(): EditTextFont {
        return _binding.edtMessage
    }

    fun startRecording() {
        with(_binding) {
            if (edtMessage.isFocused) {
                edtMessage.hideKeyboard()
            }
            _isRecording = true
            imgRecord.setImageResource(R.drawable.ic_microphone_fill)
        }
        showHideRecord(true)
    }

    private fun handleInputMention(s: CharSequence) {
        if (_hasMentionFunc) {
            if (s.isEmpty()) {
                _mentionAdapter.clear()
                return
            }
            val cursorPos: Int = _binding.edtMessage.selectionStart
            val checkPosition = mentionRemovePosition(cursorPos)
            val checkMention = s.toString().substring(checkPosition, cursorPos)
            if (checkMention.contains(StringUtil.MENTION_SIGN)) {
                val textSearch: String =
                    checkMention.substring(checkMention.lastIndexOf(StringUtil.MENTION_SIGN) + 1)
                filterMention(textSearch)
            } else {
                _mentionAdapter.clear()
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
        val finds = ArrayList<ConversationUser>()
        for (userTag in _mentionAdapter.originMentions) {
            if (!alreadyMention(userTag.user.id!!) &&
                (userTag.user.name!!.lowercase(Locale.getDefault())
                    .contains(textSearch.lowercase(Locale.getDefault())) || textSearch.isEmpty())
            ) {
                finds.add(userTag)
            }
        }
        _mentionAdapter.addFind(finds)
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
            val iterator: MutableListIterator<ConversationUser> = _mentions.listIterator()
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
                        ForegroundColorSpan(
                            ContextCompat.getColor(
                                context,
                                R.color.color_1552DC
                            )
                        ),
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

    private fun enableAction(isEnable: Boolean) {
        with(_binding) {
            imgAttachment.isEnabled = isEnable
            imgTakePhoto.isEnabled = isEnable
            imgPickImage.isEnabled = isEnable
            imgEmail.isEnabled = isEnable
            imgSms.isEnabled = isEnable
        }
    }

    private fun resetEmailAndSMS() {
        with(_binding) {
            imgEmail.setImageResource(R.drawable.ic_email)
            imgSms.setImageResource(R.drawable.ic_sms)
        }
        _isSendEmail = false
        _isSendSms = false
    }

    private fun showHideRecord(show: Boolean) {
        if (!show) {
            _isSpeechToText = false
            _isRecording = false
        }
        with(_binding) {
            tvRecord.show(show)
            imgStartRecord.show(show)
            swModeRecord.apply {
                show(show)
                check(_isSpeechToText)
            }
            tvSwMode.show(show)
        }
    }

    private fun checkAttachment() {
        _binding.rcvAttachment.show(_boxAdapter.itemCount > 0)
    }

    private fun checkSendButton(s: String) {
        val hasFile: Boolean = _boxAdapter.hasFile()
        val hasQuote: Boolean = _boxAdapter.hasQuote()

        val check = s.trim { it <= ' ' }.isEmpty() && !hasQuote && !hasFile
        with(_binding) {
            if (_isForward || _isEdit) {
                if (_hasRecordFunc) {
                    _binding.imgRecord.gone()
                }
                btnSendLike.gone()
                btnSend.apply {
                    show()
                    isEnabled = !check
                }
            } else {
                if (_hasRecordFunc) {
                    imgRecord.show(check)
                }
                if (check) {
                    btnSend.gone()
                    btnSendLike.show()
                } else {
                    btnSendLike.gone()
                    btnSend.apply {
                        show()
                        isEnabled = s.isNotEmpty() || hasFile || hasQuote
                    }
                }
            }
        }
    }

    private fun setContent(posInsert: Int, editContent: String) {
        with(_binding) {
            val currentCursor: Int = edtMessage.editableText.length
            edtMessage.editableText.replace(posInsert, currentCursor, editContent)
            edtMessage.setSelection(posInsert + editContent.length)
        }
    }

    private fun getMessageContent(): String {
        val content = StringBuilder()
        val messageForward = _boxAdapter.getListMessage()
        if (messageForward.isNotEmpty()) {
            val jsonForward = Gson().toJson(messageForward)
            content.append(StringUtil.KEY_FORWARD_JSON)
                .append(jsonForward)
                .append(StringUtil.KEY_FORWARD_JSON)
        }
        val messageLink = _boxAdapter.getListLinkMessage()
        for (link in messageLink) {
            content.append(StringUtil.KEY_HTML_HEADER)
                .append(link)
                .append(StringUtil.KEY_HTML_HEADER_END)
        }
        content.append(getEditText())
        return content.toString()
    }

    private fun getEditText(): String {
        var string: String = getEditMessage().editableText.toString()
        for (mention in _mentions) {
            val user: User = mention.user
            val mentionString = StringUtil.MENTION_SIGN + user.name
            val replaceString = StringUtil.getMentionMessage(user.id, user.name)
            string = string.replace(mentionString, replaceString)
        }
        return string
    }

    private fun validate(message: String): Boolean {
        return message.isNotEmpty() || _boxAdapter.itemCount != 0
    }

    private fun isFileLimit(): Boolean {
        return _boxAdapter.checkFileSizeLimit(context)
    }

}
