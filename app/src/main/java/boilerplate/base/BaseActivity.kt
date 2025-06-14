package boilerplate.base

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.animation.PathInterpolatorCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewbinding.ViewBinding
import boilerplate.model.file.MimeType
import boilerplate.utils.extension.*
import boilerplate.utils.keyboard.InsetsWithKeyboardCallback
import boilerplate.widget.customtext.AppEditText
import boilerplate.widget.customtext.InsetsLayout
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject
import java.lang.ref.WeakReference
import java.lang.reflect.ParameterizedType

abstract class BaseActivity<AC : ViewBinding, VM : BaseViewModel> : AppCompatActivity() {

	protected abstract val viewModel: VM
	private var _binding: AC? = null
	protected val binding: AC get() = _binding!!

	private val _application: Application by inject(Application::class.java)

	private val _disposable = CompositeDisposable()
	private var _blockGrand: (() -> Unit)? = null
	private val _listRequest: WeakReference<ArrayList<Permission>> = WeakReference(arrayListOf())
	private val _hashMap: HashMap<String, (isShowKeyboard: Boolean) -> Unit> = HashMap()
	private val _frameContainer: InsetsLayout = InsetsLayout(_application)

	private val _invalidTokeReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			intent.notNull {
				val bundle: Bundle = it.getBundleExtra(BaseApp.APP_FILTER_INVALID) ?: Bundle()
				val data = bundle.getBoolean(BaseApp.APP_FILTER_INVALID, false)
				if (data) {
					onLogout()
				}
			}
		}
	}

	private lateinit var _request: ActivityResultLauncher<Array<String>>

	override fun onCreate(savedInstanceState: Bundle?) {
		_request = registerForActivityResult(
			ActivityResultContracts.RequestMultiplePermissions()
		) { result ->
			result.notNull { map ->
				for (entry in map.entries) {
					_listRequest.get()?.removeIf { per -> per.name == entry.key && entry.value }
				}
				if (_listRequest.get().isNullOrEmpty()) {
					_blockGrand.notNull { it() }
					_blockGrand = null
				}
			}
		}
		super.onCreate(savedInstanceState)
		setContentView(
			@Suppress("UNCHECKED_CAST")
			javaClass.genericSuperclass.let { it as ParameterizedType }.actualTypeArguments[0]
				.let { it as Class<*> }.getMethod("inflate", LayoutInflater::class.java)
				.invoke(null, layoutInflater)
				.let { it as AC }
				.apply { _binding = this }.root
		)

		val insetsWithKeyboardCallback = InsetsWithKeyboardCallback(window) { show ->
			keyboardCallBack(show)
		}
		ViewCompat.setOnApplyWindowInsetsListener(getWindowInsets(), insetsWithKeyboardCallback)
		ViewCompat.setWindowInsetsAnimationCallback(getWindowInsets(), insetsWithKeyboardCallback)

		validateRes(containerId) {
			ViewCompat.setOnApplyWindowInsetsListener(findViewById(this)) { v, insets ->
				val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
				v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
				insets
			}
		}

		initialize()
		receiveIntent(intent)
		baseObserver()
		onSubscribeObserver()
		registerOnClick()
	}

	override fun onDestroy() {
		_blockGrand = null
		_request.unregister()
		_listRequest.clear()
		_hashMap.clear()
		_disposable.apply {
			clear()
			dispose()
		}
		LocalBroadcastManager.getInstance(application).unregisterReceiver(_invalidTokeReceiver)
		super.onDestroy()
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
				if (view is AppEditText && view.isFocusableInTouchMode) {
					view.hideKeyboard()
				}
				return consumed
			}
		}
		return super.dispatchTouchEvent(ev)
	}

	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		receiveIntent(intent)
	}

	protected abstract fun initialize()

	protected abstract fun onSubscribeObserver()

	protected abstract fun registerOnClick()

	protected abstract fun callApi()

	protected fun launchDisposable(job: () -> Disposable) {
		_disposable.add(job())
	}

	protected open fun onLogout() {}

	protected open fun onKeyboardCallBack(isKeyboardShow: Boolean) {
	}

	open var containerId: Int = android.R.id.content

	open var splitContainerId: Int = -1

	open fun getWindowInsets(): View = binding.root

	open fun callbackWhenReceiverData(intent: Intent) {}

	fun permission(permissions: Array<String>, grand: () -> Unit) {
		_blockGrand = grand
		for (permission in permissions) {
			if (isGranted(permission)) {
				continue
			}
			_listRequest.get()?.add(Permission(permission, false))
		}
		if (_listRequest.get().isNullOrEmpty()) {
			_blockGrand.notNull { it() }
			_blockGrand = null
		} else {
			_request.launch(_listRequest.get()!!.map { it.name }.toTypedArray())
		}
	}

	fun addKeyBoardListener(tag: String, block: (isShowKeyboard: Boolean) -> Unit) {
		_hashMap[tag] = block
	}

	fun removeKeyBoardListener(tag: String) {
		_hashMap.remove(tag)
	}

	private fun baseObserver() {
		with(viewModel) {
			error.observe(this@BaseActivity) {
				showFail(it)
			}
		}
	}

	private fun isGranted(permission: String): Boolean {
		return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
	}

	private fun keyboardCallBack(isKeyboardShow: Boolean) {
		onKeyboardCallBack(isKeyboardShow)
		lifecycleScope.launch {
			_hashMap.entries.forEach {
				it.value(isKeyboardShow)
			}
		}
	}

	private fun receiveIntent(intent: Intent) {
		val action = intent.action
		val bundle = intent.extras
		if (action == null || action == Intent.ACTION_MAIN) {
			return
		}
		when (action) {
			Intent.ACTION_SEND -> {
				intent.type.notNull { type ->
					if (bundle != null) bundle.getString(Intent.EXTRA_TEXT, "") else ""
					if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
						intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
					} else {
						intent.getParcelableExtra(Intent.EXTRA_STREAM)
					}
					if (type.contains(MimeType.TEXT.type)) {
						return
					}
					if (type.startsWith(MimeType.IMAGE.type) ||
						type.startsWith(MimeType.VIDEO.type) ||
						type.startsWith(MimeType.AUDIO.type) ||
						type.startsWith(MimeType.APPLICATION.type)
					) {
						return
					}
				}
			}
		}
		callbackWhenReceiverData(intent)
		intent.action = Intent.ACTION_MAIN
	}
}