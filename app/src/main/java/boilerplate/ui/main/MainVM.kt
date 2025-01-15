package boilerplate.ui.main

import androidx.lifecycle.MutableLiveData
import boilerplate.base.BaseViewModel
import boilerplate.data.local.repository.user.TokenRepository
import boilerplate.ui.main.tab.HomeTabIndex

class MainVM(
	private val tokenRepo: TokenRepository
) : BaseViewModel() {

	private val _logout by lazy { MutableLiveData<Boolean>() }
	val logout = _logout

	private val _tabPosition by lazy { MutableLiveData<ArrayList<String>>() }
	val tabPosition = _tabPosition

	private val _currentTabSelected by lazy { MutableLiveData(HomeTabIndex.POSITION_HOME_DASHBOARD) }
	val currentSelected = _currentTabSelected
}