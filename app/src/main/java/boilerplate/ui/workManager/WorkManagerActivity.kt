package boilerplate.ui.workManager

import boilerplate.base.BaseActivity
import boilerplate.databinding.ActivityWorkManagerBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class WorkManagerActivity : BaseActivity<ActivityWorkManagerBinding, WorkManagerVM>() {
    override val mViewModel: WorkManagerVM by viewModel()

    override fun bindingFactory(): ActivityWorkManagerBinding =
        ActivityWorkManagerBinding.inflate(layoutInflater)

    override fun initialize() {
    }

    override fun onSubscribeObserver() {
    }

    override fun registerOnClick() {
    }

    override fun callApi() {
    }
}