package boilerplate.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import boilerplate.databinding.ViewItemTabBinding
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.show
import boilerplate.utils.extension.viewBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

open class PageData(
	val index: Int,
	val fragment: BaseFragment<*, *>
)

open class BasePagerAdapter(
	fm: FragmentManager,
	lifecycle: Lifecycle
) : FragmentStateAdapter(fm, lifecycle) {

	private var _pageData: ArrayList<PageData> = arrayListOf()

	override fun createFragment(position: Int): Fragment {
		return _pageData[position].fragment
	}

	override fun getItemId(position: Int): Long {
		return _pageData[position].index.toLong()
	}

	override fun containsItem(itemId: Long): Boolean {
		return _pageData.find { it.index.toLong() == itemId } != null
	}

	override fun getItemCount(): Int {
		return _pageData.size
	}

	open fun getTabIndex(pos: Int): Int {
		return _pageData[pos].index
	}

	open fun getCurrentTab(tabIndex: Int): Int {
		val tab = _pageData.find { item -> item.index == tabIndex }
		return tab?.let { _pageData.indexOf(it) } ?: -1
	}

	open fun addFragment(list: List<PageData>) {
		val size = _pageData.size
		_pageData.clear()
		notifyItemRangeRemoved(0, size)
		_pageData.addAll(list)
		notifyItemRangeInserted(0, list.size)
	}
}

class PagerAdapterBuilder(
	fm: FragmentManager,
	lifecycle: Lifecycle,
	private val viewPager: ViewPager2,
	private val tabLayout: TabLayout,
) : BasePagerAdapter(fm, lifecycle) {

	constructor(owner: BaseActivity<*, *>, viewPager: ViewPager2, tabLayout: TabLayout) : this(
		owner.supportFragmentManager,
		owner.lifecycle,
		viewPager,
		tabLayout
	)

	constructor(owner: BaseFragment<*, *>, viewPager: ViewPager2, tabLayout: TabLayout) : this(
		owner.childFragmentManager,
		owner.lifecycle,
		viewPager,
		tabLayout
	)

	private val tabTitle = arrayListOf<String>()
	private val tabIcon = arrayListOf<Int>()
	private var tabMediator: TabLayoutMediator? = null

	init {
		viewPager.adapter = this@PagerAdapterBuilder
	}

	fun fragment(list: List<PageData>): PagerAdapterBuilder {
		synchronized(this) {
			addFragment(list)
			return this
		}
	}

	fun tabIcon(list: List<Int>): PagerAdapterBuilder {
		synchronized(this) {
			tabIcon.clear()
			tabIcon.addAll(list)
			return this
		}
	}

	fun tabTitle(list: List<String>): PagerAdapterBuilder {
		synchronized(this) {
			tabTitle.clear()
			tabTitle.addAll(list)
			return this
		}
	}

	fun customTab(
		autoRefresh: Boolean = true,
		smoothScroll: Boolean = true
	): PagerAdapterBuilder {
		return customTab(autoRefresh, smoothScroll, ViewItemTabBinding::inflate, ::defaultTab)
	}

	fun <VB : ViewBinding> customTab(
		autoRefresh: Boolean = true,
		smoothScroll: Boolean = true,
		factory: (LayoutInflater, ViewGroup, Boolean) -> VB,
		customView: (position: Int, view: VB) -> Unit
	): PagerAdapterBuilder {
		synchronized(this) {
			tabMediator = TabLayoutMediator(
				tabLayout,
				viewPager,
				autoRefresh,
				smoothScroll,
			) { tab, pos ->
				val binding = tab.view.viewBinding(factory)
				customView(pos, binding)
				tab.setCustomView(binding.root)
			}
			return this
		}
	}

	fun offsetScreenLimit(offset: Int = 1): PagerAdapterBuilder {
		synchronized(this) {
			viewPager.setOffscreenPageLimit(offset)
			return this
		}
	}

	fun userInputEnable(boolean: Boolean = true): PagerAdapterBuilder {
		synchronized(this) {
			viewPager.setUserInputEnabled(boolean)
			return this
		}
	}

	fun detach(): PagerAdapterBuilder {
		synchronized(this) {
			tabMediator?.let {
				if (it.isAttached) {
					it.detach()
				}
			}
			return this
		}
	}

	fun build(): PagerAdapterBuilder {
		return attach()
	}

	private fun attach(): PagerAdapterBuilder {
		synchronized(this) {
			tabMediator?.let {
				if (!it.isAttached) {
					it.attach()
				}
			}
			return this
		}
	}

	private fun defaultTab(position: Int, binding: ViewItemTabBinding) {
		with(binding) {
			val drawable = if (tabIcon.size > 0) tabIcon[position] else -1
			val title = if (tabTitle.size > 0) tabTitle[position] else ""

			if (drawable != -1) {
				imgIcon.show()
				imgIcon.setImageResource(drawable)
			} else {
				imgIcon.gone()
			}
			tvTitle.text = title

		}
	}
}
