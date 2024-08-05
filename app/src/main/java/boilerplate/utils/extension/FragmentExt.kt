package boilerplate.utils.extension

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.KeyEvent
import androidx.annotation.IdRes
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.viewbinding.ViewBinding
import boilerplate.R
import boilerplate.constant.Constants
import kotlin.reflect.KClass

fun <T : Fragment> Fragment.findOwner(tag: KClass<T>): Fragment {
    val fragment =
        requireActivity().supportFragmentManager.findFragmentByTag(tag.java.simpleName)
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

fun Fragment.openDialog(
    fragment: DialogFragment,
    asDialog: Boolean = context?.isTablet() ?: false,
    addToBackStack: Boolean = true,
    animateType: AnimateType = AnimateType.SLIDE_TO_LEFT,
    @IdRes containerId: Int = R.id.app_container,
    tag: String = fragment::class.java.simpleName,
) {
    if (asDialog) {
        val fm = requireActivity().supportFragmentManager
        if (fm.isExistFragment(fragment)) {
            fm.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        fragment.show(fm, tag)
    } else {
        fragment.showsDialog = false
        open(fragment, false, addToBackStack, animateType, containerId, tag)
    }
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
    fm.transact({
        hide(this@open)
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
    }, animateType)
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
    action: FragmentTransaction.() -> Unit,
    animate: AnimateType = AnimateType.SLIDE_TO_LEFT,
) {
    beginTransaction().apply {
        setCustomAnimations(this, animate)
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
                R.anim.fade_in, R.anim.fade_out,
                R.anim.fade_out, R.anim.fade_in
            )
        }

        AnimateType.SLIDE_TO_RIGHT -> {
            transaction.setCustomAnimations(
                R.anim.enter_from_left, R.anim.fade_out,
                R.anim.stay, R.anim.exit_to_left
            )
        }

        AnimateType.SLIDE_TO_LEFT -> {
            transaction.setCustomAnimations(
                R.anim.enter_from_right, R.anim.exit_to_left,
                R.anim.enter_from_left, R.anim.exit_to_right
            )
        }

        else -> {
            transaction.setCustomAnimations(
                R.anim.stay, R.anim.stay,
                R.anim.stay, R.anim.stay
            )
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

fun Fragment.callPhone(phone: String?) {
    try {
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
    } catch (_: NullPointerException) {
        view?.showSnackBarFail(R.string.error_no_phone_number)
    }
}

fun Fragment.sendSms(phone: String?) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phone")).apply {
            putExtra("sms_body", "")
        })
    } catch (_: NullPointerException) {
        view?.showSnackBarFail(R.string.error_no_phone_number)
    }
}

fun Fragment.sendEmail(email: String?) {
    if (!email.isNullOrEmpty()) {
        Intent(Intent.ACTION_SEND)
            .apply { setType("text/html") }
            .let { startActivity(Intent.createChooser(it, "Send Email")) }
    } else {
        view?.showSnackBarFail(R.string.error_no_phone_number)
    }
}