package shape.widget.chart

import android.animation.ValueAnimator
import android.content.Context
import shape.widget.base.DefaultView
import shape.widget.chart.model.Entry
import android.empty.shapeview.R
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withClip

class LineChart @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
) : DefaultView(context, attrs) {

	init {
		hasSafeVertical = true
	}

	private lateinit var _listEntry: ArrayList<Entry>

	private lateinit var _pathFill: Path
	private lateinit var _pathMeasure: PathMeasure
	private lateinit var _pathProcess: Path
	private lateinit var _paintProcess: Paint

	private val _xyCoordinate = floatArrayOf(0f, 0f)
	private val _bitmapBg: Drawable? = ContextCompat.getDrawable(context, R.drawable.bg_gradient)

	override fun drawCanvas() {
		draw()
	}

	override fun generatePaint() {
		_paintProcess = Paint()
		with(_paintProcess) {
			color = "#4BC56C".toColorInt()
			style = Paint.Style.STROKE
			strokeWidth = density.times(1.5f)
		}
	}

	override fun generateData() {
		_listEntry = arrayListOf()

		_pathFill = Path()
		_pathProcess = Path()

		_pathMeasure = PathMeasure()
	}

	override fun onUpdate(animation: ValueAnimator) {
		val process = animation.animatedValue as Float
		_pathMeasure.getPosTan(process * _pathMeasure.length, _xyCoordinate, null)

		_pathProcess.lineTo(_xyCoordinate[0], _xyCoordinate[1])

		with(_pathFill) {
			reset()
			addPath(_pathProcess)
			lineTo(_xyCoordinate[0], _xyCoordinate[1])
			lineTo(_xyCoordinate[0], drawHeight.toFloat())
			lineTo(0f, drawHeight.toFloat())
		}

		invalidate()
	}

	override fun onSizeChange() {
		if (_listEntry.isEmpty()) return
		clearAnimation()
		releaseBitmap()
		val scaleX = drawWidth.div(_listEntry.size - 1).toFloat()

		_listEntry.forEachIndexed { index, it ->
			it.x = scaleX.times(index)
		}

		generatePath()
		start()
	}

	override fun onEnd() {
		if (_pathMeasure.nextContour()) {
			start()
		}
	}

	override fun generatePath() {
		if (_listEntry.isEmpty()) return
		pathHolder.reset()
		_pathProcess.reset()
		_pathFill.reset()

		var prev = _listEntry[0]
		var cur = prev

		pathHolder.moveTo(cur.x, cur.y)
		_pathProcess.moveTo(cur.x, cur.y)
		_pathFill.moveTo(cur.x, cur.y)

		for (i in 1 until _listEntry.size) {
			prev = cur
			cur = _listEntry[i]

			val cpx: Float = (cur.x.minus(prev.x)).div(2).plus(prev.x)

			pathHolder.cubicTo(cpx, prev.y, cpx, cur.y, cur.x, cur.y)
		}
		_pathMeasure.setPath(pathHolder, false)

		duration = _pathMeasure.length.times(2).toLong()
	}

	fun setData(input: ArrayList<Entry>) {
		clearAnimation()
		_listEntry.clear()
		post {
			val maxY = input.maxOf { it.y }

			val scaleX = drawWidth.div(input.size - 1).toFloat()

			input.forEachIndexed { index, it ->
				it.y = calculatorYAxis(it.y, maxY)
				it.x = scaleX.times(index)
			}

			_listEntry.addAll(input)

			generatePath()
			start()
		}
	}

	private fun calculatorYAxis(input: Float, maxY: Float): Float {
		return drawHeight.minus(drawHeight.times(input).div(maxY)).plus(_paintProcess.strokeWidth)
	}

	private fun draw() {
		_bitmapBg?.let {
			drawCanvas?.withClip(_pathFill) {
				it.setBounds(0, 0, drawWidth.toInt(), drawHeight.toInt())
				it.draw(this)
			}
		}
		drawCanvas?.drawPath(_pathProcess, _paintProcess)
	}
}