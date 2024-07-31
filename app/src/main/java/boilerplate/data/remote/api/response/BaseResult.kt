package boilerplate.data.remote.api.response

data class BaseResult<T>(
    var result: Result<T>? = null,
)

data class BaseResults<T>(
    var result: ArrayList<T> = arrayListOf()
)

data class Result<T>(
    var items: ArrayList<T> = arrayListOf(),
    var total: Int = 0
)
