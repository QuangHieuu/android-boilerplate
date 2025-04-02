package boilerplate.widget.recyclerview.decoration

import android.graphics.Rect
import android.view.View
import androidx.annotation.Dimension
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class GridDecoration(
	@Dimension
	private val spacing: Int,
	private var spanCount: Int = DEFAULT_SPAN_COUNT
) : ItemDecoration() {

	companion object {
		const val DEFAULT_SPAN_COUNT = -1
	}

	private val halfSpacing: Int
		get() = spacing / 2

	override fun getItemOffsets(
		outRect: Rect,
		view: View,
		parent: RecyclerView,
		state: RecyclerView.State
	) {
		super.getItemOffsets(outRect, view, parent, state)
		if (spanCount == DEFAULT_SPAN_COUNT) spanCount = getTotalSpan(parent)
		if (spanCount < 1) return

		val childCount = parent.layoutManager?.itemCount ?: 0
		val childIndex = parent.getChildAdapterPosition(view)

		val itemSpanSize = getItemSpanSize(parent, childIndex)
		if (itemSpanSize == spanCount) {
			outRect[0, 0, 0] = 0
			return
		}
		val spanIndex = getItemSpanIndex(parent, childIndex)
		setSpacings(outRect, parent, childCount, childIndex, itemSpanSize, spanIndex)
	}

	private fun setSpacings(
		outRect: Rect,
		parent: RecyclerView,
		childCount: Int,
		childIndex: Int,
		itemSpanSize: Int,
		spanIndex: Int
	) {
		outRect.top = halfSpacing
		outRect.bottom = halfSpacing
		outRect.left = halfSpacing
		outRect.right = halfSpacing
		if (isTopEdge(parent, childIndex)) outRect.top = spacing
		if (isLeftEdge(spanIndex)) outRect.left = spacing
		if (isRightEdge(itemSpanSize, spanIndex)) outRect.right = spacing
		if (isBottomEdge(parent, childCount, childIndex, spanIndex)) outRect.bottom = spacing
	}

	private fun getTotalSpan(parent: RecyclerView): Int {
		return when (val mgr = parent.layoutManager) {
			is GridLayoutManager -> mgr.spanCount
			is StaggeredGridLayoutManager -> mgr.spanCount
			is LinearLayoutManager -> 1
			else -> -1
		}
	}

	private fun getItemSpanSize(parent: RecyclerView, childIndex: Int): Int {
		return when (val mgr = parent.layoutManager) {
			is GridLayoutManager -> mgr.spanSizeLookup.getSpanSize(childIndex)
			is StaggeredGridLayoutManager -> 1
			is LinearLayoutManager -> 1
			else -> -1
		}
	}

	private fun getItemSpanIndex(parent: RecyclerView, childIndex: Int): Int {
		return when (val mgr = parent.layoutManager) {
			is GridLayoutManager -> mgr.spanSizeLookup.getSpanIndex(childIndex, spanCount)
			is StaggeredGridLayoutManager -> childIndex % spanCount
			is LinearLayoutManager -> 0
			else -> -1
		}
	}

	private fun isLeftEdge(spanIndex: Int): Boolean {
		return spanIndex == 0
	}

	private fun isRightEdge(itemSpanSize: Int, spanIndex: Int): Boolean {
		return (spanIndex + itemSpanSize) == spanCount
	}

	private fun isTopEdge(parent: RecyclerView, childIndex: Int): Boolean {
		return (childIndex == 0) || isFirstItemEdgeValid((childIndex < spanCount), parent, childIndex)
	}

	private fun isBottomEdge(
		parent: RecyclerView,
		childCount: Int,
		childIndex: Int,
		spanIndex: Int
	): Boolean {
		return isLastItemEdgeValid(
			(childIndex >= childCount - spanCount),
			parent,
			childCount,
			childIndex,
			spanIndex
		)
	}

	private fun isFirstItemEdgeValid(
		isOneOfFirstItems: Boolean,
		parent: RecyclerView,
		childIndex: Int
	): Boolean {
		var totalSpanArea = 0
		if (isOneOfFirstItems) {
			for (i in childIndex downTo 0) {
				totalSpanArea += getItemSpanSize(parent, i)
			}
		}
		return isOneOfFirstItems && totalSpanArea <= spanCount
	}

	private fun isLastItemEdgeValid(
		isOneOfLastItems: Boolean,
		parent: RecyclerView,
		childCount: Int,
		childIndex: Int,
		spanIndex: Int
	): Boolean {
		var totalSpanRemaining = 0
		if (isOneOfLastItems) {
			for (i in childIndex until childCount) {
				totalSpanRemaining += getItemSpanSize(parent, i)
			}
		}
		return isOneOfLastItems && (totalSpanRemaining <= spanCount - spanIndex)
	}
}