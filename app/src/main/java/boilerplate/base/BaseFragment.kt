package boilerplate.base

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.contains
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import boilerplate.R
import boilerplate.utils.extension.Permission
import boilerplate.utils.extension.addTo
import boilerplate.utils.extension.notNull
import boilerplate.utils.extension.removeSelf
import boilerplate.utils.extension.showFail
import boilerplate.widget.loading.LoadingLayout
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import java.lang.reflect.ParameterizedType

fun <T : BaseFragment<*, *>> T.putBundle(vararg pairs: Pair<String, Any?>): T {
	arguments = bundleOf(*pairs)
	return this
}

abstract class BaseFragment<VB : ViewBinding, VM : BaseViewModel> : Fragment() {
	open val TAG = this.javaClass.simpleName

	private var _binding: VB? = null
	protected val binding: VB
		get() = checkNotNull(_binding) { "View not create" }

	private val _loadingLayout: LoadingLayout by lazy { LoadingLayout(requireActivity()) }
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
			.let { it as VB }
			.apply { _binding = this }.root.apply {
				isClickable = true
				isFocusable = true
				setBackgroundColor(ContextCompat.getColor(context, R.color.colorAppBackground))
			}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		with(viewModel) {
			loading.observe(viewLifecycleOwner) { show ->
				if (view is ViewGroup) {
					if (show && isVisible) {
						if (!view.contains(_loadingLayout)) {
							_loadingLayout.addTo(view)
						}
					} else {
						_loadingLayout.removeSelf()
					}
				}
			}
			error.observe(viewLifecycleOwner) {
				showFail(it)
			}
		}

		if (isAddKeyBoardListener()) {
			addKeyboardListener()
		}

		initialize()
		onSubscribeObserver()
		binding.registerEvent()
		callApi()
	}

	override fun onDestroyView() {
		_binding = null
		_blockGrand = null
		_request.unregister()
		_listRequest.clear()
		_disposable.apply {
			clear()
			dispose()
		}
		removeKeyBoardListener()
		super.onDestroyView()
	}

	protected abstract fun initialize()

	protected abstract fun onSubscribeObserver()

	protected abstract fun VB.registerEvent()

	protected abstract fun callApi()

	protected fun launchDisposable(vararg job: Disposable) {
		_disposable.addAll(*job)
	}

	open fun isAddKeyBoardListener(): Boolean = false

	open fun keyBoardListener(isShowKeyboard: Boolean) {}

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

	private fun addKeyboardListener() {
		if (activity is BaseActivity<*, *>) {
			(activity as BaseActivity<*, *>).addKeyBoardListener(TAG) { isShowKeyboard ->
				keyBoardListener(isShowKeyboard)
			}
		}
	}

	private fun removeKeyBoardListener() {
		if (activity is BaseActivity<*, *>) {
			(activity as BaseActivity<*, *>).removeKeyBoardListener(TAG)
		}
	}
}