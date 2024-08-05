package boilerplate.ui.dashboard.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import boilerplate.R
import boilerplate.databinding.ItemDashboardBlockBinding
import boilerplate.databinding.ItemDashboardBlockChildBinding
import boilerplate.model.dashboard.DashboardBlock
import boilerplate.model.dashboard.FeatureMenu.Companion.blockDashboardSign
import boilerplate.model.dashboard.Page
import boilerplate.ui.dashboard.DashboardFragment.OnMenuListener
import boilerplate.ui.dashboard.adapter.BlockSignAdapter.AssignVH
import boilerplate.utils.extension.click

class BlockSignAdapter(
    private val _listener: OnMenuListener
) : RecyclerView.Adapter<AssignVH>() {
    private var _process = 0

    fun setData(process: Int) {
        _process = process
        notifyItemChanged(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignVH {
        val layoutInflater = LayoutInflater.from(parent.context)
        return AssignVH(
            ItemDashboardBlockBinding.inflate(layoutInflater, parent, false),
            _listener
        )
    }

    override fun onBindViewHolder(holder: AssignVH, position: Int) {
        holder.setData(_process)
    }

    override fun getItemCount(): Int {
        return 1
    }

    class AssignVH(
        private val _binding: ItemDashboardBlockBinding,
        private val _listener: OnMenuListener
    ) : RecyclerView.ViewHolder(_binding.root) {
        internal fun setData(a: Int) {
            val features = blockDashboardSign()

            with(_binding) {
                tvTitle.setText(features.name)
                imgIcon.setImageResource(features.icon)

                lnContain.removeAllViews()
                for (feature in features.data) {
                    if (DashboardBlock.fromIndex(feature.type) == DashboardBlock.SIGN_GOING) {
                        lnContain.addView(addView(feature, a, false))
                    }
                }
            }
        }

        private fun addView(page: Page, process: Int, underline: Boolean): View {
            val binding =
                ItemDashboardBlockChildBinding.inflate(LayoutInflater.from(itemView.context))
            with(binding) {
                cardview.setCardBackgroundColor(Color.parseColor(page.color))
                imgIcon.setImageResource(page.icon)
                tvTitle.text = page.name
                tvCount.text = process.toString()
                lnBackground.setBackgroundResource(
                    if (underline) R.drawable.bg_border_bottom_grey
                    else R.color.colorWhite
                )
            }
            return binding.root.also {
                it.click { _listener.onMenu(page) }
            }
        }
    }
}
