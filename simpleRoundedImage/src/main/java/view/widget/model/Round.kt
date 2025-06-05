package view.widget.model

import androidx.annotation.IntDef

@Retention(AnnotationRetention.SOURCE)
@IntDef(Corner.TOP_LEFT, Corner.TOP_RIGHT, Corner.BOTTOM_LEFT, Corner.BOTTOM_RIGHT)
annotation class Corner {
	companion object {
		const val TOP_LEFT: Int = 0
		const val TOP_RIGHT: Int = 1
		const val BOTTOM_RIGHT: Int = 2
		const val BOTTOM_LEFT: Int = 3
	}
}