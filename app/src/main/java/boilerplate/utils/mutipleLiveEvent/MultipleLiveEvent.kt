package boilerplate.utils.mutipleLiveEvent

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.atomic.AtomicBoolean

open class MultipleLiveEvent<T> : MutableLiveData<T>() {

	private val _pending = AtomicBoolean(false)
	private val _values: Queue<T> = LinkedList()

	@MainThread
	override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
		// Observe the internal MutableLiveData
		super.observe(owner) { t: T ->
			observer.onChanged(t)
			if (_pending.compareAndSet(true, false)) {
				//call next values processing if have such
				if (_values.isNotEmpty()) {
					pollValue()
				}
			}
		}
	}

	override fun postValue(value: T) {
		_values.add(value)
		pollValue()
	}

	private fun pollValue() {
		value = _values.poll()
	}

	@MainThread
	override fun setValue(t: T?) {
		_pending.set(true)
		super.setValue(t)
	}

	/**
	 * Used for cases where T is Void, to make calls cleaner.
	 */
	@Suppress("unused")
	@MainThread
	fun call() {
		value = null
	}
}