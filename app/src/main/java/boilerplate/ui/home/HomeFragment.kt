package boilerplate.ui.home

import boilerplate.base.BaseFragment
import boilerplate.databinding.FragmentHomeBinding
import boilerplate.model.User
import boilerplate.ui.home.adapter.DetailAdapter
import boilerplate.ui.main.MainVM
import boilerplate.utils.extension.click
import calendar.widget.table.CalenderListener
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.util.Date

class HomeFragment : BaseFragment<FragmentHomeBinding, MainVM>() {

	companion object {

		fun newInstance(): HomeFragment {
			return HomeFragment()
		}
	}

	private val detailAdapter = DetailAdapter()

	override val viewModel: MainVM by activityViewModel()

	override fun FragmentHomeBinding.initialize() {

		rcv.adapter = detailAdapter

	}

	override fun onSubscribeObserver() {
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

	override fun FragmentHomeBinding.registerEvent() {
		calendar.addListener(object : CalenderListener() {
			override fun onChange(date: Date) {
				text.text = date.toString()
			}

			override fun onPick(date: Date) {
			}

			override fun onPickFromTo(from: Date?, to: Date?) {
			}
		})
		btnNext.click {
			calendar.nextMonth()
		}
		btnPrevious.click {
			calendar.previousMonth()
		}
		btnReset.click {
			calendar.moveToToday()
		}
	}

	override fun callApi() {

	}
}