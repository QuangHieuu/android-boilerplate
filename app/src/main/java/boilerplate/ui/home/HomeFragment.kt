package boilerplate.ui.home

import androidx.recyclerview.widget.PagerSnapHelper
import boilerplate.R
import boilerplate.base.BaseFragment
import boilerplate.databinding.FragmentHomeBinding
import boilerplate.ui.home.adapter.HomeAdapter
import boilerplate.ui.main.MainVM
import boilerplate.widget.recyclerview.CircleEffect
import boilerplate.widget.recyclerview.IndicatorBuilder
import boilerplate.widget.recyclerview.IndicatorType
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class HomeFragment : BaseFragment<FragmentHomeBinding, MainVM>() {

	companion object {
		fun newInstance(): HomeFragment {
			return HomeFragment()
		}
	}

	override val viewModel: MainVM by activityViewModel()

	override fun initialize() {
		IndicatorBuilder(binding.rcv.apply { adapter = HomeAdapter() })
			.indicatorWidth(resources.getDimension(R.dimen.dp_12))
			.indicatorCircleEffect(CircleEffect.SMALL)
			.indicatorType(IndicatorType.CIRCLE)
			.paddingBottom(resources.getDimension(R.dimen.dp_10))
			.snapHelper(PagerSnapHelper())
			.build()
	}

	override fun onSubscribeObserver() {
	}

	override fun registerEvent() {
	}

	override fun callApi() {
	}
}