package boilerplate.ui.contact


import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.util.Pair
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import boilerplate.R
import boilerplate.base.BaseDialogFragment
import boilerplate.databinding.FragmentContactSelectBinding
import boilerplate.databinding.ViewContactSelectedBinding
import boilerplate.model.conversation.Conversation
import boilerplate.model.user.User
import boilerplate.model.user.UserSignalR
import boilerplate.ui.contact.adapter.ContactAvatarDecoration
import boilerplate.ui.contact.adapter.ContactTabAdapter
import boilerplate.ui.contact.listener.SimpleListener
import boilerplate.ui.contact.tab.ContactTab
import boilerplate.ui.contact.tab.SelectedTabFragment
import boilerplate.ui.contact.viewModel.ContactVM
import boilerplate.ui.main.MainVM
import boilerplate.ui.main.adapter.HomePagerAdapter
import boilerplate.ui.main.adapter.customTab
import boilerplate.utils.extension.click
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.hideKeyboard
import boilerplate.utils.extension.isTablet
import boilerplate.utils.extension.launch
import boilerplate.utils.extension.performClick
import boilerplate.utils.extension.setWidthPercent
import boilerplate.utils.extension.show
import boilerplate.utils.extension.showFail
import boilerplate.utils.extension.showSuccess
import boilerplate.utils.extension.showWarning
import boilerplate.widget.recyclerview.EndlessListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ContactSelectFragment : BaseDialogFragment<FragmentContactSelectBinding, ContactVM>() {

	companion object {
		const val FOR_CREATE_REGULAR: String = "FOR_CREATE_REGULAR"
		const val FOR_EDIT_REGULAR: String = "FOR_EDIT_REGULAR"
		const val FOR_CREATE_GROUP: String = "FOR_CREATE_GROUP"
		const val FOR_ADD_MEMBER: String = "FOR_ADD_MEMBER"
		const val FOR_FROM_OTHER: String = "FOR_FROM_OTHER"
		const val FOR_SHARE_FILE: String = "FOR_SHARE_FILE"
		const val FOR_SHARE_MESSAGE: String = "FOR_SHARE_MESSAGE"
		const val FOR_SHARE_LINK: String = "FOR_SHARE_LINK"

		fun newInstance(screen: String): ContactSelectFragment {
			return Bundle().apply {
				putString(KEY_SCREEN_TYPE, screen)
			}.let { ContactSelectFragment().apply { arguments = it } }
		}

		private const val KEY_SCREEN_TYPE: String = "KEY_SCREEN_TYPE"
	}

	override val viewModel: ContactVM by viewModel()
	private val _activityVM: MainVM by activityViewModel()

	private lateinit var _tabAdapter: HomePagerAdapter
	private lateinit var _tabMediator: TabLayoutMediator
	private lateinit var _screen: String

	private lateinit var _adapter: ContactTabAdapter
	private lateinit var _adapterAvatar: ContactTabAdapter
	private lateinit var _adapterSearch: ContactTabAdapter
	private lateinit var _endLess: EndlessListener

	private lateinit var _bindSelected: ViewContactSelectedBinding
	private lateinit var _headerBehavior: BottomSheetBehavior<View>

	private val _tabTitle = arrayListOf<String>()

	private var _page = 1
	private var _currentItem = 0
	private var _isLoadMore = false

	override fun initialize() {
		setWidthPercent(90, 80)
		changeTabletView()
		_tabMediator = TabLayoutMediator(
			binding.tabLayout,
			binding.viewpager,
			true,
			false
		) { tab, position -> tab.setCustomView(tab.customTab(_tabTitle[position])) }

		with(binding) {
			viewNoData.tvNoData.setText(R.string.no_search_result)

			if (isTablet()) {
				_bindSelected.rcvAvatar.gone()

				btnCancel.show()
				tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
			} else {
				_headerBehavior = BottomSheetBehavior.from(binding.frameMobile)
				_headerBehavior.isDraggable = false
				_bindSelected.rcvAvatar.show()
				_bindSelected.tvTitle.gone()

				btnCancel.gone()
				tabLayout.tabMode = TabLayout.MODE_FIXED
			}
		}
		requireActivity().onBackPressedDispatcher.addCallback(
			this@ContactSelectFragment,
			object : OnBackPressedCallback(true) {
				override fun handleOnBackPressed() {
					if (_headerBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
						_bindSelected.imgBack.performClick()
					} else {
						handleBack()
					}
				}
			})

		handleArgument()
		handleTabLayout()
		handleSelectedView()
		handleSearchView()
	}

	override fun onSubscribeObserver() {
		with(viewModel) {
			loading.removeObservers(viewLifecycleOwner)

			selectedContact.observe(this@ContactSelectFragment) { result ->
				synchronized(result) {
					_adapter.updateSelected(result)
					if (!isTablet()) {
						_adapterAvatar.updateSelected(result)
					}
					validate()
				}
			}
			searchContact.observe(this@ContactSelectFragment) { result ->
				result.forEach { any ->
					if (any is User) {
						any.isChecked = viewModel.isAlreadySelected(any.id)
						if (_screen == FOR_ADD_MEMBER) {

						}
					}
				}

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
			putEditGroup.observe(this@ContactSelectFragment) {
				if (it != null) {
					_activityVM.putEditGroup.postValue(it)
					binding.root.showSuccess(R.string.success_update)
					handleBack()
				} else {
					binding.root.showFail(R.string.error_general)
					viewModel.setLoading(false)
				}
			}
		}
	}

	override fun registerEvent() {
		with(binding) {
			btnCreate.click {
				if (checkLimit()) {
					root.showWarning(getString(R.string.warning_limit_members, viewModel.config.limitMember))
				} else {
					handleCreate()
				}
			}
			btnCancel.performClick(tvCancel)
			tvCancel.click { handleBack() }
			tvExit.click { hideSearch() }
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
		}

		if (!isTablet()) {
			with(_bindSelected) {
				lnUncheckAll.show()
				ctlHeader.click {
					if (_headerBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
						_headerBehavior.state = BottomSheetBehavior.STATE_EXPANDED

						ctlHeader.setBackgroundResource(R.color.colorPrimary)

						rcvAvatar.gone()
						tvCount.gone()

						imgBack.show()
						tvTitle.show()
					}
				}
				imgBack.click {
					_headerBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

					ctlHeader.setBackgroundResource(R.color.colorWhite)

					rcvAvatar.show()
					tvCount.show()

					imgBack.gone()
					tvTitle.gone()
				}
			}
		}
	}

	override fun callApi() {

	}


	private fun handleArgument() {
		val bundle = arguments ?: Bundle()
		_screen = bundle.getString(KEY_SCREEN_TYPE, "")
	}

	private fun handleTabLayout() {
		_tabAdapter = HomePagerAdapter(childFragmentManager, lifecycle)

		with(binding) {
			tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
				override fun onTabSelected(tab: TabLayout.Tab?) {
					hideSearch()
				}

				override fun onTabUnselected(tab: TabLayout.Tab?) {
				}

				override fun onTabReselected(tab: TabLayout.Tab?) {
				}
			})
			viewpager.apply {
				adapter = _tabAdapter
				isUserInputEnabled = false
				offscreenPageLimit = 5
			}

			when (_screen) {
				FOR_EDIT_REGULAR -> {
					imgPickAvatar.setImageResource(R.drawable.ic_avatar_group)
					tvTitle.setText(R.string.edit_group)
					btnCreate.setText(R.string.save)
					editGroupName.setFilters(arrayOf<InputFilter>(LengthFilter(100)))

					lnCreateGroup.show()
					lnButton.show()
				}

				FOR_CREATE_REGULAR -> {
					imgPickAvatar.setImageResource(R.drawable.ic_avatar_group)
					tvTitle.setText(R.string.create_regular_group)
					btnCreate.setText(R.string.save)
					editGroupName.setFilters(arrayOf<InputFilter>(LengthFilter(100)))

					lnCreateGroup.show()
					lnButton.show()
				}

				FOR_CREATE_GROUP -> {
					tvTitle.setText(R.string.create_group)
					btnCreate.setText(R.string.save)

					lnCreateGroup.show()
					lnButton.show()

					_tabTitle.addAll(arrayListOf("Đơn vị", "Đơn vị khác"))
					val list = ArrayList<Pair<Int, Fragment>>()
					list.add(
						Pair(
							ContactTab.TYPE_TAB_DEPARTMENT_CREATE.index,
							SelectedTabFragment.newInstance(ContactTab.TYPE_TAB_DEPARTMENT_CREATE.type)
						)
					)
					list.add(
						Pair(
							ContactTab.TYPE_TAB_COMPANY_CREATE.index,
							SelectedTabFragment.newInstance(ContactTab.TYPE_TAB_COMPANY_CREATE.type)
						)
					)
					_tabAdapter.addFragment(list)
					_tabMediator.attach()
				}

				FOR_ADD_MEMBER -> {
					tvTitle.setText(R.string.add_member)
					btnCreate.setText(R.string.save)
					lnButton.show()
				}

				FOR_FROM_OTHER,
				FOR_SHARE_FILE,
				FOR_SHARE_MESSAGE,
				FOR_SHARE_LINK -> {
					tvTitle.setText(R.string.forward)
					frameChatBox.apply {
						show()
						setupForShare()
					}
				}

				else -> {}
			}
		}
	}

	private fun handleSelectedView() {
		_adapter = ContactTabAdapter(object : SimpleListener() {
			override fun removeUser(user: User) {
				viewModel.removeSelectedId(user.id)
				viewModel.postSelectedContact(user, null, false)
			}

			override fun removeConversation(conversation: Conversation) {
				viewModel.removeSelectedId(conversation.id)
				viewModel.postSelectedContact(null, conversation, false)
			}
		}).apply {
			showDescription = false
		}
		_bindSelected.rcvSelected.apply {
			adapter = _adapter
		}

		if (!isTablet()) {
			_adapterAvatar = ContactTabAdapter(object : SimpleListener() {})
				.apply { viewType = ContactTabAdapter.TYPE_AVATAR }

			_bindSelected.rcvAvatar.apply {
				adapter = _adapterAvatar
				addItemDecoration(ContactAvatarDecoration())
			}
		}

		countSelected()
	}

	private fun validate() {
		with(binding) {
			root.launch {
				frameChatBox.enableButtonSend(_adapter.members.isNotEmpty() || _adapter.conversations.isNotEmpty())
				btnCreate.isEnabled = _adapter.itemCount >= (if (_screen == FOR_ADD_MEMBER) 1 else 2)
				countSelected()
			}
		}
	}

	private fun countSelected() {
		val count = _adapter.itemCount
		var string = ""
		when (_screen) {
			FOR_EDIT_REGULAR,
			FOR_CREATE_REGULAR,
			FOR_CREATE_GROUP,
			FOR_ADD_MEMBER -> {
				string =
					getString(R.string.selected_count_limit, count, viewModel.config.limitMember)
			}

			FOR_FROM_OTHER,
			FOR_SHARE_FILE,
			FOR_SHARE_MESSAGE,
			FOR_SHARE_LINK -> {
				string = getString(R.string.selected_count, count)
			}
		}

		with(_bindSelected) {
			tvCount.text = string
			tvCountDisplay.text = string
		}
	}

	private fun changeTabletView() {
		_bindSelected = ViewContactSelectedBinding.inflate(layoutInflater, binding.root, false)
		with(binding) {
			if (isTablet()) {
				frameTablet.addView(_bindSelected.root)
				frameTablet.show()
			} else {
				frameMobile.addView(_bindSelected.root)
				frameMobile.show()
			}
		}
	}

	private fun handleSearchView() {
		_endLess = object : EndlessListener(binding.rcvSearch.layoutManager as LinearLayoutManager) {
			override fun onLoadMore(page: Int, totalItemsCount: Int) {
				if (!_isLoadMore && _currentItem == viewModel.limit) {
					_isLoadMore = true
					_page += 1
					_adapterSearch.loadMore()
					viewModel.searchMore(
						_page,
						_tabAdapter.getTabIndex(binding.viewpager.currentItem),
						binding.viewpager.currentItem,
					)
				}
			}
		}

		_adapterSearch = ContactTabAdapter(object : SimpleListener() {
			override fun onUserSelect(user: User) {

			}

			override fun onRegularSelected(conversation: Conversation) {

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
			rlSearch.show(_tabAdapter.getTabIndex(viewpager.currentItem) != ContactTab.TYPE_TAB_REGULAR.index)

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
			tvPickCompany.show(_tabAdapter.getTabIndex(viewpager.currentItem) == ContactTab.TYPE_TAB_COMPANY.index)
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
					_tabAdapter.getTabIndex(viewpager.currentItem),
					viewpager.currentItem,
					_page
				)
			}
		}
	}

	private fun checkLimit(): Boolean = _adapter.members.size > viewModel.config.limitMember

	private fun handleCreate() {
		viewModel.setLoading(true)
		val newMember: ArrayList<UserSignalR> = arrayListOf()
		for (member in _adapter.members) {
			newMember.add(UserSignalR(member.id, member.name))
		}
		when (_screen) {
			FOR_EDIT_REGULAR -> {
				if (viewModel.createGroup.regularName.trim().isEmpty()) {
					binding.root.showFail(R.string.warning_input_name_group)
					viewModel.setLoading(false)
					return
				}
				viewModel.createGroup.regularUser = _adapter.members
				if (viewModel.createGroup.regularUser.isEmpty()) {
					viewModel.setLoading(false)
					return
				}
				viewModel.putEditGroup()
			}

			FOR_CREATE_REGULAR -> {}
			FOR_CREATE_GROUP -> {

			}

			FOR_ADD_MEMBER -> {}
		}
	}
}