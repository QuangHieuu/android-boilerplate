package boilerplate.ui.chat

import boilerplate.base.BaseActivity
import boilerplate.databinding.ActivityChatBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChatActivity : BaseActivity<ActivityChatBinding, ChatVM>() {
    override val mViewModel: ChatVM by viewModel()

    override fun initialize() {
    }

    override fun onSubscribeObserver() {
    }

    override fun registerOnClick() {
    }

    override fun callApi() {
    }
}