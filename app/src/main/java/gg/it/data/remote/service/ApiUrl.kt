package gg.it.data.remote.service

object ApiUrl {
    private const val FILE_CHAT = "filechat/get/"

    const val API = "api/"

    const val FIREBASE_URL_VERSON = "https://darsitec-eoffice-default-rtdb.firebaseio.com/"
    const val FILE_VERSION_UPDATE = ""

    const val WEBSITE = "https://darsitec.dn.greenglobal.vn"

    var HOST_E_OFFICE = "https://api-darsitec.dn.greenglobal.vn/"
    var HOST_CHAT = "https://api-chat-darsitec.dn.greenglobal.vn/"
    var HOST_FILE = "https://api-file-darsitec.dn.greenglobal.vn/"
    var HOST_NOTIFICATION = "https://api-push-darsitec.dn.greenglobal.vn/"
    var HOST_SIGN_IN = "https://sso-darsitec.dn.greenglobal.vn/"
    var HOST_MEETING_CALENDAR = "https://api-meeting-darsitec.dn.greenglobal.vn/"

    var HOST_IMAGE = "https://api-file-darsitec.dn.greenglobal.vn/"
    var HOST_FILE_PREVIEW = HOST_FILE + API + FILE_CHAT
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