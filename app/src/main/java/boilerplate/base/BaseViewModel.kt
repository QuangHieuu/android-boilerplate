package boilerplate.base

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import boilerplate.R
import boilerplate.data.remote.api.ApiObservable
import boilerplate.data.remote.api.OnApiCallBack
import boilerplate.utils.InternetManager
import com.google.gson.Gson
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.koin.java.KoinJavaComponent

abstract class BaseViewModel : ViewModel() {
	val gson: Gson by KoinJavaComponent.inject<Gson>(Gson::class.java).also {
		setApiCallback(it.value)

	}

	val application: Application by KoinJavaComponent.inject(Application::class.java)

	val limit: Int by KoinJavaComponent.inject(Int::class.java)

	protected val _loading by lazy { MutableLiveData<Boolean>() }
	val loading: LiveData<Boolean> = _loading

	protected val _error by lazy { MutableLiveData<String>() }
	val error = _error

	/**
	 * This is the job for all coroutines started by this ViewModel.
	 *
	 * Cancelling this job will cancel all coroutines started by this ViewModel.
	 */
	private val viewModelJob: Job? by lazy { Job() }

	/**
	 * This is the scope for all coroutines launched by [ViewModel].
	 *
	 * Since we pass [viewModelJob], you can cancel all coroutines launched by [viewModelScope] by calling
	 * viewModelJob.cancel().  This is called in [onCleared].
	 */
	private val viewModelScope: CoroutineScope? by lazy {
		CoroutineScope(Dispatchers.Main + viewModelJob as Job)
	}

	private val _disposable = CompositeDisposable()

	private fun setApiCallback(gson: Gson) {
		ApiObservable.setServerResponseListener(object : OnApiCallBack {
			override fun notInternet() {
				_error.postValue(application.getString(R.string.error_no_connection))
			}

			override fun invalidLogin() {
				_error.postValue(application.getString(R.string.error_auth_wrong))
			}

			override fun invalidToken() {
				Intent(BaseApp.APP_FILTER_INVALID)
					.apply {
						putExtra(BaseApp.APP_FILTER_INVALID, Bundle().apply {
							putExtra(BaseApp.APP_FILTER_INVALID, true)
						})
					}
					.let { LocalBroadcastManager.getInstance(application).sendBroadcast(it) }
			}

			override fun onServerError(errorCode: Int, api: String, showError: Boolean) {
				if (!showError) {
					return
				}
				if (errorCode >= 500) {
					_error.postValue(application.getString(R.string.error_general))
//                    Firebase.reportServerError(this, api, AccountManager.getUsername(this))
					return
				}
				if (InternetManager.isConnected()) {
					_error.postValue(application.getString(R.string.error_general))
				}
			}
		}, gson)
	}

	protected fun launchDisposable(job: () -> Disposable) {
		_disposable.add(job())
	}

	protected fun launchDisposable(vararg job: Disposable) {
		_disposable.addAll(*job)
	}

	fun setLoading(boolean: Boolean) {
		_loading.postValue(boolean)
	}

	override fun onCleared() {
		super.onCleared()
		_disposable.clear()
		if (viewModelScope != null) {
			viewModelJob?.cancel()
		}
	}
}