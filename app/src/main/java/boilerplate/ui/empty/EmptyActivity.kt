package boilerplate.ui.empty

import android.content.Intent
import boilerplate.base.BaseActivity
import boilerplate.databinding.ActivityEmptyBinding
import boilerplate.ui.main.MainVM
import org.koin.androidx.viewmodel.ext.android.viewModel

class EmptyActivity : BaseActivity<ActivityEmptyBinding, MainVM>() {
    override val mViewModel: MainVM by viewModel()

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