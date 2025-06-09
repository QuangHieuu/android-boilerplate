package boilerplate.ui.home.adapter

import android.empty.base.BaseRcvAdapter
import android.empty.base.BaseVH
import android.empty.base.HolderBuilder
import android.empty.base.holder
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import boilerplate.databinding.HolderHomeBinding
import boilerplate.ui.home.adapter.DetailViewType.TYPE_USER
import shape.widget.chart.model.Entry

object HomeViewType {

	const val TYPE_HOME = 0
}

class HomeAdapter : BaseRcvAdapter<Entry>() {

	override fun onCreateViewHolder(): List<HolderBuilder> {
		return builder {
			holder<HomeVH>(TYPE_USER) { _, _ -> true }
		}
	}

	override fun onBindHolder(holder: RecyclerView.ViewHolder, position: Int) {
		when (holder) {
			is HomeVH -> holder.onBind(dataList[position] as Entry)
		}
	}
}

class HomeVH(parent: ViewGroup) : BaseVH<HolderHomeBinding>(parent, HolderHomeBinding::inflate) {

	fun onBind(user: Entry) {
		binding.tvTitle.text = user.y.toString()
	}
}