package boilerplate.model.dashboard

import boilerplate.data.remote.api.ApiUrl

class Banner {
	var file: File? = null
		get() = field ?: File().also { field = it }

	var url: String? = null
		get() = field ?: "".also { field = it }
}

class File {
	var url: String? = null
		get() {
			if (field.isNullOrEmpty()) return "".also { field = it }
			else {
				if (field!!.contains(ApiUrl.HOST_FILE)) {
					return field!!
				} else {
					var link = ""
					if (field!!.startsWith("/")) {
						link = field!!.substring(1)
					}
					return ApiUrl.HOST_FILE + link
				}
			}
		}

}