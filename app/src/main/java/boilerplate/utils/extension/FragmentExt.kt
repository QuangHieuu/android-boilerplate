package boilerplate.utils.extension

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.viewbinding.ViewBinding
import boilerplate.R
import boilerplate.constant.Constants
import kotlin.reflect.KClass

fun Fragment.findOwner(): Fragment {
	val fragment =
		requireActivity().supportFragmentManager.findFragmentByTag(this.javaClass.simpleName)
			?: parentFragmentManager.findFragmentByTag(this.javaClass.simpleName)
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
	asDialog: Boolean = isTablet(),
	split: Boolean = isTablet(),
	animateType: AnimateType = AnimateType.SLIDE_TO_LEFT,
	sharedElement: View
) {
	val containerId = requireActivity().window.findViewById<ViewGroup>(android.R.id.content).id
	val fm = requireActivity().supportFragmentManager
	val tag: String = this::class.java.simpleName

	if (asDialog && fragment is DialogFragment) {
		if (fm.isExistFragment(fragment)) {
			fm.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
		}
		fragment.show(fm, tag)
		return
	}
	if (fragment is DialogFragment) {
		fragment.showsDialog = false
	}
	fm.transact({
		addSharedElement(sharedElement, sharedElement.transitionName)
		setReorderingAllowed(true)
		addToBackStack(tag)
		if (isTablet() && split) {
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
	childFragmentManager.notNull { fm ->
		for (i in 1 until fm.backStackEntryCount) {
			fm.popBackStack()
		}
	}
}

fun Fragment.popFragment() {
	childFragmentManager.popBackStack()
}

inline fun FragmentManager.transact(
	action: FragmentTransaction.() -> Unit,
	animate: AnimateType = AnimateType.SLIDE_TO_LEFT,
) {
	beginTransaction().apply {
//		setCustomAnimations(this, animate)
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
