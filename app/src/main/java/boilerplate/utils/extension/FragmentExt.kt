package boilerplate.utils.extension

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.KeyEvent
import androidx.annotation.IdRes
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.viewbinding.ViewBinding
import boilerplate.R
import boilerplate.constant.Constants
import kotlin.reflect.KClass

fun Fragment.findOwner(tag: String): Fragment {
    val fragment = requireActivity().supportFragmentManager.findFragmentByTag(tag)
        ?: if (context == null) {
            throw IllegalStateException(
                "Fragment $this is not attached to any Fragment or host"
            )
        } else {
            throw IllegalStateException(
                "Fragment $this is not a child Fragment, it is directly attached to $context"
            )
        }
    return fragment
}

fun Fragment.open(
    fragment: Fragment,
    split: Boolean = false,
    addToBackStack: Boolean = true,
    animateType: AnimateType = AnimateType.SLIDE_TO_LEFT,
    @IdRes containerId: Int = R.id.app_container,
    tag: String = fragment::class.java.simpleName,
) {
    val fm = requireActivity().supportFragmentManager
    fm.transact(
        begin = {
            hide(this@open)
        },
        end = {
            if (addToBackStack) {
                addToBackStack(tag)
            }
            val isTablet = context?.isTablet()
            if (isTablet == true && split) {
                if (fm.isExistFragment(fragment)) {
                    fm.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                }
                add(R.id.frame_tablet, fragment, tag)
            } else {
                add(containerId, fragment, tag)
            }
        },
        animateEnd = animateType
    )
}

fun <T : Activity> Fragment.goTo(
    cls: KClass<T>, bundle: Bundle? = null,
    parcel: Parcelable? = null
) {
    val intent = Intent(context, cls.java)
    if (bundle != null) intent.putExtra(Constants.EXTRA_ARGS, bundle)
    if (parcel != null) intent.putExtra(Constants.EXTRA_ARGS, parcel)
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
    begin: FragmentTransaction.() -> Unit,
    end: FragmentTransaction.() -> Unit,
    animateBegin: AnimateType = AnimateType.SLIDE_TO_LEFT,
    animateEnd: AnimateType = AnimateType.SLIDE_TO_LEFT,
) {
    beginTransaction().apply {
        setCustomAnimations(this, animateBegin)
        begin()
        setCustomAnimations(this, animateEnd)
        end()
    }.commitAllowingStateLoss()
}

fun setCustomAnimations(
    transaction: FragmentTransaction,
    animateType: AnimateType = AnimateType.SLIDE_TO_LEFT
) {
    when (animateType) {
        AnimateType.FADE -> {
            transaction.setCustomAnimations(
                R.anim.fade_in, R.anim.fade_out,
                R.anim.fade_out, R.anim.fade_in
            )
        }

        AnimateType.SLIDE_TO_RIGHT -> {
            transaction.setCustomAnimations(
                R.anim.enter_from_left, R.anim.fade_out,
                R.anim.fade_in, R.anim.exit_to_left
            )
        }

        AnimateType.SLIDE_TO_LEFT -> {
            transaction.setCustomAnimations(
                R.anim.enter_from_right, R.anim.fade_out,
                R.anim.stay, R.anim.exit_to_right
            )
        }

        else -> {

        }
    }
}

fun <VB : ViewBinding> Fragment.showDialog(
    binding: VB,
    viewInit: (vb: VB, dialog: AlertDialog) -> Unit
) {
    val drawable = ResourcesCompat.getDrawable(resources, R.drawable.bg_base_dialog, null)
    val dialogBuilder = AlertDialog.Builder(context)
        .apply { setCancelable(false) }
        .also { it.setView(binding.root) }
    val dialog = dialogBuilder.create().apply {
        window?.setBackgroundDrawable(drawable)
        setOnKeyListener { v: DialogInterface?, keyCode: Int, event: KeyEvent ->
            if ((keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP)) {
                dismiss()
                return@setOnKeyListener true
            } else {
                return@setOnKeyListener false
            }
        }
    }
    viewInit(binding, dialog)
    dialog.show()
}