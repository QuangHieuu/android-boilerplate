package boilerplate.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.FileNotFoundException

object ImageUtil {
    private const val TAG = "ImageUtil"
    const val IMAGE_THUMB_SIZE = 200
    const val IMAGE_MAX_SIZE = 1024
    const val REQUIRED_SIZE = 100

    @Throws(FileNotFoundException::class)
    fun decodeBitmap(context: Context, selectedImage: Uri): Bitmap? {
        return BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }.let {
            BitmapFactory.decodeStream(
                context.contentResolver.openInputStream(selectedImage),
                null,
                it
            )

            var widthTmp = it.outWidth
            var heightTmp = it.outHeight
            var scale = 1
            while (widthTmp / 2 >= REQUIRED_SIZE && heightTmp / 2 >= REQUIRED_SIZE) {
                widthTmp /= 2
                heightTmp /= 2
                scale *= 2
            }
            BitmapFactory.decodeStream(
                context.contentResolver.openInputStream(selectedImage),
                null,
                BitmapFactory.Options().apply { inSampleSize = scale }
            )
        }
    }
}