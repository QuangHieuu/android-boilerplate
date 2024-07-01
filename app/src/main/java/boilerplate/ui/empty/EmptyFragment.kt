package boilerplate.ui.empty

import boilerplate.base.BaseFragment
import boilerplate.databinding.ActivityEmptyBinding
import boilerplate.ui.main.MainVM
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class EmptyFragment : BaseFragment<ActivityEmptyBinding, MainVM>() {
    companion object {
        fun newInstance(): EmptyFragment {
            return EmptyFragment()
        }
    }

    override val mViewModel: MainVM by activityViewModel()

    override fun bindingFactory(): ActivityEmptyBinding =
        ActivityEmptyBinding.inflate(layoutInflater)

    override fun initialize() {
    }

    override fun onSubscribeObserver() {
    }

    override fun registerOnClick() {
        with(binding) {
        }
    }

    override fun callApi() {
    }
}