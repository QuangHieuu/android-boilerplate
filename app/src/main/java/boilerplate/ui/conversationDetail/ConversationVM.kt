package boilerplate.ui.conversationDetail

import android.app.Application
import androidx.lifecycle.MutableLiveData
import boilerplate.base.BaseViewModel
import boilerplate.data.local.repository.user.UserRepository
import boilerplate.data.remote.api.response.BaseResult
import boilerplate.data.remote.repository.conversation.ConversationRepository
import boilerplate.model.conversation.Conversation
import boilerplate.model.file.AttachedFile
import boilerplate.model.message.Message
import boilerplate.model.user.User
import boilerplate.service.signalr.SignalRManager
import boilerplate.utils.DateTimeUtil
import boilerplate.utils.FileUtils
import boilerplate.utils.extension.BaseSchedulerProvider
import boilerplate.utils.extension.loading
import boilerplate.utils.extension.notNull
import boilerplate.utils.extension.result
import boilerplate.utils.extension.withScheduler
import io.reactivex.rxjava3.core.Flowable
import okhttp3.MultipartBody
import java.util.Date
import java.util.TreeMap

class ConversationVM(
    private val application: Application,
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

    private val _countReceiveMessage by lazy { MutableLiveData(0) }
    var recevice = _countReceiveMessage

    private val _pinMessages by lazy { MutableLiveData<ArrayList<Message>>() }
    var pinMessage = _pinMessages

    var hasRead = 0
    var otherRead = 0
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
                .doOnNext { response ->
                    response.result.notNull {
                        _pinMessages.postValue(it.pinMessage)
                        _conversation.postValue(it)
                    }
                }
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

    fun sendMessage(
        content: String,
        uploadFiles: ArrayList<AttachedFile.Conversation>,
        currentFile: ArrayList<AttachedFile.Conversation>,
        surveyFile: ArrayList<AttachedFile.SurveyFile>,
        isSms: Boolean,
        isEmail: Boolean
    ) {
        val sendMessage = createSendMessage(isSms, isEmail).apply {
            setContent(content)
            attachedFiles = currentFile
            surveyFiles = surveyFile
        }

        if (uploadFiles.isNotEmpty()) {
            val files: List<MultipartBody.Part> = FileUtils.multipartFiles(application, uploadFiles)
//            apiUploadFile(message, files, sendSms, sendEmail)
        } else {
            SignalRManager.sendMessage(sendMessage, isSms, isEmail)
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
        return otherRead == conversation.value?.totalMessage
    }

    private fun createSendMessage(sendSms: Boolean, sendEmail: Boolean): Message {
        val user = User().apply {
            id = user.id
        }

        return Message().apply {
            conversationId = this@ConversationVM.conversationId
            isSendSms = sendSms
            isSendMail = sendEmail
            personSend = user
            personSendId = user.id
        }
    }

    fun removePinMessage(messageId: String) {

    }
}