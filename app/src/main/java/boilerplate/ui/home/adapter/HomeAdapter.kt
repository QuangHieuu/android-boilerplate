package boilerplate.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import boilerplate.databinding.ItemHomeAdapterBinding

class HomeAdapter : RecyclerView.Adapter<HomeVH>() {
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeVH {
		return HomeVH(
			ItemHomeAdapterBinding
				.inflate(LayoutInflater.from(parent.context), parent, false)
		)
	}

	override fun getItemCount(): Int = 5

	override fun onBindViewHolder(holder: HomeVH, position: Int) {
	}
}

class HomeVH(binding: ItemHomeAdapterBinding) : RecyclerView.ViewHolder(binding.root) {

}