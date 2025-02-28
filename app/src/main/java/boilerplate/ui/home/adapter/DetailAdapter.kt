package boilerplate.ui.home.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import boilerplate.base.*
import boilerplate.databinding.HolderStringBinding
import boilerplate.model.User
import boilerplate.utils.extension.viewBinding

object DetailViewType {
	const val TYPE_STRING: Int = 0
	const val TYPE_USER: Int = 1
}

class DetailAdapter : BaseRcvAdapter<Any>() {
	override fun onBuildHolder(): List<HolderBuilder<Any>> {
		return builder {
			build(DetailViewType.TYPE_STRING, EmptyVH::holder, ::conTypeString)
			build(DetailViewType.TYPE_USER, UserVH::holder, ::conTypeUser)
		}
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		when (holder) {
			is UserVH -> holder.onBind(dataList[position] as User)
		}
	}

	override fun hasPageLoading(): Boolean = true

	private fun conTypeString(position: Int, item: Any?): Boolean {
		return item is String
	}

	private fun conTypeUser(position: Int, item: Any?): Boolean {
		return item is User
	}

}

class UserVH(parent: ViewGroup) :
	BaseVH<HolderStringBinding>(parent.viewBinding(HolderStringBinding::inflate)) {
	companion object {
		fun holder(parent: ViewGroup): UserVH {
			return UserVH(parent)
		}
	}

	fun onBind(user: User) {
		binding.tvTitle.text = user.name
	}
}