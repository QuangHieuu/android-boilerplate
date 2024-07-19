package boilerplate.ui.dashboard

import android.content.Intent
import android.net.Uri
import androidx.recyclerview.widget.ConcatAdapter
import boilerplate.base.BaseFragment
import boilerplate.constant.AccountManager
import boilerplate.databinding.FragmentDashboardBinding
import boilerplate.model.dashboard.Desktop
import boilerplate.model.dashboard.EOfficeMenu
import boilerplate.model.dashboard.HomeFeature
import boilerplate.ui.dashboard.adapter.BlockBannerAdapter
import boilerplate.ui.dashboard.adapter.BlockDesktopAdapter
import boilerplate.ui.dashboard.adapter.BlockDocumentAdapter
import boilerplate.ui.dashboard.adapter.BlockSignAdapter
import boilerplate.ui.dashboard.adapter.BlockWorkAdapter
import boilerplate.ui.main.MainVM
import boilerplate.utils.extension.loadImage
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class DashboardFragment : BaseFragment<FragmentDashboardBinding, DashboardVM>() {
    companion object {
        fun newInstance(): DashboardFragment {
            return DashboardFragment()
        }
    }

    interface OnSliderImageListener {
        fun onClick(url: String)
    }

    interface OnMenuListener {
        fun onMenu(menu: HomeFeature.HomePage)
    }

    interface OnDesktopListener {
        fun onDesktop(desktop: Desktop)
    }

    override val _viewModel: DashboardVM by viewModel()
    private val _activityVM: MainVM by activityViewModel()

    private lateinit var _slider: BlockBannerAdapter
    private lateinit var _blockDocument: BlockDocumentAdapter
    private lateinit var _blockSign: BlockSignAdapter
    private lateinit var _blockWork: BlockWorkAdapter
    private lateinit var _blockDesktop: BlockDesktopAdapter

    private lateinit var _dynamic: ConcatAdapter

    override fun initialize() {
        val listener = object : OnMenuListener {
            override fun onMenu(menu: HomeFeature.HomePage) {
                handleMenu(menu)
            }
        }

        _slider = BlockBannerAdapter(object : OnSliderImageListener {
            override fun onClick(url: String) {
                if (url.isNotEmpty()) {
                    startActivity(Intent(Intent.ACTION_VIEW).apply { setData(Uri.parse(url.trim())) })
                }
            }
        })
        _blockDocument = BlockDocumentAdapter(listener)
        _blockSign = BlockSignAdapter(listener)
        _blockWork = BlockWorkAdapter(listener)
        _blockDesktop = BlockDesktopAdapter(object : OnDesktopListener {
            override fun onDesktop(desktop: Desktop) {

            }
        })

        _dynamic = ConcatAdapter()

        val mainAdapter = ConcatAdapter(
            _slider,
            _dynamic,
            _blockWork,
            _blockDesktop
        )

        with(binding.rclTodayWork) {
            adapter = mainAdapter
            itemAnimator = null
        }
    }

    override fun onSubscribeObserver() {
        with(_activityVM) {
            user.observe(this@DashboardFragment) {
                binding.imgAvatar.loadImage(it.avatar)
                binding.tvName.text = it.name
                binding.tvRole.text = currentFullName
            }
        }
        with(_viewModel) {
            banners.observe(this@DashboardFragment) { list ->
                _slider.setBanners(list.orEmpty())

            }
            dashboard.observe(this@DashboardFragment) {
                val statical = it.statical

                if (AccountManager.hasIncomeDocument()) {
                    _blockDocument.setData(
                        statical?.documentUnProcess ?: 0,
                        statical?.documentInProcess ?: 0
                    )
                    _dynamic.addAdapter(0, _blockDocument)
                } else {
                    _dynamic.removeAdapter(_blockDocument)
                }
                if (AccountManager.hasDigitalSignManage()) {
                    _blockSign.setData(statical?.signGoing ?: 0)

                    val index = if (_dynamic.adapters.isEmpty()) 0 else 1
                    _dynamic.addAdapter(index, _blockSign)
                } else {
                    _dynamic.removeAdapter(_blockSign)
                }
                _dynamic.notifyItemRangeChanged(0, _dynamic.adapters.size)

                _blockWork.setData(
                    statical?.workNotAssign ?: 0,
                    statical?.workNeedDone ?: 0,
                    statical?.workOverTime ?: 0
                )
                val list = if (it.desktop!!.size > 5) {
                    it.desktop!!.subList(0, 5)
                } else {
                    it.desktop!!
                }
                _blockDesktop.setData(list)
            }
        }
    }

    override fun registerEvent() {
        with(binding) {
            swipeRefreshDashboard.setOnRefreshListener {
                swipeRefreshDashboard.isRefreshing = false
                _viewModel.getDashboard()
            }
        }
    }

    override fun callApi() {
        _viewModel.getDashboard()
    }

    private fun handleMenu(page: HomeFeature.HomePage) {
        when (EOfficeMenu.fromIndex(page.type)) {
            EOfficeMenu.REFERENCE_HANDLE -> {
//                openFragment(DocumentFragment.newInstance())
            }

            EOfficeMenu.REFERENCE_HANDLING -> {
//                openFragment(DocumentFragment.newInstance(1))
            }

            EOfficeMenu.SIGN_GOING -> {
//                openFragment(DigitalSignatureFragment.newInstance(DigitalSignType.REFERENCE_SIGNING.getType()))
            }

            EOfficeMenu.WORK_NO_ASSIGN -> {
//                openFragment(WorkDepartmentFragment.newInstance())
            }

            EOfficeMenu.WORK_NEED_DONE, EOfficeMenu.WORK_OVER_TIME -> {
//                openFragment(WorkPersonalFragment.newInstance(0))
            }

            else -> {}
        }
    }
}