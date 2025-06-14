package boilerplate.widget.customtext

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.RoundedCorner
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.view.WindowInsetsCompat
import boilerplate.R
import kotlin.math.max

class InsetsLayout(
	context: Context,
	attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

	private var _statusBarBitmap: Bitmap? = null
	private val _statusBarRect: Rect = Rect()
	private val _statusBarPaint: Paint = Paint().apply {
		isAntiAlias = true
		style = Paint.Style.FILL
		color = ContextCompat.getColor(context, R.color.colorPrimary)
	}

	init {
		setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
	}

	override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
		val insets = rootWindowInsets

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && insets != null) {
			applyRoundedCornerPadding(insets)
		}
		super.onLayout(changed, left, top, right, bottom)

	}

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)

		_statusBarBitmap?.let { canvas.drawBitmap(it, null, _statusBarRect, _statusBarPaint) }
	}

	@RequiresApi(Build.VERSION_CODES.S)
	private fun applyRoundedCornerPadding(insets: WindowInsets) {
		val topLeft = insets.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT)
		val topRight = insets.getRoundedCorner(RoundedCorner.POSITION_TOP_RIGHT)
		val bottomLeft = insets.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_LEFT)
		val bottomRight = insets.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_RIGHT)

		val leftRadius = max(topLeft?.radius ?: 0, bottomLeft?.radius ?: 0)
		val topRadius = max(topLeft?.radius ?: 0, topRight?.radius ?: 0)
		val rightRadius = max(topRight?.radius ?: 0, bottomRight?.radius ?: 0)
		val bottomRadius = max(bottomLeft?.radius ?: 0, bottomRight?.radius ?: 0)

		val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
		val windowBounds = windowManager.currentWindowMetrics.bounds
		val safeArea = Rect(
			windowBounds.left + leftRadius,
			windowBounds.top + topRadius,
			windowBounds.right - rightRadius,
			windowBounds.bottom - bottomRadius
		)

		val location = intArrayOf(0, 0)
		getLocationInWindow(location)

		val leftMargin = location[0] - windowBounds.left
		val topMargin = location[1] - windowBounds.top
		val rightMargin = windowBounds.right - right - location[0]
		val bottomMargin = windowBounds.bottom - bottom - location[1]

		val layoutBounds = Rect(
			location[0] + paddingLeft,
			location[1] + paddingTop,
			location[0] + width - paddingRight,
			location[1] + height - paddingBottom
		)

		if (layoutBounds != safeArea && layoutBounds.contains(safeArea)) {
			setPadding(
				calculatePadding(leftRadius, leftMargin, paddingLeft),
				calculatePadding(topRadius, topMargin, paddingTop),
				calculatePadding(rightRadius, rightMargin, paddingRight),
				calculatePadding(bottomRadius, bottomMargin, paddingBottom)
			)
		}

		val systemBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
		val width = Resources.getSystem().displayMetrics.widthPixels

		_statusBarRect.set(0, 0, systemBars.top, width)

		_statusBarBitmap = createStatusBar()
	}

	private fun calculatePadding(radius1: Int, margin: Int, padding: Int): Int =
		(radius1 - margin - padding).coerceAtLeast(0)

	private fun createStatusBar(): Bitmap {
		return createBitmap(_statusBarRect.width(), _statusBarRect.height()).let { bitmap ->
			with(Canvas(bitmap)) {
				drawRect(_statusBarRect, _statusBarPaint)
			}
			bitmap
		}
	}
}