package boilerplate.base

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import boilerplate.R
import boilerplate.utils.extension.hideKeyboard
import boilerplate.utils.extension.isTablet
import boilerplate.utils.extension.showSnackBarFail
import boilerplate.widget.customText.EditTextFont
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import it.cpc.vn.permission.PermissionUtils
import java.lang.reflect.ParameterizedType

abstract class BaseActivity<AC : ViewBinding, VM : BaseViewModel> : AppCompatActivity() {
    protected abstract val _viewModel: VM
    private var _binding: AC? = null
    protected val binding: AC get() = _binding!!

    private val compositeDisposable = CompositeDisposable()

    private val backPress by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val stack = supportFragmentManager.backStackEntryCount
                val fragmentTablet = supportFragmentManager.findFragmentById(R.id.frame_tablet)
                if (isTablet()) {
                    val hasContain = fragmentTablet != null
                    if (hasContain) {
                        if (stack >= 1) {
                            finish()
                        } else {
                            supportFragmentManager.popBackStack()
                        }
                    } else {
                        closeView(stack)
                    }
                } else {
                    closeView(stack)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            @Suppress("UNCHECKED_CAST")
            javaClass.genericSuperclass.let { it as ParameterizedType }.actualTypeArguments[0]
                .let { it as Class<*> }.getMethod("inflate", LayoutInflater::class.java)
                .invoke(null, layoutInflater)
                .let { it as AC }
                .apply { _binding = this }.root
        )

        PermissionUtils.initPermissionCheck()
        onBackPressedDispatcher.addCallback(this@BaseActivity, backPress)

        initialize()
        baseObserver()
        onSubscribeObserver()
        registerOnClick()

    }

    override fun onDestroy() {
        super.onDestroy()
        PermissionUtils.disposable()
    }

    protected abstract fun initialize()

    protected abstract fun onSubscribeObserver()

    protected abstract fun registerOnClick()

    protected abstract fun callApi()

    protected fun launchDisposable(job: () -> Disposable) {
        compositeDisposable.add(job())
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_UP) {
            val view = currentFocus
            if (view != null) {
                val consumed = super.dispatchTouchEvent(ev)
                val viewTmp = currentFocus
                val viewNew: View = viewTmp ?: view
                if (viewNew == view) {
                    val rect = Rect()
                    val coordinates = IntArray(2)
                    view.getLocationOnScreen(coordinates)
                    rect.set(
                        coordinates[0],
                        coordinates[1],
                        coordinates[0] + view.width,
                        coordinates[1] + view.height
                    )
                    val x = ev.x.toInt()
                    val y = ev.y.toInt()
                    if (rect.contains(x, y)) {
                        return consumed
                    }
                } else if (viewNew is EditText) {
                    return consumed
                }
                if (view is EditTextFont && view.isFocusableInTouchMode) {
                    Log.d("SSS", "activity: ")
                    view.hideKeyboard()
                }
                return consumed
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun baseObserver() {
        with(_viewModel) {
            inValidaLogin.observe(this@BaseActivity) {
                if (it) {
                    binding.root.showSnackBarFail(getString(R.string.error_auth_wrong))
                }
            }
            error.observe(this@BaseActivity) {
                binding.root.showSnackBarFail(it)
            }
        }
    }

    private fun closeView(stack: Int) {
        if (stack == 0) {
            finish()
        } else {
            supportFragmentManager.popBackStack()
        }
    }
}