package boilerplate.data.remote.repository.dashboard

import boilerplate.data.remote.api.ApiRequest
import boilerplate.data.remote.api.response.BaseResponse
import boilerplate.data.remote.api.response.BaseResult
import boilerplate.model.dashboard.Banner
import boilerplate.model.dashboard.Dashboard
import boilerplate.utils.extension.checkInternet
import io.reactivex.rxjava3.core.Flowable


interface DashboardRepository {
    fun getBanner(): Flowable<BaseResult<Banner>>

    fun getDashboardStatical(limit: Int): Flowable<BaseResponse<Dashboard>>
}

class DashboardImpl(private val apiRequest: ApiRequest) : DashboardRepository {
    override fun getBanner(): Flowable<BaseResult<Banner>> {
        return apiRequest.eOffice.getBanner().checkInternet()
    }

    override fun getDashboardStatical(limit: Int): Flowable<BaseResponse<Dashboard>> {
        return apiRequest.eOffice.getDashBoardStatical(limit).checkInternet()
    }
}