package boilerplate.ui.conversationDetail

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import boilerplate.base.BaseViewModel
import boilerplate.data.local.repository.user.UserRepository
import boilerplate.data.remote.api.response.ResponseItems
import boilerplate.data.remote.repository.conversation.ConversationRepository
import boilerplate.data.remote.repository.file.FileRepository
import boilerplate.model.conversation.Conversation
import boilerplate.model.conversation.SignalBody
import boilerplate.model.file.AttachedFile
import boilerplate.model.file.convertFile
import boilerplate.model.message.Message
import boilerplate.model.user.User
import boilerplate.service.signalr.SignalRManager
import boilerplate.utils.DateTimeUtil
import boilerplate.utils.FileUtils
import boilerplate.utils.extension.BaseSchedulerProvider
import boilerplate.utils.extension.ifEmpty
import boilerplate.utils.extension.loading
import boilerplate.utils.extension.notNull
import boilerplate.utils.extension.result
import boilerplate.utils.extension.withScheduler
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import okhttp3.MultipartBody
import java.util.Date
import java.util.TreeMap

class ConversationVM(
    private val schedulerProvider: BaseSchedulerProvider,
    private val conversationRepo: ConversationRepository,
    private val fileRepo: FileRepository,
    userRepo: UserRepository,
) : BaseViewModel() {

    val user = userRepo.getUser()
    val config = userRepo.getConversationConfig()

    private val _conversationError by lazy { MutableLiveData<Throwable>() }
    val conversationError = _conversationError

    private val _conversation by lazy { MutableLiveData<Conversation>() }
    val conversation = _conversation

    private val _messages by lazy { MutableLiveData<Pair<Int, Map<String, List<Message>>>>() }
    val messages = _messages
    private val _messageError by lazy { MutableLiveData<Throwable>() }
    val messageError = _messageError

    private val _countReceiveMessage by lazy { MutableLiveData(0) }
    val receive = _countReceiveMessage

    private val _pinMessages by lazy { MutableLiveData<ArrayList<Message>>() }
    val pinMessage = _pinMessages

    private val _changeConfig by lazy { MutableLiveData<Conversation>() }
    val changeConfig = _changeConfig

    private val _fileImage by lazy { MutableLiveData<ArrayList<AttachedFile.Conversation>>() }
    val fileImage = _fileImage
    private val _fileAttach by lazy { MutableLiveData<ArrayList<AttachedFile.Conversation>>() }
    val fileAttach = _fileAttach
    private val _linkAttach by lazy { MutableLiveData<ArrayList<Message>>() }
    val linkAttach = _linkAttach

    private val _checkMember by lazy { MutableLiveData<Boolean>() }
    val checkMember = _checkMember

    private val _conversationMessage by lazy { MutableLiveData<ArrayList<Message>>() }
    val conversationMessage = _conversationMessage

    var changeConversationAvatar: Uri? = null
    var changeConversationName: String? = null

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

        val message: Flowable<ResponseItems<Message>>
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
        val user = User(user.id)
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

    fun getConversationFile() {
        val image = fileRepo.getConversationFile(conversationId, 0, 1, 6)
            .doOnSuccess { _fileImage.postValue(it.result?.items.ifEmpty()) }
            .doOnError { _fileImage.postValue(arrayListOf()) }
        val file = fileRepo.getConversationFile(conversationId, 1, 1, 3)
            .doOnSuccess { _fileAttach.postValue(it.result?.items.ifEmpty()) }
            .doOnError { _fileAttach.postValue(arrayListOf()) }
        val link = conversationRepo.getConversationLink(conversationId, "", 4)
            .doOnSuccess { _linkAttach.postValue(it.result?.items.ifEmpty()) }
            .doOnError { _linkAttach.postValue(arrayListOf()) }

        launchDisposable {
            Single.concat(image, file, link)
                .withScheduler(schedulerProvider)
                .result()
        }
    }

    fun cancelChangeConversationInform() {
        changeConversationAvatar = null
        changeConversationName = null
    }

    fun changeConversationInform() {
        val file = changeConversationAvatar?.let { FileUtils.multiPartFile(application, it) } ?: ""

        launchDisposable {
            Flowable.just(file)
                .flatMap { file ->
                    if (file is String) {
                        Flowable.just("")
                    } else {
                        fileRepo.postFile(file as MultipartBody.Part)
                            .map { res ->
                                res.result.convertFile().apply {
                                    _conversation.value?.avatarId = get(0).id
                                }.ifEmpty()
                            }
                    }
                }
                .onErrorResumeNext { Flowable.just("") }
                .flatMap {
                    changeConversationName.notNull {
                        _conversation.value?.conversationName = it
                    }
                    conversationRepo.putConversation(postBody())
                }
                .withScheduler(schedulerProvider)
                .loading(_loading)
                .result({
                    _changeConfig.postValue(_conversation.value)
                }, {
                    _changeConfig.postValue(null)
                })
        }
    }

    private fun postBody(): SignalBody {
        return SignalBody().apply {
            id = conversationId
            creatorId = _conversation.value?.creatorId
            groupName = _conversation.value?.conversationName
            isChangeInform = _conversation.value?.isChangeInform ?: false
            isAllowApproved = _conversation.value?.isAllowApproved ?: false
            isAllowPinMessage = _conversation.value?.isAllowPinMessage ?: false
            isAllowSendMessage = _conversation.value?.isAllowSendMessage ?: false
            avatar = _conversation.value?.avatarId
            val list = ArrayList<SignalBody.ConversationUser>()
            for (user in _conversation.value?.conversationUsers.ifEmpty()) {
                list.add(SignalBody.ConversationUser(user.user.id))
            }
            member = list
        }
    }

    fun getMember() {
        launchDisposable {
            conversationRepo.getMemberConversation(conversationId, 1, null, false, 1200)
                .withScheduler(schedulerProvider)
                .loading(_loading)
                .result(
                    { _checkMember.postValue(it.result?.let { it.total > 0 }) },
                    { _checkMember.postValue(null) }
                )
        }
    }

    fun getPinMessage(page: Int) {
        launchDisposable {
            conversationRepo.getPinMessage(conversationId, page, limit)
                .withScheduler(schedulerProvider)
                .apply { if (page == 1) loading(_loading) }
                .result(
                    { _conversationMessage.postValue(it.result?.items.ifEmpty()) },
                    { _conversationMessage.postValue(arrayListOf()) }
                )
        }
    }

    fun getImportantMessage(page: Int) {
        launchDisposable {
            conversationRepo.getImportantMessage(conversationId, page, limit)
                .withScheduler(schedulerProvider)
                .apply { if (page == 1) loading(_loading) }
                .result(
                    { _conversationMessage.postValue(it.result?.items.ifEmpty()) },
                    { _conversationMessage.postValue(arrayListOf()) }
                )
        }
    }
}