package boilerplate.utils

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import android.widget.Toast
import boilerplate.R
import boilerplate.data.local.repository.user.UserRepository
import boilerplate.ui.main.MainActivity
import boilerplate.utils.extension.toTextSize
import org.koin.java.KoinJavaComponent.inject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.Locale
import java.util.Objects

object SystemUtil {
	private val userImpl by inject<UserRepository>(UserRepository::class.java)

	const val CLIPBOARD_MESSAGE: String = "CLIPBOARD_MESSAGE"

	const val SYSTEM_DELETE = "_system_deleted"

	const val urlFolder: String = "/folder"
	const val urlFolderNoSplit: String = "folder"

	const val BUFFER_SIZE: Int = 1024 * 2

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

	fun copy(context: Context, srcUri: Uri?, dstFile: File?) {
		try {
			val inputStream = context.contentResolver.openInputStream(srcUri!!)
			if (inputStream != null) {
				val outputStream: OutputStream = FileOutputStream(dstFile)
				val `in` = BufferedInputStream(inputStream, BUFFER_SIZE)
				val out = BufferedOutputStream(outputStream, BUFFER_SIZE)
				val buffer = ByteArray(BUFFER_SIZE)
				var count = 0
				var n: Int
				while ((`in`.read(buffer, 0, BUFFER_SIZE).also { n = it }) != -1) {
					out.write(buffer, 0, n)
					count += n
				}
				out.flush()
				inputStream.close()
				outputStream.close()
			}
		} catch (ignore: IOException) {
		}
	}

	fun getMime(file: File): String {
		var type = ""
		try {
			val mime = MimeTypeMap.getSingleton()
			val index = file.name.lastIndexOf('.') + 1
			val ext = file.name.substring(index).lowercase(Locale.getDefault())
			type = mime.getMimeTypeFromExtension(ext) ?: ""
		} catch (_: Exception) {
		}
		return type
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

	fun getFileSize(context: Context, uri: Uri?): Float {
		return uri?.let {
			val cr = context.contentResolver
			try {
				val `is` = cr.openInputStream(uri)
				if (`is` != null) {
					val fileSize = Math.round((`is`.available() / (1024f * 1024f)) * 100f) / 100f
					`is`.close()
					fileSize
				} else {
					0f
				}
			} catch (e: IOException) {
				0f
			}
		} ?: 0f
	}

	fun getInAppDocumentFolder(context: Context): String {
		val file = File(
			context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + urlFolder
		)
		if (!file.exists()) {
			val result = file.mkdirs()
		}
		return file.absolutePath
	}

	fun getInAppDownloadFolder(context: Context): String {
		val file = File(
			context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + urlFolder
		)
		if (!file.exists()) {
			val result = file.mkdirs()
		}
		return file.absolutePath
	}

	fun getSystemDownloadFolder(): String {
		val file = File(
			Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
				.toString() + urlFolder
		)
		if (!file.exists()) {
			val result = file.mkdirs()
		}
		return file.absolutePath
	}

	fun getInAppImageFolder(context: Context): String {
		val file = File(
			context.getExternalFilesDir(Environment.DIRECTORY_DCIM)
				.toString() + urlFolder
		)
		if (!file.exists()) {
			val result = file.mkdirs()
		}
		return file.absolutePath
	}

	fun getSystemImageFolder(): String {
		val file = File(
			Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
				.toString() + urlFolder
		)
		if (!file.exists()) {
			val result = file.mkdirs()
		}
		return file.absolutePath
	}

	fun getAppCacheFolder(context: Context): String {
		val file =
			File(context.externalCacheDir.toString() + urlFolder)
		if (!file.exists()) {
			val result = file.mkdirs()
		}
		return file.absolutePath
	}

	fun removeTempFiles(context: Context) {
		val mediaStorageDir = File(getAppCacheFolder(context))
		if (mediaStorageDir.exists()) {
			if (mediaStorageDir.isDirectory) {
				delete(mediaStorageDir)
			}
		}
	}

	fun delete(f: File) {
		synchronized(f) {
			if (f.isDirectory && f.listFiles() != null) {
				for (c in Objects.requireNonNull(f.listFiles())) {
					delete(c)
				}
			}
			val result = f.delete()
		}
	}

	fun getSaveLocationConversation(context: Context): String {
		val parent = getInAppDocumentFolder(context)
		val file = File("$parent/Conversation")
		if (!file.exists()) {
			val result = file.mkdirs()
		}
		return file.absolutePath
	}

	fun getSaveLocationMessage(context: Context): String {
		val parent = getInAppDocumentFolder(context)
		val file = File("$parent/Message")
		if (!file.exists()) {
			val result = file.mkdirs()
		}
		return file.absolutePath
	}

	fun getSaveLocationUnSent(context: Context): String {
		val parent = getInAppDocumentFolder(context)
		val file = File("$parent/Pending")
		if (!file.exists()) {
			val result = file.mkdirs()
		}
		return file.absolutePath
	}

	fun getSaveLocationFile(context: Context, conversationId: String): String {
		val parent = getInAppDocumentFolder(context)
		val file = File("$parent/MessageFile/$conversationId")
		if (!file.exists()) {
			val result = file.mkdirs()
		}
		return file.absolutePath
	}

	fun getSaveLocationFile(context: Context): String {
		val parent = getInAppDocumentFolder(context)
		val file = File("$parent/MessageFile/")
		if (!file.exists()) {
			val result = file.mkdirs()
		}
		return file.absolutePath
	}

	fun getSaveConversationUser(context: Context): String {
		val parent = getInAppDocumentFolder(context)
		val file = File("$parent/UserConversation/")
		if (!file.exists()) {
			val result = file.mkdirs()
		}
		return file.absolutePath
	}

	fun removeDocumentFile(context: Context) {
		val mediaStorageDir = File(getInAppDocumentFolder(context))
		if (mediaStorageDir.exists()) {
			if (mediaStorageDir.isDirectory) {
				delete(mediaStorageDir)
			}
		}
	}

	fun copyToClipboard(context: Context, text: String?, json: String?) {
		val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
		val appIntent = Intent(context, MainActivity::class.java).apply {
			putExtra(CLIPBOARD_MESSAGE, json)
		}
		val clip = ClipData.newIntent(text, appIntent)
		clipboard.setPrimaryClip(clip)

		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
			Toast.makeText(context, "Đã sao chép", Toast.LENGTH_SHORT).show()
		}
	}

	fun isAppIsInBackground(context: Context): Boolean {
		var isInBackground = true
		val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
		for (processInfo in am.runningAppProcesses) {
			if (processInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				for (activeProcess in processInfo.pkgList) {
					if (activeProcess == context.packageName) {
						isInBackground = false
						break
					}
				}
				break
			}
		}
		return isInBackground
	}
}