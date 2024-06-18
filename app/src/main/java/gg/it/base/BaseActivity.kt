package gg.it.base

import android.app.Dialog
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import gg.it.R
import gg.it.utils.extension.hideKeyboard
import gg.it.utils.extension.notNull
import gg.it.utils.extension.showSnackBarFail
import gg.it.widget.customText.EditTextFont
import gg.it.widget.loading.LoadingScreen
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import it.cpc.vn.permission.PermissionUtils

abstract class BaseActivity<AC : ViewBinding, VM : BaseViewModel> : AppCompatActivity() {

    protected val binding: AC by lazy { bindingFactory() }
    protected abstract val mViewModel: VM

    private val compositeDisposable = CompositeDisposable()

    private val loadingScreen: Dialog by lazy { LoadingScreen(this) }
    private val backPress by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val stack = supportFragmentManager.backStackEntryCount
                if (stack == 0) {
                    finish()
                } else {
                    supportFragmentManager.popBackStack()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        PermissionUtils.initPermissionCheck()
        onBackPressedDispatcher.addCallback(this@BaseActivity, backPress)

        initialize()
        baseObserver()
        onSubscribeObserver()
        registerOnClick()

    }

    override fun onDestroy() {
        super.onDestroy()

        loadingScreen.notNull { it.dismiss() }

        PermissionUtils.disposable()
    }

    protected abstract fun bindingFactory(): AC

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
                    rect[coordinates[0], coordinates[1], coordinates[0] + view.width] =
                        coordinates[1] + view.height
                    val x = ev.x.toInt()
                    val y = ev.y.toInt()
                    if (rect.contains(x, y)) {
                        return consumed
                    }
                } else if (viewNew is EditText) {
                    return consumed
                }
                if (view is EditTextFont && view.isFocusableInTouchMode) {
                    view.hideKeyboard()
                }
                return consumed
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun baseObserver() {
        with(mViewModel) {
            loading.observe(this@BaseActivity) {
                loadingScreen.notNull { dialog ->
                    if (it) {
                        dialog.show()
                    } else {
                        dialog.dismiss()
                    }
                }
            }
            inValidaLogin.observe(this@BaseActivity) {
                if (it) {
                    showSnackBarFail(getString(R.string.text_auth_wrong), binding.root)
                }
            }
        }
    }

}