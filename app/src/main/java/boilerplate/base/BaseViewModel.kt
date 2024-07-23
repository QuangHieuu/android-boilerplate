package boilerplate.base

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

abstract class BaseViewModel() : ViewModel() {
    init {
        val gson: Gson by KoinJavaComponent.inject(Gson::class.java)
        setApiCallback(gson)
    }

    val limit: Int by KoinJavaComponent.inject(Int::class.java)

    protected val _loading by lazy { MutableLiveData<Boolean>() }
    val loading: LiveData<Boolean> = _loading

    protected val _inValidLogin by lazy { MutableLiveData<Boolean>() }
    val inValidaLogin: LiveData<Boolean> = _inValidLogin
    protected val _inValidToken by lazy { MutableLiveData<Boolean>() }
    val inValidaToken: LiveData<Boolean> = _inValidToken
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
                _error.postValue(Resources.getSystem().getString(R.string.error_no_connection))
            }

            override fun invalidLogin() {
                _inValidLogin.postValue(true)
            }

            override fun invalidToken() {
                _inValidToken.postValue(true)
            }

            override fun onServerError(errorCode: Int, api: String, showError: Boolean) {
                if (errorCode >= 500) {
                    _error.postValue(Resources.getSystem().getString(R.string.error_general))
//                    Firebase.reportServerError(this, api, AccountManager.getUsername(this))
                    return
                }
                if (showError && InternetManager.isConnected()) {
                    _error.postValue(Resources.getSystem().getString(R.string.error_general))
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