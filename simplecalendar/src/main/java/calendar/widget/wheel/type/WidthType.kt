package calendar.widget.wheel.type

enum class WidthType(val type: Int) {
	WRAP(0),
	FILL(1);

	companion object {

		fun to(int: Int): WidthType {
			return WidthType.entries.find { it.type == int } ?: WRAP
		}
	}
}