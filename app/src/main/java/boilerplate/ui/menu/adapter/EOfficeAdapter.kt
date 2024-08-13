package boilerplate.ui.menu.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import boilerplate.databinding.ItemMenuOfficeBinding
import boilerplate.databinding.ItemMenuOfficeDividerBinding
import boilerplate.databinding.ItemTitleBinding
import boilerplate.model.menu.EOfficeMenu
import boilerplate.model.menu.Menu
import boilerplate.utils.extension.click
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.show
import boilerplate.widget.holder.TitleVH

class EOfficeAdapter(
	val _listener: OnMenuListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
	interface OnMenuListener {
		fun onChosen(menu: EOfficeMenu)
	}

	companion object {
		const val TYPE_TITLE = 2
		const val TYPE_DIVIDER = 0
		const val TYPE_DETAIL = 1
	}

	private val _list = EOfficeMenu.listMenu()

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		val inflater = LayoutInflater.from(parent.context)

		return when (viewType) {
			TYPE_DIVIDER -> {
				MenuDivider(ItemMenuOfficeDividerBinding.inflate(inflater, parent, false))
			}

			TYPE_DETAIL -> {
				MenuDetail(
					ItemMenuOfficeBinding.inflate(inflater, parent, false),
					_listener
				)
			}

			else -> {
				TitleVH(ItemTitleBinding.inflate(inflater, parent, false))
			}
		}
	}

	override fun getItemCount(): Int {
		return _list.size
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		val ob = _list[position]
		if (holder is TitleVH && ob is String) {
			holder._binding.tvTitle.text = ob
		}
		if (holder is MenuDetail && ob is Menu) {
			holder.setData(ob)
		}
	}

	override fun getItemViewType(position: Int): Int {
		val ob = _list[position]
		if (ob is String) {
			return TYPE_TITLE
		}
		if (ob is Menu) {
			return TYPE_DETAIL
		}
		return TYPE_DIVIDER
	}

	fun updateCount(result: Pair<EOfficeMenu, Int>) {
		synchronized(_list) {
			val listIterator = _list.listIterator()
			while (listIterator.hasNext()) {
				val index = listIterator.nextIndex()
				val ob = listIterator.next()
				if (ob is Menu && ob.item.index == result.first.index) {
					ob.count = result.second
					notifyItemChanged(index, ob)
					break
				}
			}
		}
	}
}

class MenuDivider(
	val binding: ItemMenuOfficeDividerBinding
) : RecyclerView.ViewHolder(binding.root)

class MenuDetail(
	val binding: ItemMenuOfficeBinding,
	val listener: EOfficeAdapter.OnMenuListener
) : RecyclerView.ViewHolder(binding.root) {

	fun setData(menu: Menu) {
		with(binding) {
			tvTitle.text = menu.item.title
			imgIcon.setImageResource(menu.item.icon)

			tvCount.apply {
				if (menu.count != 0) {
					text = menu.count.toString()
					show()
				} else {
					gone()
				}
			}

			root.click { listener.onChosen(menu.item) }
		}
	}
}