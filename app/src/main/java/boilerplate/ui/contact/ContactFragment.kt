package boilerplate.ui.contact

import android.util.Pair
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import boilerplate.base.BaseFragment
import boilerplate.databinding.FragmentContactBinding
import boilerplate.ui.contact.tab.ContactTab
import boilerplate.ui.contact.tab.ContactTabFragment
import boilerplate.ui.main.MainVM
import boilerplate.ui.main.adapter.HomePagerAdapter
import boilerplate.ui.main.adapter.customTab
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ContactFragment : BaseFragment<FragmentContactBinding, MainVM>() {
    companion object {
        fun newInstance(): ContactFragment {
            return ContactFragment()
        }
    }

    override val viewModel: MainVM by activityViewModels()

    private lateinit var _adapter: HomePagerAdapter

    override fun initialize() {
        initTabLayout()

    }

    private fun initTabLayout() {
        val tabTitle = arrayOf("Đơn vị", "Đơn vị khác",  /*, "Hay liên lạc"*/"Nhóm")

        with(binding) {
            _adapter = HomePagerAdapter(childFragmentManager, lifecycle)

            viewpager.setAdapter(_adapter)
            viewpager.setUserInputEnabled(false)

            val list = ArrayList<Pair<Int, Fragment>>().apply {
                add(
                    Pair(
                        ContactTab.TYPE_TAB_DEPARTMENT.index,
                        ContactTabFragment.newInstance(ContactTab.TYPE_TAB_DEPARTMENT.type)
                    )
                )
                add(
                    Pair(
                        ContactTab.TYPE_TAB_COMPANY.index,
                        ContactTabFragment.newInstance(ContactTab.TYPE_TAB_COMPANY.type)
                    )
                )
                add(
                    Pair(
                        ContactTab.TYPE_TAB_GROUP.index,
                        ContactTabFragment.newInstance(ContactTab.TYPE_TAB_GROUP.type)
                    )
                )
            }
            _adapter.addFragment(list)

            TabLayoutMediator(
                tabLayout,
                viewpager,
                false,
                false
            ) { tab: TabLayout.Tab, position: Int ->
                tab.setCustomView(
                    tab.customTab(tabTitle[position])
                )
            }.attach()
        }
    }

    override fun onSubscribeObserver() {
    }

    override fun registerEvent() {
        with(binding) {
            editSearch.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                }
                true
            }
        }

    }

    override fun callApi() {
    }

}