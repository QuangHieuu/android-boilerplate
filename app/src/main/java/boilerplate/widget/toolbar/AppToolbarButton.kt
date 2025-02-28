package boilerplate.widget.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.withStyledAttributes
import boilerplate.R

class AppToolbarButton(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = R.attr.AppToolbarButtonStyle,
) : AppCompatImageButton(context, attrs, defStyleAttr) {

	init {
		context.withStyledAttributes(
			attrs,
			R.styleable.AppToolbarButton,
			defStyleAttr,
			R.style.AppToolbarButton
		) {
			val width =
				getDimensionPixelSize(
					R.styleable.AppToolbarButton_android_layout_width,
					ViewGroup.LayoutParams.MATCH_PARENT
				)
			val height =
				getDimensionPixelSize(
					R.styleable.AppToolbarButton_android_layout_height,
					ViewGroup.LayoutParams.MATCH_PARENT
				)

			layoutParams = ViewGroup.LayoutParams(width, height)

		}
	}
}