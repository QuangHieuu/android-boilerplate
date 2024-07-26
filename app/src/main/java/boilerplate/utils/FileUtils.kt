package boilerplate.utils

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import boilerplate.model.file.AttachedFile
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

object FileUtils {

    fun getPath(context: Context, contentUri: Uri): String {
        //copy file and send new file path
        var fileName: String = SystemUtil.getDisplayFilename(context, contentUri)
        fileName = fileName.replace("/".toRegex(), "-")
        if (!TextUtils.isEmpty(fileName)) {
            val folder = File(SystemUtil.getAppCacheFolder(context))
            var success = true
            if (!folder.exists()) {
                success = folder.mkdirs()
            }
            val copyFile = File(SystemUtil.getAppCacheFolder(context), fileName)
            if (success && !copyFile.exists()) {
                SystemUtil.copy(context, contentUri, copyFile)
            }
            return copyFile.absolutePath
        }
        return ""
    }

    fun multiPartFile(context: Context, uri: Uri): MultipartBody.Part {
        val file = File(getPath(context, uri))
        val fileName = SystemUtil.getDisplayFilename(context, uri)
        val fileMime: String = SystemUtil.getMime(file)
        val requestFile = RequestBody.create(fileMime.toMediaTypeOrNull(), file)
        return MultipartBody.Part.createFormData("file", fileName, requestFile)
    }

    fun multipartFiles(
        context: Context,
        list: ArrayList<AttachedFile.Conversation>
    ): ArrayList<MultipartBody.Part> {
        val filesList = ArrayList<MultipartBody.Part>()
        for (file in list) {
            filesList.add(multiPartFile(context, file.uri!!))
        }
        return filesList
    }
}