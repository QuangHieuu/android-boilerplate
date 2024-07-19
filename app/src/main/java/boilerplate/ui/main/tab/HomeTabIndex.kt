package boilerplate.ui.main.tab

import android.util.Pair
import androidx.fragment.app.Fragment
import boilerplate.R
import boilerplate.ui.contact.ContactFragment
import boilerplate.ui.conversation.ConversationFragment
import boilerplate.ui.dashboard.DashboardFragment
import boilerplate.ui.empty.EmptyFragment
import boilerplate.ui.setting.SettingFragment

object HomeTabIndex {

    var POSITION_HOME_DASHBOARD: String = "POSITION_HOME_DASHBOARD"
    var POSITION_HOME_MENU: String = "POSITION_HOME_MENU"
    var POSITION_WORK_MANAGER_TAB: String = "POSITION_WORK_MANAGER_TAB"
    var POSITION_CALENDAR_TAB: String = "POSITION_CALENDAR_TAB"
    var POSITION_CONTACT_TAB: String = "POSITION_CONTACT_TAB"
    var POSITION_MESSAGE_TAB: String = "POSITION_MESSAGE_TAB"
    var POSITION_SETTING_TAB: String = "POSITION_SETTING_TAB"

    private var ID_HOME_DASHBOARD = 0
    private const val ID_HOME_MENU = 0
    private const val ID_WORK_MANAGER_TAB = 1
    private const val ID_CALENDAR_TAB = 1
    private const val ID_CONTACT_TAB = 2
    private const val ID_MESSAGE_TAB = 3
    private const val ID_SETTING_TAB = 4

    private val tabPosition = ArrayList<String>()

    private var lastStatus = false

    fun titleTab(): Array<String> {
        return arrayOf(
            "Trang chủ",
            "Lịch họp",
            "Danh bạ",
            "Tin nhắn",
            "Tiện ích"
        )
    }

    fun iconTab(): IntArray {
        return intArrayOf(
            R.drawable.state_ic_tab_home,
            R.drawable.state_ic_tab_calendar,
            R.drawable.state_ic_tab_contact,
            R.drawable.state_ic_tab_chat,
            R.drawable.state_ic_tab_setting
        )
    }

    private fun positionTab() {
        tabPosition.clear()
        tabPosition.add(POSITION_HOME_DASHBOARD)
        tabPosition.add(POSITION_CALENDAR_TAB)
        tabPosition.add(POSITION_CONTACT_TAB)
        tabPosition.add(POSITION_MESSAGE_TAB)
        tabPosition.add(POSITION_SETTING_TAB)
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

    fun setupFragment(): ArrayList<Pair<Int, Fragment>> {
        positionTab()
        val fragments = ArrayList<Pair<Int, Fragment>>()

        fragments.add(Pair(ID_HOME_DASHBOARD, DashboardFragment.newInstance()))
        fragments.add(Pair(ID_CALENDAR_TAB, EmptyFragment.newInstance()))
        fragments.add(Pair(ID_CONTACT_TAB, ContactFragment.newInstance()))
        fragments.add(Pair(ID_MESSAGE_TAB, ConversationFragment.newInstance()))
        fragments.add(Pair(ID_SETTING_TAB, SettingFragment.newInstance()))
        return fragments
    }
}
