package boilerplate.ui.splash

import android.os.Bundle
import android.view.View
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import boilerplate.R
import boilerplate.base.BaseActivity
import boilerplate.databinding.ActivityStartBinding
import boilerplate.ui.main.MainActivity
import boilerplate.utils.ClickUtil
import boilerplate.utils.extension.AnimateType
import boilerplate.utils.extension.goTo
import boilerplate.utils.extension.notNull
import boilerplate.utils.extension.replaceFragmentInActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class StartActivity : BaseActivity<ActivityStartBinding, StartVM>() {
    override val mViewModel: StartVM by viewModel()

    override fun bindingFactory(): ActivityStartBinding =
        ActivityStartBinding.inflate(layoutInflater)

    private lateinit var splashScreen: SplashScreen

    private var fromLoginScreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        splashScreen = installSplashScreen().apply {
            setKeepOnScreenCondition { true }
        }
        super.onCreate(savedInstanceState)
    }

    override fun initialize() {

    }

    override fun onSubscribeObserver() {
        with(mViewModel) {
            state.observe(this@StartActivity) {
                when (it) {
                    StartVM.STATE_LOGIN -> {
                        with(binding.viewAutoLogin) {
                            frameAutoLogin.visibility = View.GONE
                            viewLoading.pulseView.stop()
                        }
                        openLoginScreen()
                    }

                    StartVM.STATE_AUTO_LOGIN -> {
                        val token = mViewModel.token.value
                        lifecycleScope.launch(Dispatchers.Main) {
                            if (token.isNullOrEmpty()) {
                                openLoginScreen()
                            } else {
                                with(binding.viewAutoLogin) {
                                    frameAutoLogin.visibility = View.VISIBLE
                                    viewLoading.pulseView.start()
                                }
                                mViewModel.getMe(false)
                            }
                            delay(200)
                            splashScreen.setKeepOnScreenCondition { false }
                        }
                    }
                }
            }

            user.observe(this@StartActivity) {
                it.notNull {
                    lifecycleScope.launch(Dispatchers.Main) {
                        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                            delay(500)
                            goTo(MainActivity::class).also { finish() }
                        }
                    }
                }
            }
        }
    }

    override fun registerOnClick() {
        with(binding.viewAutoLogin) {
            tvCancel.setOnClickListener(ClickUtil.onClick {
                mViewModel.cancelAutoLogin()
            })
        }
    }

    override fun callApi() {
    }

    private fun openLoginScreen() {
        fromLoginScreen = true
        val fragment: LoginFragment = LoginFragment.newInstance()
        replaceFragmentInActivity(
            R.id.frame_init_contain,
            fragment,
            animateType = AnimateType.FADE,
            addToBackStack = false
        )
    }
}