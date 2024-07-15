package boilerplate.ui.main

import android.util.Log
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
    private val userRepository: UserRepository,
    private val tokenRepository: TokenRepository,
    private val conRepository: ConversationRepository
) : BaseViewModel() {

    var lastId: String = ""

    private val _currentLoad by lazy { MutableLiveData<ArrayList<Conversation>>() }
    val loadConversation = _currentLoad

    private val _logout by lazy { MutableLiveData<Boolean>() }
    val logout = _logout

    fun getUser() = userRepository.getUser()

    fun apiGetConversation(
        last: String,
        limit: Int,
        unread: Boolean,
        isImportant: Boolean,
        search: String?
    ) {
        launchDisposable {
            lastId = last
            conRepository.getConversations(
                last,
                limit,
                if (isImportant) null else unread,
                isImportant,
                search
            )
                .apply { if (last.isEmpty()) loading(_loading) }
                .withScheduler(schedulerProvider)
                .subscribeWith(ApiObservable.apiCallback(success = {
                    _currentLoad.postValue(it.result?.items)
                }))
        }
    }

    fun logout() {
        launchDisposable {
            userRepository.logout().withScheduler(schedulerProvider)
                .subscribeWith(ApiObservable.apiCallback(success = {
                    tokenRepository.wipeToken()
                    userRepository.wipeUserData()
                    Log.d("sss", "logout: ")
                    _logout.postValue(true)
                }))
        }
    }
}