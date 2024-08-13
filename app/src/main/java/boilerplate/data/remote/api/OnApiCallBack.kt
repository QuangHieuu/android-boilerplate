package boilerplate.data.remote.api

interface OnApiCallBack {
	fun notInternet()

	fun invalidLogin()

	fun invalidToken()

	fun onServerError(errorCode: Int, api: String, showError: Boolean)
}
