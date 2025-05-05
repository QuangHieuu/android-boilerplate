package calendar.widget.wheel.type

enum class WheelType(val type: Int) {
	DAY_OF_WEEK(0),
	DAY(1),
	MONTH(2),
	YEAR(3),
	HOUR(4),
	MINUTE(5),
	DAY_NIGHT(6),
	NONE(-1);

	companion object {

		fun getType(index: Int): WheelType {
			return WheelType.entries.find { it.type == index } ?: NONE
		}
	}
}
