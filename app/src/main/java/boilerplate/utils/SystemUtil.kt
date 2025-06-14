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
import android.widget.Toast
import boilerplate.R
import boilerplate.constant.FontSize
import boilerplate.data.local.repository.app.AppRepository
import boilerplate.ui.main.MainActivity
import boilerplate.utils.extension.toTextSize
import org.koin.java.KoinJavaComponent.inject
import java.io.*
import java.util.Objects

object SystemUtil {

	private val appShared: AppRepository by inject(AppRepository::class.java)

	private const val CLIPBOARD_MESSAGE: String = "CLIPBOARD_MESSAGE"
	private const val FOLDER_DIRECTION: String = "/folder"

	private const val BUFFER_SIZE: Int = 1024 * 2

	fun Context.getFontSizeChat(): Float {
		val fontSize = appShared.getAppFontSize()
		val mainSize: Float = when (fontSize) {
			FontSize.SIZE_SMALL -> resources.getDimension(R.dimen.dp_14)
			FontSize.SIZE_MEDIUM -> resources.getDimension(R.dimen.dp_17)
			FontSize.SIZE_LARGE -> resources.getDimension(R.dimen.dp_21)
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

	fun getInAppDocumentFolder(context: Context): String {
		val file = File(
			context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + FOLDER_DIRECTION
		)
		if (!file.exists()) {
			file.mkdirs()
		}
		return file.absolutePath
	}

	fun getInAppDownloadFolder(context: Context): String {
		val file = File(
			context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + FOLDER_DIRECTION
		)
		if (!file.exists()) {
			file.mkdirs()
		}
		return file.absolutePath
	}

	fun getSystemDownloadFolder(): String {
		val file = File(
			Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
				.toString() + FOLDER_DIRECTION
		)
		if (!file.exists()) {
			file.mkdirs()
		}
		return file.absolutePath
	}

	fun getInAppImageFolder(context: Context): String {
		val file = File(
			context.getExternalFilesDir(Environment.DIRECTORY_DCIM)
				.toString() + FOLDER_DIRECTION
		)
		if (!file.exists()) {
			file.mkdirs()
		}
		return file.absolutePath
	}

	fun getSystemImageFolder(): String {
		val file = File(
			Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
				.toString() + FOLDER_DIRECTION
		)
		if (!file.exists()) {
			file.mkdirs()
		}
		return file.absolutePath
	}

	fun getAppCacheFolder(context: Context): String {
		val file =
			File(context.externalCacheDir.toString() + FOLDER_DIRECTION)
		if (!file.exists()) {
			file.mkdirs()
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
			f.delete()
		}
	}

	fun getSaveLocationConversation(context: Context): String {
		val parent = getInAppDocumentFolder(context)
		val file = File("$parent/Conversation")
		if (!file.exists()) {
			file.mkdirs()
		}
		return file.absolutePath
	}

	fun getSaveLocationMessage(context: Context): String {
		val parent = getInAppDocumentFolder(context)
		val file = File("$parent/Message")
		if (!file.exists()) {
			file.mkdirs()
		}
		return file.absolutePath
	}

	fun getSaveLocationUnSent(context: Context): String {
		val parent = getInAppDocumentFolder(context)
		val file = File("$parent/Pending")
		if (!file.exists()) {
			file.mkdirs()
		}
		return file.absolutePath
	}

	fun getSaveLocationFile(context: Context, conversationId: String): String {
		val parent = getInAppDocumentFolder(context)
		val file = File("$parent/MessageFile/$conversationId")
		if (!file.exists()) {
			file.mkdirs()
		}
		return file.absolutePath
	}

	fun getSaveLocationFile(context: Context): String {
		val parent = getInAppDocumentFolder(context)
		val file = File("$parent/MessageFile/")
		if (!file.exists()) {
			file.mkdirs()
		}
		return file.absolutePath
	}

	fun getSaveConversationUser(context: Context): String {
		val parent = getInAppDocumentFolder(context)
		val file = File("$parent/UserConversation/")
		if (!file.exists()) {
			file.mkdirs()
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
			Toast.makeText(context, R.string.success_copy_text, Toast.LENGTH_SHORT).show()
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