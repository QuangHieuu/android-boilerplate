package boilerplate.ui.main

import android.util.Log
import androidx.activity.OnBackPressedCallback
import boilerplate.base.BaseFragment
import boilerplate.databinding.FragmentSplitBinding
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class SplitFragment : BaseFragment<FragmentSplitBinding, MainVM>() {

	companion object {

		fun newInstance(): SplitFragment {
			return SplitFragment()
		}
	}

	private val _backDispatch by lazy {
		object : OnBackPressedCallback(true) {
			override fun handleOnBackPressed() {
				backPressed()
			}
		}
	}

	override val viewModel: MainVM by activityViewModel<MainVM>()

	override fun FragmentSplitBinding.initialize() {

		requireActivity().onBackPressedDispatcher.addCallback(this@SplitFragment, _backDispatch)

	}

	override fun onSubscribeObserver() {
	}

	override fun FragmentSplitBinding.registerEvent() {
	}

	override fun callApi() {
	}

	private fun backPressed() {
		val stackParent = parentFragmentManager.backStackEntryCount
		val stackChild = childFragmentManager.backStackEntryCount

		
		Log.d("SSS", "stackParent: " + stackParent)
		Log.d("SSS", "stackChild: " + stackChild)

	}
}