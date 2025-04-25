package boilerplate.widget.customtext

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.util.Pair
import android.view.View
import androidx.core.view.ContentInfoCompat
import androidx.core.view.OnReceiveContentListener
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import boilerplate.constant.Constants.FILE_PATH_SAMSUNG_CLIPBOARD

class KeyboardReceiver : OnReceiveContentListener {
	override fun onReceiveContent(view: View, payload: ContentInfoCompat): ContentInfoCompat {
		val split: Pair<ContentInfoCompat, ContentInfoCompat> =
			payload.partition { item: ClipData.Item -> item.uri != null }
		val uriContent: ContentInfoCompat? = split.first
		val remaining: ContentInfoCompat = split.second
		if (uriContent != null) {
			val clip: ClipData = uriContent.clip
			for (i in 0 until clip.itemCount) {
				val uri: Uri = clip.getItemAt(i).uri
				if (!uri.toString().contains(FILE_PATH_SAMSUNG_CLIPBOARD)) {
					LocalBroadcastManager.getInstance(view.context)
						.sendBroadcast(Intent(KEYBOARD_CONTENT).apply { putExtra(KEYBOARD_CONTENT, uri) })
				}
			}
		}
		return remaining
	}

	companion object {
		private const val KEYBOARD_CONTENT = "KEYBOARD_CONTENT"
		val MIME_TYPES = arrayOf("image/*", "video/*")
	}
}