package boilerplate.utils.extension

import android.Manifest
import android.os.Build

val PERMISSION_STORAGE: Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
        )
    }


data class Permission(
    val name: String,
    val granted: Boolean
)