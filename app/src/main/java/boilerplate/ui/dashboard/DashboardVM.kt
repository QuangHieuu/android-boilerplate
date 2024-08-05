package boilerplate.ui.dashboard

import android.util.Log
import androidx.lifecycle.MutableLiveData
import boilerplate.base.BaseViewModel
import boilerplate.constant.AccountManager
import boilerplate.data.remote.repository.dashboard.DashboardRepository
import boilerplate.model.dashboard.Banner
import boilerplate.model.dashboard.Dashboard
import boilerplate.model.menu.EOfficeMenu
import boilerplate.utils.extension.BaseSchedulerProvider
import boilerplate.utils.extension.loading
import boilerplate.utils.extension.notNull
import boilerplate.utils.extension.result
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

    private val _updateCount by lazy { MutableLiveData<ArrayList<Pair<EOfficeMenu, Int>>>() }
    val updateCount = _updateCount

    fun getDashboard() {
        val slider =
            dashboardRepo.getBanner()
                .doOnNext { it.result.notNull { _banners.postValue(it.items) } }
        val statical = dashboardRepo.getDashboardStatical(10)
            .doOnNext { it.result.notNull { _dashboard.postValue(it) } }

        launchDisposable {
            Flowable.concat(slider, statical)
                .loading(_loading)
                .withScheduler(schedulerProvider).result({})
        }
    }

    fun getMenuStatical() {
        launchDisposable {
            Flowable.just(arrayListOf<Pair<EOfficeMenu, Int>>())
                .flatMap { result ->
                    dashboardRepo.getStaticalWork().map {
                        result.apply {
                            if (AccountManager.hasDepartmentWorkManager()) {
                                add(
                                    Pair(
                                        EOfficeMenu.DEPARTMENT_NOT_ASSIGN,
                                        it.result?.workNotAssign ?: 0
                                    )
                                )
                            }
                            if (AccountManager.hasPersonalWorkManager()) {
                                add(
                                    Pair(
                                        EOfficeMenu.PERSONAL_NOT_DOING,
                                        it.result?.workNeedDone ?: 0
                                    )
                                )
                            }
                            add(Pair(EOfficeMenu.WATCH_TO_KNOW, it.result?.watch ?: 0))
                        }
                    }.onErrorReturn { result }
                }
                .flatMap { result ->
                    if (AccountManager.hasIncomeDocument()) {
                        dashboardRepo.getStaticalDocument().map {
                            result.apply {
                                add(Pair(EOfficeMenu.NOT_HANDLE, it.result?.documentUnProcess ?: 0))
                                add(Pair(EOfficeMenu.IN_PROCESS, it.result?.documentInProcess ?: 0))
                                add(Pair(EOfficeMenu.MISTAKE, it.result?.documentMistake ?: 0))
                            }
                        }.onErrorReturn { result }
                    } else {
                        Flowable.just(result)
                    }
                }
                .flatMap { result ->
                    if (AccountManager.hasDigitalSignManage()) {
                        dashboardRepo.getStaticalSign().map {
                            result.apply {
                                add(Pair(EOfficeMenu.SIGN_GOING, it.result?.signGoing ?: 0))
                                add(Pair(EOfficeMenu.MISTAKE, it.result?.documentMistake ?: 0))
                            }
                        }.onErrorReturn { result }
                    } else {
                        Flowable.just(result)
                    }
                }
                .flatMap { result ->
                    if (AccountManager.hasDigitalSignManage()) {
                        Flowable.concat(
                            dashboardRepo.getCountSignInternal().map {
                                Pair(EOfficeMenu.SIGN_INTERNAL, it.result ?: 0)
                            },
                            dashboardRepo.getCountSignExternal().map {
                                Pair(EOfficeMenu.SIGN_EXTERNAL, it.result ?: 0)
                            }
                        )
                            .collect({ result }, { list, value -> list.add(value) })
                            .toFlowable()
                            .onErrorReturn { result }
                    } else {
                        Flowable.just(result)
                    }
                }
                .withScheduler(schedulerProvider)
                .result({ _updateCount.postValue(it) }, {})
        }
    }
}