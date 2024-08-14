package boilerplate.ui.main

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withCreated
import androidx.lifecycle.withStarted
import androidx.window.layout.WindowInfoTracker
import boilerplate.R
import boilerplate.base.BaseActivity
import boilerplate.databinding.ActivityMainBinding
import boilerplate.model.file.MimeType
import boilerplate.service.network.NetworkSchedulerService
import boilerplate.service.notify.FirebaseMessageService
import boilerplate.service.notify.FirebaseMessageService.Companion.CONVERSATION_ID
import boilerplate.service.notify.FirebaseMessageService.Companion.MEETING_ID
import boilerplate.service.notify.FirebaseMessageService.Companion.ROLE
import boilerplate.service.notify.FirebaseMessageService.Companion.WORK_MANAGE_ID
import boilerplate.service.signalr.SignalRManager
import boilerplate.service.signalr.SignalRService
import boilerplate.ui.dashboard.DashboardFragment
import boilerplate.ui.main.adapter.HomePagerAdapter
import boilerplate.ui.main.adapter.customTab
import boilerplate.ui.main.tab.HomeTabIndex
import boilerplate.ui.splash.StartActivity
import boilerplate.utils.InternetManager
import boilerplate.utils.extension.AnimateType
import boilerplate.utils.extension.PERMISSION_NOTIFY
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.isAppInBackground
import boilerplate.utils.extension.isTablet
import boilerplate.utils.extension.notNull
import boilerplate.utils.extension.replaceFragmentInActivity
import boilerplate.utils.extension.show
import boilerplate.utils.extension.startActivityAtRoot
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity<ActivityMainBinding, MainVM>() {
	companion object {
		const val JOB_SCHEDULER_ID: Int = 100
	}

	override val _viewModel: MainVM by viewModel()

	private lateinit var _windowInfoTracker: WindowInfoTracker
	private lateinit var _homeAdapter: HomePagerAdapter

	private lateinit var _serviceIntent: Intent

	private var _isBackFromBackground = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		_windowInfoTracker = WindowInfoTracker.getOrCreate(this@MainActivity)
		_serviceIntent = Intent(this@MainActivity, SignalRService::class.java)
		_isBackFromBackground = false

		lifecycleScope.launch {
			lifecycle.withCreated {
				SignalRManager.serviceConnect(this@MainActivity, _serviceIntent)
				scheduleJob()
			}
			lifecycle.withStarted {
				requestPermissionNotify()
				splitScreen()
			}
		}

		lifecycleScope.launch {
			lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
				if (_isBackFromBackground && InternetManager.isConnected()) {
					SignalRManager.reconnectSignal()
				}
			}
		}
	}

	override fun onStop() {
		super.onStop()
		_isBackFromBackground = baseContext.isAppInBackground()
		SignalRManager.stopSignal()
	}

	override fun onDestroy() {
		SignalRManager.unbindServices(this@MainActivity, _serviceIntent)

		super.onDestroy()
	}

	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		Log.d("SSS", "onNewIntent: " + intent)

		handleDataFromApp(intent)
	}

	override fun initialize() {
		Log.d("SSS", "initialize: " + intent)

		handleDataFromApp(intent)
		initHomepage()
	}

	override fun onSubscribeObserver() {
		with(_viewModel) {
			logout.observe(this@MainActivity) {
				if (it) {
					startActivityAtRoot(StartActivity::class.java)
					finish()
				}
			}
			currentSelected.observe(this@MainActivity) {
				binding.frameDashboard.apply {
					if (it == HomeTabIndex.POSITION_HOME_DASHBOARD && isTablet()) show() else gone()
				}
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

	override fun handleLogout() {
		_viewModel.logout()
	}

	private fun initHomepage() {
		with(binding) {
			if (isTablet()) {
				replaceFragmentInActivity(
					R.id.frame_dashboard,
					DashboardFragment.newInstance(),
					animateType = AnimateType.FADE,
					addToBackStack = false
				)
			}

			_homeAdapter = HomePagerAdapter(supportFragmentManager, lifecycle)
			viewPagerHome.apply {
				setAdapter(_homeAdapter)
				setUserInputEnabled(false)
				setOffscreenPageLimit(4)
			}

			HomeTabIndex.setupFragment(isTablet()).let {
				_homeAdapter.addFragment(it)
				_viewModel.tabPosition.value = HomeTabIndex.tabPosition
			}

			TabLayoutMediator(
				tabLayoutHome,
				viewPagerHome,
				true,
				false
			) { tab: TabLayout.Tab, position: Int ->
				tab.setCustomView(
					tab.customTab(
						HomeTabIndex.tabTitle[position],
						HomeTabIndex.tabIcon[position]
					)
				)
			}.attach()
		}
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

	private fun splitScreen() {
		binding.frameTablet.apply { if (isTablet()) show() else gone() }
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

	private fun handleDataFromApp(intent: Intent) {
		val action = intent.action
		val bundle = intent.extras
		if (action == null || action == Intent.ACTION_MAIN) {
			return
		}
		when (action) {
			Intent.ACTION_SEND -> {
				intent.type.notNull { type ->
					val sharedText = if (bundle != null) bundle.getString(Intent.EXTRA_TEXT, "") else ""
					val imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
						intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
					} else {
						intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
					}
					if (type.contains(MimeType.TEXT.type)) {


						return
					}
					if (type.startsWith(MimeType.IMAGE.type) ||
						type.startsWith(MimeType.VIDEO.type) ||
						type.startsWith(MimeType.AUDIO.type) ||
						type.startsWith(MimeType.APPLICATION.type)
					) {


						return
					}
				}
			}

			FirebaseMessageService.ACTION_NOTIFY -> {
				val conversationId = intent.getStringExtra(CONVERSATION_ID)
				val workId = intent.getStringExtra(WORK_MANAGE_ID)
				val roleId = intent.getStringExtra(ROLE)
				val meetingId = intent.getStringExtra(MEETING_ID)
			}
		}

		intent.setAction(Intent.ACTION_MAIN)
	}
}