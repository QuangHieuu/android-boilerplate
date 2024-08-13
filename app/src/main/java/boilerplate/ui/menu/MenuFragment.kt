package boilerplate.ui.menu

import androidx.fragment.app.activityViewModels
import boilerplate.base.BaseFragment
import boilerplate.databinding.FragmentMenuBinding
import boilerplate.model.menu.EOfficeMenu
import boilerplate.ui.dashboard.DashboardFragment
import boilerplate.ui.dashboard.DashboardVM
import boilerplate.ui.main.MainVM
import boilerplate.ui.main.tab.HomeTabIndex
import boilerplate.ui.menu.adapter.EOfficeAdapter
import boilerplate.utils.extension.click
import boilerplate.utils.extension.findOwner
import boilerplate.utils.extension.notNull
import org.koin.androidx.viewmodel.ext.android.viewModel

class MenuFragment : BaseFragment<FragmentMenuBinding, MainVM>() {
	companion object {
		fun newInstance(): MenuFragment {
			return MenuFragment()
		}
	}

	override val viewModel: MainVM by activityViewModels()
	private val _childVM by viewModel<DashboardVM>(ownerProducer = {
		findOwner(DashboardFragment::class)
	})

	private lateinit var _adapter: EOfficeAdapter

	override fun initialize() {
		_adapter = EOfficeAdapter(object : EOfficeAdapter.OnMenuListener {
			override fun onChosen(menu: EOfficeMenu) {

			}
		})

		with(binding) {
			rcvMenu.adapter = _adapter
			rcvMenu.itemAnimator = null
		}
	}

	override fun onSubscribeObserver() {
		with(_childVM) {
			updateCount.observe(this@MenuFragment) { data ->
				data.notNull {
					for (result in it) {
						_adapter.updateCount(result)
					}
				}
			}
		}
		with(viewModel) {
			user.observe(this@MenuFragment) {
				with(binding) {
					tvRole.text = currentFullName
				}
			}
		}
	}

	override fun registerEvent() {
		with(binding) {
			btnBackToDashboard.click {
				viewModel.currentSelected.value = HomeTabIndex.POSITION_HOME_DASHBOARD
			}
		}
	}

	override fun callApi() {
		_childVM.getMenuStatical()
	}
}