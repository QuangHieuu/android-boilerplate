package boilerplate.data.local.repository.app

import boilerplate.data.local.sharedPrefs.SharedPrefsApi
import boilerplate.data.local.sharedPrefs.SharedPrefsKey

interface AppRepository {

	fun getAppFontSize(): Int

	fun setAppFontSize(fontSize: Int)
}

class AppRepositoryImpl(
	private val shared: SharedPrefsApi
) : AppRepository {

	override fun getAppFontSize(): Int {
		return shared.get(SharedPrefsKey.APP_FONT_SIZE, Int::class.java)
	}

	override fun setAppFontSize(fontSize: Int) {
		shared.put(SharedPrefsKey.APP_FONT_SIZE, fontSize)
	}

}