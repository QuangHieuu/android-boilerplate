package boilerplate.ui.contact.tab

import android.os.Bundle
import boilerplate.base.BaseFragment
import boilerplate.databinding.FragmentContactTabBinding
import boilerplate.model.user.Department
import boilerplate.model.user.User
import boilerplate.ui.contact.ContactVM
import boilerplate.ui.contact.adapter.ContactTabAdapter
import boilerplate.ui.contact.listener.SimpleListener
import boilerplate.ui.empty.EmptyFragment
import boilerplate.utils.extension.notNull
import boilerplate.utils.extension.open
import org.koin.androidx.viewmodel.ext.android.viewModel

class ContactTabFragment : BaseFragment<FragmentContactTabBinding, ContactVM>() {
    companion object {
        const val KEY_SCREEN = "KEY_SCREEN"

        fun newInstance(screen: String): ContactTabFragment {
            val bundle = Bundle().apply { putString(KEY_SCREEN, screen) }
            return ContactTabFragment().apply { arguments = bundle }
        }
    }

    override val _viewModel: ContactVM by viewModel(ownerProducer = { requireParentFragment() })

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
                    open(split = true, fragment = EmptyFragment.newInstance())
                }

                override fun onChatWith(item: User) {

                }

                override fun onPhone(phoneNumber: String) {

                }
            })

            binding.recycleView.apply {
                adapter = _adapter
                itemAnimator = null
            }
        }
    }

    override fun onSubscribeObserver() {
        with(_viewModel) {
            if (_screen == ContactTab.TYPE_TAB_DEPARTMENT.type) {
                contact.observe(this@ContactTabFragment) {
                    _adapter.insertData(it)
                }
            }
        }
    }

    override fun registerEvent() {
    }

    override fun callApi() {
        when (ContactTab.fromType(_screen)) {
            ContactTab.TYPE_TAB_DEPARTMENT -> {
                _viewModel.getCurrentCompany()
            }

            ContactTab.TYPE_TAB_COMPANY -> {}
            ContactTab.TYPE_TAB_GROUP -> {}
            else -> {

            }
        }
    }

}