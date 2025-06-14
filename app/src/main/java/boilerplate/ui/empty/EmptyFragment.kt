package boilerplate.ui.empty

import boilerplate.base.BaseFragment
import boilerplate.base.putBundle
import boilerplate.databinding.FragmentEmptyBinding
import boilerplate.ui.home.HomeFragment
import boilerplate.ui.main.MainVM
import boilerplate.utils.extension.click
import boilerplate.utils.extension.open
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class EmptyFragment : BaseFragment<FragmentEmptyBinding, MainVM>() {
	companion object {

		private const val KEY_STRING = "KEY_STRING"

		fun newInstance(s: String = ""): EmptyFragment {
			return EmptyFragment().putBundle(
				KEY_STRING to s
			)
		}
	}

	override val viewModel: MainVM by activityViewModel()

	override fun FragmentEmptyBinding.initialize() {
	}

	override fun onSubscribeObserver() {
	}

	override fun FragmentEmptyBinding.registerEvent() {
		val string = arguments?.getString(KEY_STRING, "asd")
		tvEmpty.text = string

		tvEmpty.click { open(HomeFragment.newInstance()) }
		tvMore.click { open(HomeFragment.newInstance(), split = false) }
	}

	override fun callApi() {
	}
}