package gg.it.ui.empty

import android.content.Intent
import gg.it.base.BaseActivity
import gg.it.databinding.ActivityEmptyBinding
import gg.it.ui.main.MainVM
import org.koin.androidx.viewmodel.ext.android.viewModel

class EmptyActivity : BaseActivity<ActivityEmptyBinding, MainVM>() {
override val mViewModel: MainVM by viewModel()

    override fun bindingFactory(): ActivityEmptyBinding =
        ActivityEmptyBinding.inflate(layoutInflater)

    override fun initialize() {
    }

    override fun onSubscribeObserver() {
    }

    override fun registerOnClick() {
        with(binding) {
//            tvEmpty.setOnClickListener {
//                startActivity(
//                    Intent(
//                        baseContext,
//                        ChatActivity::class.java
//                    )
//                )
//            }
        }
    }

    override fun callApi() {
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

//        startActivity(Intent(this, ChatActivity::class.java))
    }
}