package boilerplate.ui.contact.viewholder

import androidx.recyclerview.widget.RecyclerView
import boilerplate.constant.AccountManager
import boilerplate.databinding.ItemContactSearchUserBinding
import boilerplate.model.user.User
import boilerplate.ui.contact.listener.SimpleListener
import boilerplate.utils.extension.click
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.hide
import boilerplate.utils.extension.loadAvatar
import boilerplate.utils.extension.performClick
import boilerplate.utils.extension.show

class SearchHolder(
	private val binding: ItemContactSearchUserBinding,
	private val _isCheck: Boolean,
	private val _onlyConversation: Boolean,
	private val _listener: SimpleListener,
) : RecyclerView.ViewHolder(binding.root) {
	fun setData(ob: User) {
		val isSelf = ob.id == AccountManager.getCurrentUserId()

		with(binding) {
			viewOnline.apply {
				show()
				isEnabled = ob.isOnline()
			}

			imgAvatar.loadAvatar(ob.avatar)

			ob.phoneNumber.let {
				tvPhoneCall.apply {
					show(it.isNotEmpty())
					text = it
				}
			}
			ob.diffPhoneNumber.let {
				tvPhoneCallOther.apply {
					show(it.isNotEmpty())
					text = it
				}
			}
			tvName.text = ob.name
			tvDepartment.text = ob.mainDepartment.name
			tvCompany.text = ob.mainCompany.name

			if (_isCheck) {
				imgChat.gone()
				imgPhone.gone()

				checkUser.apply {
					if (_onlyConversation) {
						isEnabled = false
					} else {
						isEnabled = ob.isEnable
					}
					isChecked = ob.isChecked
					if (isSelf) {
						hide()
						click(null)
					} else {
						show()
						click { _listener.onUserSelect(ob) }
					}
				}

			} else {
				imgChat.show(!isSelf)
				imgPhone.show(!isSelf)
			}

			tvName.click { _listener.onOpenInform(ob) }
			imgAvatar.performClick(tvName)
			tvDepartment.performClick(tvName)
			tvCompany.performClick(tvName)

			imgChat.click { _listener.onPhone(ob.phoneNumber) }
			imgChat.click { _listener.onChatWith(ob) }
		}
	}
}