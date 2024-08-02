package boilerplate.base

import android.app.Dialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.contains
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import boilerplate.R
import boilerplate.utils.extension.Permission
import boilerplate.utils.extension.addTo
import boilerplate.utils.extension.isTablet
import boilerplate.utils.extension.notNull
import boilerplate.utils.extension.removeSelf
import boilerplate.utils.extension.setWidthPercent
import boilerplate.utils.extension.showSnackBarFail
import boilerplate.widget.loading.LoadingScreen
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.launch
import java.lang.reflect.ParameterizedType


abstract class BaseDialogFragment<AC : ViewBinding, VM : BaseViewModel> : DialogFragment() {
    private var _binding: AC? = null
    protected val binding: AC
        get() = checkNotNull(_binding) { "View not create" }

    private val _loadingScreen: LoadingScreen by lazy { LoadingScreen(requireActivity()) }
    private val _disposable: CompositeDisposable by lazy { CompositeDisposable() }

    protected abstract val viewModel: VM

    private lateinit var _request: ActivityResultLauncher<Array<String>>
    private var _blockGrand: (() -> Unit)? = null
    private val _listRequest: ArrayList<Permission> = arrayListOf()

    @Suppress("UNCHECKED_CAST")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _request = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            result.notNull {
                for (entry in it.entries) {
                    _listRequest.removeIf { per -> per.name == entry.key && entry.value }
                }
                if (_listRequest.isEmpty()) {
                    _blockGrand.notNull { it() }
                    _blockGrand = null
                }
            }
        }
        return javaClass.genericSuperclass.let { it as ParameterizedType }.actualTypeArguments[0]
            .let { it as Class<*> }.getMethod(
                "inflate",
                LayoutInflater::class.java,
                ViewGroup::class.java,
                Boolean::class.java
            )
            .invoke(null, layoutInflater, container, false)
            .let { it as AC }
            .apply { _binding = this }.root.apply {
                setBackgroundColor(ContextCompat.getColor(context, R.color.colorAppBackground))
            }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState).apply {
            setOnKeyListener { v: DialogInterface?, keyCode: Int, event: KeyEvent ->
                if ((keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP)) {
                    dismiss()
                    return@setOnKeyListener true
                } else {
                    return@setOnKeyListener false
                }
            }
            setCanceledOnTouchOutside(false)
            setCancelable(false)
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setWidthPercent()

        with(viewModel) {
            loading.observe(viewLifecycleOwner) { show ->
                if (view is ViewGroup) {
                    if (show && isVisible) {
                        if (!view.contains(_loadingScreen)) {
                            _loadingScreen.addTo(view)
                        }
                    } else {
                        _loadingScreen.removeSelf()
                    }
                }
            }
            error.observe(viewLifecycleOwner) {
                binding.root.showSnackBarFail(it)
            }
        }
        initialize()
        onSubscribeObserver()
        registerEvent()
        callApi()
    }

    override fun onDestroyView() {
        _binding = null
        _blockGrand = null
        _request.unregister()
        _listRequest.clear()
        super.onDestroyView()
        clearAdjustSoftInput()
        _disposable.apply {
            clear()
            dispose()
        }
    }

    protected abstract fun initialize()

    protected abstract fun onSubscribeObserver()

    protected abstract fun registerEvent()

    protected abstract fun callApi()

    protected fun popFragment() {
        parentFragmentManager.popBackStack()
    }

    protected fun launchDisposable(vararg job: Disposable) {
        _disposable.addAll(*job)
    }

    protected fun adjustSoftInput() {
        lifecycleScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                requireActivity().window?.let {
                    WindowCompat.setDecorFitsSystemWindows(it, false)
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

    private fun clearAdjustSoftInput() {
        lifecycleScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                requireActivity().window?.let {
                    WindowCompat.setDecorFitsSystemWindows(it, true)
                }
            } else {
                requireActivity().window.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
                )
            }
        }
    }

    fun permission(permissions: Array<String>, grand: () -> Unit) {
        _blockGrand = grand
        for (permission in permissions) {
            if (isGranted(permission)) {
                continue
            }
            _listRequest.add(Permission(permission, false))
        }
        if (_listRequest.isEmpty()) {
            _blockGrand.notNull { it() }
            _blockGrand = null
        } else {
            _request.launch(_listRequest.map { it.name }.toTypedArray())
        }
    }

    private fun isGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    protected fun handleBack() {
        if (context?.isTablet() == true) {
            dismiss()
        } else {
            popFragment()
        }
    }
}