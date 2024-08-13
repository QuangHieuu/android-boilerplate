package boilerplate.model.dashboard

import boilerplate.R
import boilerplate.constant.AccountManager
import com.google.gson.annotations.SerializedName

data class Dashboard(
	@SerializedName("ban_lam_viec")
	var desktop: ArrayList<Desktop> = arrayListOf(),
	@SerializedName("can_xu_ly")
	var statical: Statical = Statical()
)

enum class DashboardBlock(val title: String, val index: Int, val icon: Int, val color: String) {
	REFERENCE_HANDLE("Chưa xử lý", 1, R.drawable.ic_dashboard_not_process, "#FBE9D7"),
	REFERENCE_HANDLING("Đang xử lý", 2, R.drawable.id_dashboard_in_process, "#1A1677FF"),
	SIGN_GOING("ký số đi", 3, R.drawable.ic_dashboard_going_digital, "#1A2FAFD0"),
	WORK_NO_ASSIGN("Chưa giao", 4, R.drawable.ic_dashboard_not_process, "#FBE9D7"),
	WORK_NOT_DOING("Chưa thực hiện", 5, R.drawable.ic_dashboard_not_doing, "#1A57C22D"),
	WORK_OVER_TIME("Trễ hạn", 6, R.drawable.ic_dashboard_over_time, "#1ADE3023");

	companion object {
		private val intToTypeMap: MutableMap<Int, DashboardBlock> = HashMap()

		init {
			for (i in entries) {
				intToTypeMap[i.index] = i
			}
		}

		fun fromIndex(code: Int): DashboardBlock {
			val type = intToTypeMap[code]
				?: return REFERENCE_HANDLE
			return type
		}
	}
}

data class Feature(
	var feature: String = "",
	var name: Int = 0,
	var icon: Int = 0,
	var data: ArrayList<Page> = arrayListOf()
) {
	constructor(menu: FeatureMenu) : this() {
		feature = menu.feature
		name = menu.displayName
		icon = menu.icon
	}
}

data class Page(
	var name: String = "",
	var type: Int = 0,
	var value: Int = 0,
	var icon: Int = 0,
	var color: String = "",
	var isShowValue: Boolean = false
) {
	constructor(menu: DashboardBlock) : this() {
		this.isShowValue = true
		this.name = menu.title
		this.type = menu.index
		this.icon = menu.icon
		this.color = menu.color
	}

	constructor(menu: DashboardBlock, showValue: Boolean) : this() {
		this.isShowValue = showValue
		this.name = menu.title
		this.type = menu.index
		this.icon = menu.icon
		this.color = menu.color
	}
}


enum class FeatureMenu(val feature: String, val displayName: Int, val icon: Int) {
	DOCUMENTS("DOCUMENTS", R.string.document, R.drawable.ic_dashboard_type_document),
	WORKS("WORKS", R.string.work, R.drawable.ic_dashboard_type_work),
	SIGNING("SIGNING", R.string.signature, R.drawable.ic_dashboard_type_sign);

	companion object {
		private val intToTypeMap: MutableMap<String, FeatureMenu> = HashMap()

		init {
			for (type in entries) {
				intToTypeMap[type.feature] = type
			}
		}

		fun fromType(code: String): FeatureMenu {
			val type = intToTypeMap[code] ?: return DOCUMENTS
			return type
		}

		fun blockDashboardDocument(): Feature {
			val home = Feature(DOCUMENTS)
			val workManagerList = ArrayList<Page>()
			workManagerList.add(Page(DashboardBlock.REFERENCE_HANDLE))
			workManagerList.add(Page(DashboardBlock.REFERENCE_HANDLING))
			home.data = workManagerList
			return home
		}

		fun blockDashboardSign(): Feature {
			val home = Feature(SIGNING)
			val workManagerList = ArrayList<Page>()
			workManagerList.add(Page(DashboardBlock.SIGN_GOING))
			home.data = workManagerList
			return home
		}

		fun blockDashboardWork(): Feature {
			val home = Feature(WORKS)
			val workManagerList = ArrayList<Page>()
			if (AccountManager.hasDepartmentWorkManager()) {
				workManagerList.add(Page(DashboardBlock.WORK_NO_ASSIGN))
			}
			workManagerList.add(Page(DashboardBlock.WORK_NOT_DOING))
			workManagerList.add(Page(DashboardBlock.WORK_OVER_TIME))
			home.data = workManagerList
			return home
		}
	}
}
