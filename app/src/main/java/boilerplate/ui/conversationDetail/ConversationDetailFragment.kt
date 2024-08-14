package boilerplate.ui.conversationDetail

import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import boilerplate.R
import boilerplate.base.BaseFragment
import boilerplate.databinding.DialogBaseBinding
import boilerplate.databinding.FragmentConversationDetailBinding
import boilerplate.databinding.ItemMessageMenuBinding
import boilerplate.databinding.ItemPinOnScreenBinding
import boilerplate.databinding.PopupMessagePinBinding
import boilerplate.databinding.ViewMessageMenuBinding
import boilerplate.databinding.ViewReactionsBinding
import boilerplate.model.conversation.AddMember
import boilerplate.model.conversation.Conversation
import boilerplate.model.conversation.ConversationRole
import boilerplate.model.conversation.ConversationRole.ALLOW_MEMBER
import boilerplate.model.conversation.ConversationRole.MAIN
import boilerplate.model.conversation.ConversationRole.MEMBER
import boilerplate.model.conversation.ConversationRole.SUB
import boilerplate.model.conversation.LeaveGroup
import boilerplate.model.conversation.SeenMessage
import boilerplate.model.conversation.Setting
import boilerplate.model.conversation.UpdateRole
import boilerplate.model.file.AttachedFile
import boilerplate.model.message.Message
import boilerplate.model.message.MessageAction
import boilerplate.model.message.PinMessage
import boilerplate.model.message.Quote
import boilerplate.model.message.SendMessageResult
import boilerplate.model.user.User
import boilerplate.service.signalr.SignalRImpl
import boilerplate.service.signalr.SignalRManager
import boilerplate.service.signalr.SignalRResult
import boilerplate.ui.contactDetail.ContactDetailFragment
import boilerplate.ui.conversationDetail.adpater.MessageAdapter
import boilerplate.ui.conversationDetail.adpater.ReactionAdapter
import boilerplate.ui.conversationDetail.adpater.SimpleMessageEvent
import boilerplate.ui.conversationInform.ConversationInformFragment
import boilerplate.ui.file.FileFragment
import boilerplate.utils.InternetManager
import boilerplate.utils.StringUtil
import boilerplate.utils.SystemUtil
import boilerplate.utils.extension.ANIMATION_DELAY
import boilerplate.utils.extension.addFile
import boilerplate.utils.extension.click
import boilerplate.utils.extension.dimBehind
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.ifEmpty
import boilerplate.utils.extension.isTablet
import boilerplate.utils.extension.isVisible
import boilerplate.utils.extension.launch
import boilerplate.utils.extension.notNull
import boilerplate.utils.extension.open
import boilerplate.utils.extension.openDialog
import boilerplate.utils.extension.sendResult
import boilerplate.utils.extension.show
import boilerplate.utils.extension.showDialog
import boilerplate.utils.extension.showFail
import boilerplate.utils.extension.showKeyboard
import boilerplate.utils.extension.showSuccess
import boilerplate.utils.extension.showWarning
import boilerplate.utils.keyboard.InsetsWithKeyboardAnimationCallback
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

	private lateinit var fadeIn: Animation
	private lateinit var slideDown: Animation
	private lateinit var metrics: DisplayMetrics

	private lateinit var _layoutManager: LinearLayoutManager
	private lateinit var _endlessListener: EndlessListener
	private lateinit var _smoothScroller: SmoothScroller

	private lateinit var _adapter: MessageAdapter
	private lateinit var _chatBoxListener: SimpleBoxListener

	private var _isOnTop = true
	private var _previousCount = 0
	private var _currentCount = 0
	private var _isLoadMore = false
	private var _isGotoMessage = false
	private var _isLeaveGroup = false

	private var _pointClickX = 0
	private var _pointClickY = 0

	override fun initialize() {
		val insetsWithKeyboardAnimationCallback = InsetsWithKeyboardAnimationCallback(binding.chatBox)
		ViewCompat.setWindowInsetsAnimationCallback(
			binding.chatBox,
			insetsWithKeyboardAnimationCallback
		)

		metrics = Resources.getSystem().displayMetrics

		fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
		slideDown = AnimationUtils.loadAnimation(context, R.anim.enter_from_top)

		initAdapter()
		initChatBoxListener()

		with(binding) {
			imgBack.apply { if (requireActivity().isTablet()) gone() else show() }
			chatBox.apply {
				setupForChat()
				setListener(_chatBoxListener)
			}
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
					it.id,
					Date(),
					it.totalMessage,
					true
				)

				var isInGroup = false
				for (user in it.members) {
					if (user.user.id.equals(viewModel.user.id)) {
						isInGroup = true
						break
					}
				}
				if (isInGroup) {
					setupChatBox(it)
				} else {
					val leaveGroup = LeaveGroup(conversationId, viewModel.user.id)
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
			markImportant.observe(this@ConversationDetailFragment) {
				_adapter.markImportant(it)
			}
			pinMessageState.observe(this@ConversationDetailFragment) {
				binding.root.showSuccess(message = it)
			}
			withdrawMessageState.observe(this@ConversationDetailFragment) {
				if (it.code == 1) {
					showDialog(DialogBaseBinding.inflate(layoutInflater)) { vb, dialog ->
						with(vb) {
							tvTitle.setText(R.string.withdraw_message)
							tvDescription.text = it.message
							btnConfirm.setText(R.string.agree)

							btnCancel.gone()
							btnConfirm.click {
								dialog.dismiss()
							}
						}
					}
				} else {
					binding.root.showFail(it.message)
				}
			}

			editMessageStatus.observe(this@ConversationDetailFragment) {
				showDialog(DialogBaseBinding.inflate(layoutInflater)) { vb, dialog ->
					with(vb) {
						tvTitle.setText(R.string.edit_message)
						tvDescription.text = it.message
						btnConfirm.setText(R.string.agree)

						btnCancel.gone()
						btnConfirm.click {
							dialog.dismiss()
						}
					}
				}
			}
			editMessage.observe(this@ConversationDetailFragment) {
				binding.chatBox.editMessage(it, true)
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

	private fun initAdapter() {
		_adapter = MessageAdapter(object : SimpleMessageEvent() {
			override fun longClick(message: Message, view: View, viewType: Int) {
				_adapter.hideMessage(message.messageId, true)
				createMessageMenu(message, view)
			}

			override fun quoteMessage(message: Message) {
				answerMessage(message)
			}

			override fun openUser(person: User) {
				openDialog(ContactDetailFragment.newInstance(person.id))
			}

			override fun showReaction(message: Message, view: View) {
				showPopupReaction(message, view)
			}

			override fun mentionUser(userId: String) {
				openDialog(ContactDetailFragment.newInstance(userId))
			}

			override fun openFile(file: AttachedFile) {
				open(FileFragment.newInstance(viewModel.gson.toJson(file)))
			}
		})
	}

	private fun initChatBoxListener() {
		_chatBoxListener = object : SimpleBoxListener() {
			override fun onSendMessage(
				content: String,
				uploadFile: ArrayList<AttachedFile>,
				currentFile: ArrayList<AttachedFile>,
				surveyFile: ArrayList<AttachedFile>,
				isSms: Boolean,
				isEmail: Boolean
			) {
				viewModel.sendMessage(content, uploadFile, currentFile, surveyFile, isSms, isEmail)
			}

			override fun onEditMessage(
				lastMessage: Message,
				content: String,
				uploadFile: ArrayList<AttachedFile>,
				currentFile: ArrayList<AttachedFile>,
				surveyFile: ArrayList<AttachedFile>,
				isSms: Boolean,
				isEmail: Boolean
			) {
				viewModel.sendEditMessage(
					lastMessage,
					content,
					uploadFile,
					currentFile,
					surveyFile,
					isSms,
					isEmail
				)
			}

			override fun onAttachedClick() {
			}

			override fun onCameraClick() {
			}

			override fun onPickImageClick() {
			}

			override fun onOpenRecord() {
			}

			override fun onStartRecord(modeSpeech: Boolean) {
			}
		}
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
			val builder: StringBuilder = StringBuilder(con.name)
			val isGroup: Boolean = con.isGroup
			val size: Int = con.totalMember
			val isOneUser = size == 1

			_adapter.setIsGroup(isGroup)

			if (con.isMyCloud) {
				chatBox.enableChat(true)
				tvTitle.setText(R.string.my_cloud)
				tvSubTitle.gone()
				viewModel.hasRead = con.totalMessage
				return
			}
			if (con.isGroup) {
				chatBox.addMentions(con.members)
				var isAllowSend: Boolean = con.isAllowSendMessage

				tvSubTitle.text = getString(
					R.string.member_count,
					con.members.size
				)

				var needCreateName = builder.toString().isEmpty()
				if (!needCreateName) {
					tvTitle.text = builder
				}
				if (isGroup && builder.toString().isEmpty() && isOneUser) {
					builder.append(getString(R.string.conversation_name))
				}
				var countName = 0
				for (user in con.members) {
					user.let {
						if (!it.isOutGroup && !(it.user.id.equals(con.creatorId) && size > 3)) {
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
								isAllowSend = when (ConversationRole.fromType(it.vaiTro)) {
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
				for (user in con.members) {
					if (!user.user.id.equals(viewModel.user.id)) {
						user.let {
							viewModel.hasRead = it.readNumber
							viewModel.otherRead = it.readNumber

							it.user.apply {
								tvTitle.text = name
								tvSubTitle.text = mainDepartment.name
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

				override fun sendMessage(sendMessage: SendMessageResult) {
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
								binding.root.showFail(R.string.error_general)
							}
						}
					}
				}

				override fun sendMessageError(string: String) {
					with(binding) {
						root.showFail(string.ifEmpty { getString(R.string.error_general) })
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

				override fun pinMessage(pinMessage: PinMessage) {
					checkCurrentConversation(pinMessage.conversationId) {

					}
				}

				override fun removePinMessage(pinMessage: PinMessage) {
					checkCurrentConversation(pinMessage.conversationId) {
						val list = viewModel.pinMessage.value.ifEmpty()
						val listIterator = list.listIterator()
						while (listIterator.hasNext()) {
							val pin = listIterator.next()
							if (pin.messagePinId == pinMessage.messageId) {
								listIterator.remove()
								break
							}
						}
						viewModel.pinMessage.postValue(arrayListOf())
					}
				}

				override fun newReaction(message: Message) {
					checkCurrentConversation(message.conversationId) {
						_adapter.updateReaction(message)
					}
				}

				override fun deleteGroup(conversationId: String) {
					checkCurrentConversation(conversationId) {
						binding.root.showSuccess(R.string.success_delete_group)
					}
				}

				override fun updateConversation(conversation: Conversation) {
					checkCurrentConversation(conversation.id) {
						viewModel.conversation.postValue(conversation)
					}
				}

				override fun updateConversationSetting(setting: Setting) {
					checkCurrentConversation(setting.conversationId) {
						viewModel.conversation.value?.apply {
							isChangeInform = setting.isChangeInform
							isAllowPinMessage = setting.isAllowPinMessage
							isAllowApproved = setting.isAllowApproved
							isAllowSendMessage = setting.isAllowSendMessage
						}.let { viewModel.conversation.postValue(it) }
					}
				}

				override fun updateRole(updateRole: UpdateRole) {
					checkCurrentConversation(updateRole.conversationId) {
						if (updateRole.userId == viewModel.user.id) {
							viewModel.conversation.value?.apply {
								for (member in members) {
									if (member.user.id == viewModel.user.id) {
										member.vaiTro = updateRole.role
										break
									}
								}
							}.let { viewModel.conversation.postValue(it) }
						}
					}
				}

				override fun addMember(addMember: AddMember) {
					checkCurrentConversation(addMember.conversationId) {
						viewModel.conversation.value?.apply {
							members.addAll(addMember.addMember)
						}.let { viewModel.conversation.postValue(it) }
					}
				}

				override fun deleteMember(leaveGroup: LeaveGroup) {
					checkCurrentConversation(leaveGroup.conversationId) {
						if (viewModel.user.id == leaveGroup.conversationId) {
							_isLeaveGroup = true
							binding.root.showWarning(R.string.warning_remove_out_of_group)
							popFragment()
						} else {
							viewModel.conversation.postValue(leaveGroup.conversation)
						}
					}
				}

				override fun seenMessage(seenMessage: SeenMessage) {

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
										viewModel.removePinMessage(pin.messagePinId)
										dialog.dismiss()
										popupWindow?.dismiss()
									}
								}
							}
						}
					}
				}
			}
			pickerPin.removeAllViews()
			val newList = arrayListOf<AttachedFile>()
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
			if (isTablet()) metrics.widthPixels / 2 else ViewGroup.LayoutParams.MATCH_PARENT,
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

	private fun createMessageMenu(message: Message, view: View) {
		val location = IntArray(2)
		view.getLocationOnScreen(location)
		val x = location[0]
		var y = location[1]

		with(createMenuPopup(message)) {
			val child: View = contentView.findViewById(R.id.ln_menu)
			child.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

			val listHeight = child.measuredHeight
			val half = listHeight + view.height

			if (y + half > metrics.heightPixels) {
				val offSetY = metrics.heightPixels - half + 10
				showAtLocation(view, Gravity.NO_GRAVITY, x, max(offSetY, 0))
			} else {
				val tv = TypedValue()
				requireContext().theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)
				val actionBarHeight = resources.getDimensionPixelSize(tv.resourceId)
				if (y < actionBarHeight) {
					y = (actionBarHeight + resources.getDimension(R.dimen.dp_52)).toInt()
				}
				showAtLocation(view, Gravity.NO_GRAVITY, x, y)
			}
			dimBehind()
		}
	}

	private fun createMenuPopup(message: Message): PopupWindow {
		val view = ViewMessageMenuBinding.inflate(layoutInflater, binding.root, false)

		val popupWindow = PopupWindow(
			view.root,
			if (isTablet()) metrics.widthPixels / 2 else ViewGroup.LayoutParams.MATCH_PARENT,
			view.root.layoutParams.height
		).apply {
			inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED
			isOutsideTouchable = true
			isFocusable = true
			animationStyle = R.style.PopupWindowAnimation
			setOnDismissListener {
				_adapter.hideMessage(message.messageId, false)
			}
		}

		with(view) {
			val messageAdapter = MessageAdapter(object : SimpleMessageEvent() {
				override fun closeMenu() {
					_adapter.hideMessage(message.messageId, false)
					popupWindow.dismiss()
				}
			}).apply {
				focusMessage(message.copy().apply { isHide = false })
				setIsGroup(viewModel.conversation.value!!.isGroup)
				disableSwipe()
			}
			rcvMessageSelected.apply {
				adapter = messageAdapter
				itemAnimator = null
			}

			lnContainerMenu.click {
				_adapter.hideMessage(message.messageId, false)
				popupWindow.dismiss()
			}

			lnMenu.apply {
				val params = layoutParams as LinearLayout.LayoutParams
				if (message.personSendId.equals(viewModel.user.id)) {
					params.gravity = Gravity.END
					params.rightMargin =
						context.resources.getDimensionPixelOffset(R.dimen.dp_10)
				} else {
					params.gravity = Gravity.START
					params.leftMargin = context.resources.getDimensionPixelOffset(R.dimen.dp_55)
				}
				loadMenu(message, this) {
					_adapter.hideMessage(message.messageId, false)
					popupWindow.dismiss()
				}
			}

			rcvMessageSelected.apply {
				startAnimation(fadeIn)
				show()
			}
			lnMenu.apply {
				startAnimation(slideDown)
				show()
			}
			scrollMenu.launch(ANIMATION_DELAY) {
				scrollMenu.fullScroll(View.FOCUS_DOWN)
			}
		}
		return popupWindow
	}

	private fun loadMenu(message: Message, lnMenu: LinearLayout, block: () -> Unit) {
		val size = SystemUtil.getFontSizeChat(requireActivity())

		val pair = viewModel.getMessagePermission()
		val list =
			if (message.status == 2) MessageAction.getMenuAction()
			else if (InternetManager.isConnected())
				MessageAction.getMenuAction(message, pair.first, pair.second)
			else MessageAction.getMenuAction(message, pair.first)

		for (menus in list) {
			val indexGroup: Int = list.indexOf(menus)
			for (menu in menus) {
				val index = menus.indexOf(menu)
				with(ItemMessageMenuBinding.inflate(layoutInflater, binding.root, false)) {
					if (index == menus.size - 1) {
						lnMessageAction.setBackgroundResource(R.drawable.bg_border_bottom_grey)
					}
					imgIcon.setImageResource(menu.icon)
					tvTitle.apply {
						if (indexGroup == list.size - 1) {
							setTextColor(
								ContextCompat.getColor(root.context, R.color.color_E80808)
							)
						}
						text = menu.getName()
						textSize = size
					}
					lnMenu.addView(root.apply {
						click {
							block()
							onMenuMessageClick(menu, message)
						}
					})
				}
			}
		}
	}

	private fun onMenuMessageClick(menu: MessageAction, message: Message) {
		val messageId = message.messageId
		val json = Gson().toJson(message)
		val copyText = StringUtil.getHtml(message.mainContent[0])
			.toString()
			.trim()
		when (menu) {
			MessageAction.CREATE_WORK -> {
//                val work: CreateWork = CreateWork()
//                val list = java.util.ArrayList<AttachedFile.Work>()
//                for (file in message.attachedFiles) {
//                    val attachedFile: AttachedFile.Work = Work()
//                    attachedFile.setFileId(file.getFileId())
//                    attachedFile.setFileName(file.getFileName())
//                    attachedFile.setFileType(file.getFileType())
//                    attachedFile.setPreventRemove(false)
//                    attachedFile.setFileUploaded(true)
//
//                    list.add(attachedFile)
//                    work.getAttachment_file_ids().add(file.getFileId())
//                }
//                work.setSelectedFiles(list)
//                work.setTitle(copyText)
//                work.setReference_id(message.messageId)
//                work.setReference_type(Common.WORK_ASSIGNMENT)
//                work.setWork_type(WorkType.CHAT.getType())
//                work.setShowTempIcon(false)
//                work.setHideSaveForm(false)
//
//                openOnlyForDetailWorkManager(CreateWorkFragment.onlyForChat(Gson().toJson(work)))
			}

			MessageAction.ANSWER -> answerMessage(message)
			MessageAction.FORWARD -> {

			}

			MessageAction.MARK, MessageAction.UN_MARK -> viewModel.markAsImportant(messageId)
			MessageAction.PIN -> viewModel.pinMessage(message.messagePinId)
			MessageAction.COPY -> SystemUtil.copyToClipboard(requireContext(), copyText, json)
			MessageAction.RECALL -> viewModel.checkWithdrawMessage(messageId)
//            MessageAction.DELETE -> deleteMultipleMessage(message)
//            MessageAction.MY_CLOUD -> apiSendToMyCloud(message)
			MessageAction.EDIT -> viewModel.checkEditMessage(message)
//            MessageAction.SHARE -> {}
			else -> {

			}
		}
	}

	private fun answerMessage(message: Message) {
		val index: Int = _adapter.getItemIndex(message.messageId)
		val forward: ArrayList<Quote> = StringUtil.formatQuoteMessage(message, true)
		val userSendId = message.personSendId

		with(binding.chatBox) {
			clearMessageQuote()
			handleForwardMessage(forward)
			if (viewModel.conversation.value!!.isGroup) {
				mentionAnswer(userSendId)
			}
			getEditMessage().showKeyboard()
		}
		with(binding) {
			root.launch(ANIMATION_DELAY) {
				snapScrollPosition(index)
			}
		}
	}

	private fun showPopupReaction(message: Message, view: View) {
		val reactBinding = ViewReactionsBinding.inflate(layoutInflater, binding.root, false)

		val popupWindow = PopupWindow(
			reactBinding.root,
			ViewGroup.LayoutParams.WRAP_CONTENT,
			ViewGroup.LayoutParams.WRAP_CONTENT,
			true
		)

		with(reactBinding) {
			val adapter = ReactionAdapter {
				popupWindow.dismiss()
				viewModel.postReaction(it.emoticonId, message.messageId)
			}
			for (reaction in viewModel.reactions) {
				reaction.isReacted = false
				for (chosen in message.reactions) {
					if (reaction.emoticonId.equals(chosen.emoticonId)) {
						for (user in chosen.nhanVienReactInfoList) {
							if (user.id.equals(viewModel.user.id)) {
								reaction.isReacted = true
								break
							}
						}
						break
					}
				}

			}
			adapter.addData(viewModel.reactions)
			recycleView.adapter = adapter

			imgNext.show(viewModel.reactions.size > 6)
			imgPre.show(viewModel.reactions.size > 6)

			imgPre.click {
				recycleView.smoothScrollToPosition(0)
			}
			imgNext.click {
				recycleView.smoothScrollToPosition(if (adapter.itemCount == 0) 0 else adapter.itemCount - 1)
			}
		}

		popupWindow.apply {
			showAsDropDown(view, -200, 0, Gravity.CENTER_HORIZONTAL)
			dimBehind()
		}
	}
}