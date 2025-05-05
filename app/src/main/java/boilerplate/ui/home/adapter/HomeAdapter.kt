package boilerplate.ui.home.adapter

import android.empty.base.BaseRcvAdapter
import android.empty.base.BaseVH
import android.empty.base.HolderBuilder
import android.empty.base.build
import android.empty.base.viewBinding
import shape.widget.chart.model.Entry
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import boilerplate.databinding.HolderHomeBinding

object HomeViewType {
	const val TYPE_HOME = 0
}

class HomeAdapter : BaseRcvAdapter<Entry>() {
	override fun onBuildHolder(): List<HolderBuilder<Entry>> {
		return builder {
			build(HomeViewType.TYPE_HOME, HomeVH::holder) { _, _ -> true }
		}
	}

	override fun onBindHolder(holder: RecyclerView.ViewHolder, position: Int) {
		when (holder) {
			is HomeVH -> holder.onBind(dataList[position] as Entry)
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

	fun onBind(user: Entry) {
		binding.tvTitle.text = user.y.toString()
	}
}