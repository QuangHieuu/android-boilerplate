package boilerplate.ui.dashboard.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import boilerplate.databinding.ItemDashboardBannerImageBinding
import boilerplate.model.dashboard.Banner
import boilerplate.ui.dashboard.DashboardFragment
import boilerplate.ui.dashboard.adapter.SlideAdapter.SliderAdapterVH
import boilerplate.utils.extension.click
import boilerplate.utils.extension.notNull
import com.bumptech.glide.Glide
import com.smarteist.autoimageslider.SliderViewAdapter

class SlideAdapter(private val _listener: DashboardFragment.OnSliderImageListener) :
	SliderViewAdapter<SliderAdapterVH>() {
	private val imageList: MutableList<Banner> = ArrayList()

	fun setImageList(list: List<Banner>) {
		imageList.clear()
		imageList.addAll(list)
		notifyDataSetChanged()
	}

	override fun onCreateViewHolder(parent: ViewGroup): SliderAdapterVH {
		val layoutInflater = LayoutInflater.from(parent.context)
		return SliderAdapterVH(
			ItemDashboardBannerImageBinding.inflate(layoutInflater, parent, false),
			_listener
		)
	}

	override fun onBindViewHolder(viewHolder: SliderAdapterVH, position: Int) {
		viewHolder.setData(imageList[position])
	}

	override fun getCount(): Int {
		return imageList.size
	}

	class SliderAdapterVH(
		private val _binding: ItemDashboardBannerImageBinding,
		private val _listener: DashboardFragment.OnSliderImageListener,
		private val _context: Context = _binding.root.context
	) : ViewHolder(_binding.root) {

		fun setData(banner: Banner) {
			Glide.with(_context)
				.load(banner.file?.url)
				.into(_binding.imageSlider)

			_binding.imageSlider.click {
				banner.url.notNull { _listener.onClick(it) }
			}
		}
	}
}
