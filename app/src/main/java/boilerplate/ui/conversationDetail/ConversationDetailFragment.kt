package boilerplate.ui.conversationDetail

import android.graphics.Rect
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import boilerplate.R
import boilerplate.base.BaseFragment
import boilerplate.databinding.DialogBaseBinding
import boilerplate.databinding.FragmentConversationDetailBinding
import boilerplate.databinding.ItemPinOnScreenBinding
import boilerplate.databinding.PopupMessagePinBinding
import boilerplate.model.conversation.Conversation
import boilerplate.model.conversation.ConversationRole
import boilerplate.model.conversation.ConversationRole.ALLOW_MEMBER
import boilerplate.model.conversation.ConversationRole.MAIN
import boilerplate.model.conversation.ConversationRole.MEMBER
import boilerplate.model.conversation.ConversationRole.SUB
import boilerplate.model.conversation.ConversationUser
import boilerplate.model.file.AttachedFile
import boilerplate.model.message.Message
import boilerplate.service.signalr.SignalRImpl
import boilerplate.service.signalr.SignalRManager
import boilerplate.service.signalr.SignalRResult
import boilerplate.ui.conversationDetail.adpater.MessageAdapter
import boilerplate.ui.conversationDetail.adpater.SimpleMessageEvent
import boilerplate.ui.conversationInform.ConversationInformFragment
import boilerplate.utils.StringUtil
import boilerplate.utils.SystemUtil
import boilerplate.utils.extension.ANIMATION_DELAY
import boilerplate.utils.extension.addFile
import boilerplate.utils.extension.click
import boilerplate.utils.extension.dimBehind
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.isTablet
import boilerplate.utils.extension.isVisible
import boilerplate.utils.extension.launch
import boilerplate.utils.extension.notNull
import boilerplate.utils.extension.open
import boilerplate.utils.extension.sendResult
import boilerplate.utils.extension.show
import boilerplate.utils.extension.showDialog
import boilerplate.utils.extension.showKeyboard
import boilerplate.utils.extension.showSnackBarFail
import boilerplate.utils.extension.showSnackBarSuccess
import boilerplate.utils.extension.showSnackBarWarning
import boilerplate.widget.chatBox.SimpleBoxListener
import boilerplate.widget.recyclerview.EndlessListener
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Date
import java.util.Locale
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

    override val viewModel: ConversationVM by viewModel()

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
    private var _isLeaveGroup = false


    private var _pointClickX = 0
    private var _pointClickY = 0

    override fun initialize() {
        with(binding) {
            imgBack.apply { if (requireActivity().isTablet()) gone() else show() }
            chatBox.setupForChat()
            chatBox.setListener(object : SimpleBoxListener() {
                override fun onSendMessage(
                    content: String,
                    uploadFile: ArrayList<AttachedFile.Conversation>,
                    currentFile: ArrayList<AttachedFile.Conversation>,
                    surveyFile: ArrayList<AttachedFile.SurveyFile>,
                    isSms: Boolean,
                    isEmail: Boolean
                ) {
                    viewModel.sendMessage(
                        content,
                        uploadFile,
                        currentFile,
                        surveyFile,
                        isSms,
                        isEmail
                    )
                }

                override fun onEditMessage(
                    lastMessage: Message,
                    content: String,
                    uploadFile: ArrayList<AttachedFile.Conversation>,
                    currentFile: ArrayList<AttachedFile.Conversation>,
                    surveyFile: ArrayList<AttachedFile.SurveyFile>,
                    isSms: Boolean,
                    isEmail: Boolean
                ) {

                }

                override fun onAttachedClick() {
                }

                override fun onCameraClick() {
                }

                override fun onPickImageClick() {
                }

                override fun onEditFocus(focus: Boolean) {
                }

                override fun onOpenRecord() {
                }

                override fun onStartRecord(modeSpeech: Boolean) {
                }
            })
            _smoothScroller = object : LinearSmoothScroller(context) {
                override fun getVerticalSnapPreference(): Int = SNAP_TO_END
            }

            _layoutManager = rcvMessage.layoutManager as LinearLayoutManager
            _endlessListener = object : EndlessListener(_layoutManager) {
                override fun onLoadMore(page: Int, totalItemsCount: Int) {
                    if (_currentCount == viewModel.limit && !_isLoadMore) {
                        _isLoadMore = true
                        val id: String = _adapter.getLastMessageId()
                        run {
                            _adapter.loadMoreNext()
                            viewModel.apiGetMoreMessage(id)
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
                    if (_isOnTop && !_adapter.checkIsLoadPrevious() && !_isLoadMore && _previousCount == viewModel.limit) {
                        _isLoadMore = true
                        val id: String = _adapter.getFirstMessageId()
                        binding.rcvMessage.post {
                            _adapter.loadMorePre()
                            snapScrollPosition(0)
                            viewModel.apiGetPreviousMessage(id)
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
                addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                    if (bottom < oldBottom && chatBox.getEditMessage().isFocused && _isOnTop && !_isGotoMessage) {
                        snapScrollPosition(0)
                        tvCountNewMessage.gone()
                    }
                }
            }
            chatBox.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                if (top < oldTop && !chatBox.getEditMessage().isFocused && _isOnTop && !_isGotoMessage) {
                    snapScrollPosition(0)
                }
            }
            tvCountNewMessage.click {
                _isGotoMessage = false
                syncReadMessage()
                if (_previousCount == viewModel.limit) {
                    viewModel.apiGetMoreMessage("")
                } else {
                    snapScrollPosition(0)
                }
                it.gone()
            }

            btnPinMessage.click {
                createPinExpand().apply {
                    showAsDropDown(ctlToolbar, 0, 0, Gravity.NO_GRAVITY)
                    dimBehind()
                }
            }
            imgMenu.click {
                open(split = true, fragment = ConversationInformFragment.newInstance())
            }
        }
    }

    override fun onSubscribeObserver() {
        with(viewModel) {
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
            pinMessage.observe(this@ConversationDetailFragment) { pin ->
                handlePinMessage(pin)
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
                    if (user.user.id.equals(viewModel.user.id)) {
                        isInGroup = true
                        break
                    }
                }
                if (isInGroup) {
                    setupChatBox(it)
                } else {
                    val leaveGroup: ConversationUser.LeaveGroup = ConversationUser.LeaveGroup(
                        conversationId,
                        viewModel.user.id
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
            receive.observe(this@ConversationDetailFragment) { count ->
                if (count > 0 && !_isOnTop) {
                    binding.tvCountNewMessage.apply {
                        text = getString(R.string.new_message_count, count)
                        show()
                    }
                } else {
                    binding.tvCountNewMessage.gone()
                }
            }
        }
    }

    override fun registerEvent() {
        with(binding) {
            imgBack.click { popFragment() }
        }
        listenerToSignalR()
    }

    override fun callApi() {
        handleArgument()
    }

    fun touchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                _pointClickX = ev.rawX.toInt()
                _pointClickY = ev.rawY.toInt()
            }

            MotionEvent.ACTION_UP -> {
                val parent = binding.root as ViewGroup
                val editBox = binding.chatBox

                val rect = Rect()
                val location = IntArray(2)
                parent.getLocationOnScreen(location)
                rect.set(
                    location[0],
                    location[1],
                    location[0] + parent.measuredWidth,
                    location[1] + parent.measuredHeight
                )
                if (rect.contains(_pointClickX, _pointClickY)) {
                    val editRect = Rect()
                    val editLocation = IntArray(2)

                    editBox.getLocationOnScreen(editLocation)
                    editRect.set(
                        editLocation[0],
                        editLocation[1],
                        editLocation[0] + editBox.measuredWidth,
                        editLocation[1] + editBox.measuredHeight
                    )
                    if (editRect.contains(_pointClickX, _pointClickY)) {
                        editBox.getEditMessage().showKeyboard()
                    } else {
                        return false
                    }
                } else {
                    return false
                }
            }
        }
        return true
    }

    private fun handleArgument() {
        arguments.notNull {
            val messageId = it.getString(KEY_MESSAGE_ID, "")
            val conversationId = it.getString(KEY_CONVERSATION, "")
            viewModel.apiGetDetail(conversationId, messageId)
            val content = it.getString(KEY_MESSAGE, "")
            if (content.isNotEmpty()) {
                binding.chatBox.setContentKeyboard(content)
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
                viewModel.hasRead = con.totalMessage
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
                        if (it.user.id.equals(viewModel.user.id)) {
                            viewModel.hasRead = it.readNumber
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
                    if (!user.user.id.equals(viewModel.user.id)) {
                        user.let {
                            viewModel.hasRead = it.readNumber
                            viewModel.otherRead = it.readNumber

                            it.user.apply {
                                tvTitle.text = name
                                tvSubTitle.text = mainDepartment?.name
                            }
                        }
                        onlyContainMe = false
                    }
                }
                if (onlyContainMe) {
                    tvTitle.text = viewModel.user.name
                    tvSubTitle.gone()
                    viewModel.hasRead = con.totalMessage
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
        _currentCount = max(size, viewModel.limit)
        val arrayList = ArrayList<Any>()
        for ((key, value) in list) {
            arrayList.addAll(value)
            arrayList.add(key)
        }
        _adapter.loadMoreData(arrayList)
    }

    private fun syncReadMessage() {
        with(viewModel) {
            if (conversation.value != null && (conversation.value!!.totalMessage > hasRead)) {
                hasRead = conversation.value!!.totalMessage
                SignalRManager.sendLastTimeRead(conversationId, Date(), hasRead, true)
                receive.postValue(0)
            }
        }
    }

    private fun snapScrollPosition(pos: Int) {
        _smoothScroller.targetPosition = pos
        _layoutManager.startSmoothScroll(_smoothScroller)
    }

    private fun showCountMessage() {
        viewModel.receive.let { it.postValue(it.value?.plus(1) ?: 0) }
    }

    private fun listenerToSignalR() {
        SignalRManager.addController(this.javaClass.simpleName)
            .setListener(object : SignalRImpl() {
                override fun newMessage(message: Message) {
                    checkCurrentConversation(message.conversationId) {
                        viewModel.conversation.value?.totalMessage =
                            message.conversation.totalMessage
                        if (_isGotoMessage) {
                            showCountMessage()
                        } else {
                            _adapter.newMessage(message)
                            if (_isOnTop) {
                                snapScrollPosition(0)
                                syncReadMessage()
                            } else {
                                showCountMessage()
                            }
                        }
                    }
                }

                override fun sendMessage(sendMessage: Message.SendMessageResult) {
                    sendMessage.entity.notNull {
                        checkCurrentConversation(it.conversationId) {
                            if (it.status == 1 || it.status == 0) {
                                if (!_isGotoMessage) {
                                    _adapter.newMessage(it)

                                    binding.root.launch {
                                        snapScrollPosition(0)
                                        showCountMessage()
                                        syncReadMessage()
                                    }
                                }
                                binding.chatBox.finishSendingMessage()
                            } else {
                                binding.chatBox.finishSendWhenError()
                                binding.root.showSnackBarFail(R.string.error_general)
                            }
                        }
                    }
                }

                override fun sendMessageError(string: String) {
                    with(binding) {
                        root.showSnackBarFail(string.ifEmpty { getString(R.string.error_general) })
                        chatBox.finishSendWhenError()
                    }
                }

                override fun deleteMessage(list: ArrayList<String>) {
                    for (string in list) {
                        _adapter.removeMessage(id = string)
                    }
                }

                override fun updateMessage(message: Message) {
                    checkCurrentConversation(message.conversationId) {
                        _adapter.updateMessage(message)
                    }
                }

                override fun pinMessage(pinMessage: Message.Pin) {
                    checkCurrentConversation(pinMessage.conversationId) {

                    }
                }

                override fun removePinMessage(pinMessage: Message.Pin) {
                    checkCurrentConversation(pinMessage.conversationId) {

                    }
                }

                override fun newReaction(message: Message) {
                    checkCurrentConversation(message.conversationId) {
                        _adapter.updateReaction(message)
                    }
                }

                override fun deleteGroup(conversationId: String) {
                    checkCurrentConversation(conversationId) {
                        binding.root.showSnackBarSuccess(R.string.success_delete_group)
                    }
                }

                override fun updateConversation(conversation: Conversation) {
                    checkCurrentConversation(conversation.conversationId) {
                        viewModel.conversation.postValue(conversation)
                    }
                }

                override fun updateConversationSetting(setting: Conversation.Setting) {
                    checkCurrentConversation(setting.conversationId) {
                        viewModel.conversation.value?.apply {
                            isChangeInform = setting.isChangeInform
                            isAllowPinMessage = setting.isAllowPinMessage
                            isAllowApproved = setting.isAllowApproved
                            isAllowSendMessage = setting.isAllowSendMessage
                        }.let { viewModel.conversation.postValue(it) }
                    }
                }

                override fun updateRole(updateRole: ConversationUser.UpdateRole) {
                    checkCurrentConversation(updateRole.conversationId) {
                        if (updateRole.userId == viewModel.user.id) {
                            viewModel.conversation.value?.apply {
                                for (member in conversationUsers) {
                                    if (member.user.id == viewModel.user.id) {
                                        member.setVaiTro(updateRole.role)
                                        break
                                    }
                                }
                            }.let { viewModel.conversation.postValue(it) }
                        }
                    }
                }

                override fun addMember(addMember: Conversation.AddMember) {
                    checkCurrentConversation(addMember.conversationId) {
                        viewModel.conversation.value?.apply {
                            conversationUsers.addAll(addMember.addMember)
                        }.let { viewModel.conversation.postValue(it) }
                    }
                }

                override fun deleteMember(leaveGroup: ConversationUser.LeaveGroup) {
                    checkCurrentConversation(leaveGroup.conversationId) {
                        if (viewModel.user.id == leaveGroup.conversationId) {
                            _isLeaveGroup = true
                            binding.root.showSnackBarWarning(R.string.warning_remove_out_of_group)
                            popFragment()
                        } else {
                            viewModel.conversation.postValue(leaveGroup.conversation)
                        }
                    }
                }

                override fun seenMessage(seenMessage: Conversation.SeenMessage) {

                }
            })
    }

    private fun checkCurrentConversation(id: String, block: () -> Unit) {
        if (viewModel.conversationId == id) {
            block()
        }
    }

    private fun handlePinMessage(pin: ArrayList<Message>) {
        with(binding) {
            ctlPinMessage.apply {
                show(pin.isNotEmpty())
                if (isVisible && pin.isNotEmpty()) {
                    lnContainerPin.removeAllViews()
                    lnContainerPin.addView(
                        addViewPinMessage(
                            pin[0],
                            false,
                            null
                        )
                    )
                    btnPinMessage.text = if (pin.size == 1) {
                        String.format("1")
                    } else {
                        String.format(Locale.getDefault(), "+%d", pin.size + 1)
                    }
                }
            }
        }

    }

    private fun addViewPinMessage(
        pin: Message,
        isShowFile: Boolean,
        popupWindow: PopupWindow?
    ): View {
        val size: Float = SystemUtil.getFontSizeChat(requireActivity())
        return ItemPinOnScreenBinding.inflate(
            layoutInflater,
            binding.root as ViewGroup,
            false
        ).apply {
            ctlPin.apply {
                if (isShowFile) {
                    setBackgroundResource(R.drawable.bg_border_only_top)
                    imgRemovePin.apply {
                        show()
                        click {
                            showDialog(DialogBaseBinding.inflate(layoutInflater)) { view, dialog ->
                                with(view) {
                                    tvTitle.setText(R.string.remove_pin)
                                    tvDescription.setText(R.string.warning_remove_pin)
                                    btnConfirm.setText(R.string.remove_pin)

                                    btnConfirm.click {
                                        viewModel.removePinMessage(pin.messageId)
                                        dialog.dismiss()
                                    }
                                }
                            }
                        }
                    }
                }
            }
            pickerPin.removeAllViews()
            val newList = arrayListOf<AttachedFile.Conversation>()
            if (pin.attachedFiles.isNotEmpty()) {
                newList.add(pin.attachedFiles[0])
                pickerPin.apply {
                    show()
                    addFile(pin.attachedFiles[0], sizeText = size) {
                        root.performClick()
                    }
                }
            }

            if (pin.surveyFiles.isNotEmpty()) {
                pickerPin.apply {
                    show()
                }
            } else {
                if (newList.isEmpty()) {
                    StringUtil.getHtml(pin.mainContent[0]).toString().let { string ->
                        tvPinContent.apply {
                            if (string.isEmpty()) {
                                show()
                                setText(R.string.forward_message)
                            } else {
                                text = string
                                show(!pickerPin.isVisible() || isShowFile)
                            }
                        }
                    }
                } else {
                    tvPinContent.gone()
                }
            }

            tvPinContent.textSize = size
            tvFromPerson.apply {
                textSize = size
                text = context.getString(R.string.send_from, pin.personPin.name)
            }
        }.root.apply { click { popupWindow?.dismiss() } }
    }

    private fun createPinExpand(): PopupWindow {
        val sizeText = SystemUtil.getFontSizeChat(requireActivity())
        val bind =
            PopupMessagePinBinding.inflate(layoutInflater, binding.root as ViewGroup, false)
        val popupWindow = PopupWindow(
            bind.root,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        val slideDown = AnimationUtils.loadAnimation(context, R.anim.enter_from_top)

        with(bind) {
            tvSeeAllPin.apply {
                textSize = sizeText
                click {
//                    mListener.openPinScreen()
                    popupWindow.dismiss()
                }
            }
            tvCollapse.click { popupWindow.dismiss() }
            lnPin.removeAllViews()
            for (pin in viewModel.pinMessage.value.orEmpty()) {
                lnPin.addView(addViewPinMessage(pin, true, popupWindow))
            }

            lnHeader.apply {
                show()
                animation = fadeIn
                launch(ANIMATION_DELAY) {
                    lnPin.show().also { animation = slideDown }
                    tvCollapse.show().also { animation = fadeIn }
                }
            }
        }

        popupWindow.inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED
        popupWindow.isOutsideTouchable = true
        return popupWindow
    }
}