package boilerplate.ui.dashboard.adapter

import android.content.Context
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
import boilerplate.model.dashboard.HomeFeatureMenu.Companion.blockDashboardDocument
import boilerplate.ui.dashboard.DashboardFragment.OnMenuListener
import boilerplate.ui.dashboard.adapter.BlockDocumentAdapter.DocumentVH
import boilerplate.utils.extension.click

class BlockDocumentAdapter(
    private val _listener: OnMenuListener
) : RecyclerView.Adapter<DocumentVH>() {
    private var _needDone = 0
    private var _notAssign = 0

    fun setData(notAssign: Int, needDone: Int) {
        _notAssign = notAssign
        _needDone = needDone
        notifyItemChanged(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentVH {
        val layoutInflater = LayoutInflater.from(parent.context)
        return DocumentVH(
            ItemDashboardBlockBinding.inflate(layoutInflater, parent, false),
            _listener
        )
    }

    override fun onBindViewHolder(holder: DocumentVH, position: Int) {
        holder.setData(_notAssign, _needDone)
    }

    override fun getItemCount(): Int {
        return 1
    }

    class DocumentVH(
        private val _binding: ItemDashboardBlockBinding,
        private val _listener: OnMenuListener,
        private val _content: Context = _binding.root.context
    ) : RecyclerView.ViewHolder(_binding.root) {

        fun setData(process: Int, inProcess: Int) {
            val features = blockDashboardDocument()

            with(_binding) {
                tvTitle.setText(features.name)
                imgIcon.setImageResource(features.icon)

                lnContain.removeAllViews()
                for (feature in features.data) {
                    when (fromIndex(feature.type)) {
                        EOfficeMenu.REFERENCE_HANDLE -> lnContain.addView(
                            addView(
                                feature,
                                process,
                                true
                            )
                        )

                        EOfficeMenu.REFERENCE_HANDLING -> lnContain.addView(
                            addView(
                                feature,
                                inProcess,
                                false
                            )
                        )

                        else -> {}
                    }
                }
            }
        }

        private fun addView(page: HomePage, process: Int, underline: Boolean): View {
            val binding =
                ItemDashboardBlockChildBinding.inflate(LayoutInflater.from(_content))
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
