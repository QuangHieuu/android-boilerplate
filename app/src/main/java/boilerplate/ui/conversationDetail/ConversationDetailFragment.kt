package boilerplate.ui.conversationDetail

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import boilerplate.R
import boilerplate.base.BaseFragment
import boilerplate.databinding.FragmentConversationDetailBinding
import boilerplate.model.conversation.Conversation
import boilerplate.model.conversation.ConversationRole
import boilerplate.model.conversation.ConversationRole.ALLOW_MEMBER
import boilerplate.model.conversation.ConversationRole.MAIN
import boilerplate.model.conversation.ConversationRole.MEMBER
import boilerplate.model.conversation.ConversationRole.SUB
import boilerplate.model.conversation.ConversationUser
import boilerplate.model.message.Message
import boilerplate.service.signalr.SignalRManager
import boilerplate.service.signalr.SignalRResult
import boilerplate.ui.conversationDetail.adpater.MessageAdapter
import boilerplate.ui.conversationDetail.adpater.SimpleMessageEvent
import boilerplate.utils.extension.click
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.isTablet
import boilerplate.utils.extension.notNull
import boilerplate.utils.extension.sendResult
import boilerplate.utils.extension.show
import boilerplate.widget.recyclerview.EndlessListener
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Date
import kotlin.math.max

class ConversationDetailFragment :
    BaseFragment<FragmentConversationDetailBinding, ConversationVM>() {
    companion object {
        private const val KEY_CONVERSATION: String = "KEY_CONVERSATION"
        private const val KEY_MESSAGE: String = "KEY_MESSAGE"
        private const val KEY_MESSAGE_ID: String = "KEY_MESSAGE_ID"

        fun newInstance(
            conversationId: String,
            content: String? = null,
            messageId: String? = null
        ): ConversationDetailFragment {
            return Bundle().let { bundle ->
                bundle.putString(KEY_CONVERSATION, conversationId)
                content.notNull { bundle.putString(KEY_MESSAGE, it) }
                messageId.notNull { bundle.putString(KEY_MESSAGE_ID, it) }
                ConversationDetailFragment().apply { arguments = bundle }
            }
        }
    }

    override val _viewModel: ConversationVM by viewModel()

    private lateinit var _layoutManager: LinearLayoutManager
    private lateinit var _endlessListener: EndlessListener
    private lateinit var _smoothScroller: SmoothScroller

    private val _adapter = MessageAdapter(object : SimpleMessageEvent() {

    })

    private var _isOnTop = true
    private var _previousCount = 0
    private var _currentCount = 0
    private var _isLoadMore = false
    private var _isGotoMessage = false

    override fun initialize() {
        with(binding) {
            imgBack.apply { if (requireActivity().isTablet()) gone() else show() }
            _smoothScroller = object : LinearSmoothScroller(context) {
                override fun getVerticalSnapPreference(): Int = SNAP_TO_END
            }

            _layoutManager = rcvMessage.layoutManager as LinearLayoutManager
            _endlessListener = object : EndlessListener(_layoutManager) {
                override fun onLoadMore(page: Int, totalItemsCount: Int) {
                    if (_currentCount == _viewModel.limit && !_isLoadMore) {
                        _isLoadMore = true
                        val id: String = _adapter.getLastMessageId()
                        run {
                            _adapter.loadMoreNext()
                            _viewModel.apiGetMoreMessage(id)
                        }
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    _isOnTop = !recyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        if (_isOnTop && !_isGotoMessage) {
                            syncReadMessage()
                            tvCountNewMessage.gone()
                        }
                    }
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        _adapter.removeFocus()
                    }
                    if (_isOnTop && !_adapter.checkIsLoadPrevious() && !_isLoadMore && _previousCount == _viewModel.limit) {
                        _isLoadMore = true
                        val id: String = _adapter.getFirstMessageId()
                        binding.rcvMessage.post {
                            _adapter.loadMorePre()
                            snapScrollPosition(0)
                            _viewModel.apiGetPreviousMessage(id)
                        }
                    }
                }
            }

            val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
            val drawable =
                ContextCompat.getDrawable(requireContext(), R.drawable.divider_transpanrent_10dp)
            if (drawable != null) {
                divider.setDrawable(drawable)
            }
            rcvMessage.apply {
                adapter = _adapter
                addItemDecoration(divider)
                setItemAnimator(null)
                addOnScrollListener(_endlessListener)
            }
        }
    }

    override fun onSubscribeObserver() {
        with(_viewModel) {
            messageError.observe(this@ConversationDetailFragment) {
                _isLoadMore = false
                _adapter.cancelLoadMore()
            }
            conversationError.observe(this@ConversationDetailFragment) {
                if (_adapter.itemCount == 0) {
                    binding.apply { rcvMessage.gone() }
                        .viewNoData.apply {
                            lnNoData.show()
                            tvNoData.setText(R.string.error_general)
                        }
                }
            }

            conversation.observe(this@ConversationDetailFragment) {
                binding.apply { rcvMessage.show() }
                    .viewNoData.apply { lnNoData.gone() }

                SignalRManager.sendLastTimeRead(
                    it.conversationId,
                    Date(),
                    it.totalMessage,
                    true
                )

                var isInGroup = false
                for (user in it.conversationUsers) {
                    if (user.user.id.equals(_viewModel.user.id)) {
                        isInGroup = true
                        break
                    }
                }
                if (isInGroup) {
//                        mView.hideErrorConnect()
//                        mView.pinMessageToScreen(conversation.getPinMessage())
                    setupChatBox(it)
                } else {
                    val leaveGroup: ConversationUser.LeaveGroup = ConversationUser.LeaveGroup(
                        conversationId,
                        _viewModel.user.id
                    )
                    context?.sendResult(
                        SignalRResult.UPDATE_CONVERSATION_DELETE_MEMBER,
                        Gson().toJson(leaveGroup)
                    )
                }
            }
            messages.observe(this@ConversationDetailFragment) { pair ->
                if (lastMessage.isEmpty()) {
                    addMessage(pair.first, pair.second)
                } else {
                    insertLoadMore(pair.first, pair.second)
                }
            }
        }
    }

    override fun registerEvent() {
        with(binding) {
            imgBack.click { popFragment() }
        }
    }

    override fun callApi() {
        handleArgument()
    }


    private fun handleArgument() {
        arguments.notNull {
            val messageId = it.getString(KEY_MESSAGE_ID, "")
            val conversationId = it.getString(KEY_CONVERSATION, "")
            _viewModel.apiGetDetail(conversationId, messageId)
            val content = it.getString(KEY_MESSAGE, "")
            if (content.isNotEmpty()) {

            }

        }
    }

    private fun setupChatBox(con: Conversation) {
        with(binding) {
            val builder: StringBuilder = StringBuilder(con.conversationName)
            val isGroup: Boolean = con.isGroup()
            val size: Int = con.tongSoNhanVien
            val isOneUser = size == 1

            _adapter.setIsGroup(isGroup)

            if (con.isMyCloud) {
                chatBox.enableChat(true)
                tvTitle.setText(R.string.my_cloud)
                tvSubTitle.gone()
                _viewModel.userReadMessage = con.totalMessage
                return
            }
            if (con.isGroup()) {
                chatBox.addMentions(con.conversationUsers)
                var isAllowSend: Boolean = con.isAllowSendMessage

                tvSubTitle.text = getString(
                    R.string.member_count,
                    con.conversationUsers.size
                )

                var needCreateName = builder.toString().isEmpty()
                if (!needCreateName) {
                    tvTitle.text = builder
                }
                if (isGroup && builder.toString().isEmpty() && isOneUser) {
                    builder.append(getString(R.string.conversation_name))
                }
                var countName = 0
                for (user in con.getConversationUsers()) {
                    user.let {
                        if (!it.isOutGroup && !(it.user.id.equals(con.getCreatorId()) && size > 3)) {
                            if (builder.toString().isNotEmpty() && needCreateName) {
                                builder.append(", ")
                            }
                            if (countName > 2 && needCreateName) {
                                needCreateName = false
                                builder.append("…")
                            }
                            if (needCreateName) {
                                countName += 1
                                builder.append(it.user.name)
                            }
                        }
                        if (it.user.id.equals(_viewModel.user.id)) {
                            _viewModel.unreadMessage = it.readNumber
                            if (!isAllowSend) {
                                isAllowSend = when (ConversationRole.fromType(it.getVaiTro())) {
                                    MAIN, SUB, ALLOW_MEMBER -> true
                                    MEMBER -> user.isAllowSendMessage
                                }
                            }
                        }
                    }
                }
                _adapter.setAnswerable(isAllowSend)
                chatBox.enableChat(isAllowSend)
                tvTitle.text = builder
            } else {
                var onlyContainMe = true
                _adapter.setAnswerable(true)
                chatBox.enableChat(true)
                for (user in con.conversationUsers) {
                    if (!user.user.id.equals(_viewModel.user.id)) {
                        user.let {
                            _viewModel.unreadMessage = it.readNumber
                            _viewModel.otherReadMessage = it.readNumber

                            it.user.apply {
                                tvTitle.text = name
                                tvSubTitle.text = mainDepartment?.name
                            }
                        }
                        onlyContainMe = false
                    }
                }
                if (onlyContainMe) {
                    tvTitle.text = _viewModel.user.name
                    tvSubTitle.gone()
                    _viewModel.unreadMessage = con.totalMessage
                }
            }
        }
    }

    private fun addMessage(size: Int, list: Map<String, List<Message>>) {
        _previousCount = 0
        _isGotoMessage = false
        _currentCount = size
        val arrayList = ArrayList<Any>()
        for ((key, value) in list) {
            arrayList.addAll(value)
            arrayList.add(key)
        }
        _adapter.insertData(arrayList)
        _layoutManager.scrollToPositionWithOffset(0, 0)
    }

    private fun insertLoadMore(size: Int, list: Map<String, List<Message>>) {
        _isLoadMore = false
        _currentCount = max(size, _viewModel.limit)
        val arrayList = ArrayList<Any>()
        for ((key, value) in list) {
            arrayList.addAll(value)
            arrayList.add(key)
        }
        _adapter.loadMoreData(arrayList)
    }

    private fun syncReadMessage() {
        with(_viewModel) {
            if (conversation.value != null && (countNewMessage != 0 || conversation.value!!.totalMessage > userReadMessage)) {
                userReadMessage = conversation.value!!.totalMessage
                SignalRManager.sendLastTimeRead(
                    conversationId,
                    Date(),
                    userReadMessage,
                    true
                )
                countNewMessage = 0
            }
        }
    }

    private fun snapScrollPosition(pos: Int) {
        _smoothScroller.targetPosition = pos
        _layoutManager.startSmoothScroll(_smoothScroller)
    }
}