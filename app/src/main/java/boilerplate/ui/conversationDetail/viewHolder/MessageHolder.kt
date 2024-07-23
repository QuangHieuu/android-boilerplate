package boilerplate.ui.conversationDetail.viewHolder

import android.content.res.Resources
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import boilerplate.R
import boilerplate.constant.AccountManager
import boilerplate.databinding.ItemFileAudioBinding
import boilerplate.databinding.ItemFileBinding
import boilerplate.databinding.ItemFileSurveyBinding
import boilerplate.databinding.ItemMessageQuoteBinding
import boilerplate.model.file.AttachedFile
import boilerplate.model.file.ExtensionType
import boilerplate.model.message.Message
import boilerplate.model.message.Quote
import boilerplate.ui.conversationDetail.adpater.SimpleMessageEvent
import boilerplate.utils.DateTimeUtil
import boilerplate.utils.StringUtil
import boilerplate.utils.SystemUtil
import boilerplate.utils.extension.ANIMATION_DELAY
import boilerplate.utils.extension.click
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.loadImage
import boilerplate.utils.extension.show
import boilerplate.widget.customText.TextViewExpand
import boilerplate.widget.customText.TextViewFont
import boilerplate.widget.image.RoundedImageView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.daimajia.swipe.SimpleSwipeListener
import com.daimajia.swipe.SwipeLayout
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.lang.reflect.Type
import java.text.MessageFormat
import java.util.Locale

abstract class MessageHolder(
    private val itemView: View,
    private val _listener: SimpleMessageEvent,
    private val _viewType: Int,
    private val _disable: Boolean,
    private val resources: Resources = itemView.resources
) : RecyclerView.ViewHolder(itemView) {
    protected var _swipeLayout: SwipeLayout = itemView.findViewById(R.id.swipe)
    protected val _lnMessage: LinearLayout = itemView.findViewById(R.id.ln_message)
    protected var _tvContent: TextViewExpand = itemView.findViewById(R.id.tv_message_content)
    protected var _lnForward: LinearLayout = itemView.findViewById(R.id.ln_forward)
    protected var _lnImage: LinearLayout = itemView.findViewById(R.id.ln_image)
    protected var _imgSms: AppCompatImageView = itemView.findViewById(R.id.img_sms)
    protected var _imgEmail: AppCompatImageView = itemView.findViewById(R.id.img_email)
    protected var _tvTime: TextViewFont = itemView.findViewById(R.id.tv_message_time)
    protected var _lnIconReaction: LinearLayout = itemView.findViewById(R.id.ln_icon_reaction)
    protected var _imgReaction: AppCompatImageView = itemView.findViewById(R.id.img_reaction)
    protected var _lnMain: LinearLayout = itemView.findViewById(R.id.ln_main)
    protected var _lnReaction: LinearLayout = itemView.findViewById(R.id.ln_reaction)
    protected var _imgStar: AppCompatImageView = itemView.findViewById(R.id.img_star)
    protected var _lnFile: LinearLayout = itemView.findViewById(R.id.ln_file)

    private val _audioAttribute: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()

    private val _gson: Gson = Gson()
    private val _typeToken: Type = object : TypeToken<ArrayList<Quote>>() {}.type

    private val _iconExpand =
        ResourcesCompat.getDrawable(resources, R.drawable.ic_double_down_grey, null)
    private val _iconCollapse =
        ResourcesCompat.getDrawable(resources, R.drawable.ic_double_up_grey, null)

    protected val _start: Int = resources.getDimension(R.dimen.dp_10).toInt()
    private val _padding = resources.getDimension(R.dimen.dp_15).toInt()
    private val _minWidth = resources.getDimension(R.dimen.dp_150).toInt()
    private val _imgRadius = resources.getDimension(R.dimen.dp_6).toInt()
    private val _imgHeight = resources.getDimension(R.dimen.dp_120).toInt()
    private val _reactionColor = ContextCompat.getColor(itemView.context, R.color.colorBlack)

    protected val _mainSize = SystemUtil.getFontSizeChat(itemView.context)

    private val _imgParams =
        LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, _imgHeight)

    private var isQuote = false

    private lateinit var _message: Message


    init {
        _swipeLayout.addSwipeListener(object : SimpleSwipeListener() {
            override fun onClose(layout: SwipeLayout?) {
                isQuote = false
            }

            override fun onStartOpen(layout: SwipeLayout?) {
                Handler(Looper.getMainLooper()).postDelayed({
                    if (_message.status != 0 && !isQuote) {
                        _listener.quoteMessage(_message)
                        isQuote = true
                    }
                    _swipeLayout.close()
                }, ANIMATION_DELAY)
            }
        })
        if (_disable) {
            _lnMain.apply {
                setBackgroundResource(R.color.transparent)
                click { _listener.closeMenu() }
            }

            _lnMessage.setOnLongClickListener(null)
            _swipeLayout.isSwipeEnabled = false
        } else {
            _lnMain.apply {
                setBackgroundResource(R.color.colorAppBackground)
                click { null }
            }
            _lnMessage.setOnLongClickListener { v ->
                _listener.longClick(_message, v, _viewType)
                false
            }
        }

        itemView.setOnLongClickListener { v ->
            _lnMessage.performLongClick()
            false
        }
        _tvContent.addListener(object : TextViewExpand.SimpleEvent() {
            override fun onMention(userId: String) {
                if (userId.isNotEmpty()) {
                    _listener.mentionUser(userId)
                }
            }

            override fun onPhoneNumber(number: String) {
                if (number.isNotEmpty()) {
                    _listener.openPhone(number)
                }
            }
        })

        _imgReaction.click { _listener.showReaction(_message, it) }
    }

    open fun setData(message: Message, isAnswerable: Boolean) {
        _message = message

        changeFontSize()
        formatMessage()
        handleFile()
        handleReaction()

        if (!_disable) {
            _swipeLayout.isSwipeEnabled = isAnswerable
        }

        if (message.status == 2) {
            _swipeLayout.isSwipeEnabled = false
        }

        if (message.status == 2) {
            _swipeLayout.isSwipeEnabled = false
        }

        _tvTime.text = DateTimeUtil.convertWithSuitableFormat(
            message.dateCreate,
            DateTimeUtil.FORMAT_ONLY_TIME
        )

        _imgEmail.apply { (if (message.isSendMail) show() else gone()) }
        _imgSms.apply { (if (message.isSendSms) show() else gone()) }
        _imgStar.apply { (if (message.isImportant) show() else gone()) }
    }

    private fun formatMessage() {
        with(_message) {
            val strings: Array<String> = getMainContent()
            val mainContent = strings[0]
            val json = strings[1]
            val s: CharSequence = StringUtil.getHtml(mainContent)
            if (mainContent.isEmpty()) {
                _tvContent.gone()
            } else {
                _tvContent.show()
                _tvContent.setText(s)
            }
            _lnForward.removeAllViews()
            if (json.isNotEmpty()) {
                try {
                    val listQuote: ArrayList<Quote> = _gson.fromJson(json, _typeToken)
                    if (listQuote.isNotEmpty()) {
                        _lnForward.show()
                        for (quote in listQuote) {
                            _lnForward.addView(addViewQuote(quote))
                        }
                    } else {
                        _lnForward.gone()
                    }
                } catch (e: JsonParseException) {
                    _lnForward.gone()
                }
            } else {
                _lnForward.gone()
            }
        }
    }

    private fun handleFile() {
        _lnImage.removeAllViews()
        _lnFile.removeAllViews()
        val surveys: java.util.ArrayList<AttachedFile.SurveyFile> = _message.surveyFiles

        var countImage = 0
        var countFile = 0

        for (file in surveys) {
            _lnFile.addView(addViewSurvey(file))
        }

        for (file in _message.attachedFiles) {
            if (ExtensionType.isFileImage(file.fileName)) {
                countImage += 1
                _lnImage.addView(addViewImage(file, _message.status))
            } else {
                countFile += 1
                _lnFile.addView(addViewFile(file, false, _message.status))
            }
        }

        _lnFile.apply { if (countFile > 0 || surveys.size > 0) show() else gone() }
        _lnImage.apply { if (countImage > 0 || surveys.size > 0) show() else gone() }
    }

    private fun changeFontSize() {
        _tvContent.setTextSize(_mainSize)
        _tvTime.textSize = _mainSize - 2
    }

    private fun addViewQuote(quote: Quote): View {
        val layoutInflater = LayoutInflater.from(itemView.context)
        val binding =
            ItemMessageQuoteBinding.inflate(layoutInflater, itemView.rootView as ViewGroup, false)

        with(binding) {
            imgClose.gone()

            lnQuote.setBackgroundResource(R.drawable.bg_message_quote)
            lnQuote.setPadding(_start, _start, _start / 2, _start)

            tvPersonSend.apply {
                text = quote.personSend.name
                textSize = _mainSize
            }

            tvLastActive.apply {
                val last = DateTimeUtil.convertWithSuitableFormat(
                    quote.dateCreate,
                    DateTimeUtil.FORMAT_NORMAL_WITH_TIME_REVERT
                )
                text = last
                textSize = _mainSize
            }

            tvDepartment.apply {
                val quoteString = java.lang.String.format(
                    "%s - %s",
                    quote.personSend.mainDepartment.shortName,
                    quote.personSend.mainCompany.shortName
                )
                text = quoteString
                textSize = _mainSize
                show()
            }

            lnFile.removeAllViews()
            if (quote.surveyFiles.isEmpty() && quote.attachedFiles.isNotEmpty()) {
                lnFile.show()
                for (file in quote.attachedFiles) {
                    lnFile.addView(addViewFile(file, true, 0))
                }
            } else {
                if (quote.surveyFiles.isNotEmpty()) {
                    lnFile.show()
                    for (file in quote.surveyFiles) {
                        lnFile.addView(addViewSurvey(file))
                    }
                } else {
                    lnFile.gone()
                }
            }

            tvExpend.apply {
                textSize = _mainSize
                click { tvMessageQuote.collapseText() }
            }
            tvMessageQuote.apply {
                setTextSize(_mainSize)
                setMaxLine(5)
            }

            val string = StringUtil.getHtml(quote.content)
            if (string.isEmpty()) {
                tvMessageQuote.apply {
                    gone()
                    addListener(null)
                }
            } else {
                tvMessageQuote.apply {
                    gone()
                    setText(string)
                    addListener(object : TextViewExpand.SimpleEvent() {
                        override fun onMention(userId: String) {
                            if (userId.isNotEmpty()) {
                                _listener.mentionUser(userId)
                            }
                        }

                        override fun onReadMore(isCollapse: Boolean) {
                            tvExpend.apply {
                                setText(if (isCollapse) R.string.show_more else R.string.collapse)
                                setCompoundDrawablesWithIntrinsicBounds(
                                    if (isCollapse) _iconExpand else _iconCollapse,
                                    null,
                                    null,
                                    null
                                )
                            }
                        }

                        override fun onViewIsExpand(isExpand: Boolean) {
                            tvExpend.visibility = if (isExpand) View.VISIBLE else View.GONE
                        }
                    })
                }
            }

            tvGoTo.apply {
                textSize = _mainSize
                if (!_disable && quote.conversationId != null &&
                    quote.conversationId.equals(_message.conversationId)
                ) {
                    show()
                    click { _listener.goToMessage(_message.messageId, _message.conversationId) }
                } else {
                    gone()
                    click { null }
                }
            }
        }
        return binding.root
    }

    private fun addViewFile(file: AttachedFile, isQuote: Boolean, status: Int): View {
        val name: String = file.getFileName(itemView.context)!!.replace("_system_deleted", "")
        val size: String = file.getFileSize(itemView.context)

        if (ExtensionType.isFileAudio(file.fileName) && !isQuote) {
            val binding = ItemFileAudioBinding.inflate(
                LayoutInflater.from(itemView.context),
                itemView.rootView as ViewGroup,
                false
            ).apply {
                tvTime.textSize = _mainSize
                tvFileName.textSize = _mainSize
                progressBarLoading.show()
                imgDownload.apply { if (status == 2) gone() else show() }
                MediaPlayer().apply {
                    run {
                        try {
                            val headers: MutableMap<String, String> =
                                HashMap<String, String>().apply {
                                    this["Authorization"] = AccountManager.getToken()
                                }

                            setAudioAttributes(_audioAttribute)
                            if (status == 2) {
                                setDataSource(
                                    itemView.context,
                                    Uri.parse(file.getUrl()),
                                    headers
                                )
                            } else {
                                setDataSource(
                                    itemView.context,
                                    Uri.parse(file.fileDownloadUrl),
                                    headers
                                )
                            }
                            prepareAsync()
                        } catch (ignored: IOException) {
                            tvTime.gone()
                            progressBarLoading.gone()
                            imgPlay.apply { setImageResource(R.drawable.ic_play_blue) }.show()
                        }
                    }
                    setOnCompletionListener { obj: MediaPlayer -> obj.release() }
                    setOnPreparedListener { mp: MediaPlayer ->
                        progressBarLoading.gone()
                        imgPlay.apply { setImageResource(R.drawable.ic_play_blue) }.show()

                        tvTime.apply {
                            var seconds = mp.duration / 1000
                            val minutes = seconds / 60
                            seconds %= 60
                            text = String.format(
                                Locale.getDefault(),
                                "%d:%02d",
                                minutes,
                                seconds
                            )
                        }.show()
                    }
                }
                tvFileName.click { imgPlay.performClick() }
                imgDownload.click { _listener.openFile(file as AttachedFile.Conversation) }
                imgPlay.click { _listener.playRecord(file as AttachedFile.Conversation) }
            }
            return binding.root
        } else {
            val binding = ItemFileBinding.inflate(
                LayoutInflater.from(itemView.context),
                itemView.rootView as ViewGroup,
                false
            )
            with(binding) {
                imgClear.gone()

                tvFileName.apply {
                    textSize = _mainSize
                    text = name
                }
                tvFileSize.apply {
                    text = size
                    textSize = _mainSize
                }
                imgIcon.setImageResource(ExtensionType.getFileIcon(file.fileName))
            }
            return binding.root.apply {
                click { _listener.openFile(file as AttachedFile.Conversation) }
                setPadding(0, _padding / 2, 0, _padding / 2)
            }
        }
    }

    private fun addViewSurvey(file: AttachedFile.SurveyFile): View {
        val layoutInflater = LayoutInflater.from(itemView.context)
        val binding =
            ItemFileSurveyBinding.inflate(layoutInflater, itemView.rootView as ViewGroup, false)

        with(binding) {
            root.setBackgroundResource(R.drawable.bg_message_survey)

            tvFileName.apply {
                textSize = _mainSize
                text = file.displayTitle
            }

            tvTitle.textSize = _mainSize
            if (file.isSurveyBreakfast) {
                tvTitle.text = itemView.context.getString(R.string.breakfast_file)
                tvTitle.setTextColor(ContextCompat.getColor(itemView.context, R.color.color_FF951A))
                imgIcon.setImageResource(R.drawable.ic_breakfast)
            } else {
                tvTitle.text = itemView.context.getString(R.string.survey_file)
                tvTitle.setTextColor(ContextCompat.getColor(itemView.context, R.color.colorPrimary))
                imgIcon.setImageResource(R.drawable.ic_survey_file)
            }
        }
        return binding.root.apply { click { _listener.openSurvey(file) } }
    }


    private fun addViewImage(file: AttachedFile.Conversation, status: Int): View {
        val imageView = RoundedImageView(itemView.context).apply {
            _imgParams.setMargins(0, _imgRadius, 0, 0)

            setAdjustViewBounds(true)
            setScaleType(ImageView.ScaleType.CENTER_CROP)
            setLayoutParams(_imgParams)
            setMinimumWidth(_minWidth)
            setRadius(_imgRadius)
            click { _listener.openImage(file) }
        }.apply {
            val options: RequestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontTransform()
                .error(R.drawable.bg_error)

            val string = if (status == 2) file.getUrl() else file.filePreview

            loadImage(string, type = file.fileType, requestOptions = options)
        }

        return imageView
    }

    private fun handleReaction() {
        _lnIconReaction.removeAllViews()
        if (_message.reactions.isNotEmpty()) {
            _lnIconReaction.show()
            var count = 0
            for (item in _message.reactions) {
                count += item.count
                _lnIconReaction.addView(TextViewFont(itemView.context).apply {
                    text = item.emoticonName
                    setTextColor(_reactionColor)
                })
            }
            TextViewFont(itemView.context).apply {
                text = MessageFormat.format("{0}", count)
                setPadding(10, 0, 10, 0)
                setFontMedium()
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, _mainSize - 3)
            }.let {
                _lnIconReaction.addView(it)
            }
            _lnIconReaction.click { _listener.whoseReactions(_message.messageId) }
        } else {
            _lnIconReaction.gone()
        }
    }

    protected fun runAnimationFocused(message: Message, drawable: Int) {
        if (message.isFocus) {
            _lnMessage.animate()
                .scaleX(1.03f)
                .scaleY(1.03f)
                .setDuration(200)
                .withEndAction {
                    _lnMessage.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .withEndAction { _lnMessage.clearAnimation() }
                }
        } else {
            _lnMessage.clearAnimation()
        }
        _lnMessage.setBackgroundResource(if (message.isFocus) R.drawable.bg_message_focus else drawable)
    }

}