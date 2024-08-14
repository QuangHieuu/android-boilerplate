package boilerplate.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import boilerplate.base.BaseViewModel
import boilerplate.data.local.repository.server.ServerRepository
import boilerplate.data.local.repository.user.TokenRepository
import boilerplate.data.local.repository.user.UserRepository
import boilerplate.data.remote.api.ApiObservable
import boilerplate.data.remote.repository.auth.LoginRepository
import boilerplate.data.remote.repository.conversation.ConversationRepository
import boilerplate.model.device.Device
import boilerplate.model.user.User
import boilerplate.utils.extension.notNull
import boilerplate.utils.extension.result
import boilerplate.utils.extension.withScheduler
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

class StartVM(
	private val tokenImpl: TokenRepository,
	private val userImpl: UserRepository,
	private val serverImpl: ServerRepository,
	private val loginServer: LoginRepository,
	private val conImpl: ConversationRepository
) : BaseViewModel() {

	companion object {
		const val STATE_LOGIN = 0
		const val STATE_CHECK_LOGIN = 1
		const val STATE_AUTO_LOGIN = 2
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
			loginServer.postUserLogin(userName, password)
				.withScheduler(schedulerProvider)
				.result({
					userImpl.saveUseLogin(userName, password)
					tokenImpl.saveToken(it.tokenType!!, it.accessToken!!)
					_state.postValue(STATE_CHECK_LOGIN)
				}, { setLoading(false) })
		}
	}

	fun getMe() {
		launchDisposable {
			loginServer.getMe()
				.flatMap { res ->
					val role = loginServer.getRolePermission()
					val contact = loginServer.getContact()
					val config = conImpl.getConversationConfig()
					Single.concat(role, contact, config)
						.toList()
						.toFlowable()
						.flatMap { Flowable.just(res) }
				}
				.withScheduler(schedulerProvider)
				.result({ res ->
					res.result.notNull {
						_user.postValue(it)
						registerDeviceId(it)
					}
				}, {
					setLoading(false)
					cancelAutoLogin()
				})
		}
	}

	fun cancelAutoLogin() {
		onCleared()
		userImpl.wipeUserData()
		tokenImpl.wipeToken()

		_state.postValue(STATE_LOGIN)
		_user.postValue(null)
	}

	fun getServer() = serverImpl.getServer()

	fun setServer(server: String) {
		serverImpl.saveServer(server)
	}

	private fun registerDeviceId(user: User) {
		FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
			if (task.isComplete) {
				task.result.notNull { currentToken ->
					val device: Device = Device().apply {
						id = user.id
						deviceToken = currentToken
						deviceType = 0
					}
					launchDisposable {
						loginServer.postRegisterDevice(device)
							.withScheduler(schedulerProvider)
							.subscribeWith(ApiObservable.apiCallback(success = {
								it.result.notNull { device ->
									if (!device.deviceId.isNullOrEmpty()) {
										tokenImpl.saveDeviceId(device.id!!)
									}
								}
							}))
					}
				}
			}
		}
	}
}