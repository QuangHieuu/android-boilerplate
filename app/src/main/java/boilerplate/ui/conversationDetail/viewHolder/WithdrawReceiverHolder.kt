package boilerplate.ui.conversationDetail.viewHolder

import android.view.View
import androidx.core.content.ContextCompat
import boilerplate.R
import boilerplate.databinding.ItemMessageReceiveBinding
import boilerplate.model.message.Message
import boilerplate.ui.conversationDetail.adpater.SimpleMessageEvent
import boilerplate.utils.extension.click
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.loadImage
import boilerplate.utils.extension.show

class WithdrawReceiverHolder(
    private val _binding: ItemMessageReceiveBinding,
    private val _listener: SimpleMessageEvent,
    private val _viewType: Int,
) : ReceiverHolder(_binding, _listener, _viewType, true) {
    override fun setData(message: Message, isAnswerable: Boolean) {
        _swipeLayout.isSwipeEnabled = isAnswerable
        runAnimationFocused(message, R.drawable.bg_message_receiver)
        changeSize()
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

        val role = String.format(
            "%s - %s",
            message.personSend.mainDepartment?.shortName,
            message.personSend.mainCompany?.shortName
        )
        with(_binding) {
            tvRole.text = role
            tvUserSend.text = message.personSend.name
            imgOnline.setVisibility(if (message.personSend.isOnline()) View.VISIBLE else View.GONE)

            imgAvatar.apply {
                loadImage(message.personSend.avatar)
                click { _listener.openUser(message.personSend) }
            }

            _lnMessage.setOnLongClickListener {
                _listener.longClick(message, it, _viewType)
                false
            }
        }
    }
}
