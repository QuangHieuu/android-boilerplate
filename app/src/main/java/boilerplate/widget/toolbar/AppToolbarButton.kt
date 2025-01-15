package boilerplate.widget.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.setPadding
import boilerplate.R

class AppToolbarButton(
	context: Context,
	attrs: AttributeSet? = null
) : AppCompatImageButton(context, attrs, R.style.AppToolbarButtonStyle) {

	private var _backListener: OnClickListener? = null

	init {
		val att = context.obtainStyledAttributes(
			attrs,
			R.styleable.AppToolbarButton,
			0,
			R.style.AppToolbarButtonStyle
		)
		with(att) {
			val width = getDimension(R.styleable.AppToolbarButton_width, 0F).toInt()
			val height = getDimension(R.styleable.AppToolbarButton_height, 0F).toInt()
			val padding = getDimension(R.styleable.AppToolbarButton_padding, 0F).toInt()

			layoutParams = ViewGroup.LayoutParams(width, height).apply { setPadding(padding) }

			val background = getResourceId(R.styleable.AppToolbarButton_background, 0)
			setBackgroundResource(background)

			recycle()
		}
	}
}