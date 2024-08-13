package boilerplate.data.local.repository.server

import boilerplate.data.local.sharedPrefs.SharedPrefsApi
import boilerplate.data.local.sharedPrefs.SharedPrefsKey
import boilerplate.data.remote.api.ApiUrl

interface ServerRepository {
	fun saveServer(server: String)

	fun getServer(): String
}

class ServerRepositoryImpl(
	private val share: SharedPrefsApi,
) : ServerRepository {
	override fun saveServer(server: String) {
		ApiUrl.setHost(server)
		share.put(SharedPrefsKey.SERVER, server)
	}

	override fun getServer(): String =
		share.get(SharedPrefsKey.SERVER, String::class.java)
			.ifEmpty { ApiUrl.DEFAULT }
			.also { ApiUrl.setHost(it) }
}
