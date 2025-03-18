package boilerplate.ui.home.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import boilerplate.base.BaseRcvAdapter
import boilerplate.base.BaseVH
import boilerplate.base.HolderBuilder
import boilerplate.base.build
import boilerplate.databinding.HolderHomeBinding
import boilerplate.utils.extension.viewBinding

object HomeViewType {
	const val TYPE_HOME = 0
}

class HomeAdapter : BaseRcvAdapter<Any>() {
	override fun onBuildHolder(): List<HolderBuilder<Any>> {
		return builder {
			build(HomeViewType.TYPE_HOME, HomeVH::holder, { position, any -> true })
		}
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
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
}