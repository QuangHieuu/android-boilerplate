package boilerplate.ui.contactDetail

import boilerplate.R
import boilerplate.base.BaseDialogFragment
import boilerplate.databinding.FragmentContactEditBinding
import boilerplate.model.user.User
import boilerplate.utils.extension.click
import boilerplate.utils.extension.findOwner
import boilerplate.utils.extension.isTablet
import boilerplate.utils.extension.loadImage
import boilerplate.utils.extension.notNull
import org.koin.androidx.viewmodel.ext.android.viewModel

class ContactEditFragment : BaseDialogFragment<FragmentContactEditBinding, ContactEditVM>() {
    companion object {
        fun newInstance(): ContactEditFragment {
            return ContactEditFragment()
        }
    }

    override val viewModel: ContactEditVM by viewModel(ownerProducer = {
        findOwner(ContactDetailFragment::class.java.simpleName)
    })

    override fun initialize() {
        with(binding) {
            toolbarEdit.apply {
                setNavigationIcon(
                    if (context.isTablet()) R.drawable.ic_close
                    else R.drawable.ic_arrow_previous_white
                )
                click { handleBack() }
            }

        }
    }

    override fun onSubscribeObserver() {
        with(viewModel) {
            userDetail.observe(this@ContactEditFragment) { user ->
                handleUserDetail(user)
            }
            updateSuccess.observe(this@ContactEditFragment) {
                if (it) {
                    handleBack()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.updateSuccess.postValue(null)
    }

    override fun registerEvent() {
        with(binding) {
            btnUpdate.click {
                viewModel.patchUser(
                    edtNumberPhone.editableText.toString(),
                    edtOtherNumberPhone.editableText.toString(),
                    edtStatus.editableText.toString()
                )
            }
            btnCancel.click { handleBack() }
        }
    }

    override fun callApi() {
    }

    private fun handleUserDetail(user: User) {
        user.notNull {
            with(binding) {
                imgAvatar.loadImage(user.avatar)
                tvName.text = user.name

                edtNumberPhone.setText(user.phoneNumber)
                edtOtherNumberPhone.setText(user.diffPhoneNumber)
                edtStatus.setText(user.mood)
            }
        }
    }
}