package boilerplate.constant

import boilerplate.data.local.repository.user.TokenRepository
import boilerplate.data.local.repository.user.UserRepository
import boilerplate.model.user.Company
import boilerplate.model.user.User
import org.koin.java.KoinJavaComponent.inject

object AccountManager {
    const val CONGVAN = "CONGVAN"
    const val CONGVANDEN = "CONGVANDEN"
    const val CONGVANDI = "CONGVANDI"
    const val CONGVIEC_PHONGBAN = "CONGVIEC_PHONGBAN"
    const val CONGVIEC_CANHAN = "CONGVIEC_CANHAN"
    const val SUDUNG_QLCV = "SUDUNG_QLCV"
    const val KYSO_QUANLY = "KYSO_QUANLY"
    const val KY_SO_TAP_TRUNG = "KY_SO_TAP_TRUNG"

    private val userImpl by inject<UserRepository>(UserRepository::class.java)
    private val tokenImpl by inject<TokenRepository>(TokenRepository::class.java)

    @JvmStatic
    fun getMainCompany(): Company {
        return userImpl.getUser().mainCompany ?: Company()
    }

    @JvmStatic
    fun getCurrentUserId(): String? {
        return userImpl.getUser().id
    }

    fun getCurrentNhanVien(): User = userImpl.getUser()

    fun getToken(): String = tokenImpl.getToken()

    fun isPlaySound(): Boolean {
        return userImpl.getSystemSound()
    }

    fun hasIncomeDocument(): Boolean {
        return userImpl.getRolePermission().contains(CONGVANDEN)
    }

    fun hasDepartmentWorkManager(): Boolean {
        return userImpl.getRolePermission().contains(CONGVIEC_PHONGBAN)
    }

    fun hasPersonalWorkManager(): Boolean {
        return userImpl.getRolePermission().contains(CONGVIEC_CANHAN)
    }

    fun hasUseWorkManager(): Boolean {
        return userImpl.getRolePermission().contains(SUDUNG_QLCV)
    }

    fun hasDigitalSignManage(): Boolean {
        return userImpl.getRolePermission().contains(KYSO_QUANLY)
    }

    fun hasDigitalConcentrateSign(): Boolean {
        return userImpl.getRolePermission().contains(KY_SO_TAP_TRUNG)
    }
}