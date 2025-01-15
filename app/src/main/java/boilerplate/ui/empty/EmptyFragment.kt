package boilerplate.ui.empty

import boilerplate.base.BaseFragment
import boilerplate.databinding.ActivityEmptyBinding
import boilerplate.ui.main.MainVM
import boilerplate.widget.toolbar.backPress
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class EmptyFragment : BaseFragment<ActivityEmptyBinding, MainVM>() {
	companion object {
		fun newInstance(): EmptyFragment {
			return EmptyFragment()
		}
	}

	override val viewModel: MainVM by activityViewModel()

	override fun initialize() {
		binding.toolbar.backPress { }
	}

	override fun onSubscribeObserver() {
	}

	override fun registerEvent() {
		with(binding) {
		}
	}

	override fun callApi() {
	}
}