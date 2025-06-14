package boilerplate.ui.home

import boilerplate.base.BaseFragment
import boilerplate.databinding.FragmentHomeBinding
import boilerplate.ui.home.adapter.DetailAdapter
import boilerplate.ui.main.MainVM
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class HomeFragment : BaseFragment<FragmentHomeBinding, MainVM>() {

	companion object {

		fun newInstance(): HomeFragment {
			return HomeFragment()
		}
	}

	private val detailAdapter = DetailAdapter()

	override val viewModel: MainVM by activityViewModel()

	override fun FragmentHomeBinding.initialize() {

	}

	override fun onSubscribeObserver() {

	}

	override fun FragmentHomeBinding.registerEvent() {

	}

	override fun callApi() {

	}
}