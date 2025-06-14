package android.empty.base

import android.empty.base.ViewType.VIEW_EMPTY
import android.empty.base.ViewType.VIEW_LOADING
import android.empty.refreshrecyclerview.databinding.HolderEmptyBinding
import android.empty.refreshrecyclerview.databinding.HolderLoadingBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

object ViewType {

	const val VIEW_LOADING = -1
	const val VIEW_EMPTY = -2
}

fun <VH : BaseVH<*>> create(clazz: KClass<VH>, parent: ViewGroup): BaseVH<*> {
	return clazz.primaryConstructor!!.call(parent)
}

inline fun <reified VH : BaseVH<*>> BaseRcvAdapter<*>.holder(
	viewType: Int,
	noinline condition: (position: Int, any: Any?) -> Boolean
): Any {
	return builders.add(
		HolderBuilder(
			viewType,
			{ parent -> create<VH>(VH::class, parent) },
			condition
		)
	)
}

data class HolderBuilder(
	var viewType: Int,
	var holder: (parent: ViewGroup) -> BaseVH<*>,
	var condition: (position: Int, any: Any?) -> Boolean
)

abstract class BaseRcvAdapter<T> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	val builders = arrayListOf<HolderBuilder>()

	var dataList: MutableList<T?> = arrayListOf()
		private set

	init {
		this.onCreateViewHolder()
	}

	abstract fun onCreateViewHolder(): List<HolderBuilder>

	abstract fun onBindHolder(holder: RecyclerView.ViewHolder, position: Int)

	override fun getItemCount(): Int = dataList.size

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

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		onBindHolder(holder, position)
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

	protected open fun hasPageLoading(): Boolean = false

	protected open fun builder(build: BaseRcvAdapter<T>.() -> Unit): List<HolderBuilder> {
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

abstract class BaseVH<VB : ViewBinding>(
	parent: ViewGroup,
	factory: (LayoutInflater, ViewGroup, Boolean) -> VB,
	protected val binding: VB = factory(LayoutInflater.from(parent.context), parent, false)
) : RecyclerView.ViewHolder(binding.root)

class EmptyVH(parent: ViewGroup) : BaseVH<HolderEmptyBinding>(parent, HolderEmptyBinding::inflate)

class LoadingVH(parent: ViewGroup) :
	BaseVH<HolderLoadingBinding>(parent, HolderLoadingBinding::inflate)