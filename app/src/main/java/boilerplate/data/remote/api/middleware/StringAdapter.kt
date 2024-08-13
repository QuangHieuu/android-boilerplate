package boilerplate.data.remote.api.middleware

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException

class StringAdapter : TypeAdapter<String>() {
	@Throws(IOException::class)
	override fun write(out: JsonWriter, value: String?) {
		out.value(value)
	}

	@Throws(IOException::class)
	override fun read(`in`: JsonReader): String? {
		val peek = `in`.peek()
		return when (peek) {
			JsonToken.STRING -> `in`.nextString()
			JsonToken.NULL -> {
				`in`.nextNull()
				""
			}

			else -> ""
		}
	}
}