package boilerplate.data.remote.api.error

import boilerplate.utils.extension.notNull
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import boilerplate.utils.extension.convertStringToListStringWithFormatPattern
import java.io.IOException

data class ErrorResponse(private val response: Response<*>?) {

    var code: Int = 0

    var message: String? = null

    var messageList: String? = null

    var allMessage: String? = null

    init {
        response.notNull {
            code = it.code()
            val errorJson = it.errorBody().toString();

            try {
                JSONObject(errorJson).notNull { json ->
                    val errorMsg = json.getString(ERROR_MESSAGE_PARAM)

                    if (errorMsg.isNotBlank()) {
                        message = errorMsg
                        allMessage = errorMsg
                    }

                    val errorObjects = json.getJSONObject(ERRORS_PARAM)
                    val errorMsgList = arrayListOf<String>()
                    val keys = errorObjects.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val array = errorObjects.getJSONArray(key)
                        for (i in 0 until array.length()) {
                            errorMsgList.add(array.getString(i))
                        }
                    }

                    messageList = errorMsgList.convertStringToListStringWithFormatPattern(
                        ENTER_SPACE_FORMAT
                    )

                    allMessage = if (allMessage?.isNotBlank() == true) {
                        allMessage + "\n" + messageList
                    } else {
                        messageList
                    }
                };
            } catch (e: JSONException) {
            } catch (e: IOException) {
            }
        }
    }

    companion object {
        private const val TAG = "ErrorResponse"
        private const val ENTER_SPACE_FORMAT = "\n"
        private const val ERRORS_PARAM = "errors"
        private const val ERROR_MESSAGE_PARAM = "message"
    }
}

