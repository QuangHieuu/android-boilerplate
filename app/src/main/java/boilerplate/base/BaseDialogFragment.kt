package boilerplate.base

import android.app.Dialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.contains
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import boilerplate.R
import boilerplate.utils.extension.*
import boilerplate.widget.customText.AppEditText
import boilerplate.widget.loading.LoadingLayout
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import java.lang.reflect.ParameterizedType

abstract class BaseDialogFragment<AC : ViewBinding, VM : BaseViewModel> : DialogFragment() {
	private var _binding: AC? = null
	protected val binding: AC
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
			.let { it as AC }
			.apply { _binding = this }.root.apply {
				isClickable = true
				isFocusable = true
				setBackgroundColor(ContextCompat.getColor(context, R.color.colorAppBackground))
			}
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return object : Dialog(requireActivity()) {
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
						if (view is AppEditText && view.isFocusableInTouchMode) {
							view.hideKeyboard()
						}
						return consumed
					}
				}
				return super.dispatchTouchEvent(ev)
			}
		}.apply {
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
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setWidthPercent()

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
				binding.root.showFail(it)
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
		_disposable.apply {
			clear()
			dispose()
		}
	}

	protected abstract fun initialize()

	protected abstract fun onSubscribeObserver()

	protected abstract fun registerEvent()

	protected abstract fun callApi()

	protected fun launchDisposable(vararg job: Disposable) {
		_disposable.addAll(*job)
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
		if (isTablet()) {
			dismiss()
		} else {
			parentFragmentManager.popBackStack()
		}
	}
}