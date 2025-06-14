package android.empty.model

import androidx.annotation.IntDef

@Retention(AnnotationRetention.SOURCE)
@IntDef(RefreshStyle.NORMAL, RefreshStyle.PINNED, RefreshStyle.FLOAT)
annotation class RefreshStyle {

	companion object {

		const val NORMAL = 0
		const val PINNED = 1
		const val FLOAT = 2
	}
}