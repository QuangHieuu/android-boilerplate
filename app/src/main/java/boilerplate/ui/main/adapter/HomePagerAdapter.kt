package boilerplate.ui.main.adapter

import android.content.Context
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import boilerplate.R
import boilerplate.databinding.ViewItemTabBinding
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.show
import boilerplate.widget.customText.AppTextView
import com.google.android.material.tabs.TabLayout

class HomePagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
	FragmentStateAdapter(fragmentManager, lifecycle) {
	private val mFragments = ArrayList<Pair<Int, Fragment>>()

	override fun createFragment(position: Int): Fragment {
		return mFragments[position].second
	}

	override fun getItemId(position: Int): Long {
		return mFragments[position].first.toLong()
	}

	override fun getItemCount(): Int {
		return mFragments.size
	}

	fun addFragment(list: ArrayList<Pair<Int, Fragment>>) {
		if (mFragments.size != 0) {
			val size = mFragments.size
			mFragments.clear()
			notifyItemRangeRemoved(0, size)
		}
		mFragments.addAll(list)
		notifyItemRangeInserted(0, list.size)
	}

	fun getTabIndex(pos: Int): Int {
		return mFragments[pos].first
	}

	fun getCurrentTab(tabIndex: Int): Int {
		val index = -1
		for (fragment in mFragments) {
			if (fragment.first == tabIndex) {
				return mFragments.indexOf(fragment)
			}
		}
		return index
	}

	fun getTabView(
		context: Context,
		root: ViewGroup,
		title: String,
		drawable: Int
	): View {
		val view: View =
			LayoutInflater.from(context).inflate(R.layout.view_item_tab, root, false)
		val icon = view.findViewById<ImageView>(R.id.img_icon)
		val appTextView: AppTextView = view.findViewById(R.id.tv_title)
		appTextView.text = title
		icon.setImageResource(drawable)
		return view
	}
}

fun TabLayout.Tab.customTab(
	title: String,
	@DrawableRes drawable: Int = -1,
	count: Int = 0,
	@ColorRes color: Int = -1
): View {
	val layoutInflater = LayoutInflater.from(view.context)
	val binding = ViewItemTabBinding.inflate(layoutInflater)
	with(binding) {
		if (drawable != -1) {
			imgIcon.show()
			imgIcon.setImageResource(drawable)
		} else {
			imgIcon.gone()
		}

		if (color != -1) {
			tvTitle.setTextColor(ContextCompat.getColor(binding.root.context, color))
		}
		tvTitle.text = title

		if (count > 0) {
			tvCount.show()
			tvCount.text = String.format(count.toString())
		}
	}
	return binding.root
}