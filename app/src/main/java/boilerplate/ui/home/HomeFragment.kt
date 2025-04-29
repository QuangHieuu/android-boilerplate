package boilerplate.ui.home

import androidx.core.os.bundleOf
import boilerplate.base.BaseFragment
import boilerplate.databinding.FragmentHomeBinding
import boilerplate.ui.empty.EmptyFragment
import boilerplate.ui.main.MainVM
import boilerplate.utils.extension.click
import boilerplate.utils.extension.open
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class HomeFragment : BaseFragment<FragmentHomeBinding, MainVM>() {

	companion object {
		fun newInstance(): HomeFragment {
			bundleOf()
			return HomeFragment()
		}
	}

	override val viewModel: MainVM by activityViewModel()

	override fun initialize() {
	}

	override fun onSubscribeObserver() {
	}

	override fun registerEvent() {
	}

	override fun callApi() {

	}
}