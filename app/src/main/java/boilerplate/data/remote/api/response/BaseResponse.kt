package boilerplate.data.remote.api.response

data class Response<T>(
    var result: T? = null,
    var message: String = ""
)

data class Responses<T>(
    var result: ArrayList<T> = arrayListOf()
)

data class ResponseItems<T>(
    var result: Items<T>? = null,
)

data class Items<T>(
    var items: ArrayList<T> = arrayListOf(),
    var total: Int = 0
)