package boilerplate.ui.contact.tab

import android.os.Bundle
import boilerplate.base.BaseFragment
import boilerplate.databinding.FragmentContactTabBinding
import boilerplate.model.user.Department
import boilerplate.model.user.User
import boilerplate.ui.contact.ContactVM
import boilerplate.ui.contact.adapter.ContactTabAdapter
import boilerplate.ui.contact.listener.SimpleListener
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
				override fun onExpandDepartment(department: Department) {
					if (department.users.isNullOrEmpty()) {
					} else {
						_adapter.expandDepartment(department)
					}
				}

				override fun onOpenInform(user: User) {
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
			if (_screen == ContactTab.TYPE_TAB_DEPARTMENT.type) {
				contact.observe(this@ContactTabFragment) {
					_adapter.insertData(it)
				}
			}
		}
		with(_activityVM) {
			if (_screen == ContactTab.TYPE_TAB_DEPARTMENT.type) {
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
			ContactTab.TYPE_TAB_DEPARTMENT -> {
				viewModel.getCurrentCompany()
			}

			ContactTab.TYPE_TAB_COMPANY -> {}
			ContactTab.TYPE_TAB_GROUP -> {}
			else -> {

			}
		}
	}

}