package boilerplate.utils

import android.content.Context
import android.content.res.Resources
import boilerplate.R
import boilerplate.data.local.repository.user.UserRepository
import org.koin.java.KoinJavaComponent.inject

object SystemUtil {
    private val userImpl by inject<UserRepository>(UserRepository::class.java)

    const val SYSTEM_DELETE = "_system_deleted"

    const val SIZE_SMALL: Int = 0 // size = 14;
    const val SIZE_MEDIUM: Int = 1 // size = 17;
    const val SIZE_LARGE: Int = 2 // size = 21;

    @JvmStatic
    fun getFontSizeChat(context: Context): Float {
        val mainSize: Float
        val size: Int = userImpl.getSystemTextSize()
        val metrics = Resources.getSystem().displayMetrics
        val resources = context.resources
        mainSize = when (size) {
            SIZE_SMALL -> resources.getDimension(R.dimen.dp_14) / metrics.density
            SIZE_MEDIUM -> resources.getDimension(R.dimen.dp_17) / metrics.density
            SIZE_LARGE -> resources.getDimension(R.dimen.dp_21) / metrics.density
            else -> resources.getDimension(R.dimen.dp_14) / metrics.density
        }
        return mainSize
    }

    @JvmStatic
    fun setAppPlaySound(isPlay: Boolean) {
        userImpl.saveSystemSound(isPlay)
    }

    @JvmStatic
    fun getAppPlaySound(): Boolean {
        return userImpl.getSystemSound()
    }
}