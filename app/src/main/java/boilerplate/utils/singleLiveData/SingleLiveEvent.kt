package boilerplate.utils.singleLiveData

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

import java.util.concurrent.atomic.AtomicBoolean

/**
 * A lifecycle-aware observable that sends only new updates after subscription, used for events like
 * navigation and Snackbar messages.
 *
 *
 * This avoids a common problem withScheduler events: on configuration change (like rotation) an update
 * can be emitted if the observer is active. This LiveData only calls the observable if there's an
 * explicit call to setValue() or call().
 *
 *
 * Note that only one observer is going to be notified of changes.
 */
class SingleLiveEvent<T> : MutableLiveData<T>() {

	private val pending = AtomicBoolean(false)

	@MainThread
	override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {

		if (hasActiveObservers()) {
			Log.w(TAG, "Multiple observers registered but only one will be notified of changes.")
		}

		// Observe the internal MutableLiveData
		super.observe(owner, Observer<T> { t ->
			if (pending.compareAndSet(true, false)) {
				observer.onChanged(t)
			}
		})
	}

	@MainThread
	override fun setValue(t: T?) {
		pending.set(true)
		super.setValue(t)
	}

	/**
	 * Used for cases where T is Void, to make calls cleaner.
	 */
	@MainThread
	fun call() {
		value = null
	}

	companion object {

		private const val TAG = "SingleLiveEvent"
	}
}
