package boilerplate.widget.image

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.annotation.Px
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import boilerplate.R
import kotlin.math.min

class RoundedImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    enum class Corner {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_RIGHT,
        BOTTOM_LEFT,
    }

    private val radii: FloatArray

    init {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.RoundedImageView, defStyleAttr, 0)
        this.adjustViewBounds = typedArray.getBoolean(
            R.styleable.RoundedImageView_android_adjustViewBounds,
            false
        )
        val radius = typedArray.getDimension(R.styleable.RoundedImageView_radius, 0f)
        val topLeftRadius =
            typedArray.getDimension(R.styleable.RoundedImageView_topLeftRadius, radius)
        val topRightRadius =
            typedArray.getDimension(R.styleable.RoundedImageView_topRightRadius, radius)
        val bottomLeftRadius =
            typedArray.getDimension(R.styleable.RoundedImageView_bottomLeftRadius, radius)
        val bottomRightRadius =
            typedArray.getDimension(R.styleable.RoundedImageView_bottomRightRadius, radius)
        radii = floatArrayOf(
            topLeftRadius,
            topLeftRadius,
            topRightRadius,
            topRightRadius,
            bottomRightRadius,
            bottomRightRadius,
            bottomLeftRadius,
            bottomLeftRadius
        )
        typedArray.recycle()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val path = Path()
        val mainRectF = RectF(0f, 0f, width.toFloat(), height.toFloat())
        path.addRoundRect(mainRectF, radii, Path.Direction.CW)
        canvas.clipPath(path)
        super.onDraw(canvas)
    }

    fun setRadius(@Px radius: Int) {
        bytefill(radii, radius)
    }

    fun setRadius(corner: Corner?, @Px radius: Int) {
        when (corner) {
            Corner.TOP_LEFT -> {
                radii[0] = radius.toFloat()
                radii[1] = radius.toFloat()
            }

            Corner.TOP_RIGHT -> {
                radii[2] = radius.toFloat()
                radii[3] = radius.toFloat()
            }

            Corner.BOTTOM_RIGHT -> {
                radii[4] = radius.toFloat()
                radii[5] = radius.toFloat()
            }

            Corner.BOTTOM_LEFT -> {
                radii[6] = radius.toFloat()
                radii[7] = radius.toFloat()
            }

            else -> {
                radii[0] = radius.toFloat()
                radii[1] = radius.toFloat()
                radii[2] = radius.toFloat()
                radii[3] = radius.toFloat()
                radii[4] = radius.toFloat()
                radii[5] = radius.toFloat()
                radii[6] = radius.toFloat()
                radii[7] = radius.toFloat()
            }
        }
    }

    private fun bytefill(array: FloatArray, value: Int) {
        val len = array.size
        array[0] = value.toFloat()
        var i = 1
        while (i < len) {
            System.arraycopy(
                array, 0, array, i, min((len - i).toDouble(), i.toDouble())
                    .toInt()
            )
            i += i
        }
    }


    fun setTextImage(content: String?) {
        var height = layoutParams.height
        var width = layoutParams.width
        if (height <= 0) {
            height = layoutParams.width
        }
        if (width <= 0) {
            width = layoutParams.height
        }
        val img = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(img)

        val bgPaint = Paint()
        bgPaint.color =
            ContextCompat.getColor(context, R.color.colorPrimary)
        canvas.drawCircle(width / 2f, width / 2f, width / 2f, bgPaint)

        val textPaint = Paint()
        textPaint.color = ContextCompat.getColor(context, android.R.color.white)
        textPaint.textSize = width / 3f
        textPaint.textAlign = Paint.Align.CENTER
        val xPos = (canvas.width / 2)
        val yPos = ((canvas.height / 2) - ((textPaint.descent() + textPaint.ascent()) / 2)).toInt()
        canvas.drawText(content!!, xPos.toFloat(), yPos.toFloat(), textPaint)

        this.setImageBitmap(img)
    }

    fun setTextBackground(content: String) {
        var height = layoutParams.height
        var width = layoutParams.width
        if (height <= 0) {
            height = layoutParams.width
        }
        if (width <= 0) {
            width = layoutParams.height
        }

        val img = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(img)

        val bgPaint = Paint()
        bgPaint.strokeWidth = 1f
        bgPaint.color =
            ContextCompat.getColor(context, R.color.color_EAEEF4)
        canvas.drawCircle(width / 2f, width / 2f, width / 2f, bgPaint)

        val textPaint = Paint()
        textPaint.color =
            ContextCompat.getColor(context, R.color.color_7589A3)
        textPaint.textSize = width / 3f
        textPaint.textAlign = Paint.Align.CENTER
        val xPos = (canvas.width / 2)
        val yPos = ((canvas.height / 2) - ((textPaint.descent() + textPaint.ascent()) / 2)).toInt()
        if (!content.isEmpty()) {
            canvas.drawText(content, xPos.toFloat(), yPos.toFloat(), textPaint)
        }

        this.setImageBitmap(img)
    }

    fun setCircleColor(color: Int) {
        var height = layoutParams.height
        var width = layoutParams.width
        if (height <= 0) {
            height = layoutParams.width
        }
        if (width <= 0) {
            width = layoutParams.height
        }

        val img = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(img)

        val bgPaint = Paint()
        bgPaint.color = color
        canvas.drawCircle(width / 2f, width / 2f, width / 2f, bgPaint)

        this.setImageBitmap(img)
    }
}
