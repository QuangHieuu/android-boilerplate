package shape.widget.extend

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap

internal fun Context.getBitmapFromDrawable(drawableId: Int): Bitmap? {
	val drawable = ContextCompat.getDrawable(this, drawableId) ?: return null
	val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
	val canvas = Canvas(bitmap)
	drawable.setBounds(0, 0, canvas.width, canvas.height)
	drawable.draw(canvas)
	return bitmap
}