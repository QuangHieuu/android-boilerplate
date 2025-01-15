package boilerplate.ui.main.tab

import android.util.Pair
import androidx.fragment.app.Fragment
import boilerplate.R
import boilerplate.ui.empty.EmptyFragment

object HomeTabIndex {

	const val POSITION_HOME_DASHBOARD: String = "POSITION_HOME_DASHBOARD"
	const val POSITION_HOME_MENU: String = "POSITION_HOME_MENU"
	const val POSITION_WORK_MANAGER_TAB: String = "POSITION_WORK_MANAGER_TAB"
	const val POSITION_CALENDAR_TAB: String = "POSITION_CALENDAR_TAB"
	const val POSITION_CONTACT_TAB: String = "POSITION_CONTACT_TAB"
	const val POSITION_MESSAGE_TAB: String = "POSITION_MESSAGE_TAB"
	const val POSITION_SETTING_TAB: String = "POSITION_SETTING_TAB"

	private var ID_HOME_DASHBOARD = 0
	private const val ID_HOME_MENU = 0
	private const val ID_WORK_MANAGER_TAB = 1
	private const val ID_CALENDAR_TAB = 1
	private const val ID_CONTACT_TAB = 2
	private const val ID_MESSAGE_TAB = 3
	private const val ID_SETTING_TAB = 4

	val tabPosition = ArrayList<String>()
	val tabTitle = arrayListOf<String>()
	val tabIcon = arrayListOf<Int>()

	fun getCurrentTabIndex(string: String): Int {
		return tabPosition.indexOf(string)
	}

	fun isCurrentMessageTab(pos: Int): Boolean {
		if (tabPosition.isEmpty()) {
			return false
		}
		return tabPosition[pos] == POSITION_MESSAGE_TAB
	}

	fun isCurrentWorkManagerTab(pos: Int): Boolean {
		if (tabPosition.isEmpty()) {
			return false
		}
		return tabPosition[pos] == POSITION_WORK_MANAGER_TAB
	}

	val workPosition: Int
		get() = tabPosition.indexOf(POSITION_WORK_MANAGER_TAB)

	val messagePosition: Int
		get() = tabPosition.indexOf(POSITION_MESSAGE_TAB)

	val homeDashboardPosition: Int
		get() = tabPosition.indexOf(POSITION_HOME_DASHBOARD)

	val contactPosition: Int
		get() = tabPosition.indexOf(POSITION_CONTACT_TAB)

	fun setupFragment(tablet: Boolean): ArrayList<Pair<Int, Fragment>> {
		tabIcon.clear()
		tabTitle.clear()
		tabPosition.clear()

		val fragments = ArrayList<Pair<Int, Fragment>>()

		if (tablet) {
			fragments.add(Pair(ID_HOME_MENU, EmptyFragment.newInstance()))
			tabIcon.add(R.drawable.state_ic_tab_menu)
			tabTitle.add("eOffice")
			tabPosition.add(POSITION_HOME_MENU)
		} else {
			fragments.add(Pair(ID_HOME_DASHBOARD, EmptyFragment.newInstance()))
			tabIcon.add(R.drawable.state_ic_tab_home)
			tabTitle.add("Trang chủ")
			tabPosition.add(POSITION_HOME_DASHBOARD)
		}

		fragments.add(Pair(ID_CALENDAR_TAB, EmptyFragment.newInstance()))
		tabIcon.add(R.drawable.state_ic_tab_calendar)
		tabTitle.add("Lịch họp")
		tabPosition.add(POSITION_CALENDAR_TAB)

		fragments.add(Pair(ID_CONTACT_TAB, EmptyFragment.newInstance()))
		tabIcon.add(R.drawable.state_ic_tab_contact)
		tabTitle.add("Danh bạ")
		tabPosition.add(POSITION_CONTACT_TAB)

		fragments.add(Pair(ID_MESSAGE_TAB, EmptyFragment.newInstance()))
		tabIcon.add(R.drawable.state_ic_tab_chat)
		tabTitle.add("Tin nhắn")
		tabPosition.add(POSITION_MESSAGE_TAB)

		fragments.add(Pair(ID_SETTING_TAB, EmptyFragment.newInstance()))
		tabIcon.add(R.drawable.state_ic_tab_setting)
		tabTitle.add("Tiện ích")
		tabPosition.add(POSITION_SETTING_TAB)

		return fragments
	}
}
