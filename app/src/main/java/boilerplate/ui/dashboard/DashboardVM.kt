package boilerplate.ui.dashboard

import androidx.lifecycle.MutableLiveData
import boilerplate.base.BaseViewModel
import boilerplate.data.remote.repository.dashboard.DashboardRepository
import boilerplate.model.dashboard.Banner
import boilerplate.model.dashboard.Dashboard
import boilerplate.utils.extension.BaseSchedulerProvider
import boilerplate.utils.extension.loading
import boilerplate.utils.extension.notNull
import boilerplate.utils.extension.with
import boilerplate.utils.extension.withScheduler
import io.reactivex.rxjava3.core.Flowable

class DashboardVM(
    private val schedulerProvider: BaseSchedulerProvider,
    private val dashboardRepo: DashboardRepository
) : BaseViewModel() {
    private val _banners by lazy { MutableLiveData<ArrayList<Banner>>() }
    val banners = _banners

    private val _dashboard by lazy { MutableLiveData<Dashboard>() }
    val dashboard = _dashboard


    fun getDashboard() {
        val slider =
            dashboardRepo.getBanner()
                .doOnNext { it.result.notNull { _banners.postValue(it.items) } }
        val statical = dashboardRepo.getDashboardStatical(10)
            .doOnNext { it.result.notNull { _dashboard.postValue(it) } }

        launchDisposable {
            Flowable.concat(slider, statical)
                .loading(_loading)
                .withScheduler(schedulerProvider).with({})
        }
    }
}