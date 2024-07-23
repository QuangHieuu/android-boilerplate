package boilerplate.ui.conversationDetail.viewHolder

import boilerplate.R
import boilerplate.databinding.ItemMessageReceiveBinding
import boilerplate.model.message.Message
import boilerplate.ui.conversationDetail.adpater.SimpleMessageEvent
import boilerplate.utils.extension.click
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.hide
import boilerplate.utils.extension.loadImage
import boilerplate.utils.extension.show

open class ReceiverHolder(
    private val _binding: ItemMessageReceiveBinding,
    private val _listener: SimpleMessageEvent,
    private val _viewType: Int,
    private val _disable: Boolean,
) : MessageHolder(_binding.root, _listener, _viewType, _disable) {

    override fun setData(message: Message, isAnswerable: Boolean) {
        with(_binding) {
            if (message.isHide) {
                lnMessage.hide()
                imgAvatar.hide()
                tvUserSend.hide()
                imgOnline.hide()
            } else {
                lnMessage.show()
                imgAvatar.show()
                tvUserSend.show()
                super.setData(message, isAnswerable)

                runAnimationFocused(message, R.drawable.bg_message_receiver)

                val role = String.format(
                    "%s - %s",
                    message.personSend.mainDepartment?.shortName,
                    message.personSend.mainCompany?.shortName
                )
                tvRole.text = role
                tvUserSend.text = message.personSend.name
                imgAvatar.loadImage(message.personSend.avatar)

                imgOnline.apply { if (message.personSend.isOnline()) show() else gone() }

                imgAvatar.click { _listener.openUser(message.personSend) }
                changeSize()
            }
        }
    }

    open fun changeSize() {
        with(_binding) {
            tvRole.textSize = _mainSize - 1
            tvUserSend.textSize = _mainSize - 3
        }
    }
}