package boilerplate.utils.extension

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.viewbinding.ViewBinding
import boilerplate.R
import kotlin.reflect.KClass

fun Fragment.open(
    fragment: Fragment,
    split: Boolean = false,
    addToBackStack: Boolean = true,
    animateType: AnimateType = AnimateType.SLIDE_TO_LEFT,
    @IdRes containerId: Int = R.id.app_container,
    tag: String = fragment::class.java.simpleName,
) {
    val fm = requireActivity().supportFragmentManager
    fm.transact({
        if (addToBackStack) {
            addToBackStack(tag)
        }
        val isTablet = context?.isTablet()
        if (isTablet == true && split) {
            add(R.id.frame_tablet, fragment, tag)
        } else {
            add(containerId, fragment, tag)
        }
    }, animateType = animateType)
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

/**
 * Runs a FragmentTransaction, then calls commitAllowingStateLoss().
 */
inline fun FragmentManager.transact(
    action: FragmentTransaction.() -> Unit,
    animateType: AnimateType = AnimateType.SLIDE_TO_LEFT
) {
    beginTransaction().apply {
        setCustomAnimations(this, animateType)
        action()
    }.commitAllowingStateLoss()
}

fun setCustomAnimations(
    transaction: FragmentTransaction,
    animateType: AnimateType = AnimateType.SLIDE_TO_LEFT
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
                R.anim.enter_from_right, R.anim.stay,
                R.anim.stay, R.anim.exit_to_right
            )
        }

        else -> {

        }
    }
}

fun <VB : ViewBinding> Fragment.showDialog(
    binding: VB,
    viewInit: (v: VB, dialog: AlertDialog) -> Unit
) {
    val dialogBuilder = AlertDialog.Builder(context)
        .apply { setCancelable(false) }
        .also { it.setView(binding.root) }
    val dialog = dialogBuilder.create()
    viewInit(binding, dialog)
    dialog.show()
}