package boilerplate.ui.empty

import androidx.core.os.bundleOf
import boilerplate.base.BaseFragment
import boilerplate.databinding.FragmentEmptyBinding
import boilerplate.ui.main.MainVM
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class EmptyFragment : BaseFragment<FragmentEmptyBinding, MainVM>() {
	companion object {
		private const val KEY_STRING = "KEY_STRING"

		fun newInstance(s: String = ""): EmptyFragment {
			return EmptyFragment().apply {
				arguments = bundleOf(
					KEY_STRING to s
				)
			}
		}
	}

	override val viewModel: MainVM by activityViewModel()

	override fun initialize() {
	}

	override fun onSubscribeObserver() {
	}

	override fun registerEvent() {
		val bundle = arguments ?: bundleOf()
		with(binding) {
			tvEmpty.text = bundle.getString(KEY_STRING, "")
		}
	}

	override fun callApi() {
	}
}