package gg.it.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import gg.it.base.BaseViewModel
import gg.it.data.remote.api.ApiObservable
import gg.it.data.remote.repository.auth.TokenRepository
import gg.it.data.remote.repository.auth.UserRepository
import gg.it.model.user.User
import gg.it.utils.extension.BaseSchedulerProvider
import gg.it.utils.extension.withScheduler

class StartVM(
    private val schedulerProvider: BaseSchedulerProvider,
    private val tokenImpl: TokenRepository,
    private val userImpl: UserRepository
) : BaseViewModel() {

    companion object {
        const val STATE_LOGIN = 0
        const val STATE_AUTO_LOGIN = 1
    }

    private val _state by lazy { MutableLiveData(STATE_AUTO_LOGIN) }
    val state = _state

    val token: LiveData<String> = tokenImpl.getTokenLiveData()

    private val _userName by lazy { MutableLiveData(userImpl.getUserName()) }
    val userName: LiveData<String> = _userName
    private val _password by lazy { MutableLiveData(userImpl.getUserPassword()) }
    val password: LiveData<String> = _password

    private val _user by lazy { MutableLiveData<User>() }
    val user: LiveData<User> = _user

    fun inputUserName(s: String) {
        _userName.postValue(s)
    }

    fun inputPassword(s: String) {
        _password.postValue(s)
    }

    fun postLogin(userName: String, password: String) {
        setLoading(true)
        launchDisposable {
            userImpl.postUserLogin(userName, password)
                .withScheduler(schedulerProvider)
                .subscribeWith(ApiObservable.apiCallback(success = {
                    userImpl.saveUseLogin(userName, password)
                    tokenImpl.saveToken(it.tokenType!!, it.accessToken!!)
                }, fail = {
                    setLoading(false)
                }))
        }
    }

    fun getMe(showLoading: Boolean) {
        if (showLoading) {
            setLoading(true)
        }
        launchDisposable {
            userImpl.getMe()
                .withScheduler(schedulerProvider)
                .subscribeWith(ApiObservable.apiCallback(success = {
                    _user.postValue(it.result)
                }, fail = {
                    setLoading(false)
                }))
        }
    }

    fun cancelAutoLogin() {
        userImpl.wipeUserData()
        tokenImpl.wipeToken()

        _state.postValue(STATE_LOGIN)
    }
}