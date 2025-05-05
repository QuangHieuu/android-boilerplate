package android.empty.base

import androidx.recyclerview.widget.DiffUtil

abstract class BaseModel {}

fun <T : Any?> T.isSameItem(newItems: T): Boolean {
	return if (this == null) {
		newItems == null
	} else {
		this == newItems
	}
}

fun <T : Any?> T.isSameContent(newItems: T): Boolean {
	return if (this == null) {
		newItems == null
	} else {
		this == newItems
	}
}

open class DiffUtilCallback<T>(
	private val oldItem: List<T?>,
	private val newItems: List<T?>
) : DiffUtil.Callback() {
	override fun getOldListSize(): Int = oldItem.size

	override fun getNewListSize(): Int = newItems.size

	override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
		oldItem[oldItemPosition].isSameItem(newItems[newItemPosition])

	override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
		oldItem[oldItemPosition].isSameContent(newItems[newItemPosition])
}