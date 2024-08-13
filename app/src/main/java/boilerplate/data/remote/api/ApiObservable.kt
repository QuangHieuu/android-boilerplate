package boilerplate.data.remote.api

import boilerplate.data.remote.api.error.ApiError
import boilerplate.data.remote.api.error.RetrofitException
import boilerplate.utils.InternetException
import boilerplate.utils.extension.notNull
import com.google.gson.Gson
import io.reactivex.rxjava3.core.CompletableObserver
import io.reactivex.rxjava3.core.FlowableSubscriber
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.internal.disposables.DisposableHelper
import io.reactivex.rxjava3.internal.util.EndConsumerHelper
import org.reactivestreams.Subscription
import java.util.concurrent.atomic.AtomicReference

interface ApiObserver<T : Any> : Observer<T>, FlowableSubscriber<T>,
	SingleObserver<T>, CompletableObserver

abstract class ApiObservable<T : Any>(
	private val mShowCommon: Boolean
) : ApiObserver<T>, Disposable {

	private val upstreamDisposable: AtomicReference<Disposable> = AtomicReference()
	private val upstreamSubscription: AtomicReference<Subscription> = AtomicReference()

	override fun onSubscribe(s: Subscription) {
		if (EndConsumerHelper.setOnce(this.upstreamSubscription, s, javaClass)) {
			onStart()
		}
	}

	override fun onSubscribe(d: Disposable) {
		if (EndConsumerHelper.setOnce(this.upstreamDisposable, d, javaClass)) {
			onStart()
		}
	}

	override fun isDisposed(): Boolean {
		return upstreamDisposable.get() === DisposableHelper.DISPOSED
	}

	override fun dispose() {
		DisposableHelper.dispose(upstreamDisposable)
	}

	override fun onNext(t: T) {
		onSuccess(t)
	}

	override fun onError(t: Throwable) {
		if (t is InternetException) {
			sListener.notInternet()
			return
		}
		if (t is RetrofitException) {
			val json: String = t.errorMessage
			try {
				val error: ApiError = sGson.fromJson(json, ApiError::class.java)
				handleError(error)
				return
			} catch (exception: Exception) {
				handleError(ApiError(0, "", ""))
				return
			}
		}
		if (t is ApiError) {
			handleError(t)
			return
		}
		handleError(ApiError(0, "", ""))
	}

	override fun onComplete() {
	}

	abstract fun onFail(error: ApiError)

	abstract override fun onSuccess(response: T)

	private fun onStart() {
		upstreamSubscription.get().notNull { it.request(Long.MAX_VALUE) }
	}

	private fun handleError(error: ApiError) {
		val api: String = error.api
		if (error.code == 401) {
			sListener.invalidToken()
			onFail(error)
			return
		}
		if (api.contains("connect/token")) {
			if (error.code == 400) {
				sListener.invalidLogin()
			} else {
				sListener.onServerError(error.code, api, mShowCommon)
			}
			onFail(error)
			return
		}
		sListener.onServerError(error.code, api, mShowCommon)
		onFail(error)
	}

	companion object {
		lateinit var sGson: Gson
		lateinit var sListener: OnApiCallBack

		fun setServerResponseListener(listener: OnApiCallBack, gson: Gson) {
			sGson = gson
			sListener = listener
		}

		fun <T : Any> apiCallback(
			success: (response: T) -> Unit,
			fail: (t: ApiError) -> Unit = {},
			common: Boolean = false
		): ApiObservable<T> = object : ApiObservable<T>(common) {
			override fun onSuccess(response: T) {
				success(response)
			}

			override fun onFail(error: ApiError) {
				fail(error)
			}
		}
	}
}
