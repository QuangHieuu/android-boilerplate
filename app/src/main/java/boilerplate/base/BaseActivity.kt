package boilerplate.base

import android.app.Dialog
import android.graphics.Rect
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import boilerplate.R
import boilerplate.utils.extension.hideKeyboard
import boilerplate.utils.extension.notNull
import boilerplate.utils.extension.showSnackBarFail
import boilerplate.utils.extension.statusBarHeight
import boilerplate.widget.customText.EditTextFont
import boilerplate.widget.loading.LoadingScreen
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import it.cpc.vn.permission.PermissionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.reflect.ParameterizedType

abstract class BaseActivity<AC : ViewBinding, VM : BaseViewModel> : AppCompatActivity() {
    protected abstract val mViewModel: VM
    private var _binding: AC? = null
    protected val binding: AC get() = _binding!!

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
        setContentView(
            @Suppress("UNCHECKED_CAST")
            javaClass.genericSuperclass.let { it as ParameterizedType }.actualTypeArguments[0]
                .let { it as Class<*> }.getMethod("inflate", LayoutInflater::class.java)
                .invoke(null, layoutInflater)
                .let { it as AC }
                .apply { _binding = this }.root
        )

        lifecycleScope.launch(Dispatchers.Main) {
            val window: Window = window
            val background =
                ContextCompat.getDrawable(this@BaseActivity, R.drawable.bg_app)?.apply {
                    val layerDrawable = mutate() as LayerDrawable
                    layerDrawable.findDrawableByLayerId(R.id.bg_app_background).mutate()
                    val height = window.statusBarHeight()
                    layerDrawable.setLayerInsetTop(1, height)
                }

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.setBackgroundDrawable(background)
        }

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
                    binding.root.showSnackBarFail(getString(R.string.error_auth_wrong))
                }
            }
        }
    }

}