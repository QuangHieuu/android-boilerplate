package boilerplate.ui.splash

import android.os.Bundle
import android.view.View
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import boilerplate.R
import boilerplate.base.BaseActivity
import boilerplate.databinding.ActivityStartBinding
import boilerplate.ui.main.MainActivity
import boilerplate.utils.extension.AnimateType
import boilerplate.utils.extension.click
import boilerplate.utils.extension.goTo
import boilerplate.utils.extension.notNull
import boilerplate.utils.extension.popFragment
import boilerplate.utils.extension.replaceFragmentInActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class StartActivity : BaseActivity<ActivityStartBinding, StartVM>() {
	override val _viewModel: StartVM by viewModel()

	private lateinit var splashScreen: SplashScreen


	override fun onCreate(savedInstanceState: Bundle?) {
		splashScreen = installSplashScreen().apply {
			setKeepOnScreenCondition { true }
		}
		super.onCreate(savedInstanceState)
	}

	override fun initialize() {

	}

	override fun onSubscribeObserver() {
		with(_viewModel) {
			state.observe(this@StartActivity) {
				when (it) {
					StartVM.STATE_CHECK_LOGIN -> {
						_viewModel.getMe()
					}

					StartVM.STATE_LOGIN -> {
						with(binding.viewAutoLogin) {
							frameAutoLogin.visibility = View.GONE
							pulseView.stop()
						}
						openLoginScreen()
					}

					StartVM.STATE_AUTO_LOGIN -> {
						popFragment()
						val token = _viewModel.token.value
						lifecycleScope.launch(Dispatchers.Main) {
							if (token.isNullOrEmpty()) {
								openLoginScreen()
							} else {
								with(binding.viewAutoLogin) {
									frameAutoLogin.visibility = View.VISIBLE
									pulseView.start()
								}
								_viewModel.getMe()
							}
							delay(200)
							splashScreen.setKeepOnScreenCondition { false }
						}
					}
				}
			}

			user.observe(this@StartActivity) {
				lifecycleScope.launch(Dispatchers.Main) {
					delay(1000)
					it.notNull {
						if (state.value != StartVM.STATE_LOGIN) {
							goTo(MainActivity::class).also { finish() }
						}
					}
				}
			}
		}
	}

	override fun registerOnClick() {
		with(binding.viewAutoLogin) {
			tvCancel.click { _viewModel.cancelAutoLogin() }
		}
	}

	override fun callApi() {
	}

	private fun openLoginScreen() {
		val fragment: LoginFragment = LoginFragment.newInstance()
		replaceFragmentInActivity(
			R.id.frame_init_contain,
			fragment,
			animateType = AnimateType.FADE,
			addToBackStack = false
		)
	}
}