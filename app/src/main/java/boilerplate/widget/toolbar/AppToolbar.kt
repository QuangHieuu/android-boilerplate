package boilerplate.widget.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import boilerplate.R
import boilerplate.databinding.ViewAppToolbarBinding
import boilerplate.utils.extension.click

class AppToolbar @JvmOverloads constructor(
	context: Context,
	private val attrs: AttributeSet? = null,
	private val defStyleAttr: Int = R.attr.AppToolbarStyle,
	private val defStyle: Int = R.style.AppToolbar
) : LinearLayout(context, attrs, defStyleAttr, defStyle) {

	private val binding = ViewAppToolbarBinding.inflate(LayoutInflater.from(context), this, true)

	private val _listLeftButton = arrayListOf<AppToolbarButton>()

	var backListener: ((v: View) -> Unit)? = null

	init {
		context.withStyledAttributes(attrs, R.styleable.AppToolbar, defStyleAttr, defStyle) {
			val hasBackIcon = getBoolean(R.styleable.AppToolbar_hasBackNavigate, false)
			if (hasBackIcon) {
				_listLeftButton.add(
					AppToolbarButton(ContextThemeWrapper(context, R.style.AppToolbarButton_BackIcon))
						.click(backListener)
				)
			}
			with(binding) {

				tvTitle.text = getString(R.styleable.AppToolbar_title) ?: ""
				tvTitle.setTextAppearance(
					getResourceId(
						R.styleable.AppToolbar_titleTextAppearance,
						android.R.style.TextAppearance
					)
				)
				tvTitle.setTextColor(
					getColor(
						R.styleable.AppToolbar_titleTextColor,
						ContextCompat.getColor(context, android.R.color.white)
					)
				)

				tvSubTitle.text = getString(R.styleable.AppToolbar_subtitle) ?: ""
				tvSubTitle.setTextAppearance(
					getResourceId(
						R.styleable.AppToolbar_subTitleTextAppearance,
						android.R.style.TextAppearance
					)
				)
				tvSubTitle.setTextColor(
					getColor(
						R.styleable.AppToolbar_subTitleTextColor,
						ContextCompat.getColor(context, android.R.color.white)
					)
				)
			}
		}
		initView()
	}

	private fun initView() {
		_listLeftButton.forEach { binding.lnLeft.addView(it) }
	}

	fun setTitle(title: String) {
		binding.tvTitle.text = title
	}

	fun setSubTitle(sub: String) {
		binding.tvSubTitle.text = sub
	}
}

fun AppToolbar.backPress(block: (v: View) -> Unit) {
	backListener = block
}
