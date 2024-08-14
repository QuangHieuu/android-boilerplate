package boilerplate.ui.contact

import android.util.Pair
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import boilerplate.base.BaseFragment
import boilerplate.databinding.FragmentContactBinding
import boilerplate.model.user.User
import boilerplate.ui.contact.adapter.ContactTabAdapter
import boilerplate.ui.contact.listener.SimpleListener
import boilerplate.ui.contact.tab.ContactTab
import boilerplate.ui.contact.tab.ContactTabFragment
import boilerplate.ui.contact.viewModel.ContactVM
import boilerplate.ui.contactDetail.ContactDetailFragment
import boilerplate.ui.main.MainVM
import boilerplate.ui.main.adapter.HomePagerAdapter
import boilerplate.ui.main.adapter.customTab
import boilerplate.utils.extension.callPhone
import boilerplate.utils.extension.click
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.hideKeyboard
import boilerplate.utils.extension.openDialog
import boilerplate.utils.extension.show
import boilerplate.widget.recyclerview.EndlessListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ContactFragment : BaseFragment<FragmentContactBinding, ContactVM>() {
	companion object {
		fun newInstance(): ContactFragment {
			return ContactFragment()
		}
	}

	override val viewModel: ContactVM by viewModel()
	private val _activityVM: MainVM by activityViewModel()

	private lateinit var _adapter: HomePagerAdapter

	private lateinit var _adapterSearch: ContactTabAdapter
	private lateinit var _endLess: EndlessListener

	private var _page = 1
	private var _currentItem = 0
	private var _isLoadMore = false

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
		handleSearchView()
	}

	override fun onSubscribeObserver() {
		with(viewModel) {
			loading.removeObservers(viewLifecycleOwner)

			searchContact.observe(this@ContactFragment) { result ->
				_currentItem = result.size
				_isLoadMore = false
				if (_page == 1) {
					if (result.isEmpty()) {
						binding.viewNoData.lnNoData.show()
					} else {
						binding.viewNoData.lnNoData.gone()
					}
					_adapterSearch.insertData(result)
				} else {
					_adapterSearch.addMore(result)
				}
			}
		}
	}

	override fun registerEvent() {
		with(binding) {
			editSearch.setOnEditorActionListener { v, actionId, event ->
				editSearch.hideKeyboard()
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					handleSearch()
					return@setOnEditorActionListener true
				}
				return@setOnEditorActionListener false
			}
			editSearch.setOnFocusChangeListener { v, hasFocus ->
				if (hasFocus) showSearch()
			}
			tvExit.click { hideSearch() }
			tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
				override fun onTabSelected(tab: TabLayout.Tab) {
					hideSearch()
					_adapterSearch.viewType = if (tab.position == 3) {
						ContactTabAdapter.TYPE_GROUP
					} else {
						ContactTabAdapter.TYPE_SEARCH_USER
					}

					editSearch.show(tab.position != 2)
				}

				override fun onTabUnselected(tab: TabLayout.Tab?) {
				}

				override fun onTabReselected(tab: TabLayout.Tab?) {
				}
			})
		}

	}

	override fun callApi() {
	}

	private fun handleSearchView() {
		_endLess = object : EndlessListener(binding.rcvSearch.layoutManager as LinearLayoutManager) {
			override fun onLoadMore(page: Int, totalItemsCount: Int) {
				if (!_isLoadMore && _currentItem == viewModel.limit) {
					_isLoadMore = true
					_page += 1
					_adapterSearch.loadMore()

				}
			}
		}

		_adapterSearch = ContactTabAdapter(object : SimpleListener() {
			override fun onOpenInform(user: User) {
				openDialog(ContactDetailFragment.newInstance(user.id))
			}

			override fun onChatWith(item: User) {
				_activityVM.postPersonConversation(item)
			}

			override fun onPhone(phoneNumber: String) {
				callPhone(phoneNumber)
			}
		}).apply {
			check = true
			showDescription = true
			viewType = ContactTabAdapter.TYPE_SEARCH_USER
		}
		binding.rcvSearch.adapter = _adapterSearch
	}

	private fun hideSearch() {
		with(binding) {
			editSearch.setText("")
			editSearch.hideKeyboard()
//			rlSearch.show(_adapter.getTabIndex(viewpager.currentItem) != ContactTab.TYPE_TAB_REGULAR.index)

			tvExit.gone()
			tvPickCompany.gone()
			viewNoData.lnNoData.gone()
			rcvSearch.gone()

			viewpager.show()
		}
	}

	private fun showSearch() {
		with(binding) {
			tvExit.show()
			rcvSearch.show()

			viewpager.gone()
			tvPickCompany.show(_adapter.getTabIndex(viewpager.currentItem) == ContactTab.TYPE_TAB_COMPANY.index)
		}
	}

	private fun handleSearch() {
		_page = 1
		with(binding) {
			editSearch.hideKeyboard()
			val keyWork = editSearch.editableText.toString()
			if (keyWork.isNotEmpty()) {
				viewModel.searchContact(
					keyWork,
					_adapter.getTabIndex(viewpager.currentItem),
					viewpager.currentItem,
					_page
				)
			}
		}
	}
}