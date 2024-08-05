package boilerplate.data.remote.repository.dashboard

import boilerplate.data.remote.api.ApiRequest
import boilerplate.data.remote.api.response.Response
import boilerplate.data.remote.api.response.ResponseItems
import boilerplate.model.dashboard.Banner
import boilerplate.model.dashboard.Dashboard
import boilerplate.model.dashboard.Statical
import boilerplate.utils.extension.checkInternet
import io.reactivex.rxjava3.core.Flowable


interface DashboardRepository {
    fun getBanner(): Flowable<ResponseItems<Banner>>

    fun getDashboardStatical(limit: Int): Flowable<Response<Dashboard>>

    fun getCountSignInternal(): Flowable<Response<Int>>

    fun getCountSignExternal(): Flowable<Response<Int>>

    fun getStaticalWork(): Flowable<Response<Statical>>

    fun getStaticalDocument(): Flowable<Response<Statical>>

    fun getStaticalSign(): Flowable<Response<Statical>>
}

class DashboardImpl(private val apiRequest: ApiRequest) : DashboardRepository {
    override fun getBanner(): Flowable<ResponseItems<Banner>> {
        return apiRequest.eOffice.getBanner().checkInternet()
    }

    override fun getDashboardStatical(limit: Int): Flowable<Response<Dashboard>> {
        return apiRequest.eOffice.getDashBoardStatical(limit).checkInternet()
    }

    override fun getCountSignInternal(): Flowable<Response<Int>> {
        return apiRequest.eOffice.getCountSignInternal(0).checkInternet()
    }

    override fun getCountSignExternal(): Flowable<Response<Int>> {
        return apiRequest.eOffice.getCountSignExternal().checkInternet()
    }

    override fun getStaticalDocument(): Flowable<Response<Statical>> {
        return apiRequest.getEOffice(ApiRequest.VERSION_2).getStaticalDocument().checkInternet()
    }

    override fun getStaticalSign(): Flowable<Response<Statical>> {
        return apiRequest.getEOffice(ApiRequest.VERSION_2).getStaticalSign().checkInternet()
    }

    override fun getStaticalWork(): Flowable<Response<Statical>> {
        return apiRequest.getEOffice(ApiRequest.VERSION_2).getStaticalWork().checkInternet()
    }
}