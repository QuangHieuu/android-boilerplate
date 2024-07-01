package boilerplate.utils.extension

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import boilerplate.R
import kotlin.reflect.KClass


fun Fragment.replaceFragment(
    @IdRes containerId: Int, fragment: Fragment,
    addToBackStack: Boolean = true, tag: String = fragment::class.java.simpleName,
    animateType: AnimateType = AnimateType.FADE
) {
    childFragmentManager.transact({
        if (addToBackStack) {
            addToBackStack(tag)
        }
        replace(containerId, fragment, tag)
    }, animateType = animateType)
}

fun Fragment.addFragment(
    @IdRes containerId: Int,
    fragment: Fragment,
    addToBackStack: Boolean = true,
    tag: String = fragment::class.java.simpleName,
    animateType: AnimateType = AnimateType.FADE
) {
    childFragmentManager.transact({
        if (addToBackStack) {
            addToBackStack(tag)
        }
        add(containerId, fragment, tag)
    }, animateType = animateType)
}

fun Fragment.clearAllFragment() {
    parentFragmentManager.notNull { fm ->
        for (i in 1 until fm.backStackEntryCount) {
            fm.popBackStack()
        }
    }
}

fun Fragment.isCanPopBackStack(): Boolean {
    parentFragmentManager.notNull {
        val isShowPreviousPage = it.backStackEntryCount > 0
        if (isShowPreviousPage) {
            it.popBackStackImmediate()
        }
        return isShowPreviousPage
    }
    return false
}

fun isExistFragment(fragmentManager: FragmentManager, tag: String): Boolean {
    val fragment = fragmentManager.findFragmentByTag(tag)
    return fragment != null
}


/**
 * Runs a FragmentTransaction, then calls commitAllowingStateLoss().
 */
inline fun FragmentManager.transact(
    action: FragmentTransaction.() -> Unit,
    animateType: AnimateType = AnimateType.FADE
) {
    beginTransaction().apply {
        setCustomAnimations(this, animateType)
        action()
    }.commitAllowingStateLoss()
}

fun setCustomAnimations(
    transaction: FragmentTransaction,
    animateType: AnimateType = AnimateType.FADE
) {
    when (animateType) {
        AnimateType.FADE -> {
            transaction.setCustomAnimations(
                R.anim.fade_in, R.anim.stay,
                R.anim.stay, R.anim.fade_out
            )
        }

        AnimateType.SLIDE_TO_RIGHT -> {
            transaction.setCustomAnimations(
                R.anim.enter_from_left, R.anim.stay,
                R.anim.stay, R.anim.exit_to_left
            )
        }

        AnimateType.BOTTOM_UP -> {
            transaction.setCustomAnimations(
                R.anim.fade_in, R.anim.stay,
                R.anim.stay, R.anim.fade_out
            )
        }

        AnimateType.SLIDE_TO_LEFT -> {
            transaction.setCustomAnimations(
                R.anim.enter_from_right, R.anim.exit_to_left,
                R.anim.enter_from_left, R.anim.exit_to_right
            )
        }

        else -> {

        }
    }
}

fun <T : Activity> Fragment.goTo(
    cls: KClass<T>, bundle: Bundle? = null,
    parcel: Parcelable? = null
) {
    val intent = Intent(context, cls.java)
    if (bundle != null) intent.putExtra(boilerplate.constant.Constants.EXTRA_ARGS, bundle)
    if (parcel != null) intent.putExtra(boilerplate.constant.Constants.EXTRA_ARGS, parcel)
    startActivity(intent)
}