package boilerplate.constant

import boilerplate.data.local.repository.user.TokenRepository
import boilerplate.data.local.repository.user.UserRepository
import boilerplate.model.user.Company
import boilerplate.model.user.User
import org.koin.java.KoinJavaComponent.inject

object AccountManager {
    const val CONGVAN = "CONGVAN"
    const val CONGVANDEN = "CONGVANDEN"
    const val CONGVANDEN_CHIDAO = "CONGVANDEN_CHIDAO"

    const val CONGVANDI = "CONGVANDI"
    const val CONGVANDI_QUANLY = "CONGVANDI"

    const val KYSO_QUANLY = "KYSO_QUANLY"

    const val KY_SO_TAP_TRUNG = "KY_SO_TAP_TRUNG"

    const val DEXUAT_THUHOI = "DEXUAT_THUHOI"
    const val DONGY_THUHOI = "DONGY_THUHOI"

    const val SUDUNG_QLCV = "SUDUNG_QLCV"

    const val CONGVIEC_PHONGBAN = "CONGVIEC_PHONGBAN"
    const val CONGVIEC_GIAOVIEC = "CONGVIEC_GIAOVIEC"
    const val CONGVIEC_CANHAN = "CONGVIEC_CANHAN"

    private val userImpl by inject<UserRepository>(UserRepository::class.java)
    private val tokenImpl by inject<TokenRepository>(TokenRepository::class.java)

    @JvmStatic
    fun getMainCompany(): Company {
        return userImpl.getUser().mainCompany
    }

    @JvmStatic
    fun getCurrentUserId(): String {
        return userImpl.getUser().id
    }

    fun getCurrentNhanVien(): User = userImpl.getUser()

    fun getToken(): String = tokenImpl.getToken()

    fun isPlaySound(): Boolean {
        return userImpl.getSystemSound()
    }

    fun hasGoingDocument(): Boolean {
        return userImpl.getRolePermission().contains(CONGVANDI)
    }

    fun hasGoingManagerDocument(): Boolean {
        return userImpl.getRolePermission().contains(CONGVANDI_QUANLY)
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

    fun hasWorkAssign(): Boolean {
        return userImpl.getRolePermission().contains(CONGVIEC_GIAOVIEC)
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

    fun hasGoingSuggestRevoke(): Boolean {
        return userImpl.getRolePermission().contains(DEXUAT_THUHOI)
    }

    fun hasGoingAcceptRevoke(): Boolean {
        return userImpl.getRolePermission().contains(DONGY_THUHOI)
    }

    fun hasIncomeCommandDocument(): Boolean {
        return userImpl.getRolePermission().contains(CONGVANDEN_CHIDAO)
    }
}