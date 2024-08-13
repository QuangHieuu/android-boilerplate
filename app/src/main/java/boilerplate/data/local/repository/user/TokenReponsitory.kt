package boilerplate.data.local.repository.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import boilerplate.data.local.sharedPrefs.SharedPrefsApi
import boilerplate.data.local.sharedPrefs.SharedPrefsKey.ACCESS_ONLY_TOKEN
import boilerplate.data.local.sharedPrefs.SharedPrefsKey.ACCESS_TOKEN
import boilerplate.data.local.sharedPrefs.SharedPrefsKey.DEVICE_ID
import boilerplate.data.local.sharedPrefs.SharedPrefsKey.DEVICE_TOKEN

interface TokenRepository {
	fun saveToken(type: String, token: String)

	fun getToken(): String

	fun getOnlyToken(): String

	fun getTokenLiveData(): LiveData<String>

	fun wipeToken()

	fun saveConnectedId(id: String)

	fun getConnectedId(): String

	fun saveDeviceId(id: String)

	fun getDeviceId(): String
}

class TokenRepositoryImpl(
	private val share: SharedPrefsApi
) : TokenRepository {

	private val _token by lazy { MutableLiveData(getToken()) }
	private var _connectionId = ""

	override fun saveToken(type: String, token: String) {
		val fullToken = String.format("%s %s", type, token)
		share.put(ACCESS_TOKEN, fullToken)
		share.put(ACCESS_ONLY_TOKEN, token)

		_token.postValue(fullToken)
	}

	override fun getToken() = share.get(ACCESS_TOKEN, String::class.java)

	override fun getOnlyToken() = share.get(ACCESS_ONLY_TOKEN, String::class.java)

	override fun getTokenLiveData(): LiveData<String> = _token

	override fun wipeToken() {
		share.clearKey(ACCESS_TOKEN)
		share.clearKey(ACCESS_ONLY_TOKEN)
		share.clearKey(DEVICE_TOKEN)

		_token.postValue("")
	}

	override fun saveConnectedId(id: String) {
		_connectionId = id
	}

	override fun getConnectedId(): String = _connectionId

	override fun saveDeviceId(id: String) {
		share.put(DEVICE_ID, id)
	}

	override fun getDeviceId(): String {
		return share.get(DEVICE_ID, String::class.java)
	}
}