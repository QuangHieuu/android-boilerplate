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
import boilerplate.widget.recyclerview.decoration.CircleEffect
import boilerplate.widget.recyclerview.decoration.IndicatorBuilder
import boilerplate.widget.recyclerview.decoration.IndicatorType
import boilerplate.widget.recyclerview.edgeEffect.BouncyEdgeEffectFactory
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

		binding.arcProgressBar.setPercent(40, animation = true)
		binding.progressBar.setPercent(50, animation = true)

		val homeAdapter = HomeAdapter()

		binding.rcv.edgeEffectFactory = BouncyEdgeEffectFactory()

		IndicatorBuilder(binding.rcv, homeAdapter)
			.indicatorWidth(resources.getDimension(R.dimen.dp_12))
			.indicatorCircleEffect(CircleEffect.RECT)
			.indicatorType(IndicatorType.CIRCLE)
			.snapHelper(PagerSnapHelper())
			.isUnderView()
			.build()

		homeAdapter.submitData(
			arrayListOf(
				User(name = "1"),
				User(name = "2"),
				User(name = "3"),
				User(name = "4"),
				User(name = "5"),
			)
		)

		binding.rcvLoading.adapter = detailAdapter
		detailAdapter.submitData(
			arrayListOf(
				User(name = "1"),
				User(name = "2"),
				User(name = "3"),
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