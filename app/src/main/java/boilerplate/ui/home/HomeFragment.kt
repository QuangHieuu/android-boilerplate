package boilerplate.ui.home

import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.PagerSnapHelper
import boilerplate.R
import boilerplate.base.BaseFragment
import boilerplate.databinding.FragmentHomeBinding
import boilerplate.model.User
import boilerplate.ui.home.adapter.DetailAdapter
import boilerplate.ui.home.adapter.HomeAdapter
import boilerplate.ui.main.MainVM
import boilerplate.widget.recyclerview.CircleEffect
import boilerplate.widget.recyclerview.IndicatorBuilder
import boilerplate.widget.recyclerview.IndicatorType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class HomeFragment : BaseFragment<FragmentHomeBinding, MainVM>() {

	companion object {
		fun newInstance(): HomeFragment {
			bundleOf()
			return HomeFragment()
		}
	}

	private val detailAdapter = DetailAdapter()

	override val viewModel: MainVM by activityViewModel()

	override fun initialize() {
		IndicatorBuilder(binding.rcv.apply { adapter = HomeAdapter() })
			.indicatorWidth(resources.getDimension(R.dimen.dp_12))
			.indicatorCircleEffect(CircleEffect.SMALL)
			.indicatorType(IndicatorType.CIRCLE)
			.paddingBottom(resources.getDimension(R.dimen.dp_10))
			.snapHelper(PagerSnapHelper())
			.build()

		binding.rcvLoading.adapter = detailAdapter
		detailAdapter.submitData(
			arrayListOf(
				User(name = "1"),
				User(name = "2"),
				User(name = "3"),
				User(name = "4"),
			)
		)
		detailAdapter.showLoading()
	}

	override fun onSubscribeObserver() {
	}

	override fun registerEvent() {
	}

	override fun callApi() {
		lifecycleScope.launch {
			delay(2000)
			detailAdapter.hideLoading()
			detailAdapter.submitData(
				arrayListOf(
					User(name = "1"),
					User(name = "2"),
					User(name = "3"),
					User(name = "4"),
					User(name = "5"),
					User(name = "6"),
					User(name = "7"),
				)
			)
		}
	}
}