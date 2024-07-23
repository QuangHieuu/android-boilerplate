package boilerplate.ui.conversationDetail.viewHolder

import androidx.recyclerview.widget.RecyclerView
import boilerplate.R
import boilerplate.databinding.ItemMessageTimeBinding
import boilerplate.utils.DateTimeUtil

class TimeHolder(
    private val _binding: ItemMessageTimeBinding
) : RecyclerView.ViewHolder(_binding.root) {

    fun setData(message: String?) {
        _binding.tvTitleTime.text =
            if (DateTimeUtil.isToDay(message)) itemView.resources.getString(R.string.today)
            else message
    }
}
