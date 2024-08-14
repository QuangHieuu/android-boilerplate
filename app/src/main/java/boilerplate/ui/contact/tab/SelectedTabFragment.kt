package boilerplate.ui.contact.tab

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import boilerplate.base.BaseFragment
import boilerplate.databinding.FragmentContactTabBinding
import boilerplate.model.user.Company
import boilerplate.model.user.Department
import boilerplate.model.user.User
import boilerplate.ui.contact.adapter.ContactTabAdapter
import boilerplate.ui.contact.listener.SimpleListener
import boilerplate.ui.contact.tab.ContactTab.TYPE_TAB_COMPANY_ADD
import boilerplate.ui.contact.tab.ContactTab.TYPE_TAB_COMPANY_CREATE
import boilerplate.ui.contact.tab.ContactTab.TYPE_TAB_COMPANY_REGULAR
import boilerplate.ui.contact.tab.ContactTab.TYPE_TAB_COMPANY_SHARE
import boilerplate.ui.contact.tab.ContactTab.TYPE_TAB_CONVERSATION
import boilerplate.ui.contact.tab.ContactTab.TYPE_TAB_DEPARTMENT_ADD
import boilerplate.ui.contact.tab.ContactTab.TYPE_TAB_DEPARTMENT_CREATE
import boilerplate.ui.contact.tab.ContactTab.TYPE_TAB_DEPARTMENT_REGULAR
import boilerplate.ui.contact.tab.ContactTab.TYPE_TAB_DEPARTMENT_SHARE
import boilerplate.ui.contact.tab.ContactTab.TYPE_TAB_GROUP
import boilerplate.ui.contact.tab.ContactTab.TYPE_TAB_REGULAR
import boilerplate.ui.contact.viewModel.ContactVM
import boilerplate.utils.extension.click
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.launch
import boilerplate.utils.extension.notNull
import boilerplate.utils.extension.show
import boilerplate.widget.recyclerview.EndlessListener
import org.koin.androidx.viewmodel.ext.android.viewModel

class SelectedTabFragment : BaseFragment<FragmentContactTabBinding, ContactVM>() {
	companion object {
		const val KEY_SCREEN = "KEY_SCREEN"

		fun newInstance(screen: String): SelectedTabFragment {
			val bundle = Bundle().apply { putString(KEY_SCREEN, screen) }
			return SelectedTabFragment().apply { arguments = bundle }
		}
	}

	override val viewModel: ContactVM by viewModel(ownerProducer = { requireParentFragment() })

	private lateinit var _screen: String
	private lateinit var _adapter: ContactTabAdapter
	private lateinit var _endLessListener: EndlessListener


	private var _currentSize = 0
	private var _page = 1
	private var _loadMore = false
	private var _checkAll = false

	override fun initialize() {
		_endLessListener =
			object : EndlessListener(binding.recycleView.layoutManager as LinearLayoutManager) {
				override fun onLoadMore(page: Int, totalItemsCount: Int) {
					if (_screen == TYPE_TAB_REGULAR.type) {
						return
					}
					if (!_loadMore && _currentSize == viewModel.limit) {
						_loadMore = true
						_page++

						binding.recycleView.launch {
							_adapter.loadMore()
						}
					}
				}
			}

		_adapter = ContactTabAdapter(object : SimpleListener() {
			override fun onExpandCompany(company: Company) {
				company.isExpanding = !company.isExpanding
				viewModel.getCompany(company)
			}

			override fun onExpandDepartment(department: Department) {
				if (department.totalUser == 0) return
				if (department.users.isEmpty()) {
					viewModel.getDepartment(department)
				} else {
					_adapter.expandDepartment(department)
				}
			}

			override fun onUserSelect(user: User) {
				if (user.isChecked) {
					_checkAll = false
					binding.checkAll.isChecked = false
					viewModel.removeSelectedId(user.id)
				} else {
					viewModel.selected.add(user.id)
				}
				viewModel.postSelectedContact(user, null, !user.isChecked)
			}

			override fun onDepartmentSelected(department: Department) {
				if (department.isChecked) {
					_checkAll = false
					binding.checkAll.isChecked = false
				}
				department.isChecked = !department.isChecked
				viewModel.handleDepartment(null, department, department.isChecked)
				_adapter.selectedDepartment(department)
			}

			override fun onCompanySelected(company: Company) {
				if (company.isChecked) {
					_checkAll = false
					binding.checkAll.isChecked = false
				}
				if (!company.isExpanding) {
					company.isExpanding = true
				}
				company.isChecked = !company.isChecked
				viewModel.getCompany(company)
			}
		})

		binding.recycleView.apply {
			adapter = _adapter
			itemAnimator = null
		}
		handleArgument()
	}

	override fun onSubscribeObserver() {
		with(viewModel) {
			when (ContactTab.fromType(_screen)) {
				TYPE_TAB_DEPARTMENT_CREATE -> {
					department.observe(this@SelectedTabFragment) {
						_adapter.insertData(it)
					}
				}

				TYPE_TAB_COMPANY_CREATE -> {
					company.observe(this@SelectedTabFragment) {
						_adapter.insertData(ArrayList(it))
					}
				}

				else -> {}
			}

			selectedContact.observe(this@SelectedTabFragment) {
				it.user.notNull { user ->
					_adapter.selectedUser(user, it.isCheck)
				}
				it.conversation.notNull { conversation ->
					_adapter.selectedConversation(conversation)
				}
			}
			expandDepartment.observe(this@SelectedTabFragment) {
				_adapter.expandDepartment(it)
			}
			expandCompany.observe(this@SelectedTabFragment) {
				_adapter.expandCompany(it)
			}
		}
	}

	override fun registerEvent() {
		with(binding) {
			swipeLayout.setOnRefreshListener {
				swipeLayout.isRefreshing = false
				callApi()
			}
			checkAll.click {
				_checkAll = !_checkAll
				for (ob in _adapter.getList()) {
					if (ob is Department) {
						ob.isChecked = _checkAll
						viewModel.handleDepartment(null, ob, ob.isChecked)
					}
					if (ob is Company) {
						ob.isChecked = _checkAll
						viewModel.getCompany(ob)
					}
				}
			}
		}
	}

	override fun callApi() {
		when (ContactTab.fromType(_screen)) {
			TYPE_TAB_DEPARTMENT_ADD,
			TYPE_TAB_DEPARTMENT_CREATE,
			TYPE_TAB_DEPARTMENT_SHARE,
			TYPE_TAB_DEPARTMENT_REGULAR -> {
				viewModel.getCurrentCompany()
			}

			TYPE_TAB_COMPANY_ADD,
			TYPE_TAB_COMPANY_CREATE,
			TYPE_TAB_COMPANY_SHARE,
			TYPE_TAB_COMPANY_REGULAR -> {
				viewModel.getAllCompanies()
			}

			TYPE_TAB_GROUP,
			TYPE_TAB_REGULAR,
			TYPE_TAB_CONVERSATION -> {
			}

			else -> {}
		}
	}

	private fun handleArgument() {
		val bundle = arguments ?: Bundle()
		_screen = bundle.getString(KEY_SCREEN, "")

		with(binding) {
			when (ContactTab.fromType(_screen)) {
				TYPE_TAB_GROUP -> {
					swipeLayout.isEnabled = true
					_adapter.apply {
						showDescription = true
						viewType = ContactTabAdapter.TYPE_GROUP
					}
					_endLessListener.refreshPage()
					_page = 1
				}

				TYPE_TAB_DEPARTMENT_ADD -> {
					viewModel.checkDisable = true
					lnCheckAll.show()
					swipeLayout.isEnabled = false
					_adapter.check = true
				}

				TYPE_TAB_DEPARTMENT_CREATE -> {
					lnCheckAll.show()
					swipeLayout.isEnabled = false
					_adapter.check = true
				}

				TYPE_TAB_DEPARTMENT_REGULAR -> {
					viewModel.checkRegular = true
					viewModel.checkSelected = true

					swipeLayout.isEnabled = false
					_adapter.check = true
					lnCheckAll.show()
				}

				TYPE_TAB_DEPARTMENT_SHARE -> {
					viewModel.checkSelected = true

					lnCheckAll.show()
					swipeLayout.isEnabled = false
					_adapter.check = true
				}

				TYPE_TAB_COMPANY_ADD -> {
					viewModel.checkDisable = true

					_adapter.check = true
					swipeLayout.isEnabled = false
				}

				TYPE_TAB_COMPANY_CREATE -> {
					_adapter.check = true
					swipeLayout.isEnabled = false
				}

				TYPE_TAB_COMPANY_SHARE -> {
					viewModel.checkSelected = true

					_adapter.check = true
					swipeLayout.isEnabled = false
				}

				TYPE_TAB_COMPANY_REGULAR -> {
					viewModel.checkRegular = true
					viewModel.checkSelected = true

					_adapter.check = true
					swipeLayout.isEnabled = false
				}

				TYPE_TAB_REGULAR -> {
					swipeLayout.isEnabled = true
					_adapter.check = false

					_endLessListener.refreshPage()
					_page = 1
				}

				TYPE_TAB_CONVERSATION -> {
					swipeLayout.isEnabled = true
					_adapter.check = true
					lnCheckAll.gone()

					_endLessListener.refreshPage()
					_page = 1
				}

				else -> {}
			}
		}
	}
}