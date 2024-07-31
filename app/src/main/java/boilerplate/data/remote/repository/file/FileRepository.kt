package boilerplate.data.remote.repository.file

import boilerplate.data.remote.api.ApiRequest
import boilerplate.data.remote.api.response.BaseResult
import boilerplate.data.remote.api.response.BaseResults
import boilerplate.model.file.AttachedFile
import boilerplate.model.file.UploadFile
import boilerplate.utils.extension.checkInternet
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import okhttp3.MultipartBody

interface FileRepository {

    fun getConversationFile(
        conversationId: String?,
        type: Int,
        page: Int,
        limit: Int
    ): Single<BaseResult<AttachedFile.Conversation>>

    fun postFile(files: List<MultipartBody.Part>): Flowable<BaseResults<UploadFile>>

    fun postFile(file: MultipartBody.Part): Flowable<BaseResults<UploadFile>>

}

class FileImpl(
    private val apiRequest: ApiRequest,
) : FileRepository {

    override fun postFile(files: List<MultipartBody.Part>): Flowable<BaseResults<UploadFile>> {
        return apiRequest.file.postConversationFile(files).checkInternet()
    }

    override fun postFile(file: MultipartBody.Part): Flowable<BaseResults<UploadFile>> {
        return apiRequest.file.postConversationFile(file).checkInternet()
    }

    override fun getConversationFile(
        conversationId: String?,
        type: Int,
        page: Int,
        limit: Int
    ): Single<BaseResult<AttachedFile.Conversation>> {
        return apiRequest.chat.getConversationFile(conversationId, type, page, limit)
            .checkInternet()
    }
}