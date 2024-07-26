package boilerplate.ui.conversation.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import boilerplate.R
import boilerplate.constant.AccountManager
import boilerplate.databinding.ItemConversationBinding
import boilerplate.model.conversation.Conversation
import boilerplate.model.conversation.ConversationUser
import boilerplate.model.message.Message
import boilerplate.utils.DateTimeUtil
import boilerplate.utils.ImageUtil.IMAGE_THUMB_SIZE
import boilerplate.utils.StringUtil
import boilerplate.utils.SystemUtil.getFontSizeChat
import boilerplate.utils.extension.click
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.hide
import boilerplate.utils.extension.loadImage
import boilerplate.utils.extension.notNull
import boilerplate.utils.extension.show
import boilerplate.widget.image.RoundedImageView
import com.bumptech.glide.Glide
import com.daimajia.swipe.SimpleSwipeListener
import com.daimajia.swipe.SwipeLayout
import com.daimajia.swipe.implments.SwipeItemRecyclerMangerImpl

class ConversationVH(
    private val _binding: ItemConversationBinding,
    private val _sm: SwipeItemRecyclerMangerImpl?,
    private val mListListener: ConversationAdapter.SimpleEvent,
    private val pin: Boolean,
    private val _context: Context = _binding.root.context
) : RecyclerView.ViewHolder(_binding.root) {

    private val _imgSize: Int
    private val _imgSizeSingle: Int
    private val _readColor: Int
    private val _unReadColor: Int
    private val _readBackground: Int
    private val _unReadBackground: Int

    private var _conversation: Conversation? = null
    private var _isUserNotify = false

    init {
        _sm.notNull {
            _binding.swipeLayout.addSwipeListener(object : SimpleSwipeListener() {
                override fun onStartOpen(layout: SwipeLayout) {
                    it.closeAllExcept(layout)
                }
            })
        }

        _imgSize = _context.resources.getDimension(R.dimen.dp_26).toInt()
        _imgSizeSingle = _context.resources.getDimension(R.dimen.dp_46).toInt()

        _readColor = ContextCompat.getColor(_context, R.color.color_959595)
        _unReadColor = ContextCompat.getColor(_context, R.color.colorBlack)
        _readBackground = ContextCompat.getColor(_context, R.color.colorWhite)
        _unReadBackground = ContextCompat.getColor(_context, R.color.color_conversation_unread)

        with(_binding) {
            tvPin.setText(if (pin) R.string.un_pin else R.string.pin)
            imgPin.apply { if (pin) show() else hide() }

            rlConversationItem.click { _conversation?.let { con -> mListListener.onItemClick(con) } }
            lnMark.click {
                _conversation?.let { con -> mListListener.onMarkAsImportant(con) }
                swipeLayout.close()
            }
            lnDelete.click {
                _conversation?.let { con -> mListListener.onDelete(con) }
                swipeLayout.close()
            }
            lnNotify.click {
                _conversation?.let { con -> mListListener.onNotify(con, _isUserNotify) }
                swipeLayout.close()
            }
        }
    }

    fun setData(con: Conversation?, isShowPin: Boolean) {
        _conversation = con

        _binding.lnPin.apply { if (isShowPin) show() else gone() }

        _sm?.bindView(_binding.root, bindingAdapterPosition)

        setFontSize()
        val isGroup = con!!.isGroup()
        val thumb = con.getThumb(IMAGE_THUMB_SIZE)
        val size = con.tongSoNhanVien
        val userSize = con.conversationUsers.size
        val isOneUser = size == 1
        var unRead = 0

        _binding.swipeLayout.isSwipeEnabled = !con.isMyCloud

        val lastActive: String = DateTimeUtil.convertToTextTimePast(con.lastActive)

        _binding.tvLastActive.apply {
            show()
            text = lastActive
        }

        if (con.isMyCloud) {
            myCloud()
        } else {
            _binding.frameUserOnline.apply { if (isGroup) gone() else show() }
            val builder = StringBuilder(if (isGroup) con.conversationName else "")

            var needCreateName = builder.toString().isEmpty()
            if (isGroup && builder.toString().isEmpty() && isOneUser) {
                builder.append(itemView.context.getString(R.string.conversation_name))
            }
            if (isGroup) {
                if (thumb != null) {
                    val image = addSingleAvatar()
                    image.loadImage(con.getThumb(IMAGE_THUMB_SIZE))
                } else {
                    if (size == 1) {
                        addAvatar(1, size, null)
                    }
                    if (size > 4) {
                        addAvatar(3, size, null)
                    }
                }
            }
            var onlyContainMe = !isGroup
            var countName = 0
            val iterator: ListIterator<ConversationUser> = con.conversationUsers.listIterator()
            while (iterator.hasNext()) {
                val index = iterator.nextIndex()
                val user = iterator.next()

                if (user.user.id == AccountManager.getCurrentUserId()) {
                    unRead = con.totalMessage - user.readNumber

                    checkImportantMessage(user)
                }
                if (isGroup) {
                    if (!(user.user.id == con.creatorId && size > 3)) {
                        if (builder.toString().isNotEmpty() && needCreateName) {
                            builder.append(", ")
                        }
                        if (countName > 2 && needCreateName) {
                            needCreateName = false
                            builder.append("…")
                        }
                        if (needCreateName) {
                            countName += 1
                            builder.append(user.user.name)
                        }
                    }
                    if (thumb == null) {
                        if (size == 1) {
                            addAvatar(index, size, user)
                            break
                        }
                        if ((index < MAX_AVATAR) || size <= 4) {
                            addAvatar(index, size, user)
                        }
                    }
                } else {
                    if (userSize > 0 && user.user.id != AccountManager.getCurrentUserId()) {
                        onlyContainMe = false
                        builder.append(user.user.name)
                        addSingleAvatar().loadImage(user.user.avatar)
                        _binding.frameUserOnline.isEnabled = user.user.isOnline()
                    }
                }
            }
            if (onlyContainMe) {
                addSingleAvatar().loadImage(AccountManager.getCurrentNhanVien().avatar)
                builder.append(AccountManager.getCurrentNhanVien().name)
                _binding.frameUserOnline.gone()
            }
            _binding.tvConversationName.text = builder
        }

        val message: Message? = con.lastMessage
        handleContent(message, isGroup, con.isMyCloud)

        with(_binding) {
            if (unRead <= 0) {
                tvConversationName.setFontRegular()
                tvContent.setTextColor(_readColor)
                tvUnread.gone()
                rlConversationItem.setBackgroundColor(_readBackground)
            } else {
                tvUnread.apply {
                    visibility = View.VISIBLE
                    if (unRead > 9) {
                        setText(R.string.text_unread_message_count)
                    } else {
                        text = unRead.toString()
                    }
                }

                tvConversationName.setFontMedium()
                tvContent.setTextColor(_unReadColor)
                rlConversationItem.setBackgroundColor(_unReadBackground)
            }
        }
    }

    private fun handleContent(message: Message?, isGroup: Boolean, isMyCloud: Boolean) {
        StringUtil.handleMessageContent(_context, message, isGroup, isMyCloud).let {
            _binding.tvContent.apply {
                if (it.isNotEmpty()) show() else gone()
                text = it
            }
        }
    }

    private fun checkImportantMessage(user: ConversationUser) {
        _isUserNotify = user.isOffNotify

        with(_binding) {
            imgNoNotify.apply {
                if (user.isOffNotify) show() else gone()
            }
            imgNotify.apply {
                setImageResource(
                    if (!user.isOffNotify
                    ) R.drawable.ic_notification_on_white
                    else R.drawable.ic_notification_off_white
                )
            }
            tvNotify.setText(
                if (user.isOffNotify) R.string.turn_on_notify
                else R.string.turn_off_notify
            )

            imgBookmark.apply {
                if (user.isImportant) show() else gone()
            }
            tvMark.setText(if (user.isImportant) R.string.un_mark else R.string.mark)
        }
    }

    private fun addAvatar(index: Int, size: Int, user: ConversationUser?) {
        RoundedImageView(_context).apply {
            id = View.generateViewId()
            adjustViewBounds = false
            scaleType = ImageView.ScaleType.CENTER_CROP
            layoutParams = ViewGroup.LayoutParams(_imgSize, _imgSize)
            minimumWidth = _imgSizeSingle

            setRadius(_imgSizeSingle)

            if (user == null) {
                if ((size == 1 && index == 1)) {
                    setTextBackground("")
                } else {
                    setTextBackground(size.toString())
                }
            } else {
                loadImage(user.user.avatar)
            }
        }.let {
            val params = RelativeLayout.LayoutParams(_imgSize, _imgSize)

            when (index) {
                0 -> when (size) {
                    1, 2 -> {
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                        params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE)
                    }

                    3 -> {
                        params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE)
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
                    }

                    else -> {
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
                        params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE)
                    }
                }

                1 -> if (size == 3) {
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                    params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE)
                } else {
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
                    params.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE)
                }

                2 -> if (size == 3) {
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                    params.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE)
                } else {
                    params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE)
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                }

                3 -> {
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                    params.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE)
                }
            }
            it.translationZ = index.toFloat()
            _binding.rlAvatarGroup.addView(it, params)
        }
    }

    private fun addSingleAvatar(): RoundedImageView {
        return RoundedImageView(_context).apply {
            id = View.generateViewId()
            adjustViewBounds = false
            scaleType = ImageView.ScaleType.CENTER_CROP
            layoutParams = ViewGroup.LayoutParams(_imgSize, _imgSize)
            minimumWidth = _imgSizeSingle

            setRadius(_imgSizeSingle)
        }.let { image ->
            RelativeLayout.LayoutParams(_imgSizeSingle, _imgSizeSingle)
                .apply { addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE) }
                .let {
                    _binding.rlAvatarGroup.addView(image, it)
                }
            image
        }
    }

    private fun setFontSize() {
        val size = getFontSizeChat(_context)

        with(_binding) {
            tvConversationName.textSize = size
            tvContent.textSize = size
            tvLastActive.textSize = size
        }
    }

    private fun myCloud() {
        with(_binding) {
            tvConversationName.show()
            _binding.frameUserOnline.gone()

            addSingleAvatar().let {
                it.setImageResource(R.drawable.ic_my_cloud)
                tvConversationName.setText(R.string.my_cloud)
            }
        }
    }

    fun clear() {
        for (i in 0 until _binding.rlAvatarGroup.childCount) {
            _binding.rlAvatarGroup.getChildAt(i).let {
                Glide.with(itemView.context).clear(it)
            }
        }
        _binding.rlAvatarGroup.removeAllViews()
    }

    companion object {
        private const val MAX_AVATAR = 3
    }
}