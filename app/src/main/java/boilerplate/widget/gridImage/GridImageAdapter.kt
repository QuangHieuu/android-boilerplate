package boilerplate.widget.gridImage

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import boilerplate.R
import boilerplate.databinding.ItemConversationImageBinding
import boilerplate.databinding.ItemLoadMoreBinding
import boilerplate.databinding.ItemTitleBinding
import boilerplate.model.file.AttachedFile
import boilerplate.model.file.ExtensionType
import boilerplate.utils.extension.loadImage
import boilerplate.utils.extension.show
import boilerplate.widget.holder.LoadingVH
import boilerplate.widget.holder.TitleVH
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

open class GridImageAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	private val mList = ArrayList<Any?>()
	private var mIsMultiChoose = false
	private var mIsAdjustView = false

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		val inflater: LayoutInflater = LayoutInflater.from(parent.context)
		when (viewType) {
			TYPE_IMAGE -> {
				return ImageHolder(
					ItemConversationImageBinding.inflate(inflater, parent, false),
					mIsAdjustView
				)
			}

			TYPE_TITLE -> {
				return TitleVH(ItemTitleBinding.inflate(inflater))
			}

			else -> {
				return LoadingVH(ItemLoadMoreBinding.inflate(inflater))
			}
		}
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		when (holder.itemViewType) {
			TYPE_IMAGE -> (holder as ImageHolder).setImage(
				mList[position] as AttachedFile,
				mIsMultiChoose
			)

			TYPE_TITLE -> (holder as TitleVH).apply {
				_binding.tvTitle.text = mList[position] as String?
			}

			else -> {}
		}
	}

	override fun getItemViewType(position: Int): Int {
		if (mList[position] is String) {
			return TYPE_TITLE
		}
		if (position == mList.size - 1 && mList[position] == null) {
			return TYPE_LOAD_MORE
		}
		return TYPE_IMAGE
	}

	override fun getItemCount(): Int {
		return mList.size
	}

	fun addImage(items: ArrayList<AttachedFile>) {
		val size = mList.size
		if (size > 0) {
			mList.clear()
			notifyItemRangeRemoved(0, size)
		}
		mList.addAll(items)
		notifyItemRangeInserted(0, mList.size)
	}

	fun loadMore(items: ArrayList<Any>) {
		val size = mList.size
		val ob = mList[size - 1]
		if (items.size != 0 && mList.contains(items[0])) {
			items.removeAt(0)
		}
		mList.addAll(items)
		notifyItemRangeInserted(size, items.size)
	}

	fun showLoadMore() {
		mList.add(null)
		notifyItemInserted(mList.size - 1)
	}

	fun cancelLoadMore() {
		val size = mList.size
		if (size > 0) {
			val ob = mList[size - 1]
			if (ob == null) {
				mList.remove(null)
				notifyItemRemoved(size - 1)
			}
		}
	}

	fun clearAll() {
		val size = mList.size
		mList.clear()
		notifyItemRangeRemoved(0, size)
	}

	fun setIsMultiChoose(isChoose: Boolean) {
		mIsMultiChoose = isChoose
		for (ob in mList) {
			if (ob is AttachedFile) {
				if (!mIsMultiChoose) {
					ob.isChecked = false
				}
			}
		}
		notifyItemRangeChanged(0, mList.size)
	}

	fun updateChecked(file: AttachedFile?) {
		val index = mList.indexOf(file)
		mList[index] = file
		notifyItemChanged(index, file)
	}

	val countImageCheck: Int
		get() {
			var count = 0
			for (ob in mList) {
				if (ob is AttachedFile) {
					if (ob.isChecked) {
						count += 1
					}
				}
			}
			return count
		}

	val shareImage: ArrayList<AttachedFile>
		get() {
			val files: ArrayList<AttachedFile> = ArrayList()
			for (ob in mList) {
				if (ob is AttachedFile) {
					if (ob.isChecked) {
						files.add(ob)
					}
				}
			}
			return files
		}

	companion object {
		private const val TYPE_IMAGE = 0
		const val TYPE_TITLE: Int = 1
		const val TYPE_LOAD_MORE: Int = 2
	}
}


class ImageHolder(
	val binding: ItemConversationImageBinding,
	val isAdjust: Boolean,
	val context: Context = binding.root.context
) : RecyclerView.ViewHolder(binding.root) {

	fun setImage(file: AttachedFile, isMultiChoose: Boolean) {
		with(binding) {
			checkBox.apply {
				isChecked = file.isChecked
				show(isMultiChoose)
			}
			val type = file.fileType
			val url = if (ExtensionType.isFileGIF(type)) file.filePreview
			else file.fileThumb

			val options: RequestOptions = RequestOptions()
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.error(R.drawable.bg_error)
			imgRoundFile.apply { adjustViewBounds = isAdjust }
				.loadImage(url, type = type, requestOptions = options)
		}
	}
}