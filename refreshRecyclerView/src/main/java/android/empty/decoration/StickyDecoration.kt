package android.empty.decoration

import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.withTranslation
import androidx.recyclerview.widget.RecyclerView

class StickyDecoration : RecyclerView.ItemDecoration() {

	override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
		super.onDrawOver(c, parent, state)

		val topChild = parent.getChildAt(0)
		if (topChild == null) return

		val topChildPosition = parent.getChildAdapterPosition(topChild)
		if (topChildPosition == RecyclerView.NO_POSITION) return

//		val currentHeader = getHeaderViewForItem(topChildPosition, parent)

//		fixLayoutSize(parent, currentHeader)

//		val contactPoint = currentHeader.bottom
//		val childInContact = getChildInContact(parent, contactPoint, headerPos)
//
//		if (childInContact != null && mListener.isHeader(parent.getChildAdapterPosition(childInContact))) {
//			moveHeader(c, currentHeader, childInContact)
//			return
//		}

//		drawHeader(c, currentHeader)
	}

//	private fun getHeaderViewForItem(headerPosition: Int, parent: RecyclerView): View {
//		return LayoutInflater.from(parent.context).inflate("layoutResId", parent, false)
//	}

	private fun drawHeader(c: Canvas, header: View) {
		c.withTranslation(0f, 0f) {
			header.draw(this)
		}
	}

	private fun moveHeader(c: Canvas, currentHeader: View, nextHeader: View) {
		c.withTranslation(0f, nextHeader.top.minus(currentHeader.height).toFloat()) {
			currentHeader.draw(this)
		}
	}

	private fun getChildInContact(
		parent: RecyclerView,
		contactPoint: Int,
		currentHeaderPos: Int
	): View? {
		var childInContact: View? = null
		for (i in 0..<parent.childCount) {
			var heightTolerance = 0
			val child = parent.getChildAt(i)

			//measure height tolerance with child if child is another header
//			if (currentHeaderPos != i) {
//				val isChildHeader: Boolean = mListener.isHeader(parent.getChildAdapterPosition(child))
//				if (isChildHeader) {
//					heightTolerance = mHeaderHeight - child.height
//				}
//			}

			val childBottomPosition: Int = if (child.top > 0) {
				child.bottom + heightTolerance
			} else {
				child.bottom
			}

			if (childBottomPosition > contactPoint) {
				if (child.top <= contactPoint) {
					childInContact = child
					break
				}
			}
		}
		return childInContact
	}

	private fun fixLayoutSize(parent: ViewGroup, view: View) {

		val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY)
		val heightSpec = View.MeasureSpec.makeMeasureSpec(parent.height, View.MeasureSpec.UNSPECIFIED)

		val childWidthSpec = ViewGroup.getChildMeasureSpec(
			widthSpec,
			parent.getPaddingLeft().plus(parent.getPaddingRight()),
			view.layoutParams.width
		)
		val childHeightSpec = ViewGroup.getChildMeasureSpec(
			heightSpec,
			parent.paddingTop.plus(parent.paddingBottom),
			view.layoutParams.height
		)

		view.measure(childWidthSpec, childHeightSpec)

//		view.layout(0, 0, view.measuredWidth, view.measuredHeight.also { mHeaderHeight = it })
	}
}