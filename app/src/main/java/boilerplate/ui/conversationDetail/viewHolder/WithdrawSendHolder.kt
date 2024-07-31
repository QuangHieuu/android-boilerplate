package boilerplate.ui.conversationDetail.viewHolder

import androidx.core.content.ContextCompat
import boilerplate.R
import boilerplate.databinding.ItemMessageSendBinding
import boilerplate.model.message.Message
import boilerplate.ui.conversationDetail.adpater.SimpleMessageEvent
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.show


class WithdrawSendHolder(
    private val _binding: ItemMessageSendBinding,
    private val _viewType: Int,
    private val _listener: SimpleMessageEvent
) : SendHolder(_binding, _listener, _viewType, true, false) {

    override fun setData(message: Message, isAnswerable: Boolean) {
        _swipeLayout.isSwipeEnabled = isAnswerable
        runAnimationFocused(message, R.drawable.bg_message_send)

        _tvContent.apply {
            show()
            setText(R.string.message_is_already_withdraw)
            setTextColor(ContextCompat.getColor(_binding.root.context, R.color.color_081C36))
            setAlpha(0.64f)
        }
        _lnForward.gone()
        _tvTime.gone()
        _lnFile.gone()
        _lnReaction.gone()

        _lnMessage.setOnLongClickListener {
            _listener.longClick(message, it, _viewType)
            false
        }
    }
}
