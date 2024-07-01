package boilerplate.base

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.launch

abstract class BaseFragment<AC : ViewBinding, VM : BaseViewModel> : Fragment() {
    private val mDisposable: CompositeDisposable by lazy { CompositeDisposable() }

    protected val binding: AC by lazy { bindingFactory() }

    protected abstract val mViewModel: VM

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.isClickable = true
        view.isFocusable = true

        initialize()
        onSubscribeObserver()
        registerOnClick()
        callApi()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearAdjustSoftInput()
    }

    protected abstract fun bindingFactory(): AC

    protected abstract fun initialize()

    protected abstract fun onSubscribeObserver()

    protected abstract fun registerOnClick()

    protected abstract fun callApi()

    protected fun runOnMainUI(runnable: Runnable?) {
        lifecycle.run {

        }
        if (!isAdded) {
            return
        }
        requireActivity().runOnUiThread(runnable)
    }

    protected fun runOnMainUI(runnable: Runnable?, delay: Int) {
        if (!isAdded) {
            return
        }
        requireActivity().runOnUiThread {
            Handler(Looper.getMainLooper()).postDelayed(
                runnable!!, delay.toLong()
            )
        }
    }

    protected fun popFragment() {
        requireActivity().supportFragmentManager.popBackStack()
    }

    protected fun setLoading(visibility: Int) {
//        if (mLayoutLoading != null && mPulseLayout != null) {
//            mLayoutLoading.setVisibility(visibility)
//            if (visibility == View.VISIBLE) {
//                mPulseLayout.start()
//            } else {
//                mPulseLayout.stop()
//            }
//        }
    }

    protected fun launchDisposable(vararg job: Disposable) {
        mDisposable.addAll(*job)
    }

//    protected fun openFragment(fragment: BaseFragment) {
//        if (!this.isAdded) {
//            return
//        }
//        val transaction = requireActivity().supportFragmentManager.beginTransaction()
//        transaction.setCustomAnimations(
//            R.anim.enter_from_right,
//            R.anim.stay,
//            R.anim.stay,
//            R.anim.exit_to_right
//        )
//        transaction.add(R.id.frame_main, fragment)
//        transaction.addToBackStack(fragment.tag())
//        transaction.commit()
//    }

    protected fun adjustSoftInput() {
        lifecycleScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                requireActivity().window?.let {
                    WindowCompat.setDecorFitsSystemWindows(
                        it,
                        false
                    )
                }
            } else {
                @Suppress("DEPRECATION")
                requireActivity().window
                    .setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
                                or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    )
            }
        }
    }

    protected fun clearAdjustSoftInput() {
        lifecycleScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                requireActivity().window?.let {
                    WindowCompat.setDecorFitsSystemWindows(
                        it,
                        true
                    )
                }
            } else {
                requireActivity().window.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
                )
            }
        }
    }
}