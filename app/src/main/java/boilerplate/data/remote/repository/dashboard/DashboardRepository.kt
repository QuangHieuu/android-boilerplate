package boilerplate.data.remote.repository.dashboard

import boilerplate.data.remote.api.ApiRequest
import boilerplate.data.remote.api.response.Response
import boilerplate.data.remote.api.response.ResponseItems
import boilerplate.model.dashboard.Banner
import boilerplate.model.dashboard.Dashboard
import boilerplate.utils.extension.checkInternet
import io.reactivex.rxjava3.core.Flowable


interface DashboardRepository {
    fun getBanner(): Flowable<ResponseItems<Banner>>

    fun getDashboardStatical(limit: Int): Flowable<Response<Dashboard>>
}

class DashboardImpl(private val apiRequest: ApiRequest) : DashboardRepository {
    override fun getBanner(): Flowable<ResponseItems<Banner>> {
        return apiRequest.eOffice.getBanner().checkInternet()
    }

    override fun getDashboardStatical(limit: Int): Flowable<Response<Dashboard>> {
        return apiRequest.eOffice.getDashBoardStatical(limit).checkInternet()
    }
}