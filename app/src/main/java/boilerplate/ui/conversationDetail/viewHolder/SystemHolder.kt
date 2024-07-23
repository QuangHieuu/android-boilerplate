package boilerplate.ui.conversationDetail.viewHolder

import androidx.recyclerview.widget.RecyclerView
import boilerplate.databinding.ItemMessageSystemBinding
import boilerplate.model.message.Message
import boilerplate.utils.StringUtil
import boilerplate.utils.SystemUtil

class SystemHolder(
    private val _binding: ItemMessageSystemBinding
) : RecyclerView.ViewHolder(_binding.root) {

    fun setData(message: Message) {
        val size: Float = SystemUtil.getFontSizeChat(itemView.context)
        _binding.tvTitleSystem.text = StringUtil.getHtml(message.getContent().trim())
        _binding.tvTitleSystem.textSize = size
    }
}
