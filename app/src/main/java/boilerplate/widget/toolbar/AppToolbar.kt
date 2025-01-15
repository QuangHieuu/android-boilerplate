package boilerplate.widget.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import boilerplate.R
import boilerplate.databinding.ViewAppToolbarBinding
import boilerplate.utils.extension.click

class AppToolbar @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet?,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
	private val binding by lazy {
		ViewAppToolbarBinding.inflate(LayoutInflater.from(context))
	}
	private val _listLeftButton = arrayListOf<AppToolbarButton>()

	private var _backListener: OnClickListener? = null

	init {
		addView(binding.root)
		initView(attrs)
	}

	private fun initView(attrs: AttributeSet?) {
		if (attrs == null) {
			return
		}
		with(context.obtainStyledAttributes(attrs, R.styleable.AppToolbar, 0, 0)) {
			getResourceId(
				R.styleable.AppToolbar_backIcon,
				R.drawable.ic_arrow_previous_white
			).also { image ->
				_listLeftButton.add(AppToolbarButton(context).apply {
					click { _backListener?.onClick(this) }
					setImageResource(image)
				})
			}
			getString(R.styleable.AppToolbar_title) ?: "".also {
				binding.tvTitle.text = it
			}
			getString(R.styleable.AppToolbar_subtitle) ?: "".also {
				binding.tvSubTitle.text = it
			}
			getResourceId(R.styleable.AppToolbar_titleTextAppearance, 0).also {
				if (it != 0) {
					binding.tvTitle.setTextAppearance(it)
				}
			}
			getResourceId(R.styleable.AppToolbar_subTitleTextAppearance, 0).also {
				if (it != 0) {
					binding.tvTitle.setTextAppearance(it)
				}
			}
			getColor(R.styleable.AppToolbar_titleTextColor, 0).also {
				if (it != 0) {
					binding.tvTitle.setTextColor(it)
				}
			}
			getColor(R.styleable.AppToolbar_subTitleTextColor, 0).also {
				if (it != 0) {
					binding.tvTitle.setTextColor(it)
				}
			}
			recycle()
		}
		_listLeftButton.forEach { binding.lnLeft.addView(it) }
	}

	fun setBackPress(back: OnClickListener) {
		_backListener = back
	}

	fun setTitle(title: String) {
		binding.tvTitle.text = title
	}

	fun setSubTitle(sub: String) {
		binding.tvSubTitle.text = sub
	}
}

fun AppToolbar.backPress(block: View.OnClickListener) {
	setBackPress(block)
}
