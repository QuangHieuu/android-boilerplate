package android.empty.view

import android.content.Context
import android.content.res.Resources
import android.empty.extension.gone
import android.empty.layoutManager.RefreshParams
import android.empty.model.RefreshStyle
import android.util.AttributeSet
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.core.view.size
import androidx.core.view.isEmpty

class RefreshLayout(
	context: Context,
	attrs: AttributeSet
) : ViewGroup(context, attrs) {

	companion object {

		private const val INVALID_INDEX: Int = -1
		private const val INVALID_POINTER: Int = -1
		private const val DEFAULT_REFRESH_SIZE_DP: Int = 30
		private const val DEFAULT_ANIMATE_DURATION: Int = 300
		private const val DEFAULT_REFRESH_TARGET_OFFSET_DP: Int = 50
		private const val DECELERATE_INTERPOLATION_FACTOR: Float = 2.0f
	}

	private val _density = Resources.getSystem().displayMetrics.density

	private val _conf: ViewConfiguration = ViewConfiguration.get(context)
	private val _touchSlop = _conf.scaledTouchSlop

	private var _style: Int = RefreshStyle.NORMAL
	private var _refreshIndex = INVALID_INDEX

	private var _scrollOffset: Float = 0f

	private var _refreshTargetOffset = _density.times(DEFAULT_REFRESH_TARGET_OFFSET_DP)
	private var _refreshSize = _density.times(DEFAULT_REFRESH_SIZE_DP).toInt()
	private val _refreshView = RefreshView(context)

	private var _isRefresh = false
	private var _isAnimStart = false

	private var _target: View? = null

	init {
		val layoutParams = RefreshParams(_refreshSize, _refreshSize)
		addView(_refreshView, layoutParams)

		isNestedScrollingEnabled = true
		isChildrenDrawingOrderEnabled = true
	}

	override fun onDetachedFromWindow() {
		reset()
		clearAnimation()
		super.onDetachedFromWindow()
	}

	override fun getChildDrawingOrder(childCount: Int, i: Int): Int {
		return when (_style) {
			RefreshStyle.FLOAT -> if (_refreshIndex < 0) {
				i
			} else if (i == childCount.minus(1)) {
				// Draw the selected child last
				_refreshIndex
			} else if (i >= _refreshIndex) {
				// Move the children after the selected child earlier one
				i.plus(1)
			} else {
				// Keep the children before the selected child the same
				i
			}

			else -> if (_refreshIndex < 0) {
				i
			} else if (i == 0) {
				// Draw the selected child first
				_refreshIndex
			} else if (i <= _refreshIndex) {
				// Move the children before the selected child earlier one
				i.minus(1)
			} else {
				i
			}
		}
	}

	override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
		if (isEmpty()) return

		ensureTarget()

		if (_target == null) return


	}
	/* --------- */

	private fun reset() {

		_scrollOffset = 0f

		_refreshView.reset()
		_refreshView.gone()

		_isRefresh = false
		_isAnimStart = false
	}

	private fun ensureTarget() {
		if (!isTargetValid()) {
			for (i in 0..<size) {
				val child = getChildAt(i)
				if (child != _refreshView) {
					_target = child
					break
				}
			}
		}
	}

	private fun isTargetValid(): Boolean {
		for (i in 0..<size) {
			if (_target == getChildAt(i)) {
				return true
			}
		}

		return false
	}

	/* --------- */
	override fun generateLayoutParams(attrs: AttributeSet): RefreshParams {
		return RefreshParams(context, attrs)
	}

	override fun generateLayoutParams(p: LayoutParams?): RefreshParams {
		return RefreshParams(p)
	}

	override fun generateDefaultLayoutParams(): RefreshParams {
		return RefreshParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
	}

	override fun checkLayoutParams(p: LayoutParams?): Boolean {
		return p is RefreshParams
	}
}