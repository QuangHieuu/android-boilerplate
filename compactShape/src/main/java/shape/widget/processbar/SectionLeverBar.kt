package shape.widget.processbar

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.IntDef
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt

@Retention(AnnotationRetention.SOURCE)
@IntDef(
	ShapeType.CIRCLE,
	ShapeType.LINE,
)
annotation class ShapeType {

	companion object {

		const val CIRCLE = 0
		const val LINE = 1
	}
}

class SectionLeverBar(
	context: Context,
	attrs: AttributeSet
) : View(context, attrs) {

	interface Listener {

		fun onSelected(position: Int)
	}

	private val density = Resources.getSystem().displayMetrics.density

	private var type = ShapeType.CIRCLE

	private var barHeight = density.times(4)
	private var thumbStep = density.times(10)
	private var thumbMain = density.times(18)
	private var stepWidth = 0f

	private val processRect = RectF()
	private val lineRect = RectF()

	private val backgroundRect = RectF()
	private val backgroundPaint = Paint().apply {
		color = "#E0E0E0".toColorInt()
		style = Paint.Style.FILL
	}

	private val processPath = Path()
	private val processPaint = Paint().apply {
		color = "#03A959".toColorInt()
		style = Paint.Style.FILL
	}
	private val thumbPaint = Paint().apply {
		color = Color.WHITE
		style = Paint.Style.FILL
	}

	private val points = arrayListOf<PointF>()

	private var level = 5

	private var holderBitmap: Bitmap? = null

	private var lastX = 0f
	private var moveX = 0f

	private var iListener: Listener? = null

	init {
		setWillNotDraw(false)
	}

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		val widthSize = MeasureSpec.getSize(widthMeasureSpec)
		val heightResult = density.times(50).plus(paddingTop).plus(paddingEnd).toInt()

		setMeasuredDimension(widthSize, heightResult)

		backgroundRect.set(
			paddingStart.toFloat(),
			paddingTop.toFloat(),
			widthSize.minus(paddingStart).toFloat(),
			thumbMain.times(2)
		)

		lineRect.set(
			backgroundRect.left.plus(thumbMain),
			backgroundRect.centerY().minus(barHeight.div(2)),
			backgroundRect.right.minus(thumbMain),
			backgroundRect.centerY().plus(barHeight.div(2)),
		)

		processRect.set(lineRect.left, lineRect.top, lineRect.left, lineRect.bottom)

		stepWidth = lineRect.width().div(level)

		calculatorStep()

		holderBitmap = createHolder(
			backgroundRect.width().toInt(),
			backgroundRect.height().toInt()
		)
	}

	override fun onDraw(canvas: Canvas) {
		holderBitmap?.let { canvas.drawBitmap(it, null, backgroundRect, backgroundPaint) }

		drawProcess(canvas)
	}

	override fun performClick(): Boolean {
		super.performClick()
		return true
	}

	override fun onTouchEvent(event: MotionEvent): Boolean {
		when (event.action) {
			MotionEvent.ACTION_DOWN -> {
				parent?.requestDisallowInterceptTouchEvent(true)

				lastX = event.x
				moveX = event.x
			}

			MotionEvent.ACTION_MOVE -> {
				moveX = event.x
				snap()
			}

			MotionEvent.ACTION_UP -> {
				parent?.requestDisallowInterceptTouchEvent(false)
				snap()
				reset()
			}
		}
		return performClick()
	}

	/* ---- */
	fun addListener(listener: Listener) {
		iListener = listener
	}

	/* ---- */
	private fun reset() {
		moveX = 0f
		lastX = 0f
	}

	private fun snap() {
		val nearPoint = points.findLast { it.x <= moveX }?.x ?: processRect.right
		val checkDistance = nearPoint.plus(stepWidth.div(2))
		processRect.right = if (moveX > checkDistance) {
			nearPoint.plus(stepWidth)
		} else {
			nearPoint
		}
		iListener?.onSelected(points.indexOfLast { it.x == processRect.right })
		invalidate()
	}

	private fun drawProcess(canvas: Canvas) {
		with(processPath) {
			reset()
			addRect(processRect, Path.Direction.CCW)

			for (x in points.map { it.x }) {
				if (x <= processRect.right) {
					addCircle(x, processRect.centerY(), thumbStep.div(2), Path.Direction.CCW)
				} else {
					break
				}
			}
		}
		canvas.drawPath(processPath, processPaint)

		canvas.drawCircle(
			processRect.right,
			processRect.centerY(),
			thumbMain.div(2),
			thumbPaint
		)

		canvas.drawCircle(
			processRect.right,
			processRect.centerY(),
			thumbMain.minus(density.times(2)).div(2),
			processPaint
		)
	}

	private fun calculatorStep() {
		val y = lineRect.centerY()

		with(points) {
			clear()
			add(PointF(lineRect.left, y))
			for (index in 1..level) {
				add(PointF(lineRect.left.plus(stepWidth.times(index)), y))
			}
		}

	}

	private fun createHolder(width: Int, height: Int): Bitmap {
		return createBitmap(width, height).let {
			with(Canvas(it)) {
				drawRect(lineRect, backgroundPaint)
				points.forEach {
					drawCircle(it.x, it.y, thumbStep.div(2), backgroundPaint)
				}
			}
			it
		}
	}
}