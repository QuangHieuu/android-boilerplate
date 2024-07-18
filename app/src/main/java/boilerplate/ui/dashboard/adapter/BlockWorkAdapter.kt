package boilerplate.ui.dashboard.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import boilerplate.R
import boilerplate.databinding.ItemDashboardBlockBinding
import boilerplate.databinding.ItemDashboardBlockChildBinding
import boilerplate.model.dashboard.EOfficeMenu
import boilerplate.model.dashboard.EOfficeMenu.Companion.fromIndex
import boilerplate.model.dashboard.HomeFeature.HomePage
import boilerplate.model.dashboard.HomeFeatureMenu.Companion.blockDashboardWork
import boilerplate.ui.dashboard.DashboardFragment.OnMenuListener
import boilerplate.ui.dashboard.adapter.BlockWorkAdapter.WorkHolder
import boilerplate.utils.ClickUtil

class BlockWorkAdapter(private val _listener: OnMenuListener) : RecyclerView.Adapter<WorkHolder>() {
    private var _notAssign = 0
    private var _needDone = 0
    private var _overTime = 0

    fun setData(a: Int, b: Int, c: Int) {
        _notAssign = a
        _needDone = b
        _overTime = c
        notifyItemChanged(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return WorkHolder(
            ItemDashboardBlockBinding.inflate(layoutInflater, parent, false),
            _listener
        )
    }

    override fun onBindViewHolder(holder: WorkHolder, position: Int) {
        holder.setData(_notAssign, _needDone, _overTime)
    }

    override fun getItemCount(): Int {
        return 1
    }

    class WorkHolder(
        private val _binding: ItemDashboardBlockBinding,
        private val _listener: OnMenuListener
    ) : RecyclerView.ViewHolder(_binding.root) {
        internal fun setData(a: Int, b: Int, c: Int) {
            val features = blockDashboardWork()
            with(_binding) {
                tvTitle.setText(features.name)
                imgIcon.setImageResource(features.icon)

                lnContain.removeAllViews()
                for (feature in features.data) {
                    when (fromIndex(feature.type)) {
                        EOfficeMenu.WORK_NO_ASSIGN -> lnContain.addView(addView(feature, a, true))
                        EOfficeMenu.WORK_NEED_DONE -> lnContain.addView(addView(feature, b, true))
                        EOfficeMenu.WORK_OVER_TIME -> lnContain.addView(addView(feature, c, false))
                        else -> {}
                    }
                }
            }
        }

        private fun addView(page: HomePage, process: Int, underline: Boolean): View {
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
                it.setOnClickListener(ClickUtil.onClick {
                    _listener.onMenu(page)
                })
            }
        }
    }
}
