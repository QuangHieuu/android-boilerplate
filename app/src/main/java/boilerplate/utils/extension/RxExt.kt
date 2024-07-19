package boilerplate.utils.extension

import androidx.lifecycle.MutableLiveData
import boilerplate.data.remote.api.ApiObservable
import boilerplate.utils.InternetException
import boilerplate.utils.InternetManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

class SchedulerProvider : BaseSchedulerProvider {

    override fun computation(): Scheduler {
        return Schedulers.computation()
    }

    override fun io(): Scheduler {
        return Schedulers.io()
    }

    override fun ui(): Scheduler {
        return AndroidSchedulers.mainThread()
    }
}

interface BaseSchedulerProvider {

    fun computation(): Scheduler

    fun io(): Scheduler

    fun ui(): Scheduler
}

/**
 * Use SchedulerProvider configuration for Completable
 */
fun Completable.withScheduler(scheduler: BaseSchedulerProvider): Completable =
    this.subscribeOn(scheduler.io()).observeOn(scheduler.ui()).checkInternet()

/**
 * Use SchedulerProvider configuration for Single
 */
fun <T : Any> Single<T>.withScheduler(scheduler: BaseSchedulerProvider): Single<T> =
    this.subscribeOn(scheduler.io()).observeOn(scheduler.ui()).checkInternet()

/**
 * Use SchedulerProvider configuration for Observable
 */
fun <T : Any> Observable<T>.withScheduler(scheduler: BaseSchedulerProvider): Observable<T> =
    this.subscribeOn(scheduler.io()).observeOn(scheduler.ui()).checkInternet()

/**
 * Use SchedulerProvider configuration for Flowable
 */
fun <T : Any> Flowable<T>.withScheduler(scheduler: BaseSchedulerProvider): Flowable<T> =
    this.subscribeOn(scheduler.io()).observeOn(scheduler.ui()).checkInternet()

fun <T : Any> Single<T>.loading(liveData: MutableLiveData<Boolean>) =
    doOnSubscribe { liveData.postValue(true) }.doFinally { liveData.postValue(false) }

fun <T : Any> Observable<T>.checkInternet(): Observable<T> = doOnSubscribe {
    val isConnected = InternetManager.isConnected()
    if (!isConnected) {
        throw InternetException()
    }
}

fun <T : Any> Flowable<T>.checkInternet(): Flowable<T> = doOnSubscribe {
    val isConnected = InternetManager.isConnected()
    if (!isConnected) {
        throw InternetException()
    }
}

fun <T : Any> Single<T>.checkInternet() = doOnSubscribe {
    val isConnected = InternetManager.isConnected()
    if (!isConnected) {
        throw InternetException()
    }
}

fun Completable.checkInternet(): Completable = doOnSubscribe {
    val isConnected = InternetManager.isConnected()
    if (!isConnected) {
        throw InternetException()
    }
}

fun <T : Any> Observable<T>.loading(liveData: MutableLiveData<Boolean>): Observable<T> =
    doOnSubscribe { liveData.postValue(true) }.doFinally { liveData.postValue(false) }

fun <T : Any> Flowable<T>.loading(liveData: MutableLiveData<Boolean>): Flowable<T> =
    doOnSubscribe { liveData.postValue(true) }.doFinally { liveData.postValue(false) }

fun Completable.loading(liveData: MutableLiveData<Boolean>) =
    doOnSubscribe { liveData.postValue(true) }.doFinally { liveData.postValue(false) }


fun <T : Any> Flowable<T>.result(
    success: (response: T) -> Unit = {},
    fail: (t: Throwable) -> Unit = {},
    common: Boolean = false
): Disposable =
    subscribeWith(ApiObservable.apiCallback(success = { success(it) }, fail = { fail(it) }, common))

fun <T : Any> Single<T>.result(
    success: (response: T) -> Unit = {},
    fail: (t: Throwable) -> Unit = {},
    common: Boolean = false
): Disposable =
    subscribeWith(ApiObservable.apiCallback(success = { success(it) }, fail = { fail(it) }, common))
