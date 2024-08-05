package boilerplate.ui.contact.viewholder

import androidx.recyclerview.widget.RecyclerView
import boilerplate.constant.AccountManager
import boilerplate.databinding.ItemContactUserBinding
import boilerplate.model.user.User
import boilerplate.ui.contact.listener.OnContactListener
import boilerplate.utils.extension.click
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.isVisible
import boilerplate.utils.extension.loadAvatar
import boilerplate.utils.extension.show

class UserHolder(
    private val _binding: ItemContactUserBinding,
    private val _listener: OnContactListener,
    private val _isContact: Boolean = true,
    private val _isCheck: Boolean = false
) : RecyclerView.ViewHolder(_binding.root) {

    private val _padding = _binding.crlSelectedUser.getPaddingStart()

    fun setData(any: Any) {
        check(any is User).apply {
            with(_binding) {
                tvName.text = any.name
                imgAvatar.loadAvatar(any.avatar)
                frameUserOnline.apply {
                    show()
                    isEnabled = any.isOnline()
                }

                if (_isContact) {
                    tvStatus.show()
                    tvStatus.text = any.mood
                }

                if (!imgClear.isVisible()) {
                    val level = getPaddingLevel(any.contactLevel, _isCheck)
                    crlSelectedUser.setPadding(
                        (_padding * level).toInt(),
                        crlSelectedUser.paddingTop,
                        crlSelectedUser.getPaddingEnd(),
                        crlSelectedUser.paddingBottom
                    )
                }

                if (any.id.equals(AccountManager.getCurrentUserId())) {
                    imgChat.gone()
                    imgPhone.gone()
                } else {
                    imgChat.show()
                    imgPhone.show()
                }

                imgChat.click { _listener.onChatWith(any) }
                imgPhone.click { _listener.onPhone(any.phoneNumber.orEmpty()) }

                root.click {
                    if (!any.isForContactEdit) {
                        _listener.onOpenInform(any)
                    }
                }
            }
        }
    }
}

private fun getPaddingLevel(level: Int, isCheck: Boolean): Float {
    return (level - 1 + (if (isCheck) 0.85f else 0f)) + level
}