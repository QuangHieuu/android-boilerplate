package boilerplate.data.remote.api.response

import android.empty.base.BaseModel

abstract class ResMessage {

	var message: String = ""
	var status: Int = 0
}

abstract class Response<T : BaseModel>(
	var result: T? = null,
) : ResMessage()

abstract class Responses<T>(
	var result: ArrayList<T> = arrayListOf()
) : ResMessage()

abstract class ResponseItems<T>(
	var result: Items<T>? = null,
) : ResMessage()

abstract class Items<T>(
	var items: ArrayList<T> = arrayListOf(),
	var total: Int = 0
) : ResMessage()
