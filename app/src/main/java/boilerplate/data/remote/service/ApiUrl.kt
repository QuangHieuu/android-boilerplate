package boilerplate.data.remote.service

import boilerplate.data.remote.api.ApiServer
import boilerplate.data.remote.api.ApiServer.LIVE
import boilerplate.data.remote.api.ApiServer.STAGING
import boilerplate.data.remote.api.ApiServer.TEST1

object ApiUrl {
    const val API: String = "api/"
    const val FILE_CHAT: String = "filechat/get/"

    var DEFAULT: String = LIVE.serverName

    const val FIREBASE_URL_VERSON: String = "https://darsitec-eoffice-default-rtdb.firebaseio.com/"
    const val FILE_VERSION_UPDATE: String = ""

    const val WEBSITE: String = "https://darsitec.dn.greenglobal.vn"

    var HOST_MAIN: String = ""
    var HOST_CHAT: String = ""
    var HOST_FILE: String = ""
    var HOST_NOTIFICATION: String = ""
    var HOST_SIGN_IN: String = ""
    var HOST_MEETING_CALENDAR: String = ""
    var HOST_SURVEY: String = ""
    var HOST_FILE_PREVIEW: String = ""
    var HOST_IMAGE: String = ""

    fun setHost(host: String) {
        when (ApiServer.fromType(host)) {
            TEST1 -> {
                HOST_SIGN_IN = "https://sso-darsitec.dn.greenglobal.vn/"
                HOST_MAIN = "https://api-darsitec.dn.greenglobal.vn/"
                HOST_CHAT = "https://api-chat-darsitec.dn.greenglobal.vn/"
                HOST_FILE = "https://api-file-darsitec.dn.greenglobal.vn/"
                HOST_NOTIFICATION = "https://api-push-darsitec.dn.greenglobal.vn/"
                HOST_MEETING_CALENDAR = "https://api-meeting-darsitec.dn.greenglobal.vn/"
                HOST_SURVEY = "https://api-darsitec.dn.greenglobal.vn/"
                HOST_IMAGE = "https://api-file-darsitec.dn.greenglobal.vn/"
            }

            LIVE -> {
                HOST_SIGN_IN = "https://sso.thongtintinhieudsdn.vn/"
                HOST_MAIN = "https://api-cv.thongtintinhieudsdn.vn/"
                HOST_CHAT = "https://api-chat.thongtintinhieudsdn.vn/"
                HOST_FILE = "https://api-file.thongtintinhieudsdn.vn/"
                HOST_NOTIFICATION = "https://api-push.thongtintinhieudsdn.vn/"
                HOST_MEETING_CALENDAR = "https://api-meeting.thongtintinhieudsdn.vn/"
                HOST_SURVEY = "https://api-survey.thongtintinhieudsdn.vn/"
                HOST_IMAGE = "https://api-file.thongtintinhieudsdn.vn/"
            }

            STAGING -> {

            }
        }
        HOST_FILE_PREVIEW = HOST_FILE + API + FILE_CHAT
    }
}

object ApiDomain {
    const val LOGIN = "login"
    const val E_OFFICE = "E_OFFICE"
    const val CHAT = "CHAT"
    const val NOTIFY = "NOTIFY"
    const val FILE = "FILE"
    const val FIREBASE = "FIREBASE"
    const val CALENDAR = "CALENDAR"
}