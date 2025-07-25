package boilerplate.data.local.sharedPrefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson

@Suppress("UNCHECKED_CAST")
class SharedPrefsImpl(context: Context, private val gson: Gson) : SharedPrefsApi {

	private var sharedPreferences: SharedPreferences = context.getSharedPreferences(
		SharedPrefsKey.PREF_NAME, Context.MODE_PRIVATE
	)

	override fun <T> get(key: String, clazz: Class<T>): T {
		return when (clazz) {
			String::class.java -> {
				sharedPreferences.getString(key, "").let { it as T }
			}

			Boolean::class.java -> {
				sharedPreferences.getBoolean(key, false).let { it as T }
			}

			Float::class.java -> {
				sharedPreferences.getFloat(key, 0f).let { it as T }
			}

			Int::class.java -> {
				sharedPreferences.getInt(key, 0).let { it as T }
			}

			Long::class.java -> {
				sharedPreferences.getLong(key, 0).let { it as T }
			}

			else -> {
				gson.fromJson(sharedPreferences.getString(key, ""), clazz)
			}
		}
	}

	override fun <T> put(key: String, data: T) {
		sharedPreferences.edit {
			when (data) {
				is String -> putString(key, data)
				is Boolean -> putBoolean(key, data)
				is Float -> putFloat(key, data)
				is Int -> putInt(key, data)
				is Long -> putLong(key, data)
				else -> putString(key, Gson().toJson(data))
			}
		}
	}

	override fun clear() {
		sharedPreferences.edit { clear() }
	}

	override fun clearKey(key: String) {
		sharedPreferences.edit { remove(key) }
	}

}