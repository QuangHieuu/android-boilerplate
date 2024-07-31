package boilerplate.ui.conversationSetting

import boilerplate.R
import boilerplate.base.BaseFragment
import boilerplate.databinding.DialogBaseBinding
import boilerplate.databinding.DialogLeaveGroupBinding
import boilerplate.databinding.FragmentConversationSettingBinding
import boilerplate.model.conversation.Conversation
import boilerplate.model.conversation.ConversationRole
import boilerplate.service.signalr.SignalRManager
import boilerplate.ui.conversationDetail.ConversationDetailFragment
import boilerplate.ui.conversationDetail.ConversationVM
import boilerplate.utils.extension.click
import boilerplate.utils.extension.findOwner
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.ifEmpty
import boilerplate.utils.extension.show
import boilerplate.utils.extension.showDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class ConversationSettingFragment :
    BaseFragment<FragmentConversationSettingBinding, ConversationVM>() {

    companion object {
        fun newInstance(): ConversationSettingFragment {
            return ConversationSettingFragment()
        }
    }

    override val viewModel: ConversationVM by viewModel(ownerProducer = {
        findOwner(ConversationDetailFragment::class.java.simpleName)
    })

    private var _leaveInSilent: Boolean = false
    private var _isChangeSetting: Boolean = false

    override fun initialize() {

    }

    override fun onSubscribeObserver() {
        with(viewModel) {
            conversation.observe(this@ConversationSettingFragment) {
                settingConversation(it)
            }
            checkMember.observe(this@ConversationSettingFragment) { check ->
                checkMemberApproved(check)
            }
        }
    }

    override fun registerEvent() {
        with(binding) {
            toolbarSetting.setNavigationOnClickListener { popFragment() }

            btnLeave.click { leaveGroup() }
            btnDelete.click { deleteConversation() }
            checkAllowChangeName.click { allowChangeName() }
            checkAllowPinMessage.click { allowChangePin() }
            swAllowApproved.click { allowApproved() }
            swAllowSendMessage.click { allowSendMessage() }
        }
    }

    override fun callApi() {
    }

    private fun settingConversation(con: Conversation) {
        val config = viewModel.config
        val currentUser = viewModel.user
        binding.apply {
            checkAllowChangeName.isChecked = con.isChangeInform
            checkAllowPinMessage.isChecked = con.isAllowPinMessage
            swAllowApproved.isChecked = con.isAllowApproved
            swAllowSendMessage.isChecked = con.isAllowSendMessage

            btnLeave.show(config.isAllowLeftGroup)

            for (user in con.conversationUsers) {
                if (user.user.id.equals(currentUser.id)) {
                    when (ConversationRole.fromType(user.getVaiTro())) {
                        ConversationRole.MAIN -> {
                            viewDisable.gone()
                            btnDisable.gone()
                            btnDelete.show()
                        }

                        ConversationRole.SUB -> {
                            viewDisable.gone()
                            btnDisable.gone()
                            btnDelete.gone()
                        }

                        ConversationRole.MEMBER, ConversationRole.ALLOW_MEMBER -> {
                            viewDisable.show()
                            btnDisable.show()
                            btnDelete.gone()
                        }
                    }
                    break
                }
            }
        }
    }

    private fun leaveGroup() {
        _leaveInSilent = false
        var isMain = false
        for (item in viewModel.conversation.value?.conversationUsers.ifEmpty()) {
            if (item.user.id.equals(viewModel.user.id)) {
                val role = item.getVaiTro()
                isMain = ConversationRole.fromType(role).type == ConversationRole.MAIN.type
                break
            }
        }
        showDialog(DialogLeaveGroupBinding.inflate(layoutInflater)) { vb, dialog ->
            with(vb) {
                tvTitle.setText(R.string.leave_group)
                tvDescription.setText(
                    if (isMain) R.string.warning_main_leave_conversation
                    else R.string.warning_member_leave_conversation
                )

                swSilent.isChecked = _leaveInSilent
                swSilent.setOnCheckedChangeListener { buttonView, isChecked ->
                    _leaveInSilent = isChecked
                }

                btnCancel.click { dialog.dismiss() }
                btnConfirm.apply {
                    setText(R.string.leave_group)
                    click {
                        viewModel.setLoading(true)
                        SignalRManager.leaveGroup(
                            viewModel.conversationId, /*_leaveInSilent*/
                            true
                        )
                    }
                }
            }
        }
    }

    private fun deleteConversation() {
        showDialog(DialogBaseBinding.inflate(layoutInflater)) { vb, dialog ->
            with(vb) {
                tvTitle.setText(R.string.dismiss_conversation)
                tvDescription.setText(R.string.warning_dismiss_conversation)
                btnConfirm.setText(R.string.dismiss)

                btnCancel.click { dialog.dismiss() }
                btnConfirm.click {
                    viewModel.setLoading(true)
                    SignalRManager.deleteGroup(viewModel.conversationId)
                }
            }
        }
    }

    private fun allowSendMessage() {
        viewModel.conversation.value?.let {
            it.isAllowSendMessage = !it.isAllowSendMessage
            SignalRManager.updateGroupSetting(it)
        }
    }

    private fun checkMemberApproved(hasMemberInApproving: Boolean?) {
        if (hasMemberInApproving == null) {
            binding.swAllowApproved.isChecked = true
            return
        }
        if (hasMemberInApproving) {
            binding.swAllowApproved.isChecked = true
            showDialog(DialogBaseBinding.inflate(layoutInflater)) { vb, dialog ->
                with(vb) {
                    tvTitle.setText(R.string.approve_member)
                    tvDescription.setText(R.string.warning_allow_approved_description)
                    btnConfirm.setText(R.string.close)

                    btnCancel.gone()

                    btnConfirm.click { dialog.dismiss() }
                }
            }
        } else {
            viewModel.conversation.value?.let {
                it.isAllowApproved = false
                SignalRManager.updateGroupSetting(it)
            }
        }
    }

    private fun allowApproved() {
        viewModel.conversation.value?.let {
            if (it.isAllowApproved) {
                viewModel.getMember()
            } else {
                it.isAllowApproved = true
                SignalRManager.updateGroupSetting(it)
            }
        }
    }

    private fun allowChangePin() {
        viewModel.conversation.value?.let {
            it.isAllowPinMessage = !it.isAllowPinMessage
            SignalRManager.updateGroupSetting(it)
        }
    }

    private fun allowChangeName() {
        viewModel.conversation.value?.let {
            it.isChangeInform = !it.isChangeInform
            SignalRManager.updateGroupSetting(it)
        }
    }
}