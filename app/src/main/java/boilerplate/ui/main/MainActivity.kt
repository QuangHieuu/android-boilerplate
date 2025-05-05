package boilerplate.ui.main

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withCreated
import androidx.lifecycle.withStarted
import androidx.window.layout.WindowInfoTracker
import boilerplate.R
import boilerplate.base.BaseActivity
import boilerplate.base.PagerAdapterBuilder
import boilerplate.databinding.ActivityMainBinding
import boilerplate.service.network.NetworkSchedulerService
import boilerplate.ui.main.tab.HomeTabIndex
import boilerplate.ui.splash.StartActivity
import boilerplate.utils.InternetManager
import boilerplate.utils.extension.PERMISSION_NOTIFY
import boilerplate.utils.extension.isAppInBackground
import boilerplate.utils.extension.isTablet
import boilerplate.utils.extension.show
import boilerplate.utils.extension.startActivityAtRoot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity<ActivityMainBinding, MainVM>() {
	companion object {
		const val JOB_SCHEDULER_ID: Int = 100
	}

	override val viewModel: MainVM by viewModel()
	private lateinit var _splashScreen: SplashScreen
	private lateinit var _windowInfoTracker: WindowInfoTracker
	private lateinit var _homeAdapter: PagerAdapterBuilder

	private var _isBackFromBackground = false

	override var splitContainerId: Int = R.id.frame_tablet

	override fun getWindowInsets(): View = binding.frameTablet

	override fun onConfigurationChanged(newConfig: Configuration) {
		super.onConfigurationChanged(newConfig)

		setPageData()
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		_splashScreen = installSplashScreen().apply {
			setKeepOnScreenCondition { true }
		}
		super.onCreate(savedInstanceState)

		_windowInfoTracker = WindowInfoTracker.getOrCreate(this@MainActivity)
		_isBackFromBackground = false

		lifecycleScope.launch {
			lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
				_windowInfoTracker.windowLayoutInfo(this@MainActivity).collect {
					splitScreen()
				}
			}
		}

		lifecycleScope.launch(Dispatchers.IO) {
			lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
				if (_isBackFromBackground && InternetManager.isConnected()) {
				}
			}
		}

		lifecycleScope.launch {
			lifecycle.withCreated {
				scheduleJob()
			}
			lifecycle.withStarted {
				requestPermissionNotify()
			}
		}
	}

	override fun onStop() {
		super.onStop()
		_isBackFromBackground = baseContext.isAppInBackground()
	}

	override fun initialize() {
		_splashScreen.setKeepOnScreenCondition { false }
		createPage()
	}

	override fun onSubscribeObserver() {
		with(viewModel) {
			logout.observe(this@MainActivity) {
				if (it) {
					startActivityAtRoot(StartActivity::class.java)
					finish()
				}
			}
			currentSelected.observe(this@MainActivity) {
				binding.frameDashboard.show(it == HomeTabIndex.POSITION_HOME_DASHBOARD && isTablet())
				if (it != HomeTabIndex.POSITION_HOME_DASHBOARD) {
					binding.viewPagerHome.setCurrentItem(HomeTabIndex.getCurrentTabIndex(it), false)
				}
			}
		}
	}

	override fun registerOnClick() {

	}

	override fun callApi() {
	}

	override fun onLogout() {
	}

	override fun onKeyboardCallBack(isKeyboardShow: Boolean) {
		binding.tabLayoutHome.show(!isKeyboardShow)
	}

	override fun callbackWhenReceiverData(intent: Intent) {

	}

	private fun createPage() {
		with(binding) {
			_homeAdapter = PagerAdapterBuilder(this@MainActivity, viewPagerHome, tabLayoutHome)
				.offsetScreenLimit(4)
				.userInputEnable(false)
				.customTab(autoRefresh = true, smoothScroll = false)
		}
		setPageData()
	}

	private fun setPageData() {
		val fragments = HomeTabIndex.setupFragment(isTablet())
		_homeAdapter
			.detach()
			.fragment(fragments)
			.tabIcon(HomeTabIndex.tabIcon)
			.tabTitle(HomeTabIndex.tabTitle)
			.build()
	}

	private fun scheduleJob() {
		val jobScheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
		var isScheduler = false
		for (jobInfo in jobScheduler.allPendingJobs) {
			if (jobInfo.id == JOB_SCHEDULER_ID) {
				isScheduler = true
				break
			}
		}
		if (!isScheduler) {
			JobInfo.Builder(
				JOB_SCHEDULER_ID,
				ComponentName(this, NetworkSchedulerService::class.java)
			)
				.setMinimumLatency(1000)
				.setOverrideDeadline(2000)
				.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
				.build()
				.let { jobScheduler.schedule(it) }
		}
	}

	private suspend fun splitScreen() = coroutineScope {
		binding.frameTablet.show(isTablet())
		with(binding.appContainer) {
			val set = ConstraintSet()
			set.clone(this)
			if (isTablet()) {
				set.connect(
					R.id.ln_home,
					ConstraintSet.END,
					R.id.frame_tablet,
					ConstraintSet.START
				)
				set.connect(
					R.id.frame_tablet,
					ConstraintSet.START,
					R.id.ln_home,
					ConstraintSet.END
				)
			} else {
				set.connect(
					R.id.ln_home,
					ConstraintSet.END,
					ConstraintSet.PARENT_ID,
					ConstraintSet.END
				)
			}
			set.applyTo(this)
		}
	}

	private fun requestPermissionNotify() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			permission(PERMISSION_NOTIFY) {}
		}
	}


}