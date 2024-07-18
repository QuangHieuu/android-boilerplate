package boilerplate.model.dashboard

class HomeFeature {
    var feature: String? = null
        get() = field ?: "".also { field = it }

    var name: Int = 0
    var icon: Int = 0
    var data: ArrayList<HomePage> = arrayListOf()

    constructor()

    constructor(menu: HomeFeatureMenu) {
        feature = menu.feature
        name = menu.displayName
        icon = menu.icon
    }

    class HomePage {
        var name: String? = null
            get() = field ?: "".also { field = it }

        var type: Int = 0
            private set
        var value: Int = 0
            private set
        var icon: Int = 0
            private set
        var color: String? = null
            get() = field ?: "".also { field = it }
            private set
        var isShowValue: Boolean = false
            private set

        constructor()

        constructor(menu: EOfficeMenu) {
            this.isShowValue = true
            this.name = menu.title
            this.type = menu.index
            this.icon = menu.icon
            this.color = menu.color
        }

        constructor(menu: EOfficeMenu, showValue: Boolean) {
            this.isShowValue = showValue
            this.name = menu.title
            this.type = menu.index
            this.icon = menu.icon
            this.color = menu.color
        }
    }
}
