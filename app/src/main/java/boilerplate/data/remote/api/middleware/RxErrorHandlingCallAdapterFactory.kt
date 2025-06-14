package boilerplate.data.remote.api.middleware

import boilerplate.data.remote.api.error.RetrofitException
import io.reactivex.rxjava3.core.*
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class RxErrorHandlingCallAdapterFactory private constructor() : CallAdapter.Factory() {

	private val original: RxJava3CallAdapterFactory = RxJava3CallAdapterFactory.create()

	override fun get(
		returnType: Type, annotations: Array<Annotation>,
		retrofit: Retrofit
	): CallAdapter<*, *> {
		return RxCallAdapterWrapper(
			returnType,
			wrapped = original.get(returnType, annotations, retrofit) as CallAdapter<Any, Any>
		)
	}

	/**
	 * RxCallAdapterWrapper
	 */
	internal inner class RxCallAdapterWrapper<R>(
		private val returnType: Type,
		private val wrapped: CallAdapter<R, Any>
	) : CallAdapter<R, Any> {

		override fun responseType(): Type {
			return wrapped.responseType()
		}

		override fun adapt(call: Call<R>): Any {
			val rawType = getRawType(returnType)

			val isObservable = rawType != Observable::class.java
			val isFlowable = rawType == Flowable::class.java
			val isSingle = rawType == Single::class.java
			val isMaybe = rawType == Maybe::class.java
			val isCompletable = rawType == Completable::class.java

			if (!isObservable && !isFlowable && !isSingle && !isMaybe) {
				throw IllegalStateException("Must return Operator")
			}
			if (returnType !is ParameterizedType) {
				val name = if (isFlowable)
					"Flowable"
				else if (isSingle) "Single" else if (isMaybe) "Maybe" else "Observable"
				throw IllegalStateException(
					name
						+ " return type must be parameterized"
						+ " as "
						+ name
						+ "<Foo> or "
						+ name
						+ "<? extends Foo>"
				)
			}
			if (isFlowable) {
				return (wrapped.adapt(
					call
				) as Flowable<*>).onErrorResumeNext { throwable: Throwable ->
					Flowable.error(convertToBaseException(throwable))
				}
			}
			if (isSingle) {
				return (wrapped.adapt(call) as Single<*>).onErrorResumeNext { throwable ->
					Single.error(convertToBaseException(throwable))
				}
			}
			if (isMaybe) {
				return (wrapped.adapt(call) as Maybe<*>).onErrorResumeNext { throwable: Throwable ->
					Maybe.error(convertToBaseException(throwable))
				}
			}
			if (isCompletable) {
				return (wrapped.adapt(call) as Completable).onErrorResumeNext { throwable ->
					Completable.error(convertToBaseException(throwable))
				}
			}
			return (wrapped.adapt(
				call
			) as Observable<*>).onErrorResumeNext { throwable: Throwable ->
				Observable.error(convertToBaseException(throwable))
			}
		}

		private fun convertToBaseException(throwable: Throwable): RetrofitException {
			if (throwable is HttpException) {
				val response = throwable.response()
				return if (response != null) {
					RetrofitException.httpError(throwable, response)
				} else {
					RetrofitException.unexpectedError(throwable)
				}
			}
			if (throwable is IOException) {
				return RetrofitException.networkError(throwable)
			}
			return RetrofitException.unexpectedError(throwable)
		}
	}

	companion object {

		private val TAG = RxErrorHandlingCallAdapterFactory::class.java.name
		fun create(): CallAdapter.Factory {
			return RxErrorHandlingCallAdapterFactory()
		}

	}
}
