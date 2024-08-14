package boilerplate.ui.contact.viewholder

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import boilerplate.constant.AccountManager
import boilerplate.databinding.ItemContactUserBinding
import boilerplate.model.user.User
import boilerplate.ui.contact.adapter.ContactTabAdapter
import boilerplate.ui.contact.adapter.ContactTabAdapter.Companion.TYPE_BIRTH
import boilerplate.ui.contact.listener.OnContactListener
import boilerplate.utils.extension.click
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.hide
import boilerplate.utils.extension.isVisible
import boilerplate.utils.extension.loadAvatar
import boilerplate.utils.extension.show

class UserHolder(
	private val _binding: ItemContactUserBinding,
	private val _listener: OnContactListener,
	private val _viewType: Int,
	private val _showDescription: Boolean,
	private val _isCheck: Boolean,
	private val _onlyConversation: Boolean,
	private val _allowCheckSelf: Boolean
) : RecyclerView.ViewHolder(_binding.root) {

	private val _padding = _binding.crlSelectedUser.paddingStart

	fun setData(any: Any) {
		check(any is User).apply {
			val isMe = any.id == AccountManager.getCurrentUserId()

			with(_binding) {
				tvName.text = any.name
				if (_isCheck) {
					imgChat.gone()
					imgPhone.gone()
					frameUserOnline.gone()
					if (_viewType == ContactTabAdapter.TYPE_ONLY_NAME) {
						imgAvatar.gone()
					} else {
						imgAvatar.show()
					}

					checkUser.apply {
						isEnabled = if (_onlyConversation) {
							false
						} else {
							any.isEnable
						}
						isChecked = any.isChecked

						if (!isMe || _allowCheckSelf) {
							click { _listener.onUserSelect(any) }
							show()
						} else {
							click(null)
							hide()
						}
					}
				} else {
					checkUser.gone()
					frameUserOnline.apply {
						isEnabled = any.isOnline()
						show()
					}

					if (isMe || any.isForContactEdit || _viewType == TYPE_BIRTH) {
						imgChat.gone()
						imgPhone.gone()
					} else {
						imgChat.show()
						imgPhone.show()

						imgChat.click { _listener.onChatWith(any) }
						imgPhone.click { _listener.onPhone(any.phoneNumber) }
					}

					root.click { _listener.onOpenInform(any) }

					imgClear.apply {
						show(any.isForContactEdit || _viewType == TYPE_BIRTH)
						click { _listener.removeUser(any) }
					}
				}
				if (_showDescription) {
					tvStatus.apply {
						text = any.mood
						show()
					}
					tvCompany.gone()
					tvDepartment.gone()
				} else {
					tvStatus.gone()
					tvDepartment.show()
					tvCompany.show()
				}

				if (!imgClear.isVisible()) {
					val level = getPaddingLevel(any.childLevel, _isCheck)
					crlSelectedUser.setPadding(
						(_padding * level).toInt(),
						crlSelectedUser.paddingTop,
						crlSelectedUser.paddingEnd,
						crlSelectedUser.paddingBottom
					)
				}

				imgAvatar.apply { if (isVisible()) loadAvatar(any.avatar) }
			}
		}
	}
}

private fun getPaddingLevel(level: Int, isCheck: Boolean): Float {
	return (level - 1 + (if (isCheck) 0.85f else 0f)) + level
}