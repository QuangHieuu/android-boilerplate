package boilerplate.widget.chatBox

internal class Range(@JvmField var from: Int, @JvmField var to: Int) {
	fun isWrappedBy(start: Int, end: Int): Boolean {
		return (start in (from + 1)..<to) || (end in (from + 1)..<to)
	}

	fun contains(start: Int, end: Int): Boolean {
		return from <= start && to >= end
	}

	fun isEqual(start: Int, end: Int): Boolean {
		return (from == start && to == end) || (from == end && to == start)
	}

	fun getAnchorPosition(value: Int): Int {
		return if ((value - from) - (to - value) >= 0) {
			to
		} else {
			from
		}
	}
}