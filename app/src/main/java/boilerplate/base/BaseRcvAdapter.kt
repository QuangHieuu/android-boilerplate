package boilerplate.base

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import boilerplate.base.AppRcvViewType.VIEW_EMPTY
import boilerplate.base.AppRcvViewType.VIEW_LOADING
import boilerplate.databinding.HolderEmptyBinding
import boilerplate.databinding.HolderLoadingBinding
import boilerplate.model.DiffUtilCallback
import boilerplate.utils.extension.viewBinding

object AppRcvViewType {
	const val VIEW_LOADING = -1
	const val VIEW_EMPTY = -2
}

fun <T> BaseRcvAdapter<T>.build(
	viewType: Int,
	holder: (parent: ViewGroup) -> BaseVH<*>,
	condition: (position: Int, any: Any?) -> Boolean
): Any {
	return builders.add(HolderBuilder(viewType, holder, condition))
}

data class HolderBuilder<T>(
	var viewType: Int,
	var holder: (parent: ViewGroup) -> BaseVH<*>,
	var condition: (position: Int, any: Any?) -> Boolean
)

abstract class BaseRcvAdapter<T> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
	val builders = arrayListOf<HolderBuilder<T>>()

	var dataList: MutableList<T?> = arrayListOf()
		private set

	init {
		this.onBuildHolder()
	}

	abstract fun onBuildHolder(): List<HolderBuilder<T>>

	override fun getItemCount(): Int = setItemCount()

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		for ((type, holder, _) in builders) {
			if (type == viewType) {
				return holder(parent)
			}
		}
		return if (hasPageLoading()) {
			LoadingVH(parent)
		} else {
			EmptyVH(parent)
		}
	}

	override fun getItemViewType(position: Int): Int {
		val any = dataList[position]
		if (any == null && dataList.lastIndex == position) {
			return if (hasPageLoading()) {
				VIEW_LOADING
			} else {
				VIEW_EMPTY
			}
		}
		for ((viewType, _, condition) in builders) {
			if (condition(position, any)) {
				return viewType
			}
		}
		return super.getItemViewType(position)
	}

	protected open fun setItemCount(): Int = dataList.size

	protected open fun hasPageLoading(): Boolean = false

	protected open fun builder(build: BaseRcvAdapter<T>.() -> Unit): List<HolderBuilder<T>> {
		build()
		return builders
	}

	fun submitData(newList: List<T?>) {
		val results = DiffUtil.calculateDiff(DiffUtilCallback(dataList, newList))
		dataList = newList.toMutableList()
		results.dispatchUpdatesTo(this)
	}

	fun showLoading() {
		dataList.add(null)
		notifyItemInserted(dataList.size - 1)
	}

	fun hideLoading() {
		val lastIndex = dataList.lastIndex
		val last = dataList.lastOrNull()
		if (last == null) {
			dataList.remove(last)
			notifyItemRemoved(lastIndex)
		}
	}
}

class LoadingAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		return LoadingVH(parent)
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
	}

	override fun getItemCount(): Int = 1
}

abstract class BaseVH<VB : ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root)

class EmptyVH(
	parent: ViewGroup,
) : BaseVH<HolderEmptyBinding>(parent.viewBinding(HolderEmptyBinding::inflate)) {
	companion object {
		fun holder(parent: ViewGroup): EmptyVH {
			return EmptyVH(parent)
		}
	}
}

class LoadingVH(
	parent: ViewGroup,
) : BaseVH<HolderLoadingBinding>(parent.viewBinding(HolderLoadingBinding::inflate))
