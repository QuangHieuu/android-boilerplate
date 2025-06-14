package boilerplate.ui.home.adapter

import android.empty.base.*
import android.empty.refreshrecyclerview.databinding.HolderEmptyBinding
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import boilerplate.model.User

object DetailViewType {

	const val TYPE_STRING: Int = 0
	const val TYPE_USER: Int = 1
}

class DetailAdapter : BaseRcvAdapter<Any>() {

	override fun onCreateViewHolder(): List<HolderBuilder> {
		return builder {
			holder<EmptyVH>(DetailViewType.TYPE_STRING, ::conTypeString)
			holder<UserVH>(DetailViewType.TYPE_USER, ::conTypeUser)
		}
	}

	override fun onBindHolder(holder: RecyclerView.ViewHolder, position: Int) {
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

class UserVH(parent: ViewGroup) : BaseVH<HolderEmptyBinding>(parent, HolderEmptyBinding::inflate) {

	fun onBind(user: User) {
		binding.tvTitle.text = user.name
	}
}