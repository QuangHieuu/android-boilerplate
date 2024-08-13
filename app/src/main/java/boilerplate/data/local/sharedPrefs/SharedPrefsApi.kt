package boilerplate.data.local.sharedPrefs

interface SharedPrefsApi {
	fun <T> get(key: String, clazz: Class<T>): T

	fun <T> put(key: String, data: T)

	fun clear()

	fun clearKey(key: String)
}