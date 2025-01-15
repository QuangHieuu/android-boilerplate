package boilerplate.ui.splash

import android.os.Bundle
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import boilerplate.base.BaseActivity
import boilerplate.databinding.ActivityStartBinding
import boilerplate.ui.main.MainActivity
import boilerplate.utils.extension.goTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class StartActivity : BaseActivity<ActivityStartBinding, StartVM>() {
	override val viewModel: StartVM by viewModel()

	private lateinit var splashScreen: SplashScreen

	override fun onCreate(savedInstanceState: Bundle?) {
		splashScreen = installSplashScreen().apply {
			setKeepOnScreenCondition { true }
		}
		super.onCreate(savedInstanceState)
	}

	override fun initialize() {
		lifecycleScope.launch(Dispatchers.Main) {
			delay(1000)
			goTo(MainActivity::class, isFinish = true)
		}
	}

	override fun onSubscribeObserver() {

	}

	override fun registerOnClick() {
	}

	override fun callApi() {
	}
}