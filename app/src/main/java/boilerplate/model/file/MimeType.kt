package boilerplate.model.file

enum class MimeType(private val string: String) {
	TEXT("text/*"),
	VIDEO("video/*"),
	AUDIO("audio/*"),
	APPLICATION("application/*"),
	IMAGE("image/*");

	val type: String
		get() {
			val index = string.indexOf("/*")
			return string.substring(0, index)
		}

	companion object {

		private val intToTypeMap: MutableMap<String, MimeType> = HashMap()

		init {
			for (i in entries) {
				intToTypeMap[i.string] = i
			}
		}
	}
}
