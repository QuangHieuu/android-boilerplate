package boilerplate.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import boilerplate.R
import boilerplate.data.local.repository.user.UserRepository
import boilerplate.utils.extension.toTextSize
import org.koin.java.KoinJavaComponent.inject
import java.io.File
import java.io.IOException

object SystemUtil {
    private val userImpl by inject<UserRepository>(UserRepository::class.java)

    const val SYSTEM_DELETE = "_system_deleted"

    const val SIZE_SMALL: Int = 0 // size = 14;
    const val SIZE_MEDIUM: Int = 1 // size = 17;
    const val SIZE_LARGE: Int = 2 // size = 21;

    @JvmStatic
    fun getFontSizeChat(context: Context): Float {
        val mainSize: Float
        val size: Int = userImpl.getSystemTextSize()
        val resources = context.resources
        mainSize = when (size) {
            SIZE_SMALL -> resources.getDimension(R.dimen.dp_14)
            SIZE_MEDIUM -> resources.getDimension(R.dimen.dp_17)
            SIZE_LARGE -> resources.getDimension(R.dimen.dp_21)
            else -> resources.getDimension(R.dimen.dp_14)
        }
        return mainSize.toTextSize()
    }

    @JvmStatic
    fun setAppPlaySound(isPlay: Boolean) {
        userImpl.saveSystemSound(isPlay)
    }

    @JvmStatic
    fun getAppPlaySound(): Boolean {
        return userImpl.getSystemSound()
    }

    fun getDisplayFilename(context: Context, uri: Uri): String {
        val uriString = uri.toString()
        val myFile = File(uriString)
        var displayName = ""
        if (uriString.startsWith("content://")) {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    displayName = cursor.getString(index)
                }
                cursor.close()
            }
        }
        if (uriString.startsWith("file://")) {
            displayName = myFile.name
        }
        return displayName
    }

    fun getDisplayFileSize(size: Int): String {
        var fileSize: String
        var text = ""
        var sizeByte = size
        if (sizeByte > 0) {
            var count = 0
            var sodu = sizeByte % 1024
            val saudauphay: String
            do {
                if (sizeByte > 1024) {
                    sizeByte = sizeByte / 1024
                    if (sizeByte > 1024) sodu = sizeByte % 1024
                    count += 1
                }
            } while (sizeByte > 1024)
            if (sodu > 0) {
                if (sodu * 100 / 1024 + "".length == 1) {
                    text = (sodu * 100 / 1024).toString()
                } else if (sodu * 100 / 1024 + "".length > 1) {
                    text = (sodu * 100 / 1024).toString().substring(0, 1)
                }
                saudauphay = "." + sodu * 10 / 1024 + text
            } else saudauphay = ".0"
            fileSize = sizeByte.toString() + saudauphay
            when (count) {
                0 -> fileSize += " bytes"
                1 -> fileSize += " KB"
                2 -> fileSize += " MB"
                3 -> fileSize += " GB"
                else -> {}
            }
            return fileSize
        } else return "0 KB"
    }

    fun getDisplayFileSize(context: Context, uri: Uri): String {
        var fileSize = ""
        var sizeByte: Int
        try {
            val inStream = context.contentResolver.openInputStream(uri)
            if (inStream != null) {
                sizeByte = inStream.available()
                if (sizeByte > 0) {
                    var count = 0
                    var sodu = sizeByte % 1024
                    do {
                        sizeByte = sizeByte / 1024
                        if (sizeByte > 1024) sodu = sizeByte % 1024
                        count += 1
                    } while (sizeByte > 1024)
                    val saudauphay = if (sodu > 0) "." + sodu * 10 / 1024
                    else ".0"
                    fileSize = sizeByte.toString() + saudauphay
                    when (count) {
                        1 -> fileSize += "KB"
                        2 -> fileSize += "MB"
                        3 -> fileSize += "GB"
                        else -> {}
                    }
                }
                inStream.close()
            }
        } catch (ignore: IOException) {
        }
        return fileSize
    }
}