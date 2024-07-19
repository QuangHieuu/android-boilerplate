package boilerplate.data.remote.api.response

open class BaseResult<T> {
    val result: Result<T>? = null
    val items: T? = null
}

open class BaseResults<T> {
    val result: ArrayList<T>? = null
    val items: T? = null
}

class Result<T> {
    val items: ArrayList<T> = arrayListOf()
        get() = field
}