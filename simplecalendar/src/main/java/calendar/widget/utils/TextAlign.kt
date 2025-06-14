package calendar.widget.utils

enum class TextAlign(val align: Int) {
	CENTER(0),
	LEFT(1),
	RIGHT(2);

	companion object {

		fun to(align: Int): TextAlign {
			return entries.find { it.align == align } ?: LEFT
		}
	}
}