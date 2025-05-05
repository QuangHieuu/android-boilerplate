package boilerplate.ui.home

import android.empty.customview.model.Entry
import androidx.core.os.bundleOf
import boilerplate.base.BaseFragment
import boilerplate.databinding.FragmentHomeBinding
import boilerplate.ui.main.MainVM
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import kotlin.random.Random

class HomeFragment : BaseFragment<FragmentHomeBinding, MainVM>() {

	companion object {
		fun newInstance(): HomeFragment {
			bundleOf()
			return HomeFragment()
		}
	}

	override val viewModel: MainVM by activityViewModel()

	override fun initialize() {
		binding.chartLine.setData(generateData())
	}

	override fun onSubscribeObserver() {
	}

	override fun registerEvent() {
		binding.swipe.setOnRefreshListener {
			binding.chartLine.setData(generateData())
		}
	}

	override fun callApi() {

	}

	private fun generateData(): ArrayList<Entry> {
		val list = arrayListOf<Entry>()
		val min = Random.nextInt(20, 500)
		val max = Random.nextInt(min, 1000)
		var index = 0f
		while (index < Random.nextInt(5, 20)) {
			list.add(Entry(Random.nextFloat().times(max.minus(min)).plus(min)))
			index++
		}
		binding.swipe.isRefreshing = false
		return list
	}
}