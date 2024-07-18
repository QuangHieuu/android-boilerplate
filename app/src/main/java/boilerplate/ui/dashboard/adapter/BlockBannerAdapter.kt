package boilerplate.ui.dashboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import boilerplate.databinding.ItemDashboardBannerBinding
import boilerplate.model.dashboard.Banner
import boilerplate.ui.dashboard.DashboardFragment
import boilerplate.ui.dashboard.adapter.BlockBannerAdapter.BannerViewHolder
import com.smarteist.autoimageslider.SliderView

class BlockBannerAdapter(private val _listener: DashboardFragment.OnSliderImageListener) :
    RecyclerView.Adapter<BannerViewHolder>() {
    private val _list: MutableList<Banner> = ArrayList()

    fun setBanners(list: List<Banner>) {
        if (_list.isNotEmpty()) {
            _list.clear()
        }
        _list.addAll(list)
        notifyItemChanged(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return BannerViewHolder(
            ItemDashboardBannerBinding.inflate(layoutInflater, parent, false),
            _listener
        )
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.setSlide(_list)
    }

    override fun getItemCount(): Int {
        return 1
    }

    class BannerViewHolder(
        _binding: ItemDashboardBannerBinding,
        _listener: DashboardFragment.OnSliderImageListener
    ) : RecyclerView.ViewHolder(_binding.root) {
        private var _sliderAdapter: SlideAdapter = SlideAdapter(_listener)

        init {
            with(_binding.sliderView) {
                autoCycleDirection = SliderView.AUTO_CYCLE_DIRECTION_RIGHT
                scrollTimeInSec = 3
                setSliderAdapter(_sliderAdapter, true)
                startAutoCycle()
            }
        }

        fun setSlide(list: List<Banner>) {
            _sliderAdapter.setImageList(list)
        }
    }
}
