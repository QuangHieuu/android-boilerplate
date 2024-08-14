package boilerplate.ui.contact.viewholder

import androidx.recyclerview.widget.RecyclerView
import boilerplate.R
import boilerplate.constant.AccountManager
import boilerplate.databinding.ItemContactAvatarBinding
import boilerplate.model.conversation.Conversation
import boilerplate.model.user.User
import boilerplate.ui.contact.adapter.ContactTabAdapter
import boilerplate.utils.extension.loadAvatar
import com.bumptech.glide.Glide
import java.util.Locale

class AvatarHolder(
	val _binding: ItemContactAvatarBinding
) : RecyclerView.ViewHolder(_binding.root) {

	fun setData(any: Any, size: Int) {
		with(_binding) {
			Glide.with(root).clear(imgAvatar)

			if (bindingAdapterPosition == ContactTabAdapter.LIMIT_AVATAR_POSITION) {
				imgAvatar.setTextImage(
					String.format(
						Locale.getDefault(),
						"+%d",
						size - ContactTabAdapter.LIMIT_AVATAR_POSITION
					)
				)
			} else {
				if (any is User) {
					imgAvatar.loadAvatar(any.avatar)
					return@with
				}
				if (any is Conversation) {
					if (any.isGroup) {
						if (any.thumb.isNotEmpty()) {
							imgAvatar.loadAvatar(any.thumb)
						} else {
							var name: kotlin.String = any.name
							if (name.isEmpty()) {
								name = any.members[0].user.name
							}
							imgAvatar.setTextBackground(name[0].toString())
						}
					} else {
						if (any.members.isNotEmpty()) {
							val user =
								any.members.findLast { it.user.id != AccountManager.getCurrentUserId() }

							imgAvatar.loadAvatar(user?.user?.avatar)
						} else {
							imgAvatar.setImageResource(if (any.isMyCloud) R.drawable.ic_my_cloud else R.drawable.ic_avatar_group)
						}
					}
					return@with
				}
			}
		}
	}
}