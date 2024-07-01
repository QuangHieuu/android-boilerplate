<<<<<<<< HEAD:app/src/main/java/boilerplate/ui/workManager/WorkManagerActivity.kt
package boilerplate.ui.workManager
========
package boilerplate.ui.work
>>>>>>>> 899900c (feature/add_dynamic_host):app/src/main/java/boilerplate/ui/work/WorkManagerActivity.kt

import boilerplate.base.BaseActivity
import boilerplate.databinding.ActivityWorkManagerBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class WorkManagerActivity : BaseActivity<ActivityWorkManagerBinding, WorkManagerVM>() {
    override val mViewModel: WorkManagerVM by viewModel()

    override fun initialize() {
    }

    override fun onSubscribeObserver() {
    }

    override fun registerOnClick() {
    }

    override fun callApi() {
    }
}