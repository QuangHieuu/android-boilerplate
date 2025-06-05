package android.empty.layoutManager

import android.content.Context
import android.empty.constant.Constants.HORIZONTAL
import android.empty.constant.Constants.SCROLL_OFFSET
import android.empty.constant.Constants.SCROLL_RANGE
import android.empty.constant.Constants.TOWARDS_BOTTOM_RIGHT
import android.empty.constant.Constants.TOWARDS_HIGHER_INDICES
import android.empty.constant.Constants.TOWARDS_LOWER_INDICES
import android.empty.constant.Constants.TOWARDS_TOP_LEFT
import android.empty.constant.Constants.VERTICAL
import android.empty.constant.Constants.defaultDecider
import android.empty.extension.loop
import android.empty.extension.loopedDecrement
import android.empty.extension.loopedIncrement
import android.graphics.PointF
import android.graphics.Rect
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutParams
import kotlin.math.abs

open class InfinityLayoutManager : LayoutManager, RecyclerView.SmoothScroller.ScrollVectorProvider {
	var orientation: Int = VERTICAL
		set(value) {
			require(value == HORIZONTAL || value == VERTICAL) { "invalid orientation:$value" }
			if (value != field) {
				orientationHelper = OrientationHelper.createOrientationHelper(this, value)
				assertNotInLayoutOrScroll(null)
				field = value
				requestLayout()
				return
			}
			if (!::orientationHelper.isInitialized) {
				orientationHelper = OrientationHelper.createOrientationHelper(this, value)
			}
		}

	var reverseLayout = false
		set(reverseLayout) {
			if (reverseLayout == this.reverseLayout) {
				return
			}
			assertNotInLayoutOrScroll(null)
			field = reverseLayout
			requestLayout()
		}

	/**
	 * The width of the layout - not the recycler.
	 * AKA the width of the recycler, minus the padding on the left and right.
	 */
	val layoutWidth: Int
		get() = width - paddingLeft - paddingRight

	/**
	 * The height of the layout - not the recycler.
	 * AKA the height of the recycler, minus the padding on the top and bottom.
	 */
	val layoutHeight: Int
		get() = height - paddingTop - paddingBottom

	/** Describes the adapter index of the view in the top/left -most position. */
	var topLeftIndex = 0
		protected set

	/** Describes the adapter index of the view in the bottom/right -most position. */
	var bottomRightIndex = 0
		protected set

	/**
	 * Describes the way the layout should be... laid out. Anchor index, anchor edge, and scroll
	 * offset. Used for triggering scrollTo, updating after an adapter change, and orientation
	 * changes.
	 */
	protected var layoutRequest = LayoutRequest(anchorIndex = 0)

	/**
	 * The amount of extra (i.e. not visible) space to fill up with views after we have filled up
	 * the visible space. This is used during smooth scrolling, so that the target view can be found
	 * before it becomes visible (helps with smooth deceleration).
	 */
	protected var extraLayoutSpace = 0

	/** Helps with some layout calculations. Mainly getting the non-scrolling edges of each view. */
	protected lateinit var orientationHelper: OrientationHelper

	protected val isLayoutRTL: Boolean
		get() = layoutDirection == View.LAYOUT_DIRECTION_RTL

	protected var smoothScrollDirectionDecider: (Int, InfinityLayoutManager, Int) -> Int =
		::defaultDecider

	//-----------------------------------------------------------

	constructor(orient: Int = VERTICAL, reverse: Boolean = false) {
		orientation = orient
		reverseLayout = reverse
	}

	constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) {
		getProperties(context, attrs, defStyleAttr, defStyleRes).let {
			orientation = it.orientation
			reverseLayout = it.reverseLayout
		}
	}

	override fun isAutoMeasureEnabled(): Boolean = true

	override fun generateDefaultLayoutParams(): LayoutParams {
		return LayoutParams(
			ViewGroup.LayoutParams.WRAP_CONTENT,
			ViewGroup.LayoutParams.WRAP_CONTENT
		)
	}

	override fun onSaveInstanceState(): Parcelable? {
		// All of this information is based on keeping the item currently at the anchor edge
		// at the anchor edge.
		val direction = getMovementDirectionFromAdapterDirection(TOWARDS_LOWER_INDICES)
		return if (childCount == 0) {  // Occurs if the screen is off on startup.
			null
		} else {
			LayoutRequest(
				_anchorIndex = getInitialIndex(direction),
				_scrollOffset = getInitialItem(direction).hiddenSize
			)
		}
	}

	override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
		layoutRequest.initialize(this, state)

		detachAndScrapAttachedViews(recycler)

		// A) We want to layout the item at the adapter index first, so that we can set the scroll offset.
		// B) We want the item to be laid out /at/ the edge associated with the adapter direction.
		// This means after it gets laid out we need to move /away/ from that edge.
		// Hence the direction is inverted.
		val movementDir = getMovementDirectionFromAdapterDirection(-layoutRequest.adapterDirection)
		var prevItem: ListItem? = null
		val size = if (orientation == HORIZONTAL) layoutWidth else layoutHeight
		var sizeFilled = 0
		var index = layoutRequest.anchorIndex.coerceAtMost(state.itemCount - 1)

		while (sizeFilled < size) {
			val view = createViewForIndex(index, movementDir, recycler)
			val item = getItemForView(movementDir, view)
			var layoutRect = getNonScrollingEdges(view)

			layoutRect = prevItem?.getPositionOfItemFollowingSelf(item, layoutRect)
				?: item.getPositionOfSelfAsFirst(layoutRect, layoutRequest.scrollOffset)

			layoutDecorated(view, layoutRect.left, layoutRect.top, layoutRect.right, layoutRect.bottom)

			index = stepIndex(index, movementDir, state, false)
			sizeFilled += item.size
			prevItem = item
		}

		if (movementDir == TOWARDS_TOP_LEFT) {
			bottomRightIndex = layoutRequest.anchorIndex
			topLeftIndex = stepIndex(index, -movementDir, state, false)
		} else {
			topLeftIndex = layoutRequest.anchorIndex
			bottomRightIndex = stepIndex(index, -movementDir, state, false)
		}
	}

	override fun onRestoreInstanceState(state: Parcelable?) {
		if (state is LayoutRequest) {
			layoutRequest = state
		}
	}

	override fun computeVerticalScrollOffset(state: RecyclerView.State): Int {
		return computeScrollOffset()
	}

	override fun computeVerticalScrollRange(state: RecyclerView.State): Int {
		return computeScrollRange()
	}

	override fun computeVerticalScrollExtent(state: RecyclerView.State): Int {
		return computeScrollExtent()
	}

	override fun computeHorizontalScrollOffset(state: RecyclerView.State): Int {
		return computeScrollOffset()
	}

	override fun computeHorizontalScrollRange(state: RecyclerView.State): Int {
		return computeScrollRange()
	}

	override fun computeHorizontalScrollExtent(state: RecyclerView.State): Int {
		return computeScrollExtent()
	}

	/**
	 * Returns the vector that points to where the [targetPosition] can be found.
	 *
	 * By default it tries to return the direction that will require the least amount of scrolling
	 * to get to, but if some views are larger or smaller than other views this may be incorrect.
	 *
	 * A different function may be provided by assigning it to the [smoothScrollDirectionDecider]
	 * property of the [InfinityLayoutManager].
	 *
	 * This method is used by the [RecyclerView.SmoothScroller] to initiate a scroll towards the
	 * target position.
	 */
	override fun computeScrollVectorForPosition(targetPosition: Int): PointF {
		return computeScrollVectorForPosition(targetPosition, itemCount)
	}

	override fun canScrollVertically(): Boolean {
		return orientation == VERTICAL
	}

	override fun canScrollHorizontally(): Boolean {
		return orientation == HORIZONTAL
	}

	override fun scrollVerticallyBy(
		dy: Int,
		recycler: RecyclerView.Recycler,
		state: RecyclerView.State
	): Int {
		return scrollBy(dy, recycler, state)
	}

	override fun scrollHorizontallyBy(
		dx: Int,
		recycler: RecyclerView.Recycler,
		state: RecyclerView.State
	): Int {
		return scrollBy(dx, recycler, state)
	}

	//-----------------------------------------------------------


	fun convertAdapterDirToMovementDir(adapterDirection: Int): Int {
		return getMovementDirectionFromAdapterDirection(adapterDirection)
	}

	/**
	 * Returns the adapter index of the view with the *lowest* adapter index that is even slightly
	 * visible.
	 */
	fun findFirstVisibleItemPosition(): Int {
		var lowestIndex = Int.MAX_VALUE
		for (i in 0 until childCount) {
			val view = getChildAt(i)
			if (view != null && getPosition(view) < lowestIndex && viewIsVisible(view)) {
				lowestIndex = getPosition(view)
			}
		}
		return lowestIndex
	}

	/**
	 * Returns the adapter index of the view with the *highest* adapter index that is even slightly
	 * visible.
	 */
	fun findLastVisibleItemPosition(): Int {
		var highestIndex = 0
		for (i in 0 until childCount) {
			val view = getChildAt(i)
			if (view != null && getPosition(view) > highestIndex && viewIsVisible(view)) {
				highestIndex = getPosition(view)
			}
		}
		return highestIndex
	}
	//-----------------------------------------------------------
	/**
	 * Scrolls the list of views by the given [delta]. Creates and binds new views if necessary.
	 * Whether to scroll horizontally or vertically is determined by the orientation.
	 * @return The actual distance scrolled. This will be equal to delta unless the layout manager
	 * does not have children, in which case it will be zero. Other layout managers may
	 * return less than the delta if they hit a bound, but the boilerplate.widget.recyclerview.layoutManager.InfinityLayoutManager has no
	 * bounds.
	 */
	protected fun scrollBy(
		delta: Int,
		recycler: RecyclerView.Recycler,
		state: RecyclerView.State
	): Int {
		if (childCount == 0 || delta == 0) {
			return 0
		}

		val movementDir = Integer.signum(delta)
		scrapNonVisibleViews(recycler)
		val absDelta = abs(delta)
		var amountScrolled = 0
		var index = getInitialIndex(movementDir)
		var selectedItem = getInitialItem(movementDir)
		while (amountScrolled < absDelta) {
			val hiddenSize = selectedItem.hiddenSize
			// Scroll just enough to complete the scroll, or bring the view fully into view.
			val amountToScroll = hiddenSize.coerceAtMost(absDelta.minus(amountScrolled))
			amountScrolled += amountToScroll
			offsetChildren(amountToScroll * -movementDir)
			if (amountScrolled < absDelta) {
				index = stepIndex(index, movementDir, state)
				val newView = createViewForIndex(index, movementDir, recycler)
				val newItem = getItemForView(movementDir, newView)
				var layoutRect = getNonScrollingEdges(newView)
				layoutRect = selectedItem.getPositionOfItemFollowingSelf(newItem, layoutRect)
				layoutDecorated(
					newView,
					layoutRect.left,
					layoutRect.top,
					layoutRect.right,
					layoutRect.bottom
				)
				selectedItem = newItem
			}
		}

		// The amount of extra (i.e not visible) space currently covered by views.
		var viewSpace = selectedItem.hiddenSize
		while (viewSpace < extraLayoutSpace) {
			// We don't want the topLeftIndex or bottomRightIndex to reflect non-visible views.
			index = stepIndex(index, movementDir, state, updateIndex = false)
			val newView = createViewForIndex(index, movementDir, recycler)
			val newItem = getItemForView(movementDir, newView)
			var layoutRect = getNonScrollingEdges(newView)
			layoutRect = selectedItem.getPositionOfItemFollowingSelf(newItem, layoutRect)
			layoutDecorated(
				newView,
				layoutRect.left,
				layoutRect.top,
				layoutRect.right,
				layoutRect.bottom
			)
			selectedItem = newItem
			viewSpace += selectedItem.size
		}

		recycleViews(movementDir, recycler, state)
		return amountScrolled.times(movementDir)
	}

	/**
	 * Recycles views that are no longer visible given the [movementDir] of the scroll that was just
	 * completed.
	 */
	protected fun recycleViews(
		movementDir: Int,
		recycler: RecyclerView.Recycler,
		state: RecyclerView.State
	) {
		val initialIndex = getInitialIndex(movementDir)
		// The first visible item will bump us to zero.
		var distanceFromStart = -1
		var foundVisibleView = false
		val viewsToRemove = mutableListOf<Int>()

		// We want to loop through the views in the order opposite the direction of movement so that
		// we remove views that have become hidden because of scrolling.
		val range = if (movementDir == TOWARDS_TOP_LEFT) {
			0 until childCount
		} else {
			childCount - 1 downTo 0
		}

		// Ignore hidden views at the start of the range.
		// Only recycle hidden views at the end of the range.
		for (i in range) {
			val view = getChildAt(i)!!
			if (viewIsVisible(view)) {
				if (!foundVisibleView) {
					foundVisibleView = true
				}
				distanceFromStart++
			} else if (foundVisibleView) {
				viewsToRemove.add(i)
			}
		}

		// Removing the views after collecting them and putting them in order from greatest -> least
		// makes sure we don't get null errors. See #19.
		viewsToRemove.sortedDescending().forEach { i -> removeAndRecycleViewAt(i, recycler) }

		if (viewsToRemove.count() == 0) {
			// If we didn't find anything that needed to be disposed, no indices need to be updated.
			return
		}

		// We need to flip the direction, since we looped through views in the opposite order.
		// When we flip the movement direction, the adapter direction will be flipped as well.
		val adapterDirection = getAdapterDirectionFromMovementDirection(movementDir.times(-1))
		val changeInPosition = adapterDirection.times(distanceFromStart)
		val count = state.itemCount
		if (movementDir == TOWARDS_TOP_LEFT) {
			bottomRightIndex = initialIndex.loop(changeInPosition, count)
		} else {
			topLeftIndex = initialIndex.loop(changeInPosition, count)
		}
	}

	/**
	 * Moves all child views by the given [amount] (can be positive or negative). Determines whether
	 * they are moved horizontally or vertically based on the orientation.
	 */
	protected fun offsetChildren(amount: Int) {
		if (orientation == HORIZONTAL) {
			offsetChildrenHorizontal(amount)
		} else {
			offsetChildrenVertical(amount)
		}
	}

	/**
	 * Sends any currently non-visible (i.e. views completely outside the visible bounds of the
	 * recycler) views to the scrap heap. Used by scrollBy to make sure we're only dealing with
	 * visible views before adding new ones.
	 */
	protected fun scrapNonVisibleViews(recycler: RecyclerView.Recycler) {
		for (i in (childCount - 1) downTo 0) {
			val view = getChildAt(i) ?: continue
			if (!viewIsVisible(view)) {
				detachAndScrapView(view, recycler)
			}
		}
	}

	/**
	 * Returns true if any part of the [view] is within the visible bounds of the recycler.
	 * False otherwise.
	 */
	protected fun viewIsVisible(view: View): Boolean {
		// Note for future: Making these checks or= breaks extraLayoutSpacing because (I think) if
		// the hidden view's edge is aligned with the recycler edge, it isn't scrapped when it
		// should be.
		return if (orientation == HORIZONTAL) {
			getDecoratedRight(view) > paddingLeft && getDecoratedLeft(view) < width.minus(paddingRight)
		} else {
			getDecoratedBottom(view) > paddingTop && getDecoratedTop(view) < height.minus(paddingBottom)
		}
	}

	/**
	 * Increments/decrements and returns the provided [index] based on the [movementDir] the list is
	 * being moved in. For example, if the list is being scrolled towards items with higher adapter
	 * indices the index will be incremented.
	 *
	 * Also (by default) handles updating [topLeftIndex] or [bottomRightIndex] to reflect the
	 * newest view. This functionality can be disabled by passing "false" to the [updateIndex] parameter.
	 */
	protected fun stepIndex(
		index: Int,
		movementDir: Int,
		state: RecyclerView.State,
		updateIndex: Boolean = true
	): Int {
		val adapterDirection = getAdapterDirectionFromMovementDirection(movementDir)
		val count = state.itemCount

		val isTowardsTopLeft = movementDir == TOWARDS_TOP_LEFT
		val isTowardsBottomRight = movementDir == TOWARDS_BOTTOM_RIGHT
		val isTowardsHigherIndices = adapterDirection == TOWARDS_HIGHER_INDICES
		val isTowardsLowerIndices = adapterDirection == TOWARDS_LOWER_INDICES

		val newIndex: Int
		when {
			isTowardsTopLeft && isTowardsHigherIndices -> {
				newIndex = index.loopedIncrement(count)
				if (updateIndex) topLeftIndex = newIndex
			}

			isTowardsTopLeft && isTowardsLowerIndices -> {
				newIndex = index.loopedDecrement(count)
				if (updateIndex) topLeftIndex = newIndex
			}

			isTowardsBottomRight && isTowardsHigherIndices -> {
				newIndex = index.loopedIncrement(count)
				if (updateIndex) bottomRightIndex = newIndex
			}

			isTowardsBottomRight && isTowardsLowerIndices -> {
				newIndex = index.loopedDecrement(count)
				if (updateIndex) bottomRightIndex = newIndex
			}

			else -> throw IllegalStateException("Invalid move & adapter direction combination.")
		}
		return newIndex
	}

	/**
	 * Creates, measures, and inserts a view into the recycler at the given [adapterIndex].
	 * The view is then returned so it can be properly positioned.
	 * @param movementDir The current direction the views are being scrolled in.
	 */
	protected fun createViewForIndex(
		adapterIndex: Int,
		movementDir: Int,
		recycler: RecyclerView.Recycler
	): View {
		val newView = recycler.getViewForPosition(adapterIndex)
		if (movementDir == TOWARDS_LOWER_INDICES) {
			addView(newView, 0)
		} else {
			addView(newView)
		}
		measureChildWithMargins(newView, 0, 0)
		return newView
	}

	/**
	 * Returns a rect populated with the positions of the static edges of the view. I.e. right and
	 * left in horizontal mode, top and bottom in vertical mode.
	 */
	protected fun getNonScrollingEdges(view: View): Rect {
		val layoutRect = Rect()
		val isVertical = orientation == VERTICAL
		// In LTR we align vertical layouts with the left edge, and in RTL the right edge.
		when {
			isVertical && isLayoutRTL -> {
				layoutRect.right = width.minus(paddingRight)
				layoutRect.left =
					layoutRect.right.minus(orientationHelper.getDecoratedMeasurementInOther(view))
			}

			isVertical && !isLayoutRTL -> {
				layoutRect.left = paddingLeft
				layoutRect.right =
					layoutRect.left.plus(orientationHelper.getDecoratedMeasurementInOther(view))
			}

			else -> {  // Horizontal
				layoutRect.top = paddingTop
				layoutRect.bottom =
					layoutRect.top.plus(orientationHelper.getDecoratedMeasurementInOther(view))
			}
		}
		return layoutRect
	}

	/**
	 * Returns the "offset" the view is from the top. This is documented as needed to support
	 * scrollbars (which the boilerplate.widget.recyclerview.layoutManager.InfinityLayoutManager does not support), but it is also needed to
	 * support TalkBack accessibility gestures. This function returns a constant to ensure that
	 * the layout is always scrollable.
	 */
	protected fun computeScrollOffset(): Int {
		if (childCount == 0) {
			return 0
		}
		return SCROLL_OFFSET
	}

	/**
	 * Returns the "range" of scrolling the view supports. This is documented as needed to support
	 * scrollbars (which the boilerplate.widget.recyclerview.layoutManager.InfinityLayoutManager does not support), but it is also needed to
	 * support TalkBack accessibility gestures. This function returns a constant to ensure that
	 * the layout is always scrollable.
	 */
	protected fun computeScrollRange(): Int {
		if (childCount == 0) {
			return 0
		}
		return SCROLL_RANGE
	}

	/**
	 * Returns the "extent" of the view. This is documented as needed to support scrollbars (which
	 * the boilerplate.widget.recyclerview.layoutManager.InfinityLayoutManager does not support), but it is also needed to support TalkBack
	 * accessibility gestures. This function returns a constant to ensure that the layout is always
	 * scrollable.
	 */
	protected fun computeScrollExtent(): Int {
		return 0
	}

	protected fun computeScrollVectorForPosition(targetPosition: Int, count: Int): PointF {
		val movementDir = smoothScrollDirectionDecider(targetPosition, this, count)
		return if (orientation == HORIZONTAL) {
			PointF(movementDir.toFloat(), 0F)
		} else {
			PointF(0F, movementDir.toFloat())
		}
	}

	/**
	 * Returns the adapter index of the view closest to where new views will be shown.
	 * This is determined based on the [movementDir] views are being scrolled in.
	 * For example, if the user is trying to see new views at the top, this will return the adapter
	 * index of the top-most view.
	 */
	protected fun getInitialIndex(movementDir: Int): Int {
		return if (movementDir == TOWARDS_TOP_LEFT) {
			topLeftIndex
		} else {
			bottomRightIndex
		}
	}

	/**
	 * Returns the view (wrapped in a ListItem) closest to where new views will be shown.
	 * This is determined based on the [movementDir] views are being scrolled in.
	 * For example, if the user is trying to see new views at the top, this will return the
	 * top-most view.
	 */
	protected fun getInitialItem(movementDir: Int): ListItem {
		val initialView = if (movementDir == TOWARDS_LOWER_INDICES) {
			getChildAt(0)
		} else {
			getChildAt(childCount - 1)
		}
		// initialView should never be null, so we'll just ask for an exception.
		return getItemForView(movementDir, initialView!!)
	}

	/**
	 * Returns the [view] wrapped in the correct ListItem based on the [movementDir] and
	 * configuration of the LayoutManager.
	 *
	 * ListItems give the view an interface that's more usable when writing a LayoutManager.
	 */
	@Suppress("KotlinConstantConditions")
	protected fun getItemForView(movementDir: Int, view: View): ListItem {
		val isVertical = orientation == VERTICAL
		val isHorizontal = !isVertical
		val isTowardsTopLeft = movementDir == TOWARDS_TOP_LEFT
		val isTowardsBottomRight = !isTowardsTopLeft

		return when {
			isVertical && isTowardsTopLeft -> LeadingBottomListItem(view)
			isVertical && isTowardsBottomRight -> LeadingTopListItem(view)
			isHorizontal && isTowardsTopLeft -> LeadingRightListItem(view)
			isHorizontal && isTowardsBottomRight -> LeadingLeftListItem(view)
			else -> throw IllegalStateException("Invalid movement state.")
		}
	}

	/**
	 * Converts the [movementDir] ([TOWARDS_TOP_LEFT] or [TOWARDS_BOTTOM_RIGHT]) to an adapter
	 * direction ([TOWARDS_HIGHER_INDICES] OR [TOWARDS_LOWER_INDICES]). The adapter direction
	 * tells us which direction we would be traversing the views in if we moved in the given
	 * movement direction.
	 *
	 * If you are working inside the layout manager this is what you should call!
	 */
	@Suppress("KotlinConstantConditions")
	protected fun getAdapterDirectionFromMovementDirection(movementDir: Int): Int {
		val isVertical = orientation == VERTICAL
		val isHorizontal = !isVertical
		val isTowardsTopLeft = movementDir == TOWARDS_TOP_LEFT
		val isTowardsBottomRight = !isTowardsTopLeft
		val isRTL = isLayoutRTL
		val isLTR = !isRTL
		val isReversed = reverseLayout
		val isNotReversed = !isReversed

		return when {
			isVertical && isTowardsTopLeft && isNotReversed -> TOWARDS_LOWER_INDICES
			isVertical && isTowardsTopLeft && isReversed -> TOWARDS_HIGHER_INDICES
			isVertical && isTowardsBottomRight && isNotReversed -> TOWARDS_HIGHER_INDICES
			isVertical && isTowardsBottomRight && isReversed -> TOWARDS_LOWER_INDICES
			isHorizontal && isTowardsTopLeft && isLTR && isNotReversed -> TOWARDS_LOWER_INDICES
			isHorizontal && isTowardsTopLeft && isLTR && isReversed -> TOWARDS_HIGHER_INDICES
			isHorizontal && isTowardsTopLeft && isRTL && isNotReversed -> TOWARDS_HIGHER_INDICES
			isHorizontal && isTowardsTopLeft && isRTL && isReversed -> TOWARDS_LOWER_INDICES
			isHorizontal && isTowardsBottomRight && isLTR && isNotReversed -> TOWARDS_HIGHER_INDICES
			isHorizontal && isTowardsBottomRight && isLTR && isReversed -> TOWARDS_LOWER_INDICES
			isHorizontal && isTowardsBottomRight && isRTL && isNotReversed -> TOWARDS_LOWER_INDICES
			isHorizontal && isTowardsBottomRight && isRTL && isReversed -> TOWARDS_HIGHER_INDICES
			else -> throw IllegalStateException("Invalid movement state.")
		}
	}

	/**
	 * Converts the [movementDir] ([TOWARDS_HIGHER_INDICES] OR [TOWARDS_LOWER_INDICES]) to a movement
	 * direction ([TOWARDS_TOP_LEFT] or [TOWARDS_BOTTOM_RIGHT]). The movement direction tells us
	 * which direction we need to scroll the views in to move towards the given adapter direction.
	 *
	 * If you are working inside the layout manager this is what you should call!
	 */
	@Suppress("KotlinConstantConditions")
	protected fun getMovementDirectionFromAdapterDirection(movementDir: Int): Int {
		val isVertical = orientation == VERTICAL
		val isHorizontal = !isVertical
		val isTowardsHigher = movementDir == TOWARDS_HIGHER_INDICES
		val isTowardsLower = !isTowardsHigher
		val isRTL = isLayoutRTL
		val isLTR = !isRTL
		val isReversed = reverseLayout
		val isNotReversed = !isReversed

		return when {
			isVertical && isTowardsHigher && isNotReversed -> TOWARDS_BOTTOM_RIGHT
			isVertical && isTowardsHigher && isReversed -> TOWARDS_TOP_LEFT
			isVertical && isTowardsLower && isNotReversed -> TOWARDS_TOP_LEFT
			isVertical && isTowardsLower && isReversed -> TOWARDS_BOTTOM_RIGHT
			isHorizontal && isTowardsHigher && isLTR && isNotReversed -> TOWARDS_BOTTOM_RIGHT
			isHorizontal && isTowardsHigher && isLTR && isReversed -> TOWARDS_TOP_LEFT
			isHorizontal && isTowardsHigher && isRTL && isNotReversed -> TOWARDS_TOP_LEFT
			isHorizontal && isTowardsHigher && isRTL && isReversed -> TOWARDS_BOTTOM_RIGHT
			isHorizontal && isTowardsLower && isLTR && isNotReversed -> TOWARDS_TOP_LEFT
			isHorizontal && isTowardsLower && isLTR && isReversed -> TOWARDS_BOTTOM_RIGHT
			isHorizontal && isTowardsLower && isRTL && isNotReversed -> TOWARDS_BOTTOM_RIGHT
			isHorizontal && isTowardsLower && isRTL && isReversed -> TOWARDS_TOP_LEFT
			else -> throw IllegalStateException("Invalid adapter state.")
		}
	}

	//-----------------------------------------------------------

	inner class LeadingLeftListItem(view: View) : ListItem(view) {
		override val hiddenSize: Int
			get() = (getDecoratedRight(view) - (width - paddingRight)).coerceAtLeast(0)

		override val leadingEdge: Int
			get() = getDecoratedLeft(view)

		override val followingEdge: Int
			get() = getDecoratedRight(view)

		override val size: Int
			get() = getDecoratedMeasuredWidth(view)

		override fun getPositionOfItemFollowingSelf(item: ListItem, rect: Rect): Rect {
			rect.left = followingEdge
			rect.right = rect.left + item.size
			return rect
		}

		override fun getPositionOfSelfAsFirst(rect: Rect, hiddenAmount: Int): Rect {
			rect.left = paddingLeft - hiddenAmount
			rect.right = rect.left + size
			return rect
		}
	}

	inner class LeadingTopListItem(view: View) : ListItem(view) {
		override val hiddenSize: Int
			get() = (getDecoratedBottom(view) - (height - paddingBottom)).coerceAtLeast(0)

		override val leadingEdge: Int
			get() = getDecoratedTop(view)

		override val followingEdge: Int
			get() = getDecoratedBottom(view)

		override val size: Int
			get() = getDecoratedMeasuredHeight(view)

		override fun getPositionOfItemFollowingSelf(item: ListItem, rect: Rect): Rect {
			rect.top = followingEdge
			rect.bottom = rect.top + item.size
			return rect
		}

		override fun getPositionOfSelfAsFirst(rect: Rect, hiddenAmount: Int): Rect {
			rect.top = paddingTop - hiddenAmount
			rect.bottom = rect.top + size
			return rect
		}
	}

	inner class LeadingRightListItem(view: View) : ListItem(view) {
		override val hiddenSize: Int
			get() = (paddingLeft - getDecoratedLeft(view)).coerceAtLeast(0)

		override val leadingEdge: Int
			get() = getDecoratedRight(view)

		override val followingEdge: Int
			get() = getDecoratedLeft(view)

		override val size: Int
			get() = getDecoratedMeasuredWidth(view)

		override fun getPositionOfItemFollowingSelf(item: ListItem, rect: Rect): Rect {
			rect.right = followingEdge
			rect.left = rect.right - item.size
			return rect
		}

		override fun getPositionOfSelfAsFirst(rect: Rect, hiddenAmount: Int): Rect {
			rect.right = (width - paddingRight) + hiddenAmount
			rect.left = rect.right - size
			return rect
		}
	}

	inner class LeadingBottomListItem(view: View) : ListItem(view) {
		override val hiddenSize: Int
			get() = (paddingTop - getDecoratedTop(view)).coerceAtLeast(0)

		override val leadingEdge: Int
			get() = getDecoratedBottom(view)

		override val followingEdge: Int
			get() = getDecoratedTop(view)

		override val size: Int
			get() = getDecoratedMeasuredHeight(view)

		override fun getPositionOfItemFollowingSelf(item: ListItem, rect: Rect): Rect {
			rect.bottom = followingEdge
			rect.top = rect.bottom - item.size
			return rect
		}

		override fun getPositionOfSelfAsFirst(rect: Rect, hiddenAmount: Int): Rect {
			rect.bottom = (height - paddingBottom) + hiddenAmount
			rect.top = rect.bottom - size
			return rect
		}
	}

	/**
	 * A smooth scroller that supports the boilerplate.widget.recyclerview.layoutManager.InfinityLayoutManager's two (at the time of writing) quirks:
	 *    1) By default the layout manager only lays out visible views.
	 *    2) The layout manager must be given the state.itemCount to properly calculate
	 *       a scroll vector.
	 */
	protected inner class LoopingSmoothScroller(
		val context: Context,
		val state: RecyclerView.State
	) : LinearSmoothScroller(context) {

		/**
		 * Tells the boilerplate.widget.recyclerview.layoutManager.InfinityLayoutManager to start laying out extra (i.e. not visible) views. This
		 * allows the target view to be found before it becomes visible, which helps with smooth
		 * deceleration.
		 */
		override fun onStart() {
			// Based on the Material Design Guidelines, 500 ms should be plenty of time to decelerate.
			val rate = calculateSpeedPerPixel(context.resources.displayMetrics)  // MS/Pixel
			val time = 500  // MS.
			(layoutManager as InfinityLayoutManager).extraLayoutSpace = (rate * time).toInt()
		}

		/**
		 * Tells the boilerplate.widget.recyclerview.layoutManager.InfinityLayoutManager to stop laying out extra views, b/c there's no need
		 * to lay out views the user can't see.
		 */
		override fun onStop() {
			(layoutManager as InfinityLayoutManager).extraLayoutSpace = 0
		}

		override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
			val layoutManager = layoutManager  // Enables smart cast.
			if (layoutManager is InfinityLayoutManager) {
				return layoutManager.computeScrollVectorForPosition(targetPosition, state.itemCount)
			}
			return null
		}
	}

	protected class LayoutRequest(
		/**
		 * The target adapter index we want to layout at the edge associated with the adapterDirection
		 */
		private var _anchorIndex: Int = RecyclerView.NO_POSITION,
		/**
		 * The amount (in pixels) of the view associated with the anchorIndex that should be hidden.
		 */
		private var _scrollOffset: Int = 0,
		/**
		 * Tells us which edge the view associated with the [_anchorIndex] should be laid out at. If
		 * it is [TOWARDS_LOWER_INDICES] the view will be laid out at the edge where the view
		 * associated with the zero adapter index was originally laid out at. If it is
		 * [TOWARDS_HIGHER_INDICES] it will be the opposite edge.
		 */
		private var _adapterDirection: Int = TOWARDS_LOWER_INDICES,

		/**
		 * A directional decider used to pick a direction to "move" in if one was not provided
		 * explicitly.
		 *
		 * This value cannot be parceled.
		 */
		private var _scrollStrategy: ((Int, InfinityLayoutManager, Int) -> Int)? = null

	) : Parcelable {
		val adapterDirection: Int
			get() = _adapterDirection

		val anchorIndex: Int
			get() = _anchorIndex

		val scrollOffset: Int
			get() = _scrollOffset

		/**
		 * Has the layout request been initialized to make sure all of its public vars are valid?
		 */
		private var _hasBeenInitialized = false

		constructor(parcel: Parcel) : this() {
			_anchorIndex = parcel.readInt()
			_scrollOffset = parcel.readInt()
			_adapterDirection = parcel.readInt()
		}

		constructor(anchorIndex: Int) : this(_anchorIndex = anchorIndex)

		init {
			if (!_hasBeenInitialized && _anchorIndex != RecyclerView.NO_POSITION && _scrollStrategy == null) {
				_hasBeenInitialized = true
			}
		}

		/**
		 * Makes sure that all of this LayoutRequests public variables are valid.
		 */
		fun initialize(layoutManager: InfinityLayoutManager, state: RecyclerView.State) {
			if (_hasBeenInitialized) return
			_hasBeenInitialized = true
			// If this is executing a scrollTo, the anchorIndex will be set, but the
			// adapterDirection still needs to be decided.
			_adapterDirection =
				_scrollStrategy?.invoke(_anchorIndex, layoutManager, state.itemCount)?.let {
					layoutManager.getAdapterDirectionFromMovementDirection(it)
				} ?: _adapterDirection
			// If this is an adapter data update, the adapterDirection will be set but the
			// anchorIndex and scrollOffset still need to be decided.
			if (_anchorIndex == RecyclerView.NO_POSITION) {
				if (layoutManager.childCount == 0) {
					_anchorIndex = 0
				} else {
					val direction = layoutManager.getMovementDirectionFromAdapterDirection(_adapterDirection)
					_anchorIndex = layoutManager.getInitialIndex(direction)
					_scrollOffset = layoutManager.getInitialItem(direction).hiddenSize
				}
			}
		}

		/**
		 * Resets this layout request to a default layout request so that the information can be
		 * re-initialized if onLayoutChildren gets called.
		 */
		fun finishProcessing() {
			_anchorIndex = RecyclerView.NO_POSITION
			_scrollOffset = 0
			_adapterDirection = TOWARDS_LOWER_INDICES
			_scrollStrategy = null
			_hasBeenInitialized = false
		}

		override fun writeToParcel(parcel: Parcel, flags: Int) {
			parcel.writeInt(_anchorIndex)
			parcel.writeInt(_scrollOffset)
			parcel.writeInt(_adapterDirection)
		}

		override fun describeContents(): Int {
			return 0
		}

		companion object CREATOR : Parcelable.Creator<LayoutRequest> {
			override fun createFromParcel(parcel: Parcel): LayoutRequest {
				return LayoutRequest(parcel)
			}

			override fun newArray(size: Int): Array<LayoutRequest?> {
				return Array(size) { LayoutRequest() }
			}
		}
	}
}

/** Defines a better interface for interacting with views in the context of the LayoutManager. */
abstract class ListItem(val view: View) {
	// The "leading edge" refers to the edge that appears first when scrolling.
	// In the case of a vertical list where you are trying to see items /lower/ in the list,
	// it would be the top edge of the bottom-most view.
	// The "following edge" is the opposite, in the case above that would be the bottom edge.

	/**
	 * Returns the size of the part of the view that is hidden (scrolled off screen). Should
	 * never be negative.
	 */
	abstract val hiddenSize: Int

	/**
	 * Returns the location of the edge of the view that is coming into view first.
	 */
	abstract val leadingEdge: Int

	/**
	 * Returns the location of the edge of the view that is coming into view last.
	 */
	abstract val followingEdge: Int

	/**
	 * Returns the size of the view along the layout axis (i.e. the width in horizontal
	 * mode, the height in vertical mode).
	 */
	abstract val size: Int

	/**
	 * Returns a rect defining the position the provided [item] would have if it was
	 * position "after" this item.
	 *
	 * After is defined as towards the top for a bottom leading item, towards the left for a
	 * right leading item, etc.
	 * @param rect A rect defining the static edges of the layout (i.e. left and right for a
	 * vertical layout, top and bottom for a horizontal one).
	 */
	abstract fun getPositionOfItemFollowingSelf(item: ListItem, rect: Rect): Rect

	/**
	 * Returns a rect defining the position this item would have if it was positioned at
	 * the "start" of the layout.
	 *
	 * Start is defined as the Leading edge aligned with the same edge of the recycler.
	 * This means the top aligned with the top for a top leading item, or the left aligned
	 * with the left for a left leading item.
	 * @param rect A rect defining the static edges of the layout (i.e. left and right for a
	 * vertical layout, top and bottom for a horizontal one).
	 * @param hiddenAmount The amount of the view that should be hidden beyond the edge it
	 * is being aligned with.
	 */
	abstract fun getPositionOfSelfAsFirst(rect: Rect, hiddenAmount: Int): Rect
}
