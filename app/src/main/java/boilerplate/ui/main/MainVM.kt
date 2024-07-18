package boilerplate.ui.main

import androidx.lifecycle.MutableLiveData
import boilerplate.base.BaseViewModel
import boilerplate.data.local.repository.user.TokenRepository
import boilerplate.data.local.repository.user.UserRepository
import boilerplate.data.remote.api.ApiObservable
import boilerplate.data.remote.repository.conversation.ConversationRepository
import boilerplate.model.conversation.Conversation
import boilerplate.utils.extension.BaseSchedulerProvider
import boilerplate.utils.extension.loading
import boilerplate.utils.extension.withScheduler

class MainVM(
    private val schedulerProvider: BaseSchedulerProvider,
    private val userRepo: UserRepository,
    private val tokenRepo: TokenRepository,
    private val conversationRepo: ConversationRepository
) : BaseViewModel() {

    val currentFullName = userRepo.getCurrentRoleFullName()

    private val _currentLoad by lazy { MutableLiveData<Pair<String, ArrayList<Conversation>?>>() }
    val loadConversation = _currentLoad

    private val _logout by lazy { MutableLiveData<Boolean>() }
    val logout = _logout

    private val _user by lazy { MutableLiveData(userRepo.getUser()) }
    val user = _user

    fun apiGetConversation(
        last: String,
        limit: Int,
        unread: Boolean,
        isImportant: Boolean,
        search: String?
    ) {
        launchDisposable {
            conversationRepo.getConversations(
                last,
                limit,
                if (isImportant) null else unread,
                isImportant,
                search
            )
                .apply { if (last.isEmpty()) loading(_loading) }
                .withScheduler(schedulerProvider)
                .subscribeWith(ApiObservable.apiCallback(success = {
                    _currentLoad.postValue(Pair(last, it.result?.items))
                }))
        }
    }

    fun logout() {
        launchDisposable {
            userRepo.logout().withScheduler(schedulerProvider)
                .subscribeWith(ApiObservable.apiCallback(success = {
                    tokenRepo.wipeToken()
                    userRepo.wipeUserData()
                    _logout.postValue(true)
                }))
        }
    }
}