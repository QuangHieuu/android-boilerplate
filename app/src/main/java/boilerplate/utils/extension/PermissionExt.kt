package boilerplate.utils.extension

import android.Manifest.permission.*
import android.os.Build
import androidx.annotation.RequiresApi

val PERMISSION_STORAGE: Array<String> =
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
		arrayOf(
			READ_MEDIA_IMAGES,
			READ_MEDIA_VIDEO,
			READ_MEDIA_VISUAL_USER_SELECTED
		)
	} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
		arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO)
	} else {
		arrayOf(READ_EXTERNAL_STORAGE)
	}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
val PERMISSION_NOTIFY: Array<String> = arrayOf(POST_NOTIFICATIONS)

data class Permission(
	val name: String,
	val granted: Boolean
)