package boilerplate.data.remote.api

enum class ApiServer(val serverName: String, val displayName: String) {
	DARSITEC_TEST("DARSITEC_TEST", "darsitec test"),
	LIVE("LIVE", "LIVE");

	companion object {
		private val intToTypeMap: MutableMap<String, ApiServer> = HashMap()

		init {
			for (type in entries) {
				intToTypeMap[type.name] = type
			}
		}

		fun fromType(name: String): ApiServer {
			val type = intToTypeMap[name] ?: return LIVE
			return type
		}

		fun listServer(): ArrayList<ApiServer> {
			val list = ArrayList<ApiServer>()
			list.add(DARSITEC_TEST)
			list.add(LIVE)
			return list
		}
	}
}