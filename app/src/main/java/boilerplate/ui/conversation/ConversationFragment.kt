package boilerplate.ui.conversation

import android.media.MediaPlayer
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import boilerplate.R
import boilerplate.base.BaseFragment
import boilerplate.constant.AccountManager
import boilerplate.databinding.DialogBaseBinding
import boilerplate.databinding.FragmentConversationBinding
import boilerplate.model.conversation.Conversation
import boilerplate.model.conversation.ConversationUser
import boilerplate.model.message.Message
import boilerplate.service.signalr.SignalRImpl
import boilerplate.service.signalr.SignalRManager
import boilerplate.service.signalr.SignalRResult
import boilerplate.ui.conversation.adapter.ConversationAdapter
import boilerplate.ui.conversationDetail.ConversationDetailFragment
import boilerplate.ui.main.MainVM
import boilerplate.utils.ClickUtil
import boilerplate.utils.extension.AnimateType
import boilerplate.utils.extension.click
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.hide
import boilerplate.utils.extension.isTablet
import boilerplate.utils.extension.launch
import boilerplate.utils.extension.open
import boilerplate.utils.extension.show
import boilerplate.utils.extension.showDialog
import boilerplate.utils.extension.showSnackBarFail
import boilerplate.utils.extension.showSnackBarSuccess
import boilerplate.widget.recyclerview.EndlessListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ConversationFragment : BaseFragment<FragmentConversationBinding, MainVM>() {
    companion object {
        fun newInstance() = ConversationFragment().apply {
        }
    }

    private lateinit var _adapter: ConversationAdapter
    private lateinit var _adapterPin: ConversationAdapter
    private lateinit var _endLessListener: EndlessListener
    private lateinit var _layoutManager: LinearLayoutManager

    private var _isOnReload = false
    private var _isOnTop = true
    private var _isLoadMore = false
    private var _currentSize = 0
    private var _isDisable = false

    private var _isDeleteConversation = false
    private val _isUnRead = false
    private val _isImportant = false
    private val _currentFilter = 0

    override val _viewModel: MainVM by viewModel()

    override fun initialize() {
        val simpleEvent = object : ConversationAdapter.SimpleEvent() {
            override fun onItemClick(con: Conversation) {
                _adapter.closeAll()
                open(
                    split = true,
                    fragment = ConversationDetailFragment.newInstance(con.getConversationId()),
                    animateType = if (context?.isTablet() == true) AnimateType.FADE
                    else AnimateType.SLIDE_TO_LEFT
                )
            }

            override fun onMarkAsImportant(conversation: Conversation) {

            }

            override fun onDelete(conversation: Conversation) {
                showDialog(DialogBaseBinding.inflate(layoutInflater)) { b, dialog ->
                    with(b) {
                        tvTitle.setText(R.string.delete_conversation_message)
                        btnConfirm.setText(R.string.delete)
                        tvDescription.setText(R.string.warning_confirm_delete_conversation)

                        btnCancel.click { dialog.dismiss() }

                        btnConfirm.click {
                            _isDeleteConversation = true
                            SignalRManager.disableConversation(conversation.conversationId)
                            dialog.dismiss()
                        }
                    }
                }
            }

            override fun onNotify(item: Conversation, isNotify: Boolean) {
                if (isNotify) {
                    SignalRManager.turnNotifyConversation(item.conversationId, false)
                } else {
                    showDialog(DialogBaseBinding.inflate(layoutInflater)) { view, dialog ->
                        with(view) {
                            tvTitle.setText(R.string.turn_off_notify)
                            tvDescription.setText(R.string.warning_dialog_turn_off_notify)
                            btnConfirm.setText(R.string.off)

                            btnConfirm.click {
                                _isDeleteConversation = true
                                SignalRManager.turnNotifyConversation(item.conversationId, true)
                                dialog.dismiss()
                            }
                        }
                    }
                }
            }
        }

        _adapterPin = ConversationAdapter(simpleEvent, true)
        _adapter = ConversationAdapter(simpleEvent)

        val divider = DividerItemDecoration(
            context,
            DividerItemDecoration.VERTICAL
        )
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.divider_1dp)
        if (drawable != null) {
            divider.setDrawable(drawable)
        }

        _layoutManager = binding.rcvMessage.layoutManager as LinearLayoutManager
        _endLessListener =
            object : EndlessListener(_layoutManager) {
                override fun onLoadMore(page: Int, totalItemsCount: Int) {
                    if (_currentSize > 0 && !_isLoadMore && !_isOnReload) {
                        _isLoadMore = true
                        val id: String = _adapter.lastConversationId
                        binding.rcvMessage.post {
                            _adapter.loadMore()
                            loadMore(id)
                        }
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    recyclerView.post {
                        _isOnTop = !recyclerView.canScrollVertically(-1) ||
                                _layoutManager.findFirstCompletelyVisibleItemPosition() == 0
                    }
                }
            }

        binding.rcvMessage.apply {
            adapter = ConcatAdapter(_adapterPin, _adapter)
            addItemDecoration(divider)
            setItemAnimator(null)
            setHasFixedSize(true)
            setItemViewCacheSize(0)

            addOnScrollListener(_endLessListener)
        }
    }

    override fun onSubscribeObserver() {
        with(_viewModel) {
            conversations.observe(this@ConversationFragment) { pair ->
                binding.root.launch {
                    if (pair.second.isEmpty() && pair.first.isEmpty()) {
                        disableLoading()
                        binding.viewNoData.lnNoData.show()
                        binding.rcvMessage.gone()
                        return@launch
                    }
                    addData(pair.second, pair.first.isNotEmpty())
                }
            }
            conversationUpdate.observe(this@ConversationFragment) {
                binding.root.launch {
                    val boolean: Boolean =
                        if (_adapterPin.updatePinConversation(it)) {
                            false
                        } else {
                            !_adapter.updateConversation(it)
                        }
                    if (boolean) {
                        _adapter.newConversation(it)
                    }
                }
            }
            pinConversations.observe(this@ConversationFragment) {
                binding.root.launch {
                    _adapterPin.insertData(it)
                }
            }
        }
        listenerToSignalR()
    }

    override fun registerEvent() {
        with(binding) {
            swipeLayout.setOnRefreshListener {
                _endLessListener.refreshPage()
                loadMore("")
            }
        }
    }

    override fun callApi() {
        loadMore("")
    }

    private fun loadMore(last: String) {
        _viewModel.apiGetConversation(last, _isUnRead, _isImportant, null)
    }

    private fun disableLoading() {
        if (_adapter.itemCount == 0) {
            binding.viewNoData.lnNoData.show()
        }
        _isLoadMore = false
        _isOnReload = false
        binding.swipeLayout.isRefreshing = false
    }

    private fun addData(items: ArrayList<Conversation>, loadMore: Boolean) {
        _currentSize = items.size
        _isLoadMore = false
        _isOnReload = false
        if (binding.swipeLayout.isRefreshing) {
            binding.swipeLayout.isRefreshing = false
        }
        if (loadMore) {
            _adapter.cancelLoadMore()
            _adapter.addMore(items)
        } else {
            if (!binding.rcvMessage.isShown()) {
                binding.viewNoData.lnNoData.gone()
                binding.rcvMessage.show()
            }
            _adapter.insertData(items)
        }
    }

    private fun newMessage(message: Message, isSelf: Boolean): Boolean {
        val isExistPin: Boolean = _adapterPin.newPinMessage(message, isSelf, true)
        if (!isExistPin) {
            val isExist: Boolean = _adapter.newMessage(message, isSelf, true)
            if (isExist && _isOnTop) {
                binding.rcvMessage.scrollToPosition(0)
            }
            return !isExist
        }
        return false
    }

    private fun listenerToSignalR() {
        SignalRManager.addController(this.javaClass.simpleName)
            .setListener(object : SignalRImpl() {
                override fun readyToChat(list: ArrayList<Conversation>) {
                    val listPin = java.util.ArrayList<Conversation>()
                    val listNormal = java.util.ArrayList<Conversation>()
                    for (conversation in list) {
                        if (conversation.getPinDate().isEmpty()) {
                            listNormal.add(conversation)
                        } else {
                            listPin.add(conversation)
                        }
                    }
                    for (conversation in listPin) {
                        _adapter.updateConversation(conversation)
                    }
                    val iterator: ListIterator<Conversation> = listNormal.listIterator()
                    while (iterator.hasNext()) {
                        val index = iterator.nextIndex()
                        val conversation = iterator.next()
                        _adapter.updateConversation(conversation, index)
                    }
                }

                override fun syncRead(conversationId: String) {
                    if (!_adapterPin.syncPinLastRead(conversationId)) {
                        _adapter.syncLastRead(conversationId, _isUnRead)
                    }
                }

                override fun deleteMessageConversation(conversationId: String) {
                    if (!_adapterPin.deletePinMessage(conversationId)) {
                        _adapter.deleteMessage(conversationId)
                    }
                }

                override fun createConversation(conversation: Conversation) {
                    if (_adapter.itemCount == 0) {
                        binding.rcvMessage.show()
                        binding.viewNoData.lnNoData.hide()
                    }

                    if (!_adapter.newPinConversation(conversation)) {
                        _adapter.newConversation(conversation)
                        lifecycleScope.launch {
                            delay(400)
                            if (_isOnTop) {
                                binding.rcvMessage.scrollToPosition(0)
                            }
                        }
                    }
                }

                override fun newMessage(message: Message) {
                    val userId = _viewModel.user.value?.id
                    val isPlaySound = AccountManager.isPlaySound()
                    if (message.personSend != null) {
                        for (user in message.receiverNotifies) {
                            if (user.receiverId.equals(userId) && !user.isOffNotify && isPlaySound) {
                                playAudio()
                            }
                        }
                        val isSelf: Boolean =
                            message.personSend.id.equals(AccountManager.getCurrentUserId())
                        if (newMessage(message, isSelf)) {
                            _viewModel.apiGetConversationDetail(message)
                        }
                    }
                }

                override fun sendMessage(sendMessage: Message.SendMessageResult) {
                    if ((sendMessage.status == 1 || sendMessage.status == 0) && sendMessage.entity != null) {
                        if (newMessage(sendMessage.entity, true)) {
                            _viewModel.apiGetConversationDetail(sendMessage.getEntity())
                        }
                    }
                }

                override fun updateConversation(conversation: Conversation) {
                    _viewModel.apiGetConversationDetail(conversation)
                }

                override fun leaveGroup(leaveGroup: ConversationUser.LeaveGroup) {
                    if (!_adapterPin.leavePinConversation(leaveGroup.conversationId)) {
                        _adapter.leaveConversation(leaveGroup.conversationId)
                    }
                }

                override fun deleteMember(leaveGroup: ConversationUser.LeaveGroup) {
                    if (leaveGroup.userId.equals(_viewModel.user.value?.id)) {
                        if (!_adapterPin.leavePinConversation(leaveGroup.conversationId)) {
                            _adapter.leaveConversation(leaveGroup.conversationId)
                        }
                    } else {
                        val boolean: Boolean =
                            if (_adapterPin.updatePinConversation(leaveGroup.conversation)) {
                                false
                            } else {
                                !_adapter.updateConversation(leaveGroup.conversation)
                            }
                        if (boolean) {
                            _viewModel.apiGetConversationDetail(leaveGroup.conversationId)
                        }
                    }
                }

                override fun deleteGroup(conversationId: String) {
                    if (!_adapterPin.removePinConversation(conversationId)) {
                        _adapter.removeConversation(conversationId)
                    }
                }

                override fun disableConversation(conversationId: String) {
                    if (conversationId != SignalRResult.ERROR.key && conversationId.isNotEmpty()) {
                        if (!_adapterPin.removePinConversation(conversationId)) {
                            _adapter.removeConversation(conversationId)
                        }
                        if (_isDeleteConversation) {
                            _isDeleteConversation = false
                            binding.root.showSnackBarSuccess(R.string.success_delete_conversation)
                        }
                    } else {
                        if (_isDeleteConversation) {
                            _isDeleteConversation = false
                            binding.root.showSnackBarFail(R.string.error_delete_conversation)
                        }
                    }
                }

                override fun onOffNotify(conversationId: String) {
                    var item: Boolean? = _adapterPin.onOffNotifyConversation(conversationId)
                    if (item == null) {
                        item = _adapter.onOffNotifyConversation(conversationId)
                    }
                    if (item != null) {
                        val success = if (item) R.string.turn_off_notify_success
                        else R.string.turn_on_notify_success
                        binding.root.showSnackBarSuccess(success)
                    }
                }

                override fun updateMessage(message: Message) {
                    if (!_adapterPin.newPinConversation(message, true, false)) {
                        _adapter.newMessage(message, true, false)
                    }
                }

                override fun importConversation(important: Conversation.Important) {
                    val conversation = important.conversation
                    val isImportant = important.isImportant

                    if (!_adapterPin.updatePinImportant(conversation.conversationId, isImportant)) {
                        _adapter.updateImportant(conversation.conversationId, isImportant)
                    }
                }

                override fun addMember(addMember: Conversation.AddMember) {
//                    _viewModel.apiGetConversationDetail(addMember.conversationId)
                }

                override fun pinConversation(pinConversation: Conversation.Pin) {
                    val conversation: Conversation = pinConversation.conversation
                    if (pinConversation.isPin) {
                        _adapterPin.insertPin(conversation)
                        _adapter.removeConversation(conversation)
                    } else {
                        _adapterPin.removeConversation(pinConversation.conversation)
                        _adapter.reInsertPinAfterRemove(conversation)
                    }
                }
            })
    }

    private fun playAudio() {
        val player: MediaPlayer = MediaPlayer.create(context, R.raw.sound_message_receiver)
        player.start()
        player.setOnCompletionListener { mp: MediaPlayer ->
            mp.stop()
            mp.reset()
            mp.release()
        }
    }
}