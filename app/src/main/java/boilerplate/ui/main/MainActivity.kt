package boilerplate.ui.main

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withResumed
import androidx.window.layout.WindowInfoTracker
import boilerplate.R
import boilerplate.base.BaseActivity
import boilerplate.databinding.ActivityMainBinding
import boilerplate.service.network.NetworkSchedulerService
import boilerplate.service.signalr.SignalRManager
import boilerplate.service.signalr.SignalRReceiver
import boilerplate.service.signalr.SignalRService
import boilerplate.ui.main.adapter.HomePagerAdapter
import boilerplate.ui.main.adapter.customTab
import boilerplate.ui.main.tab.HomeTabIndex
import boilerplate.ui.splash.StartActivity
import boilerplate.utils.InternetManager
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.isAppInBackground
import boilerplate.utils.extension.isTablet
import boilerplate.utils.extension.show
import boilerplate.utils.extension.startActivityAtRoot
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity<ActivityMainBinding, MainVM>() {
    companion object {
        const val JOB_SCHEDULER_ID: Int = 100
    }

    override val mViewModel: MainVM by viewModel()

    private val _signalRReceiver by inject<SignalRReceiver>()

    private lateinit var _windowInfoTracker: WindowInfoTracker
    private lateinit var _homeAdapter: HomePagerAdapter

    private lateinit var _serviceIntent: Intent

    private var _isBackFromBackground = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _windowInfoTracker = WindowInfoTracker.getOrCreate(this@MainActivity)
        _serviceIntent = Intent(this@MainActivity, SignalRService::class.java)
        _isBackFromBackground = false

        lifecycleScope.launch(Dispatchers.Main) {
            lifecycle.withResumed {
                SignalRManager.serviceConnect(this@MainActivity, _serviceIntent)
                scheduleJob()

                if (_isBackFromBackground && InternetManager.isConnected()) {
                    SignalRManager.reconnectSignal()
                }

                _signalRReceiver.register()
            }
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
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
        }
    }

    override fun onStop() {
        super.onStop()
        _isBackFromBackground = baseContext.isAppInBackground()
    }

    override fun onDestroy() {
        super.onDestroy()

        _signalRReceiver.unregister()
        SignalRManager.stopSignal()
        SignalRManager.unbindServices(this@MainActivity, _serviceIntent)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun initialize() {
        initHomepage()
    }

    override fun onSubscribeObserver() {
        with(mViewModel) {
            logout.observe(this@MainActivity) {
                if (it) {
                    startActivityAtRoot(StartActivity::class.java)
                    finish()
                }
            }
        }
    }

    override fun registerOnClick() {

    }

    override fun callApi() {
    }

    private fun initHomepage() {
        with(binding) {
            _homeAdapter = HomePagerAdapter(supportFragmentManager, lifecycle)
            viewPagerHome.apply {
                setAdapter(_homeAdapter)
                setUserInputEnabled(false)
                setOffscreenPageLimit(5)
            }

            HomeTabIndex.setupFragment().let {
                _homeAdapter.addFragment(it)
            }

            TabLayoutMediator(
                tabLayoutHome,
                viewPagerHome,
                false,
                false
            ) { tab: TabLayout.Tab, position: Int ->
                tab.setCustomView(
                    tab.customTab(
                        HomeTabIndex.titleTab()[position],
                        HomeTabIndex.iconTab()[position]
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

}