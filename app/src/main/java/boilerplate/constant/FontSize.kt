package boilerplate.constant

import androidx.annotation.IntDef

@Retention(AnnotationRetention.SOURCE)
@IntDef(FontSize.SIZE_SMALL, FontSize.SIZE_MEDIUM, FontSize.SIZE_LARGE)
annotation class FontSize {

	companion object {

		const val SIZE_SMALL: Int = 0 // size = 14;
		const val SIZE_MEDIUM: Int = 1 // size = 17;
		const val SIZE_LARGE: Int = 2 // size = 21;
	}
}