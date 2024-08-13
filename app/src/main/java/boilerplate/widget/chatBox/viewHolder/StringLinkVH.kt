package boilerplate.widget.chatBox.viewHolder

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import boilerplate.databinding.ItemLinkBinding
import boilerplate.utils.extension.click
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.show

class StringLinkVH(
	val binding: ItemLinkBinding,
	val block: (link: String) -> Unit,
	context: Context = binding.root.context
) : RecyclerView.ViewHolder(binding.root) {

	fun setData(link: String) {
		with(binding) {
			tvTitle.gone()
			tvLink.text = link

			imgClear.show()
			imgIcon.click { block(link) }
		}
	}
}