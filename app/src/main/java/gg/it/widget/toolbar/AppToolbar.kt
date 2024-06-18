package gg.it.widget.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import gg.it.R
import gg.it.databinding.ViewAppToolbarBinding
import gg.it.utils.ClickUtil
import gg.it.utils.extension.notNull

class AppToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    interface OnBackListener {
        fun onPress()
    }

    private val binding by lazy {
        ViewAppToolbarBinding.inflate(LayoutInflater.from(context))
    }

    private lateinit var mBack: OnBackListener

    private var mBackIcon: Int = 0
    private var mTitle: String = ""
    private var mSubTitle: String = ""
    private var mTitleTextAppearance: Int = 0
    private var mSubTitleTextAppearance: Int = 0
    private var mTitleColor: Int = 0
    private var mSubTitleColor: Int = 0

    init {
        addView(binding.root)
        initView(attrs)
        init()
    }

    companion object {
        fun backPress(block: () -> Unit): OnBackListener = object : OnBackListener {
            override fun onPress() {
                block()
            }
        }
    }

    private fun initView(attrs: AttributeSet?) {
        if (attrs == null) {
            return
        }
        val array = context.obtainStyledAttributes(attrs, R.styleable.AppToolbar, 0, 0)

        mBackIcon = array.getResourceId(R.styleable.AppToolbar_backIcon, 0).also {
            if (it == 0) {
                binding.btnBack.visibility = View.GONE
            } else {
                binding.btnBack.apply {
                    visibility = View.VISIBLE
                    setImageResource(it)
                }
            }
        }

        mTitle = array.getString(R.styleable.AppToolbar_title) ?: "".also {
            binding.tvTitle.text = it
        }
        mSubTitle = array.getString(R.styleable.AppToolbar_subtitle) ?: "".also {
            binding.tvSubTitle.text = it
        }
        mTitleTextAppearance =
            array.getResourceId(R.styleable.AppToolbar_titleTextAppearance, 0)
                .also {
                    if (it != 0) {
                        binding.tvTitle.setTextAppearance(it)
                    }
                }
        mSubTitleTextAppearance =
            array.getResourceId(R.styleable.AppToolbar_subTitleTextAppearance, 0)
                .also {
                    if (it != 0) {
                        binding.tvTitle.setTextAppearance(it)
                    }
                }
        mTitleColor = array.getColor(R.styleable.AppToolbar_titleTextColor, 0).also {
            if (it != 0) {
                binding.tvTitle.setTextColor(it)
            }
        }
        mSubTitleColor = array.getColor(R.styleable.AppToolbar_subTitleTextColor, 0).also {
            if (it != 0) {
                binding.tvTitle.setTextColor(it)
            }
        }
        array.recycle()
    }

    private fun init() {
        with(binding) {
            btnBack.setOnClickListener(ClickUtil.onClick {
                mBack.notNull { it.onPress() }
            })
        }
    }

    fun setBackPress(back: OnBackListener) {
        mBack = back
    }

    fun setTitle(title: String) {
        binding.tvTitle.text = title
    }

    fun setSubTitle(sub: String) {
        binding.tvSubTitle.text = sub
    }
}