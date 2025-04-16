package boilerplate.ui.empty

import boilerplate.base.BaseFragment
import boilerplate.base.putBundle
import boilerplate.databinding.FragmentEmptyBinding
import boilerplate.ui.main.MainVM
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

	override fun initialize() {
	}

	override fun onSubscribeObserver() {
	}

	override fun registerEvent() {
		with(binding) {
			val string = arguments?.getString(KEY_STRING, "asd")
			tvEmpty.text = string
		}
	}

	override fun callApi() {
	}
}