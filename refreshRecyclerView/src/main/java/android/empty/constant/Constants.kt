package android.empty.constant

import android.empty.layoutManager.InfinityLayoutManager
import android.view.View
import androidx.recyclerview.widget.OrientationHelper
import kotlin.math.abs
import kotlin.math.max

object Constants {
	const val HORIZONTAL = OrientationHelper.HORIZONTAL
	const val VERTICAL = OrientationHelper.VERTICAL

	/**
	 * Describes the user scrolling towards the top/left of the screen. NOTE: this does *not*
	 * describe the direction views are moving in. The user is trying to see new views at the
	 * top/left.
	 */
	const val TOWARDS_TOP_LEFT = -1

	/**
	 * Describes the user scrolling towards the bottom/right of the screen. NOTE: this does
	 * *not* describe the direction views are moving in. The user is trying to see new views
	 * at the bottom/right.
	 */
	const val TOWARDS_BOTTOM_RIGHT = 1

	/**
	 * Describes the direction we need to traverse view indices in to get to larger adapter indices.
	 * In this case we need to traverse the views backwards (Max -> 0) to get to higher adapter
	 * indices.
	 */
	const val TOWARDS_LOWER_INDICES = -1

	/**
	 * Describes the direction we need to traverse view indices in to get to larger adapter indices.
	 * In this case we need to traverse the views forwards (0 -> Max) to get to higher adapter
	 * indices.
	 */
	const val TOWARDS_HIGHER_INDICES = 1

	/**
	 * A constant returned by the [computeScrollOffset] function so that accessibility knows
	 * the layout is always scrollable.
	 *
	 */
	const val SCROLL_OFFSET = 100

	/**
	 * A constant returned by the [computeScrollRange] function so that accessibility knows
	 * the layout is always scrollable.
	 */
	const val SCROLL_RANGE = 200


	/**
	 * The default view picker used when one is not provided.
	 * @return A view with the given adapter index.
	 */
	fun defaultPicker(targetAdapterIndex: Int, layoutManager: InfinityLayoutManager): View? {
		return childClosestToMiddle(targetAdapterIndex, layoutManager)
	}

	/**
	 * Returns a view with the given [targetAdapterIndex]. If there are multiple views associated with the
	 * given index, this returns the view closest to the anchor edge.
	 *
	 * The anchor edge is the edge the view associated with index 0 would be initially laid out
	 * against. For example: In a RTL horizontal layout, the anchor edge would be the right edge.
	 */
	fun childClosestToAnchorEdge(
		targetAdapterIndex: Int,
		layoutManager: InfinityLayoutManager
	): View? {
		val movementDir = layoutManager.convertAdapterDirToMovementDir(TOWARDS_HIGHER_INDICES)
		val range = if (movementDir == TOWARDS_HIGHER_INDICES) {
			0 until layoutManager.childCount
		} else {
			layoutManager.childCount - 1 downTo 0
		}

		for (i in range) {
			val view = layoutManager.getChildAt(i) ?: break
			if (layoutManager.getPosition(view) == targetAdapterIndex) {
				return view
			}
		}
		return null
	}

	/**
	 * Returns a view with the given [targetAdapterIndex]. If there are multiple views associated with the
	 * given index, this returns the view whose middle is closest to the middle of the layout.
	 */
	fun childClosestToMiddle(
		targetAdapterIndex: Int,
		layoutManager: InfinityLayoutManager
	): View? {
		var minDistance = Int.MAX_VALUE
		var closestView: View? = null
		val layoutMiddle = if (layoutManager.orientation == HORIZONTAL) {
			layoutManager.paddingLeft.plus(layoutManager.layoutWidth.div(2))
		} else {
			layoutManager.paddingTop.plus(layoutManager.layoutHeight.div(2))
		}
		for (i in 0 until layoutManager.childCount) {
			val view = layoutManager.getChildAt(i) ?: return null
			if (layoutManager.getPosition(view) != targetAdapterIndex) {
				continue
			}
			val childMiddle = if (layoutManager.orientation == HORIZONTAL) {
				layoutManager.getDecoratedLeft(view)
					.plus(layoutManager.getDecoratedMeasuredWidth(view).div(2))
			} else {
				layoutManager.getDecoratedTop(view)
					.plus(layoutManager.getDecoratedMeasuredHeight(view).div(2))
			}
			val distance = abs(childMiddle.minus(layoutMiddle))
			if (distance < minDistance) {
				minDistance = distance
				closestView = view
			}
		}
		return closestView
	}

	/**
	 * The default decider used when one is not provided.
	 * @return A movement direction that should be used to "scroll" to the given adapter index.
	 */
	fun defaultDecider(
		adapterIndex: Int,
		layoutManager: InfinityLayoutManager,
		itemCount: Int
	): Int {
		return estimateShortestRoute(adapterIndex, layoutManager, itemCount)
	}

	/**
	 * Returns a movement direction that should be used to "scroll" to the given [adapterIndex].
	 * This function always returns the direction associated with creating views at the
	 * anchor edge. The anchor edge being the edge the 0 indexed view was aligned with when
	 * the recycler was initially laid out.
	 */
	fun addViewsAtAnchorEdge(
		adapterIndex: Int,
		layoutManager: InfinityLayoutManager,
		itemCount: Int
	): Int {
		return layoutManager.convertAdapterDirToMovementDir(TOWARDS_LOWER_INDICES)
	}

	/**
	 * Returns a movement direction that should be used to "scroll" to the given [adapterIndex].
	 * This function always returns the direction associated with creating views at the edge
	 * opposite the anchor edge. The anchor edge being the edge the 0 indexed view was aligned with when
	 * the recycler was initially laid out.
	 */
	fun addViewsAtOptAnchorEdge(
		adapterIndex: Int,
		layoutManager: InfinityLayoutManager,
		itemCount: Int
	): Int {
		return layoutManager.convertAdapterDirToMovementDir(TOWARDS_HIGHER_INDICES)
	}

	/**
	 * Returns a movement direction that should be used to "scroll" to the given [adapterIndex].
	 * This function estimates which direction puts the view on screen with the least amount
	 * of scrolling. It is an estimation because the function assumes all views are the same
	 * size. If some views are larger or smaller than others, this may not return the correct
	 * direction.
	 */
	@Suppress("KotlinConstantConditions")
	fun estimateShortestRoute(
		adapterIndex: Int,
		layoutManager: InfinityLayoutManager,
		itemCount: Int
	): Int {
		// Special case the view being partially visible.
		when {
			layoutManager.topLeftIndex == adapterIndex -> return TOWARDS_TOP_LEFT
			layoutManager.bottomRightIndex == adapterIndex -> return TOWARDS_BOTTOM_RIGHT
			else -> {

				val (topLeftInLoopDist, topLeftOverSeamDist) =
					calculateDistances(adapterIndex, layoutManager.topLeftIndex, itemCount)
				val topLeftTargetSmaller = adapterIndex < layoutManager.topLeftIndex

				val (bottomRightInLoopDist, bottomRightOverSeamDist) =
					calculateDistances(adapterIndex, layoutManager.bottomRightIndex, itemCount)
				val bottomRightTargetSmaller = adapterIndex < layoutManager.bottomRightIndex

				val minDist = arrayOf(
					topLeftInLoopDist,
					topLeftOverSeamDist,
					bottomRightInLoopDist,
					bottomRightOverSeamDist
				).min()

				val minDistIsInLoop = when (minDist) {
					topLeftInLoopDist, bottomRightInLoopDist -> true
					topLeftOverSeamDist, bottomRightOverSeamDist -> false
					else -> throw IllegalStateException()  // Should never happen.
				}
				val minDistIsOverSeam = !minDistIsInLoop
				val targetIsSmaller = when (minDist) {
					topLeftInLoopDist, topLeftOverSeamDist -> topLeftTargetSmaller
					bottomRightInLoopDist, bottomRightOverSeamDist -> bottomRightTargetSmaller
					else -> throw IllegalStateException()  // Should never happen.
				}
				val targetIsLarger = !targetIsSmaller

				val adapterDir = when {
					targetIsSmaller && minDistIsInLoop -> TOWARDS_LOWER_INDICES
					targetIsSmaller && minDistIsOverSeam -> TOWARDS_HIGHER_INDICES
					targetIsLarger && minDistIsInLoop -> TOWARDS_HIGHER_INDICES
					targetIsLarger && minDistIsOverSeam -> TOWARDS_LOWER_INDICES
					else -> throw IllegalStateException()  // Should never happen.
				}
				return layoutManager.convertAdapterDirToMovementDir(adapterDir)
			}
		}
	}

	fun calculateDistances(adapterIndex: Int, anchorIndex: Int, count: Int): Pair<Int, Int> {
		val inLoopDist = abs(adapterIndex - anchorIndex)
		val smallerIndex = adapterIndex.coerceAtMost(anchorIndex)
		val largerIndex = max(adapterIndex, anchorIndex)
		val overSeamDist = (count - largerIndex) + smallerIndex
		return Pair(inLoopDist, overSeamDist)
	}
}