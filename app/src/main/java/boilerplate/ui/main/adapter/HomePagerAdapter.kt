package boilerplate.ui.main.adapter

import android.content.Context
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import boilerplate.R
import boilerplate.ui.main.tab.HomeTabIndex
import boilerplate.widget.customText.TextViewFont

/**
 * Created by dungvhp on 4/26/17.
 */
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

    fun addOnlyForHomeFragment(list: ArrayList<Pair<Int, Fragment>>) {
        if (mFragments.size == 0) {
            mFragments.addAll(list)
            notifyItemRangeInserted(0, list.size)
        } else {
            if (mFragments.size != list.size) {
                if (mFragments.size < list.size) {
                    mFragments.add(
                        HomeTabIndex.workPosition,
                        list[HomeTabIndex.workPosition]
                    )
                    notifyItemInserted(HomeTabIndex.workPosition)
                } else {
                    mFragments.removeAt(HomeTabIndex.workPosition)
                    notifyItemRemoved(HomeTabIndex.workPosition)
                }
            }
            val homeIndex: Int = HomeTabIndex.homeDashboardPosition
            if (mFragments[homeIndex].first != list[homeIndex].first) {
                mFragments[homeIndex] = list[homeIndex]
                notifyItemChanged(homeIndex)
            }
            notifyItemRangeChanged(HomeTabIndex.contactPosition, mFragments.size - 1)
        }
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

    companion object {
        fun getTabView(
            context: Context,
            root: ViewGroup,
            title: String,
            drawable: Int
        ): View {
            val view: View = LayoutInflater.from(context)
                .inflate(R.layout.view_item_tab, root, false)
            val icon = view.findViewById<ImageView>(R.id.img_icon)
            val textViewFont: TextViewFont = view.findViewById(R.id.tv_title)
            textViewFont.text = title
            icon.setImageResource(drawable)
            return view
        }
    }
}
