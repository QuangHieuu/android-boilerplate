package boilerplate.ui.contact.viewholder

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import boilerplate.R
import boilerplate.databinding.ItemContactCompanyBinding
import boilerplate.model.user.Company
import boilerplate.model.user.Department
import boilerplate.ui.contact.listener.SimpleListener
import boilerplate.utils.extension.click
import java.util.Locale

class CompanyHolder(
    private val _binding: ItemContactCompanyBinding,
    private val _listener: SimpleListener,
    private val _isCheck: Boolean = false,
    private val _context: Context = _binding.root.context
) : RecyclerView.ViewHolder(_binding.root) {

    private val _padding = _binding.lnContactCompany.getPaddingStart()

    fun setData(any: Any) {
        if (any is Company) {
            with(_binding) {
                val paddingLevel = getPaddingLevel(any.contactLevel)
                lnContactCompany.setPadding(
                    _padding * paddingLevel,
                    lnContactCompany.paddingTop,
                    lnContactCompany.getPaddingEnd(),
                    lnContactCompany.paddingBottom
                )
                tvName.text = any.name

                imgDropDown.setImageResource(if (any.isExpanding) R.drawable.ic_minus_circle else R.drawable.ic_add_circle)
                if (any.isExpanding && !_isCheck) {
                    tvName.setTextColor(
                        ContextCompat.getColor(_context, R.color.colorPrimary)
                    )
                } else {
                    tvName.setTextColor(
                        ContextCompat.getColor(_context, R.color.color_212121)
                    )
                }
            }
        }
        if (any is Department) {
            val name = SpannableStringBuilder(any.name)
            val countAvail = SpannableStringBuilder()

            with(_binding) {
                val paddingLevel = getPaddingLevel(any.contactLevel)
                lnContactCompany.setPadding(
                    _padding * paddingLevel,
                    lnContactCompany.paddingTop,
                    lnContactCompany.getPaddingEnd(),
                    lnContactCompany.paddingBottom
                )
                imgDropDown.setImageResource(if (any.isExpanding) R.drawable.ic_minus_circle else R.drawable.ic_add_circle)

                var count = 0
                for (user in any.users ?: arrayListOf()) {
                    if (user.isOnline()) {
                        count += 1
                    }
                }
                countAvail.append(
                    String.format(
                        Locale.getDefault(),
                        "(%d/%s)",
                        count,
                        any.totalUser
                    )
                )
                if (any.isExpanding && !_isCheck) {
                    val color = ContextCompat.getColor(itemView.context, R.color.colorPrimary)
                    name.setSpan(
                        ForegroundColorSpan(color),
                        0,
                        name.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    countAvail.setSpan(
                        ForegroundColorSpan(color),
                        0,
                        countAvail.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                } else {
                    val main = ContextCompat.getColor(itemView.context, R.color.color_212121)
                    val second = ContextCompat.getColor(itemView.context, R.color.color_7589A3)

                    name.setSpan(
                        ForegroundColorSpan(main),
                        0,
                        name.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    countAvail.setSpan(
                        ForegroundColorSpan(second),
                        0,
                        countAvail.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                tvName.text = name.append("\u0020").append(countAvail)
                tvName.click { _listener.onExpandDepartment(any) }
            }
            _binding.imgDropDown.click { _binding.tvName.performClick() }
        }
    }
}

private fun getPaddingLevel(level: Int): Int {
    return if (level > 1) (level + 1.5f).toInt() else level
}