package boilerplate.ui.menu

import androidx.fragment.app.activityViewModels
import boilerplate.base.BaseFragment
import boilerplate.databinding.FragmentMenuBinding
import boilerplate.ui.main.MainVM
import boilerplate.ui.main.tab.HomeTabIndex
import boilerplate.utils.extension.click

class MenuFragment : BaseFragment<FragmentMenuBinding, MainVM>() {
    companion object {
        fun newInstance(): MenuFragment {
            return MenuFragment()
        }
    }

    override val _viewModel: MainVM by activityViewModels()

    override fun initialize() {
    }

    override fun onSubscribeObserver() {
    }

    override fun registerEvent() {
        with(binding) {
            btnBackToDashboard.click {
                _viewModel.currentSelected.value = HomeTabIndex.POSITION_HOME_DASHBOARD
            }
        }
    }

    override fun callApi() {
    }
}