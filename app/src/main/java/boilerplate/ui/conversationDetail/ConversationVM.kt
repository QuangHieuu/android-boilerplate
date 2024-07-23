package boilerplate.ui.conversationDetail

import androidx.lifecycle.MutableLiveData
import boilerplate.base.BaseViewModel
import boilerplate.data.local.repository.user.UserRepository
import boilerplate.data.remote.api.response.BaseResult
import boilerplate.data.remote.repository.conversation.ConversationRepository
import boilerplate.model.conversation.Conversation
import boilerplate.model.message.Message
import boilerplate.utils.DateTimeUtil
import boilerplate.utils.extension.BaseSchedulerProvider
import boilerplate.utils.extension.loading
import boilerplate.utils.extension.result
import boilerplate.utils.extension.withScheduler
import io.reactivex.rxjava3.core.Flowable
import java.util.Date
import java.util.TreeMap

class ConversationVM(
    private val schedulerProvider: BaseSchedulerProvider,
    private val conversationRepo: ConversationRepository,
    userRepo: UserRepository,
) : BaseViewModel() {

    val user = userRepo.getUser()

    private val _conversationError by lazy { MutableLiveData<Throwable>() }
    val conversationError = _conversationError

    private val _conversation by lazy { MutableLiveData<Conversation>() }
    val conversation = _conversation

    private val _messages by lazy { MutableLiveData<Pair<Int, Map<String, List<Message>>>>() }
    val messages = _messages
    private val _messageError by lazy { MutableLiveData<Throwable>() }
    val messageError = _messageError

    var countNewMessage = 0
    var userReadMessage = 0
    var unreadMessage = 0
    var otherReadMessage = 0
    var lastMessage: String = ""

    lateinit var goToMessageId: String
    lateinit var conversationId: String

    fun apiGetMoreMessage(last: String) {
        lastMessage = last
        launchDisposable {
            conversationRepo.getMessages(conversationId, limit, lastMessage)
                .map { res ->
                    val list = res.result?.items.orEmpty()
                    for (m in list) {
                        m.status = if (isOtherReadMessage()) 1 else 0
                    }
                    Pair(list.size, toMap(list))
                }
                .withScheduler(schedulerProvider)
                .result({ _messages.postValue(it) }, { _messageError.postValue(it) })
        }
    }

    fun apiGetPreviousMessage(id: String) {

    }

    fun apiGetDetail(id: String, goTo: String) {
        conversationId = id
        goToMessageId = goTo

        val message: Flowable<BaseResult<Message>>
//        if (goTo.isEmpty()) {
        message = conversationRepo.getMessages(id, limit, null)
//        } else {
//            onGoToMessage(_goToMessageId)
//        }
        launchDisposable {
            conversationRepo.getConversationDetail(id)
                .doOnNext { _conversation.postValue(it.result) }
                .doOnError { _conversationError.postValue(it) }
                .flatMap { message }
                .map { res ->
                    val list = res.result?.items.orEmpty()
                    for (m in list) {
                        m.status = if (isOtherReadMessage()) 1 else 0
                    }
                    Pair(list.size, toMap(list))
                }
                .doOnNext { _messages.postValue(it) }
                .withScheduler(schedulerProvider)
                .loading(_loading)
                .result({}, {})
        }
    }

    private fun toMap(events: List<Message>): Map<String, MutableList<Message>> {
        val comparator = Comparator<String> { ob1: String, ob2: String ->
            val date1: Date = DateTimeUtil.convertWithSuitableFormat(ob1)
            val date2: Date = DateTimeUtil.convertWithSuitableFormat(ob2)
            return@Comparator date2.compareTo(date1)
        }
        val map = TreeMap<String, MutableList<Message>>(comparator)
        for (message in events) {
            val javaDate: String = DateTimeUtil.convertWithSuitableFormat(
                message.dateCreate,
                DateTimeUtil.FORMAT_NORMAL
            )
            var value = map[javaDate]
            if (value == null) {
                value = java.util.ArrayList()
                map[javaDate] = value
            }
            value.add(message)
        }
        return map
    }


    private fun isOtherReadMessage(): Boolean {
        return otherReadMessage == conversation.value?.totalMessage
    }
}