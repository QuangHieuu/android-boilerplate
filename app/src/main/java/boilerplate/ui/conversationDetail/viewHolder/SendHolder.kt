package boilerplate.ui.conversationDetail.viewHolder

import boilerplate.R
import boilerplate.databinding.ItemMessageSendBinding
import boilerplate.model.message.Message
import boilerplate.ui.conversationDetail.adpater.SimpleMessageEvent
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.hide
import boilerplate.utils.extension.show

open class SendHolder(
    private val _binding: ItemMessageSendBinding,
    private val _listener: SimpleMessageEvent,
    private val _viewType: Int,
    private val _disable: Boolean,
    private val _group: Boolean
) : MessageHolder(_binding.root, _listener, _viewType, _disable) {

    override fun setData(message: Message, isAnswerable: Boolean) {
        val isFirst = bindingAdapterPosition == 0 && message.isShow

        with(_binding) {
            if (message.isHide) {
                lnMessage.hide()
                lnStatus.gone()

            } else {
                lnMessage.show()
                if (message.status == 2) {
                    lnStatus.show()
                    tvMessageStatus.setText(R.string.pending)
                    imgStatus.setImageResource(R.drawable.ic_send_message_failed)
                } else {
                    if (_group) {
                        lnStatus.gone()
                    } else {
                        if (isFirst) {
                            lnStatus.show()
                            imgStatus.setImageResource(if (message.status == 0) R.drawable.ic_double_check else R.drawable.ic_eye_grey)
                            tvMessageStatus.setText(if (message.status == 0) R.string.sent else R.string.seen)
                        } else {
                            lnStatus.gone()
                        }
                    }
                }

                super.setData(message, isAnswerable)
                runAnimationFocused(message, R.drawable.bg_message_send)
            }
        }
    }
}
