package boilerplate.utils.extension

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import boilerplate.constant.Constants.EXTRA_ARGS
import kotlin.reflect.KClass

/**
 * Various extension functions for AppCompatActivity.
 */

enum class AnimateType {
    FADE,
    SLIDE_TO_RIGHT,
    SLIDE_TO_LEFT,
    BOTTOM_UP,
    NO_ANIMATION
}

const val ANIMATION_DELAY: Long = 200

fun <T : Activity> AppCompatActivity.goTo(
    cls: KClass<T>, bundle: Bundle? = null,
    parcel: Parcelable? = null
) {
    intent = Intent(this, cls.java)
    if (bundle != null) intent.putExtra(EXTRA_ARGS, bundle)
    if (parcel != null) intent.putExtra(EXTRA_ARGS, parcel)
    startActivity(intent)
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
    }, animateType = animateType)
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
    }, animateType = animateType)
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