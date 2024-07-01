package boilerplate.ui.main

import android.os.Bundle
import androidx.window.layout.WindowInfoTracker
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import boilerplate.base.BaseActivity
import boilerplate.databinding.ActivityMainBinding
import boilerplate.ui.main.adapter.HomePagerAdapter
import boilerplate.ui.main.tab.HomeTabIndex
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity<ActivityMainBinding, MainVM>() {
    override fun bindingFactory(): ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
    override val mViewModel: MainVM by viewModel()

    private lateinit var windowInfoTracker: WindowInfoTracker
    private lateinit var mHomeAdapter: HomePagerAdapter
    private lateinit var mTabMediator: TabLayoutMediator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun initialize() {
        initHomepage()
    }

    override fun onSubscribeObserver() {

    }

    override fun registerOnClick() {

    }

    override fun callApi() {
    }

    private fun initHomepage() {
        with(binding) {
            mHomeAdapter = HomePagerAdapter(supportFragmentManager, lifecycle)
            viewPagerHome.setAdapter(mHomeAdapter)
            viewPagerHome.setUserInputEnabled(false)
            mTabMediator = TabLayoutMediator(
                tabLayoutHome,
                viewPagerHome,
                false,
                false
            ) { tab: TabLayout.Tab, position: Int ->
                tab.setCustomView(
                    HomePagerAdapter.getTabView(
                        this@MainActivity,
                        binding.root,
                        HomeTabIndex.titleTab()[position],
                        HomeTabIndex.iconTab()[position]
                    )
                )
            }

            HomeTabIndex.setupFragment().let {
                viewPagerHome.setOffscreenPageLimit(it.size - 1)
                mHomeAdapter.addOnlyForHomeFragment(it)
            }
            if (mTabMediator.isAttached) {
                mTabMediator.detach()
            }
            mTabMediator.attach()
        }
    }
}