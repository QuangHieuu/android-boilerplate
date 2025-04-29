package boilerplate.utils.extension

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import boilerplate.R
import boilerplate.constant.Constants.EXTRA_ARGS
import boilerplate.databinding.ViewToastBinding
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_SLIDE
import com.google.android.material.snackbar.Snackbar
import kotlin.reflect.KClass

/**
 * Various extension functions for AppCompatActivity.
 */

enum class AnimateType {
	FADE,
	SLIDE_TO_RIGHT,
	SLIDE_TO_LEFT,
	STAY
}

fun <T : Activity> AppCompatActivity.goTo(
	cls: KClass<T>,
	bundle: Bundle? = null,
	parcel: Parcelable? = null,
	isFinish: Boolean = false
) {
	intent = Intent(this, cls.java)
	if (bundle != null) intent.putExtra(EXTRA_ARGS, bundle)
	if (parcel != null) intent.putExtra(EXTRA_ARGS, parcel)
	startActivity(
		intent,
		ActivityOptionsCompat
			.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out)
			.toBundle()
	)
	if (isFinish) {
		finishAfterTransition()
	}
}

fun AppCompatActivity.replaceFragmentInActivity(
	@IdRes containerId: Int,
	fragment: Fragment,
	addToBackStack: Boolean = true,
	tag: String = fragment::class.java.simpleName,
	animateType: AnimateType = AnimateType.SLIDE_TO_LEFT
) {
	supportFragmentManager.transact({
		if (addToBackStack) {
			addToBackStack(tag)
		}
		replace(containerId, fragment, tag)
	}, animateType)
}

fun AppCompatActivity.clearAllFragment() {
	supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
}

fun AppCompatActivity.startActivity(
	intent: Intent,
	flags: Int? = null
) {
	flags.notNull {
		intent.flags = it
	}
	startActivity(intent)
}

fun FragmentManager.isExistFragment(fragment: Fragment): Boolean {
	return findFragmentByTag(fragment::class.java.simpleName) != null
}

fun AppCompatActivity.switchFragment(
	@IdRes containerId: Int,
	currentFragment: Fragment,
	newFragment: Fragment,
	addToBackStack: Boolean = true,
	tag: String = newFragment::class.java.simpleName,
	animateType: AnimateType = AnimateType.SLIDE_TO_LEFT
) {
	val fm = supportFragmentManager
	fm.transact({
		if (fm.isExistFragment(newFragment)) {
			hide(currentFragment).show(newFragment)
		} else {
			hide(currentFragment)
			if (addToBackStack) {
				addToBackStack(tag)
			}
			add(containerId, newFragment, tag)
		}
	}, animateType)
}

fun AppCompatActivity.startActivityAtRoot(
	clazz: Class<out Activity>, args: Bundle? = null
) {
	val intent = Intent(this, clazz)
	args.notNull {
		intent.putExtras(it)
	}
	intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
	startActivity(intent)
}

fun AppCompatActivity.popFragment() {
	supportFragmentManager.apply {
		if (backStackEntryCount <= 0) popBackStack()
	}
}

fun AppCompatActivity.findFragmentByTag(tag: String): Fragment? {
	return supportFragmentManager.findFragmentByTag(tag)
}


@SuppressLint("RestrictedApi")
fun Activity.showSnackBar(
	rootView: View = findViewById(android.R.id.content),
	@StringRes message: Int = R.string.no_text,
	@ColorRes color: Int = R.color.color_toast,
) = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).apply {
	animationMode = ANIMATION_MODE_SLIDE
	view.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
	val binding =
		ViewToastBinding.inflate(LayoutInflater.from(context), rootView as ViewGroup, false)
	with(binding) {
		lnToast.setBackgroundColor(ContextCompat.getColor(context, color))
		tvToast.setText(message)
	}
	val layout = view as Snackbar.SnackbarLayout
	layout.addView(binding.root)
}.show()

@SuppressLint("RestrictedApi")
fun Activity.showSnackBar(
	rootView: View = findViewById(android.R.id.content),
	message: String? = getString(R.string.no_text),
	@ColorRes color: Int = R.color.color_toast,
) = Snackbar.make(rootView, message ?: "", Snackbar.LENGTH_SHORT).apply {
	animationMode = ANIMATION_MODE_SLIDE
	view.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
	val binding =
		ViewToastBinding.inflate(LayoutInflater.from(context), rootView as ViewGroup, false)
	with(binding) {
		lnToast.setBackgroundColor(ContextCompat.getColor(context, color))
		tvToast.text = message
	}
	val layout = view as Snackbar.SnackbarLayout
	layout.addView(binding.root)
}.show()

fun Activity.showSuccess(
	message: String? = ""
) = showSnackBar(
	message = message,
	color = R.color.color_toast_success
)

fun Activity.showSuccess(
	@StringRes message: Int = R.string.no_text
) = showSnackBar(
	message = message,
	color = R.color.color_toast_success
)

fun Activity.showFail(
	message: String? = ""
) = showSnackBar(
	message = message,
	color = R.color.color_toast_fail
)

fun Activity.showFail(
	@StringRes message: Int = R.string.no_text
) = showSnackBar(
	message = message,
	color = R.color.color_toast_fail
)

fun Activity.showWarning(
	message: String? = ""
) = showSnackBar(
	message = message,
	color = R.color.color_toast_warning
)

fun Activity.showWarning(
	@StringRes message: Int = R.string.no_text
) = showSnackBar(
	message = message,
	color = R.color.color_toast_warning
)