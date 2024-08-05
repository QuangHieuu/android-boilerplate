package boilerplate.ui.setting

import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.activityViewModels
import boilerplate.R
import boilerplate.base.BaseFragment
import boilerplate.databinding.DialogBaseBinding
import boilerplate.databinding.FragmentSettingBinding
import boilerplate.databinding.ItemSettingProfileBinding
import boilerplate.ui.contactDetail.ContactDetailFragment
import boilerplate.ui.main.MainVM
import boilerplate.ui.setting.SettingMenu.HOTLINE
import boilerplate.ui.setting.SettingMenu.LOG_OUT
import boilerplate.ui.setting.SettingMenu.PROFILE
import boilerplate.utils.StringUtil
import boilerplate.utils.extension.click
import boilerplate.utils.extension.loadImage
import boilerplate.utils.extension.notNull
import boilerplate.utils.extension.openDialog
import boilerplate.utils.extension.showDialog
import boilerplate.widget.customText.TextViewFont
import boilerplate.widget.image.RoundedImageView
import kotlin.properties.Delegates

class SettingFragment : BaseFragment<FragmentSettingBinding, MainVM>() {
    companion object {
        fun newInstance(): SettingFragment {
            return SettingFragment()
        }
    }

    override val viewModel: MainVM by activityViewModels()

    private var _margin by Delegates.notNull<Int>()
    private val _buttonIDs = intArrayOf(0, 0, 0, 0)
    private var _pressNumber = -1

    private var _handler: Handler? = null
    private var _runnable: Runnable? = null

    override fun initialize() {
        _margin = resources.getDimension(R.dimen.dp_15).toInt()

        val metrics = Resources.getSystem().displayMetrics
        val resources = requireContext().resources

        with(binding) {
            lnSetting.removeAllViews()

            lnSetting.addView(addMenuItem(null))
            for (menu in SettingMenu.listSettingMenu) {
                lnSetting.addView(addMenuItem(menu))
            }

            val tvVersion = TextViewFont(requireContext())
            tvVersion.setText(R.string.app_name)
            tvVersion.setFontRegular()
            tvVersion.textSize = resources.getDimension(R.dimen.dp_11) / metrics.density
            tvVersion.gravity = Gravity.CENTER
            lnSetting.addView(tvVersion)

            val params = LinearLayout.LayoutParams(lnSetting.getLayoutParams())
            params.setMargins(0, 0, 0, _margin)
            val tvCopyRight = TextViewFont(requireContext()).apply {
                text = getString(R.string.copy_right)
                setFontRegular()
                textSize = resources.getDimension(R.dimen.dp_11) / metrics.density
                gravity = Gravity.CENTER
                layoutParams = params
                click { v ->
                    if (_pressNumber < 3) {
                        _pressNumber += 1
                        _buttonIDs[_pressNumber] = v.id
                        _runnable.notNull { _handler?.removeCallbacks(_runnable!!) }
                        startCounter()
                    } else {
                        clearEverything()
                    }
                }
            }
            lnSetting.addView(tvCopyRight)
        }
    }

    override fun onSubscribeObserver() {
        with(viewModel) {
            user.observe(this@SettingFragment) {
                val avatar: RoundedImageView = binding.root.findViewById(R.id.img_avatar)
                val name: TextViewFont = binding.root.findViewById(R.id.tv_name)

                avatar.loadImage(it.avatar)
                name.text = it.name
            }
        }
    }

    override fun registerEvent() {
    }

    override fun callApi() {
    }

    private fun addMenuItem(menu: SettingMenu?): View {
        if (menu == null) {
            return ItemSettingProfileBinding.inflate(layoutInflater).root
        }
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.item_setting_menu, view as ViewGroup?, false)
        val image = view.findViewById<AppCompatImageView>(R.id.img_icon)
        image.setImageResource(menu.icon)
        val tvTitle: TextViewFont = view.findViewById(R.id.tv_title)

        val lnMenu = view.findViewById<LinearLayout>(R.id.ln_item_menu)

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        if (SettingMenu.isMargin(menu.index)) {
            params.setMargins(0, 0, 0, _margin)
            lnMenu.layoutParams = params
        }

        tvTitle.text = if (menu.index == HOTLINE.index) {
            StringUtil.getHtmlSpan(menu.title)
        } else {
            menu.title
        }
        view.click { handleMenu(menu) }
        return view
    }

    private fun handleMenu(menu: SettingMenu) {
        when (SettingMenu.fromType(menu.index)) {
            PROFILE -> {
                openDialog(ContactDetailFragment.newInstance())
            }

            LOG_OUT -> {
                showDialog(DialogBaseBinding.inflate(layoutInflater)) { b, dialog ->
                    with(b) {
                        tvTitle.setText(R.string.logout)
                        btnConfirm.setText(R.string.logout)
                        tvDescription.setText(R.string.logout_description)

                        btnCancel.click { dialog.dismiss() }
                        btnConfirm.click {
                            dialog.dismiss()
                            viewModel.logout()
                        }
                    }
                }
            }

            else -> {}
        }
    }

    private fun startCounter() {
        _handler = Handler(Looper.getMainLooper())
        _runnable = Runnable {
            if (_buttonIDs[3] == 0) {
                clearEverything()
            }
        }
        _handler?.postDelayed(_runnable!!, 1000)
    }

    private fun clearEverything() {
        _pressNumber = -1
        for (i in 0..3) {
            _buttonIDs[i] = 0
        }
    }
}