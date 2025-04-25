package android.empty.extension

import android.view.View
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

internal fun View.show(b: Boolean = true) {
	visibility = if (b) View.VISIBLE else View.GONE
}

internal fun View.hide() {
	visibility = View.INVISIBLE
}

internal fun View.gone() {
	visibility = View.GONE
}

internal fun convert(scrollDistance: Float, refreshDistance: Float): Float {
	val originalDragPercent = scrollDistance.div(refreshDistance)
	val dragPercent = min(1f, abs(originalDragPercent))
	val extraOS = abs(scrollDistance).minus(refreshDistance)
	val tensionSlingshotPercent = max(
		0f,
		min(extraOS, refreshDistance.times(2f)).div(refreshDistance)
	)

	val tensionSlingshotPercentDiv = tensionSlingshotPercent.div(4)
	val tensionSlingshotPercentPow = tensionSlingshotPercent.div(4).pow(2)
	val tensionPercent = tensionSlingshotPercentDiv.minus(tensionSlingshotPercentPow)
	val extraMove = refreshDistance.times(tensionPercent.times(2)).times(2)
	val convertY = refreshDistance.times(dragPercent).plus(extraMove)
	return convertY
}

/**
 * Returns the value of the Int plus the given [amount], but "loops" the value to keep it between
 * the [count] and zero.
 */
internal fun Int.loop(amount: Int, count: Int): Int {
	var newVal = this + amount;
	newVal %= count;
	if (newVal < 0)
		newVal += count
	return newVal
}

/**
 * Returns the value of the Int plus one, but "loops" the value to keep it between the [count] and zero.
 */
internal fun Int.loopedIncrement(count: Int): Int {
	return this.loop(1, count)
}

/**
 * Returns the value of the Int minus one, but "loops" the value to keep it between the [count] and zero.
 */
internal fun Int.loopedDecrement(count: Int): Int {
	return this.loop(-1, count)
}
