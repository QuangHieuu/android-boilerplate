package boilerplate.ui.setting

import boilerplate.R
import boilerplate.utils.StringUtil.getHotLine

enum class SettingMenu(val index: Int, val title: String, val icon: Int) {
    PROFILE(0, "Xem thông tin cá nhân", R.drawable.ic_setting_personal),
    MANUAL(1, "Hướng dẫn sử dụng", R.drawable.ic_setting_manual_instruction),
    HOTLINE(2, getHotLine(), R.drawable.ic_setting_hotline),
    APP_STORE(3, "Di chuyển qua store", R.drawable.ic_setting_move_to_store),
    VERSION(4, "Thông tin phiên bản", R.drawable.ic_setting_info),
    SETTING(5, "Cài đặt", R.drawable.ic_setting_grey),
    MY_CLOUD(6, "Cloud của tôi", R.drawable.ic_setting_my_cloud),
    REPORT(7, "Hộp thư góp ý", R.drawable.ic_setting_feedback),
    LOG_OUT(8, "Đăng xuất", R.drawable.ic_setting_logout),
    SUPPORT(9, "Hỗ trợ CNTT", R.drawable.ic_setting_support_cntt),
    SETTING_DETAIL(10, "Cài đặt", -1),
    SETTING_OFFICE(11, "eOffice", R.drawable.ic_app_round),
    SETTING_BIRTHDAY(12, "Thông báo sinh nhật", R.drawable.ic_setting_birthday),
    SETTING_TOKEN(13, "Gửi Token", R.drawable.ic_setting_send_token),
    SETTING_SOUND(14, "Âm thanh", -1),
    SETTING_USE_OFFLINE(15, "Sử dụng chat offline", -1),
    SETTING_NOTIFY_OFFICE(16, "Thông báo eOffice", -1),
    SETTING_FONT(17, "Kích thước tin nhắn", -1),
    SETTING_BIRTH_ALL(18, "CBCNV trong toàn EVNCPC", -1),
    SETTING_BIRTH_ONLY(19, "CBCNV trong đơn vị", -1),
    SETTING_BIRTH_SELECTED(20, "CBCNV trong danh sách tùy chọn", -1),
    SETTING_BIRTH_NO(21, "Không nhận thông báo", -1);

    companion object {
        private val intToTypeMap: MutableMap<Int, SettingMenu> = HashMap()

        init {
            for (type in entries) {
                intToTypeMap[type.index] = type
            }
        }

        @JvmStatic
        fun fromType(code: Int): SettingMenu {
            val type = intToTypeMap[code] ?: return LOG_OUT
            return type
        }

        val listSettingMenu: ArrayList<SettingMenu>
            get() {
                val list = ArrayList<SettingMenu>()
                list.add(PROFILE)
                list.add(APP_STORE)
                list.add(VERSION)
                list.add(SETTING)
                list.add(MY_CLOUD)
                list.add(LOG_OUT)
                return list
            }

        @JvmStatic
        fun isMargin(index: Int): Boolean {
            return index == SETTING.index ||
                index == MY_CLOUD.index ||
                index == LOG_OUT.index
        }

        @JvmStatic
        fun getListSettingDetail(type: Int): ArrayList<SettingMenu> {
            val list = ArrayList<SettingMenu>()
            when (fromType(type)) {
                SETTING_DETAIL -> {
                    list.add(SETTING_OFFICE)
                    list.add(SETTING_BIRTHDAY)
                    list.add(SETTING_TOKEN)
                }

                SETTING_OFFICE -> {
                    list.add(SETTING_SOUND)
                    list.add(SETTING_USE_OFFLINE)
                    list.add(SETTING_NOTIFY_OFFICE)
                    list.add(SETTING_FONT)
                }

                SETTING_BIRTHDAY -> {
                    list.add(SETTING_BIRTH_ALL)
                    list.add(SETTING_BIRTH_ONLY)
                    list.add(SETTING_BIRTH_SELECTED)
                    list.add(SETTING_BIRTH_NO)
                }

                else -> {

                }
            }
            return list
        }
    }
}
