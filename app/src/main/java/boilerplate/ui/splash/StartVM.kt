package boilerplate.ui.splash

import boilerplate.base.BaseViewModel

class StartVM() : BaseViewModel() {

	companion object {
		const val STATE_LOGIN = 0
		const val STATE_CHECK_LOGIN = 1
		const val STATE_AUTO_LOGIN = 2
	}

}