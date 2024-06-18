package gg.it.data.remote.api

interface OnApiCallBack {
    fun invalidLogin()

    fun invalidToken()

    fun onServerError(errorCode: Int, api: String, showError: Boolean)
}
