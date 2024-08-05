package boilerplate.ui.contactDetail

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import boilerplate.R
import boilerplate.base.BaseDialogFragment
import boilerplate.databinding.FragmentContactDetailBinding
import boilerplate.databinding.ItemContactDetailBinding
import boilerplate.model.user.User
import boilerplate.ui.main.MainVM
import boilerplate.utils.DateTimeUtil
import boilerplate.utils.extension.callPhone
import boilerplate.utils.extension.click
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.isTablet
import boilerplate.utils.extension.loadAvatar
import boilerplate.utils.extension.notNull
import boilerplate.utils.extension.openDialog
import boilerplate.utils.extension.sendEmail
import boilerplate.utils.extension.sendSms
import boilerplate.utils.extension.show
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ContactDetailFragment : BaseDialogFragment<FragmentContactDetailBinding, ContactEditVM>() {
    companion object {
        const val KEY_USER_ID = ""

        fun newInstance(id: String = ""): ContactDetailFragment {
            return Bundle().apply {
                putString(KEY_USER_ID, id)
            }.let { ContactDetailFragment().apply { arguments = it } }
        }
    }

    override val viewModel: ContactEditVM by viewModel()
    private val _activityVM: MainVM by activityViewModel()

    override fun initialize() {
        with(binding) {
            imgBack.apply {
                setImageResource(
                    if (context.isTablet()) R.drawable.ic_close
                    else R.drawable.ic_arrow_previous_white
                )
                click { handleBack() }
            }

            tvPhoneCall.click {
                viewModel.userDetail.value?.let { callPhone(it.phoneNumber) }
                handleBack()
            }
            tvSendSms.click {
                viewModel.userDetail.value?.let { sendSms(it.phoneNumber) }
                handleBack()
            }
            tvMessage.click {
                viewModel.userDetail.value?.let { user ->
                    _activityVM.postPersonConversation(user)
                    handleBack()
                }
            }
            btnEditInformation.click {
                openDialog(ContactEditFragment.newInstance())
            }
        }
    }

    override fun onSubscribeObserver() {
        with(viewModel) {
            userDetail.observe(this@ContactDetailFragment) { user ->
                handleUserDetail(user)
                _activityVM.user.postValue(user)
            }
        }
    }

    override fun registerEvent() {
    }

    override fun callApi() {
        arguments.notNull {
            val id = it.getString(KEY_USER_ID, "")
            viewModel.getUser(id)
        }
    }

    private fun handleUserDetail(user: User) {
        user.notNull {
            with(binding) {
                imgAvatar.loadAvatar(user.avatar)
                tvName.text = user.name
                tvDepartment.text = user.mainDepartment.name ?: ""

                if (user.id == viewModel.user.id) {
                    btnEditInformation.show()

                    tvPhoneCall.gone()
                    tvSendSms.gone()
                    tvMessage.gone()
                }

                lnInformation.apply {
                    removeAllViews()
                    addView(
                        initRowInform(
                            R.drawable.ic_birthday_blue,
                            R.string.birthday,
                            R.color.color_081C36,
                            DateTimeUtil.convertWithSuitableFormat(
                                user.dayOfBirth,
                                DateTimeUtil.FORMAT_NORMAL
                            )
                        )
                    )
                    addView(
                        initRowInform(
                            R.drawable.ic_double_quote,
                            R.string.state,
                            R.color.color_081C36,
                            user.mood
                        )
                    )
                    addView(
                        initRowInform(
                            R.drawable.ic_phone_blue,
                            R.string.phone_number,
                            R.color.color_1552DC,
                            user.phoneNumber
                        ) { callPhone(user.phoneNumber) }
                    )
                    addView(
                        initRowInform(
                            R.drawable.ic_phone_blue,
                            R.string.different_number,
                            R.color.color_1552DC,
                            user.diffPhoneNumber
                        ) { callPhone(user.diffPhoneNumber) }
                    )
                    addView(
                        initRowInform(
                            R.drawable.ic_email_blue,
                            R.string.email,
                            R.color.color_1552DC,
                            user.diffPhoneNumber
                        ) { sendEmail(user.email) }
                    )
                    addView(
                        initRowInform(
                            R.drawable.ic_company_blue,
                            R.string.company,
                            R.color.color_1552DC,
                            user.mainCompany.name
                        ) { sendEmail(user.email) }
                    )
                    addView(
                        initRowInform(
                            R.drawable.ic_company_blue,
                            R.string.department,
                            R.color.color_1552DC,
                            user.diffPhoneNumber
                        )
                    )
                }
            }
        }
    }

    private fun initRowInform(
        @DrawableRes imgId: Int,
        @StringRes field: Int,
        @ColorRes textColor: Int,
        content: String,
        listener: (() -> Unit)? = null
    ): View {
        val binding = ItemContactDetailBinding.inflate(
            layoutInflater,
            view as ViewGroup?,
            false
        )
        with(binding) {
            imgIcon.setImageResource(imgId)
            tvKey.setText(field)
            tvValue.apply {
                text = content
                setTextColor(ContextCompat.getColor(context, textColor))
                click { listener?.invoke() }
            }
        }
        return binding.root
    }
}