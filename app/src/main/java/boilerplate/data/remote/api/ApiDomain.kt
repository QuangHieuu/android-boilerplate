package boilerplate.data.remote.api

object ApiDomain {

	var DEFAULT: String = ApiServer.LIVE.serverName

	@JvmField
	var HOST_FILE_PREVIEW: String = ""

	fun setHost(host: String) {
		when (ApiServer.fromType(host)) {
			ApiServer.LIVE -> {

			}
		}
	}
}
