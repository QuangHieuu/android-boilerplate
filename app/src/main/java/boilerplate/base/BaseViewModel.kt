package boilerplate.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import boilerplate.data.remote.api.ApiObservable
import boilerplate.data.remote.api.OnApiCallBack
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

    protected val _loading by lazy { MutableLiveData<Boolean>() }
    val loading: LiveData<Boolean> = _loading

    protected val _inValidLogin by lazy { MutableLiveData<Boolean>() }
    val inValidaLogin: LiveData<Boolean> = _inValidLogin
    protected val _inValidToken by lazy { MutableLiveData<Boolean>() }
    val inValidaToken: LiveData<Boolean> = _inValidToken

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

    private val compositeDisposable = CompositeDisposable()

    private fun setApiCallback(gson: Gson) {
        ApiObservable.setServerResponseListener(object : OnApiCallBack {
            override fun invalidLogin() {
                _inValidLogin.postValue(true)
            }

            override fun invalidToken() {
                _inValidToken.postValue(true)
            }

            override fun onServerError(errorCode: Int, api: String, showError: Boolean) {
//                if (errorCode >= 500) {
//                    Firebase.reportServerError(this, api, AccountManager.getUsername(this))
//                    return
//                }
//                if (showError && NetworkUtil.isNetworkConnected(this)) {
//                    ViewUtil.showToastFail(this, getString(R.string.generalError))
//                }
            }
        }, gson)
    }

    protected fun launchDisposable(job: () -> Disposable) {
        compositeDisposable.add(job())
    }

    fun setLoading(boolean: Boolean) {
        _loading.postValue(boolean)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
        if (viewModelScope != null) {
            viewModelJob?.cancel()
        }
    }
}