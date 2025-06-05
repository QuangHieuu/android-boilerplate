package boilerplate.ui.home

import boilerplate.base.BaseFragment
import boilerplate.databinding.FragmentHomeBinding
import boilerplate.ui.main.MainVM
import calendar.widget.wheel.SimpleWheelListener
import calendar.widget.wheel.base.IWheelListener
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class HomeFragment : BaseFragment<FragmentHomeBinding, MainVM>() {

	companion object {

		fun newInstance(): HomeFragment {
			return HomeFragment()
		}
	}

	override val viewModel: MainVM by activityViewModel()

	override fun initialize() {
//		binding.pickerTime.setCurrent("16:00")
	}

	override fun onSubscribeObserver() {
	}

	override fun FragmentHomeBinding.registerEvent() {
		pickerDay.addListener(object : SimpleWheelListener {
			override fun onPickDay(value: String) {
				tvDay.text = value
			}
		})

		pickerTime.addListener(object : SimpleWheelListener {
			override fun onPickDay(value: String) {
				tvTime.text = value
			}
		})
		picker.addListener(object : IWheelListener {
			override fun onScroll(value: Int, display: String) {
			}
		})
	}

	override fun callApi() {

	}
}