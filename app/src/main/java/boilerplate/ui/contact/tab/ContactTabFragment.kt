package boilerplate.ui.contact.tab

import android.os.Bundle
import android.util.Log
import boilerplate.base.BaseFragment
import boilerplate.databinding.FragmentContactTabBinding
import boilerplate.model.user.Company
import boilerplate.model.user.Department
import boilerplate.model.user.User
import boilerplate.ui.contact.adapter.ContactTabAdapter
import boilerplate.ui.contact.listener.SimpleListener
import boilerplate.ui.contact.tab.ContactTab.TYPE_TAB_COMPANY
import boilerplate.ui.contact.tab.ContactTab.TYPE_TAB_DEPARTMENT
import boilerplate.ui.contact.tab.ContactTab.TYPE_TAB_GROUP
import boilerplate.ui.contact.viewModel.ContactVM
import boilerplate.ui.contactDetail.ContactDetailFragment
import boilerplate.ui.main.MainVM
import boilerplate.utils.extension.callPhone
import boilerplate.utils.extension.notNull
import boilerplate.utils.extension.openDialog
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ContactTabFragment : BaseFragment<FragmentContactTabBinding, ContactVM>() {
	companion object {
		const val KEY_SCREEN = "KEY_SCREEN"

		fun newInstance(screen: String): ContactTabFragment {
			val bundle = Bundle().apply { putString(KEY_SCREEN, screen) }
			return ContactTabFragment().apply { arguments = bundle }
		}
	}

	override val viewModel: ContactVM by viewModel(ownerProducer = { requireParentFragment() })
	private val _activityVM: MainVM by activityViewModel()

	private lateinit var _screen: String
	private lateinit var _adapter: ContactTabAdapter

	override fun initialize() {
		arguments.notNull {
			_screen = it.getString(KEY_SCREEN) ?: ""

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

				override fun onOpenInform(user: User) {
					Log.d("SSS", "onOpenInform: ")
					openDialog(ContactDetailFragment.newInstance(user.id))
				}

				override fun onChatWith(item: User) {
					_activityVM.postPersonConversation(item)
				}

				override fun onPhone(phoneNumber: String) {
					callPhone(phoneNumber)
				}
			})

			binding.recycleView.apply {
				adapter = _adapter
				itemAnimator = null
			}
		}
	}

	override fun onSubscribeObserver() {
		with(viewModel) {
			when (ContactTab.fromType(_screen)) {
				TYPE_TAB_DEPARTMENT -> {
					department.observe(this@ContactTabFragment) {
						_adapter.insertData(it)
					}
					putEditGroup.observe(this@ContactTabFragment) {
						_adapter.updateRegularGroup(it)
					}
				}

				TYPE_TAB_COMPANY -> {
					company.observe(this@ContactTabFragment) {
						_adapter.insertData(ArrayList(it))
					}
				}

				else -> {}
			}
			expandDepartment.observe(this@ContactTabFragment) {
				_adapter.expandDepartment(it)
			}
			expandCompany.observe(this@ContactTabFragment) {
				_adapter.expandCompany(it)
			}
		}
		with(_activityVM) {
			if (_screen == TYPE_TAB_DEPARTMENT.type) {
				user.observe(this@ContactTabFragment) {
					_adapter.updateContact(it)
				}
			}
		}
	}

	override fun registerEvent() {
		with(binding) {
			swipeLayout.setOnRefreshListener {
				swipeLayout.isRefreshing = false
				callApi()
			}
		}
	}

	override fun callApi() {
		when (ContactTab.fromType(_screen)) {
			TYPE_TAB_DEPARTMENT -> {
				viewModel.getCurrentCompany()
			}

			TYPE_TAB_COMPANY -> {
				viewModel.getAllCompanies()
			}

			TYPE_TAB_GROUP -> {}
			else -> {

			}
		}
	}

}