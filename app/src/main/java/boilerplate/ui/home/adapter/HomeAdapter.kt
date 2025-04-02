package boilerplate.ui.home.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import boilerplate.base.BaseRcvAdapter
import boilerplate.base.BaseVH
import boilerplate.base.HolderBuilder
import boilerplate.base.build
import boilerplate.databinding.HolderHomeBinding
import boilerplate.model.User
import boilerplate.utils.extension.viewBinding

object HomeViewType {
	const val TYPE_HOME = 0
}

class HomeAdapter : BaseRcvAdapter<Any>() {
	override fun onBuildHolder(): List<HolderBuilder<Any>> {
		return builder {
			build(HomeViewType.TYPE_HOME, HomeVH::holder, { _, _ -> true })
		}
	}

	override fun onBindHolder(holder: RecyclerView.ViewHolder, position: Int) {
		when (holder) {
			is HomeVH -> holder.onBind(dataList[position] as User)
		}
	}
}

class HomeVH(
	parent: ViewGroup
) : BaseVH<HolderHomeBinding>(parent.viewBinding(HolderHomeBinding::inflate)) {

	companion object {
		fun holder(parent: ViewGroup): HomeVH {
			return HomeVH(parent)
		}
	}

	fun onBind(user: User) {
		binding.tvTitle.text = user.name
	}
}