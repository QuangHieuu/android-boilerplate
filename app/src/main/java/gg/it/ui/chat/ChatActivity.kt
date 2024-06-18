package gg.it.ui.chat

import gg.it.base.BaseActivity
import gg.it.databinding.ActivityChatBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChatActivity : BaseActivity<ActivityChatBinding, ChatVM>() {
    override val mViewModel: ChatVM by viewModel()

    override fun bindingFactory(): ActivityChatBinding = ActivityChatBinding.inflate(layoutInflater)

    override fun initialize() {
    }

    override fun onSubscribeObserver() {
    }

    override fun registerOnClick() {
    }

    override fun callApi() {
    }
}