package android.empty.listener

interface RefreshListener {
	/**
	 * When the content view has reached to the start point and refresh has been completed, view will be reset.
	 */
	fun reset()

	/**
	 * Refresh View is refreshing
	 */
	fun refreshing()

	/**
	 * @param pullDistance The drop-down distance of the refresh View
	 * @param pullProgress The drop-down progress of the refresh View and the pullProgress may be more than 1.0f
	 * pullProgress = pullDistance / refreshTargetOffset
	 */
	fun pullProgress(pullDistance: Float, pullProgress: Float)
}
