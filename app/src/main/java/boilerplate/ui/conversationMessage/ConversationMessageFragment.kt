package boilerplate.ui.conversationMessage

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import boilerplate.R
import boilerplate.base.BaseFragment
import boilerplate.databinding.FragmentConversationMessageBinding
import boilerplate.model.file.AttachedFile
import boilerplate.model.message.Message
import boilerplate.model.user.User
import boilerplate.ui.contactDetail.ContactDetailFragment
import boilerplate.ui.conversationDetail.ConversationDetailFragment
import boilerplate.ui.conversationDetail.ConversationVM
import boilerplate.ui.conversationMessage.adapter.ConversationMessageAdapter
import boilerplate.ui.conversationMessage.adapter.ConversationMessageListener
import boilerplate.ui.main.MainVM
import boilerplate.utils.extension.findOwner
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.launch
import boilerplate.utils.extension.notNull
import boilerplate.utils.extension.openDialog
import boilerplate.utils.extension.show
import boilerplate.widget.recyclerview.EndlessListener
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ConversationMessageFragment :
    BaseFragment<FragmentConversationMessageBinding, ConversationVM>() {

    companion object {
        const val KEY_MESSAGE = "KEY_MESSAGE"
        const val MESSAGE_PIN = "MESSAGE_PIN"
        const val MESSAGE_IMPORTANT = "MESSAGE_IMPORTANT"

        fun messagePin(): ConversationMessageFragment {
            return Bundle().let {
                it.putString(KEY_MESSAGE, MESSAGE_PIN)
                ConversationMessageFragment().apply { arguments = it }
            }
        }

        fun messageImportant(): ConversationMessageFragment {
            return Bundle().let {
                it.putString(KEY_MESSAGE, MESSAGE_IMPORTANT)
                ConversationMessageFragment().apply { arguments = it }
            }
        }
    }

    override val viewModel: ConversationVM by viewModel(ownerProducer = {
        findOwner(ConversationDetailFragment::class.java.simpleName)
    })

    private val _activityVM by activityViewModel<MainVM>()

    private lateinit var screen: String
    private lateinit var _adapter: ConversationMessageAdapter
    private lateinit var _endless: EndlessListener

    private var _page = 1
    private var _isLoadMore = false
    private var _currentSize = 0

    override fun initialize() {
        arguments.notNull {
            screen = it.getString(KEY_MESSAGE, MESSAGE_PIN)

            when (screen) {
                MESSAGE_PIN -> {
                    binding.toolbar.setTitle(R.string.pin_message)
                }

                MESSAGE_IMPORTANT -> {
                    binding.toolbar.setTitle(R.string.important_message)
                }
            }
        }
        with(binding) {
            _endless = object : EndlessListener(rcvMessage.layoutManager as LinearLayoutManager) {
                override fun onLoadMore(page: Int, totalItemsCount: Int) {
                    if (_currentSize == viewModel.limit && !_isLoadMore) {
                        _isLoadMore = true
                        _page++
                        binding.rcvMessage.launch {
                            _adapter.loadMore()
                            callApi()
                        }
                    }
                }

            }

            _adapter = ConversationMessageAdapter(screen, object : ConversationMessageListener {
                override fun onFile(file: AttachedFile.Conversation) {
                }

                override fun onMention(id: String) {
                    openDialog(ContactDetailFragment.newInstance(id))
                }

                override fun onPhoneNumber(phone: String) {
                }

                override fun onRemoveImportant(message: Message) {
                }

                override fun onGoTo(message: Message) {
                }

                override fun onRemovePin(message: Message) {
                }

                override fun onAvatar(personSend: User) {
                    openDialog(ContactDetailFragment.newInstance(personSend.id))
                }
            })
            rcvMessage.apply {
                adapter = _adapter
                setItemAnimator(null)
                addOnScrollListener(_endless)
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.conversationMessage.postValue(null)
    }

    override fun onSubscribeObserver() {
        with(viewModel) {
            conversationMessage.observe(this@ConversationMessageFragment) { list ->
                list.notNull { handleData(it) }
            }
        }
    }

    override fun registerEvent() {
        with(binding) {
            toolbar.setNavigationOnClickListener { popFragment() }
            swipeLayout.setOnRefreshListener {
                _page = 0

                _endless.refreshPage()
                swipeLayout.isRefreshing = false

                callApi()
            }
        }
    }

    override fun callApi() {
        when (screen) {
            MESSAGE_PIN -> {
                viewModel.getPinMessage(_page)
            }

            MESSAGE_IMPORTANT -> {
                viewModel.getImportantMessage(_page)
            }
        }
    }

    private fun handleData(list: ArrayList<Message>) {
        _currentSize = list.size
        _isLoadMore = false
        _adapter.cancelLoadMore()
        if (_page == 1) {
            if (list.isEmpty()) {
                binding.apply {
                    rcvMessage.gone()
                    viewNoData.lnNoData.show()
                }
            } else {
                binding.apply {
                    rcvMessage.show()
                    viewNoData.lnNoData.gone()
                }
                _adapter.insertData(list)
            }
        } else {
            if (list.isNotEmpty()) {
                _adapter.addMore(list)
            }
        }
    }
}