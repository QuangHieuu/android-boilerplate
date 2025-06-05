package calendar.widget.wheel.view

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.graphics.toColorInt

class CurtainView(context: Context) : View(context) {

	private val _density = Resources.getSystem().displayMetrics.density
	private val _radius = _density.times(8)

	private val _viewHeight: Float = _density.times(50)
		get() = field / 2

	private val _paint = Paint().apply {
		style = Paint.Style.FILL
		color = "#14747480".toColorInt()
	}
	private val _rectDraw = RectF()

	override fun onDraw(canvas: Canvas) {
		canvas.drawRoundRect(_rectDraw, _radius, _radius, _paint)
	}

	fun computeWidth(start: Float, end: Float) {
		_rectDraw.set(
			start,
			height.div(2).minus(_viewHeight),
			end,
			height.div(2).plus(_viewHeight)
		)
		postInvalidate()
	}
}