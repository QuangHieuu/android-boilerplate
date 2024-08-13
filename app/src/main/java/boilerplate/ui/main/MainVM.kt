package boilerplate.ui.main

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import boilerplate.base.BaseViewModel
import boilerplate.data.local.repository.user.TokenRepository
import boilerplate.data.local.repository.user.UserRepository
import boilerplate.data.remote.api.ApiObservable
import boilerplate.data.remote.repository.auth.LoginRepository
import boilerplate.data.remote.repository.conversation.ConversationRepository
import boilerplate.model.conversation.Conversation
import boilerplate.model.conversation.SignalBody
import boilerplate.model.message.Message
import boilerplate.model.user.User
import boilerplate.ui.main.tab.HomeTabIndex
import boilerplate.utils.SystemUtil
import boilerplate.utils.extension.BaseSchedulerProvider
import boilerplate.utils.extension.ifEmpty
import boilerplate.utils.extension.loading
import boilerplate.utils.extension.result
import boilerplate.utils.extension.withScheduler
import io.reactivex.rxjava3.core.Flowable

class MainVM(
	private val schedulerProvider: BaseSchedulerProvider,
	private val userRepo: UserRepository,
	private val tokenRepo: TokenRepository,
	private val conversationRepo: ConversationRepository,
	private val loginRepo: LoginRepository,
) : BaseViewModel() {

	val currentFullName = userRepo.getCurrentRoleFullName()

	private val _logout by lazy { MutableLiveData<Boolean>() }
	val logout = _logout

	private val _tabPosition by lazy { MutableLiveData<ArrayList<String>>() }
	val tabPosition = _tabPosition

	private val _currentTabSelected by lazy { MutableLiveData(HomeTabIndex.POSITION_HOME_DASHBOARD) }
	val currentSelected = _currentTabSelected

	private val _currentUser by lazy { MutableLiveData(userRepo.getUser()) }
	val user = _currentUser

	private val _conversations by lazy { MutableLiveData<Pair<String, ArrayList<Conversation>>>() }
	val conversations = _conversations
	private val _pinConversations by lazy { MutableLiveData<ArrayList<Conversation>>() }
	val pinConversations = _pinConversations

	private val _conversationUpdate by lazy { MutableLiveData<Conversation>() }
	val conversationUpdate = _conversationUpdate

	private val _conversationDetail by lazy { MutableLiveData<Conversation>() }
	val conversationDetail = _conversationDetail

	fun logout() {
		launchDisposable {
			userRepo.logout().withScheduler(schedulerProvider)
				.subscribeWith(ApiObservable.apiCallback(success = {
					tokenRepo.wipeToken()
					userRepo.wipeUserData()

					SystemUtil.removeTempFiles(application)
					SystemUtil.removeDocumentFile(application)

					_logout.postValue(true)
				}))
		}
	}

	@SuppressLint("CheckResult")
	fun apiGetConversation(
		last: String,
		unread: Boolean,
		isImportant: Boolean,
		search: String?
	) {
		launchDisposable {
			Flowable.concatArray(
				conversationRepo.getConversations(
					last,
					limit,
					if (isImportant) null else unread,
					isImportant,
					search
				).doOnNext { _conversations.postValue(Pair(last, it.result?.items.ifEmpty())) },
				if (last.isEmpty() && !unread && !isImportant) {
					conversationRepo.getPinConversation()
						.doOnNext { _pinConversations.postValue(it.result?.items.ifEmpty()) }
				} else {
					Flowable.empty()
				}
			)
				.apply { if (last.isEmpty()) loading(_loading) }
				.withScheduler(schedulerProvider)
				.result()
		}
	}

	fun apiGetConversationDetail(message: Message) {
		launchDisposable {
			conversationRepo.getConversationDetail(message.conversationId)
				.withScheduler(schedulerProvider)
				.result({
					_conversationUpdate.postValue(it.result?.apply {
						lastActive = message.dateCreate
						lastMessage = message
						totalMessage = message.conversation.totalMessage
					})
				})
		}
	}

	fun apiGetConversationDetail(conversationId: String) {
		launchDisposable {
			conversationRepo.getConversationDetail(conversationId)
				.withScheduler(schedulerProvider)
				.result({
					_conversationUpdate.postValue(it.result)
				})
		}
	}

	fun apiGetConversationDetail(conversation: Conversation) {
		launchDisposable {
			conversationRepo.getConversationDetail(conversation.id)
				.withScheduler(schedulerProvider)
				.result({
					_conversationUpdate.postValue(it.result)
				})
		}
	}

	fun postPersonConversation(user: User) {
		val chatBody = SignalBody(
			user.id,
			user.name,
			user.avatarId
		)
		launchDisposable {
			conversationRepo.postPersonConversation(chatBody)
				.loading(_loading)
				.withScheduler(schedulerProvider)
				.result({
					_conversationDetail.postValue(it.result)
				})
		}
	}
}