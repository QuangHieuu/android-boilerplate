package boilerplate.model.dashboard

import boilerplate.R
import boilerplate.constant.AccountManager
import boilerplate.model.dashboard.HomeFeature.HomePage

enum class HomeFeatureMenu(val feature: String, val displayName: Int, val icon: Int) {
    DOCUMENTS("DOCUMENTS", R.string.document, R.drawable.ic_menu_type_document),
    WORKS("WORKS", R.string.work, R.drawable.ic_menu_type_work),
    WORKS_MANAGE("WORKS_MANAGE", R.string.work_manager, -1),
    SIGNING("SIGNING", R.string.signature, R.drawable.ic_menu_type_sign),
    FEEDBACK("FEEDBACK", R.string.request_comment_need_handle, -1);

    companion object {
        private val intToTypeMap: MutableMap<String, HomeFeatureMenu> = HashMap()

        init {
            for (type in entries) {
                intToTypeMap[type.feature] = type
            }
        }

        fun fromType(code: String): HomeFeatureMenu {
            val type = intToTypeMap[code] ?: return DOCUMENTS
            return type
        }

        fun blockDocument(): HomeFeature {
            val document = HomeFeature()
            document.feature = DOCUMENTS.feature
            val documentList = ArrayList<HomePage>()
            if (AccountManager.hasIncomeDocument()) {
                documentList.add(HomePage(EOfficeMenu.REFERENCE_NOT_HANDLED))
            }
            document.data = documentList
            return document
        }

        fun blockWork(): HomeFeature {
            val work = HomeFeature()
            work.feature = WORKS.feature
            val workList = ArrayList<HomePage>()
            if (AccountManager.hasDepartmentWorkManager()) {
                workList.add(HomePage(EOfficeMenu.WORK_NOT_ASSIGNED));
            }
            if (AccountManager.hasPersonalWorkManager()) {
                workList.add(HomePage(EOfficeMenu.WORK_NEW));
            }
            work.data = workList
            return work
        }

        fun blockWorkManager(): HomeFeature {
            val workManager = HomeFeature()
            workManager.feature = WORKS_MANAGE.feature
            val workManagerList = ArrayList<HomePage>()
            if (AccountManager.hasUseWorkManager()) {
                workManagerList.add(HomePage(EOfficeMenu.WORK_MANAGE_NEW));
                workManagerList.add(HomePage(EOfficeMenu.WORK_MANAGE_DOING));
            }
            workManager.data = workManagerList
            return workManager
        }

        fun blockSign(): HomeFeature {
            val sign = HomeFeature()
            sign.feature = SIGNING.feature
            val signList = ArrayList<HomePage>()
            if (AccountManager.hasDigitalSignManage()) {
                signList.add(HomePage(EOfficeMenu.REFERENCE_SIGNING));
//            signList.add(new HomeFeature.HomePage(EOfficeMenu.INTERNAL_DOCUMENT_SIGNING));
//            signList.add(new HomeFeature.HomePage(EOfficeMenu.EXPAND_SIGNING));
            }
            if (AccountManager.hasDigitalConcentrateSign()) {
                signList.add(HomePage(EOfficeMenu.CONCENTRATE_SIGNING));
            }
            sign.data = signList
            return sign
        }

        fun blockFeedback(): HomeFeature {
            val feedback = HomeFeature()
            feedback.feature = FEEDBACK.feature
            val feedList = ArrayList<HomePage>()
            //        if (RoleManager.hasViewPhieuGui()) {
//            feedList.add(new HomeFeature.HomePage(EOfficeMenu.SENT_FEEDBACK));
//        }
//        if (RoleManager.hasViewPhieuNhan()) {
//            feedList.add(new HomeFeature.HomePage(EOfficeMenu.RECEIVE_FEEDBACK));
//        }
            feedback.data = feedList
            return feedback
        }

        fun listOfDashboard(): ArrayList<HomeFeature> {
            val list = ArrayList<HomeFeature>()
            val document = blockDocument()
            if (document.data.isNotEmpty()) {
                list.add(document)
            }

            val work = blockWork()
            if (work.data.isNotEmpty()) {
                list.add(work)
            }

            val workManager = blockWorkManager()
            if (workManager.data.isNotEmpty()) {
                list.add(workManager)
            }

            val sign = blockSign()
            if (sign.data.isNotEmpty()) {
                list.add(sign)
            }

            val feedback = blockFeedback()
            if (feedback.data.isNotEmpty()) {
                list.add(feedback)
            }

            return list
        }

        @JvmStatic
        fun blockDashboardDocument(): HomeFeature {
            val home = HomeFeature(DOCUMENTS)
            val workManagerList = ArrayList<HomePage>()
            workManagerList.add(HomePage(EOfficeMenu.REFERENCE_HANDLE))
            workManagerList.add(HomePage(EOfficeMenu.REFERENCE_HANDLING))
            home.data = workManagerList
            return home
        }

        @JvmStatic
        fun blockDashboardSign(): HomeFeature {
            val home = HomeFeature(SIGNING)
            val workManagerList = ArrayList<HomePage>()
            workManagerList.add(HomePage(EOfficeMenu.SIGN_GOING))
            home.data = workManagerList
            return home
        }

        @JvmStatic
        fun blockDashboardWork(): HomeFeature {
            val home = HomeFeature(WORKS)
            val workManagerList = ArrayList<HomePage>()
            //        if (RoleManager.hasWorkDepartmentRole()) {
//            workManagerList.add(new HomeFeature.HomePage(EOfficeMenu.WORK_NO_ASSIGN));
//        }
            workManagerList.add(HomePage(EOfficeMenu.WORK_NEED_DONE))
            workManagerList.add(HomePage(EOfficeMenu.WORK_OVER_TIME))
            home.data = workManagerList
            return home
        }
    }
}
