package boilerplate.widget.chatBox.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import boilerplate.databinding.ItemLinkBinding
import boilerplate.databinding.ItemLoadMoreBinding
import boilerplate.databinding.ItemMessageQuoteBinding
import boilerplate.model.file.AttachedFile
import boilerplate.model.message.Quote
import boilerplate.utils.SystemUtil
import boilerplate.widget.chatBox.viewHolder.QuoteVH
import boilerplate.widget.chatBox.viewHolder.StringLinkVH
import boilerplate.widget.holder.LoadingVH


class BoxAdapter(
	private val _block: (quote: Any) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
	companion object {
		const val TYPE_ITEM: Int = 1
		const val TYPE_QUOTE: Int = 2
		const val TYPE_LINK: Int = 3
	}

	private val _list = arrayListOf<Any>()

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		return when (viewType) {
			TYPE_QUOTE -> {
				QuoteVH(
					ItemMessageQuoteBinding.inflate(layoutInflater, parent, false),
					_block
				)
			}

			TYPE_LINK -> {
				StringLinkVH(
					ItemLinkBinding.inflate(layoutInflater, parent, false),
					_block
				)
			}

			else -> {
				LoadingVH(ItemLoadMoreBinding.inflate(layoutInflater, parent, false))
			}
		}
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		val ob = _list[position]
		if (holder is QuoteVH && ob is Quote) {
			holder.setData(ob)
			return
		}
	}

	override fun getItemCount(): Int {
		return _list.size
	}

	override fun getItemViewType(position: Int): Int {
		val ob = _list[position]
		if (ob is String) {
			return TYPE_LINK
		}
		if (ob is Quote) {
			return TYPE_QUOTE
		}
		return TYPE_ITEM
	}

	fun hasFile(): Boolean {
		for (ob in _list) {
			if (ob is AttachedFile) {
				return true
			}
		}
		return false
	}

	fun hasQuote(): Boolean {
		for (ob in _list) {
			if (ob is Quote) {
				return true
			}
		}
		return false
	}

	fun getFileUpload(): ArrayList<AttachedFile> {
		val files = arrayListOf<AttachedFile>()
		for (ob in _list) {
			if (ob is AttachedFile && ob.isUpload) {
				files.add(ob)
			}
		}
		return files
	}

	fun getCurrentFile(): ArrayList<AttachedFile> {
		val files = arrayListOf<AttachedFile>()
		for (ob in _list) {
			if (ob is AttachedFile && ob.surveyId.isNotEmpty() && !ob.isUpload) {
				files.add(ob)
			}
		}
		return files
	}

	fun getSurveyFile(): ArrayList<AttachedFile> {
		val files = arrayListOf<AttachedFile>()
		for (ob in _list) {
			if (ob is AttachedFile && ob.surveyId.isNotEmpty()) {
				files.add(ob)
			}
		}
		return files
	}

	fun getListLinkMessage(): ArrayList<String> {
		val list = arrayListOf<String>()
		for (ob in _list) {
			if (ob is String) {
				list.add(ob)
			}
		}
		return list
	}

	fun getListMessage(): ArrayList<Quote> {
		val list = arrayListOf<Quote>()
		for (ob in _list) {
			if (ob is Quote) {
				list.add(ob)
			}
		}
		return list
	}

	fun checkFileSizeLimit(context: Context): Boolean {
		val maximum = 25f
		var totalSize = 0f
		for (ob in _list) {
			if (ob is AttachedFile) {
				if (ob.isUpload) {
					totalSize = totalSize.plus(SystemUtil.getFileSize(context, ob.uri))
				}
			}
		}
		return totalSize > maximum
	}

	fun clearAll() {
		val size: Int = _list.size
		_list.clear()
		notifyItemRangeRemoved(0, size)
	}

	fun addQuote(forwardMessages: ArrayList<Quote>) {
		_list.clear()
		_list.addAll(forwardMessages)
		notifyItemRangeInserted(0, forwardMessages.size)
	}

	fun addFile(file: AttachedFile) {
		_list.add(file)
		notifyItemInserted(_list.lastIndex)
	}

	fun clearQuote(message: Quote) {
		val index: Int = _list.indexOf(message)
		_list.remove(message)
		notifyItemRemoved(index)
	}

}