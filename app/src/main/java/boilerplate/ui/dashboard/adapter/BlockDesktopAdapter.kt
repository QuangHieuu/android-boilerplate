package boilerplate.ui.dashboard.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import boilerplate.R
import boilerplate.databinding.ItemDashboardBlockBinding
import boilerplate.databinding.ItemDashboardBlockDesktopBinding
import boilerplate.model.dashboard.Desktop
import boilerplate.model.dashboard.HomeFeatureMenu
import boilerplate.ui.dashboard.DashboardFragment.OnDesktopListener
import boilerplate.ui.dashboard.adapter.BlockDesktopAdapter.DesktopHolder
import boilerplate.utils.extension.click

class BlockDesktopAdapter(
    private val _listener: OnDesktopListener
) : RecyclerView.Adapter<DesktopHolder>() {
    private val _list = ArrayList<Desktop>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DesktopHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return DesktopHolder(
            ItemDashboardBlockBinding.inflate(layoutInflater, parent, false),
            _listener
        )
    }

    override fun onBindViewHolder(holder: DesktopHolder, position: Int) {
        holder.setData(_list)
    }

    override fun getItemCount(): Int {
        return 1
    }

    fun setData(list: List<Desktop>?) {
        _list.clear()
        _list.addAll(list!!)
        notifyItemChanged(0)
    }

    class DesktopHolder(
        private val _binding: ItemDashboardBlockBinding,
        private val _listener: OnDesktopListener
    ) : RecyclerView.ViewHolder(_binding.root) {

        init {
            with(_binding) {
                tvTitle.setText(R.string.desktop)
                imgIcon.setImageResource(R.drawable.ic_menu_type_desktop)

            }
        }

        fun setData(list: ArrayList<Desktop>) {
            _binding.lnContain.removeAllViews()
            val iterator: ListIterator<Desktop> = list.listIterator()
            while (iterator.hasNext()) {
                val index = iterator.nextIndex()
                val desktop = iterator.next()
                _binding.lnContain.addView(addView(desktop, index != list.size - 1))
            }
        }

        private fun addView(desktop: Desktop, underline: Boolean): View {
            val binding =
                ItemDashboardBlockDesktopBinding.inflate(LayoutInflater.from(itemView.context))

            var type = -1
            var deadline: String? = ""
            var titleDeadline = -1
            var background = -1
            var color = -1
            when (desktop.type) {
                0 -> {
                    type = HomeFeatureMenu.DOCUMENTS.displayName
                    titleDeadline = R.string.document_time_with_dot
                    color = R.color.color_work_blue
                    background = R.drawable.bg_feedback_status_blue
                    deadline = desktop.documentTime
                }

                1 -> {
                    type = HomeFeatureMenu.SIGNING.displayName
                    titleDeadline = R.string.document_time_with_dot
                    color = R.color.color_work_yellow
                    background = R.drawable.bg_feedback_status_yellow
                    deadline = desktop.documentTime
                }

                2, 3 -> {
                    type = HomeFeatureMenu.WORKS.displayName
                    titleDeadline = R.string.deadline_with_dot
                    color = R.color.color_work_green
                    background = R.drawable.bg_feedback_status_green
                    deadline = desktop.deadline
                }
            }
            with(binding) {
                tvDeadline.text = deadline
                tvType.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        if (desktop.isOverTime()) R.color.color_D93448
                        else R.color.colorBlack
                    )
                )
                if (type != -1) {
                    tvType.setText(type)
                }

                if (titleDeadline != -1) {
                    tvTitleDeadline.setText(titleDeadline)
                }
                if (color != -1) {
                    tvType.setTextColor(ContextCompat.getColor(itemView.context, color))
                }
                if (background != -1) {
                    tvType.setBackgroundResource(background)
                }
                tvNumber.text = desktop.documentNumber
                tvContent.text = desktop.content

                lnBackground.setBackgroundResource(
                    if (underline) R.drawable.bg_border_bottom_grey
                    else R.color.colorWhite
                )
            }
            return binding.root.also {
                it.click { _listener.onDesktop(desktop) }
            }
        }
    }
}
