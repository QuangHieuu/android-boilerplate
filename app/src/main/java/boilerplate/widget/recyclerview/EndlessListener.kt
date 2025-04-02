package boilerplate.widget.recyclerview

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager


abstract class EndlessListener : RecyclerView.OnScrollListener {
	constructor(layoutManager: LinearLayoutManager) {
		_layoutManager = layoutManager
	}

	constructor(layoutManager: GridLayoutManager) {
		_layoutManager = layoutManager
		_visibleThreshold *= layoutManager.spanCount
	}

	constructor(layoutManager: StaggeredGridLayoutManager) {
		_layoutManager = layoutManager
		_visibleThreshold *= layoutManager.spanCount
	}

	// Sets the starting page index
	private var _layoutManager: RecyclerView.LayoutManager

	// The minimum amount of items to have below your current scroll position
	// before loading more.
	private var _visibleThreshold = 2

	// The current offset index of data you have loaded
	private var _currentPage = 0

	// The total number of items in the dataset after the last load
	private var _previousTotalItemCount = 0

	// True if we are still waiting for the last set of data to load.
	private var _loading = true

	fun getLastVisibleItem(lastVisibleItemPositions: IntArray): Int {
		var maxSize = 0
		for (i in lastVisibleItemPositions.indices) {
			if (i == 0) {
				maxSize = lastVisibleItemPositions[i]
			} else if (lastVisibleItemPositions[i] > maxSize) {
				maxSize = lastVisibleItemPositions[i]
			}
		}
		return maxSize
	}

	// This happens many times a second during a scroll, so be wary of the code you place here.
	// We are given a few useful parameters to help us work out if we need to load some more data,
	// but first we check if we are waiting for the previous load to finish.
	override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
		var lastVisibleItemPosition = 0
		val totalItemCount = _layoutManager.itemCount

		if (_layoutManager is StaggeredGridLayoutManager) {
			val lastVisibleItemPositions =
				(_layoutManager as StaggeredGridLayoutManager).findLastVisibleItemPositions(null)
			// get maximum element within the list
			lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions)
		} else if (_layoutManager is LinearLayoutManager) {
			lastVisibleItemPosition =
				(_layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
		}

		// If the total item count is zero and the previous isn't, assume the
		// list is invalidated and should be reset back to initial state
		if (totalItemCount < _previousTotalItemCount) {
			_currentPage = 0
			_previousTotalItemCount = totalItemCount
			if (totalItemCount == 0) {
				_loading = true
			}
		}
		// If it’s still loading, we check to see if the dataset count has
		// changed, if so we conclude it has finished loading and update the current page
		// number and total item count.
		if (_loading && (totalItemCount > _previousTotalItemCount)) {
			_loading = false
			_previousTotalItemCount = totalItemCount
		}

		// If it isn’t currently loading, we check to see if we have breached
		// the visibleThreshold and need to reload more data.
		// If we do need to reload some more data, we execute onLoadMore to fetch the data.
		// threshold should reflect how many total columns there are too
		if (!_loading && (lastVisibleItemPosition + _visibleThreshold) > totalItemCount) {
			_currentPage++
			onLoadMore(_currentPage, totalItemCount)
			_loading = true
		}
	}

	// Defines the process for actually loading more data based on page
	abstract fun onLoadMore(page: Int, totalItemsCount: Int)

	fun refreshPage() {
		_currentPage = 0
		_previousTotalItemCount = 0
	}
}